-- Backend auditida topilgan teshiklarni yopish.
--
-- Har biri hozirgi holatda AMALDA suiiste'mol qilinishi mumkin bo'lgan
-- kamchilik: RLS siyosatlari "ishtirokchi" darajasida to'xtab qolgan,
-- rollar va biznes shartlari tekshirilmagan.
--
-- Manba: docs/01-mahsulot.md (huquqlar matritsasi),
--        docs/04-buyurtma-oqimi.md, docs/06-ishonch.md

-- ---------------------------------------------------------------------------
-- 1) is_blocked() — 0007 bayroqni qo'shdi, lekin uni HECH QAYERDA
--    tekshirmagan. Ya'ni admin panelidagi "bloklash" tugmasi bezakdan
--    boshqa hech narsa emas edi: bloklangan foydalanuvchi baribir
--    buyurtma, xabar va sharh yaratardi.
-- ---------------------------------------------------------------------------
create or replace function public.is_blocked() returns boolean
language sql stable security definer set search_path = public as $$
  select coalesce((select is_blocked from public.profiles where id = auth.uid()), false);
$$;

revoke execute on function public.is_blocked() from public, anon;
grant execute on function public.is_blocked() to authenticated;

-- ---------------------------------------------------------------------------
-- 2) orders: buyurtmani FAQAT MIJOZ o'z nomidan yaratadi.
--
--    Avval `client_id = uid OR usta_id = uid` edi — usta client_id'ga
--    boshqa odamni yozib soxta buyurtma yaratishi, uni yakunlab
--    "yakunlangan buyurtmalar" ko'rsatkichini shishirishi mumkin edi.
--    docs/01: "So'rov yaratish — Mijoz ✅, Usta ❌".
-- ---------------------------------------------------------------------------
drop policy if exists "orders_insert_participant" on public.orders;
drop policy if exists "orders_insert_client_only" on public.orders;

create policy "orders_insert_client_only" on public.orders
  for insert with check (
    (select auth.uid()) = client_id
    and not public.is_blocked()
  );

-- ---------------------------------------------------------------------------
-- 3) order_items: avval `for all` bilan ikkala tomon ham buyurtma
--    tarkibini VA NARXINI istalgan vaqtda o'zgartira olardi — kelishuvdan
--    keyin ham. Endi: yozish faqat mijozga, tahrirlash faqat buyurtma
--    hali `pending` bo'lganda.
-- ---------------------------------------------------------------------------
drop policy if exists "order_items_write_participant" on public.order_items;
drop policy if exists "order_items_insert_client" on public.order_items;
drop policy if exists "order_items_update_pending" on public.order_items;
drop policy if exists "order_items_delete_pending" on public.order_items;

create policy "order_items_insert_client" on public.order_items
  for insert with check (
    exists (
      select 1 from public.orders o
      where o.id = public.order_items.order_id
        and o.client_id = (select auth.uid())
    )
  );

create policy "order_items_update_pending" on public.order_items
  for update using (
    exists (
      select 1 from public.orders o
      where o.id = public.order_items.order_id
        and o.client_id = (select auth.uid())
        and o.status = 'pending'
    )
  ) with check (
    exists (
      select 1 from public.orders o
      where o.id = public.order_items.order_id
        and o.client_id = (select auth.uid())
        and o.status = 'pending'
    )
  );

create policy "order_items_delete_pending" on public.order_items
  for delete using (
    exists (
      select 1 from public.orders o
      where o.id = public.order_items.order_id
        and o.client_id = (select auth.uid())
        and o.status = 'pending'
    )
  );

-- ---------------------------------------------------------------------------
-- 4) reviews: ENG JIDDIY TESHIK. Avval `with check (uid = client_id)` —
--    boshqa hech qanday shart yo'q. Ya'ni istalgan foydalanuvchi istalgan
--    ustaga CHEKSIZ sharh yozib, reytingni butunlay boshqarishi mumkin
--    edi (o'zining reytingini ko'tarish yoki raqobatchini tushirish).
--
--    Endi: sharh faqat o'zining TUGALLANGAN buyurtmasi uchun, bir
--    buyurtmaga bittadan. docs/06-ishonch.md, 3-bo'lim.
-- ---------------------------------------------------------------------------
alter table public.reviews
  add column if not exists order_id uuid references public.orders(id) on delete set null,
  add column if not exists image_url text,
  add column if not exists updated_at timestamptz not null default now();

-- Bir buyurtma = bir sharh (eski, order_id'siz qatorlar cheklanmaydi)
create unique index if not exists uq_reviews_order
  on public.reviews(order_id) where order_id is not null;

drop policy if exists "reviews_insert_own" on public.reviews;
drop policy if exists "reviews_insert_after_completed_order" on public.reviews;

create policy "reviews_insert_after_completed_order" on public.reviews
  for insert with check (
    (select auth.uid()) = client_id
    and not public.is_blocked()
    and public.reviews.order_id is not null
    and exists (
      select 1 from public.orders o
      where o.id = public.reviews.order_id
        and o.client_id = (select auth.uid())
        and o.usta_id = public.reviews.usta_id
        and o.status = 'completed'
    )
  );

-- Tahrirlash oynasi: 7 kun (docs/06)
drop policy if exists "reviews_update_own" on public.reviews;
drop policy if exists "reviews_update_own_window" on public.reviews;

create policy "reviews_update_own_window" on public.reviews
  for update using (
    (select auth.uid()) = client_id
    and created_at > now() - interval '7 days'
  ) with check ((select auth.uid()) = client_id);

-- Mijoz o'z sharhini o'chira olmaydi — faqat admin (docs/01, 3-cheklov)
drop policy if exists "reviews_delete_own" on public.reviews;
drop policy if exists "reviews_delete_admin" on public.reviews;

create policy "reviews_delete_admin" on public.reviews
  for delete using (public.is_admin());

-- ---------------------------------------------------------------------------
-- 5) messages: avval har qanday ishtirokchi har qanday xabarni
--    tahrirlay olardi — usta mijozning yozgan gapini o'zgartirib
--    qo'yishi mumkin edi. Chat "yozma yozuv" bo'lishi kerak.
--
--    Ustun darajasida faqat `price_offer_status` yangilanadi, va faqat
--    taklifni OLGAN tomon (yuboruvchi o'z taklifini qabul qila olmaydi).
-- ---------------------------------------------------------------------------
revoke update on public.messages from anon, authenticated;
grant update (price_offer_status) on public.messages to authenticated;

drop policy if exists "messages_update_participant" on public.messages;
drop policy if exists "messages_update_offer_recipient" on public.messages;

create policy "messages_update_offer_recipient" on public.messages
  for update using (
    (select auth.uid()) <> sender_id
    and exists (
      select 1 from public.conversations c
      where c.id = public.messages.conversation_id
        and ((select auth.uid()) = c.client_id or (select auth.uid()) = c.usta_id)
    )
  ) with check (
    (select auth.uid()) <> sender_id
    and exists (
      select 1 from public.conversations c
      where c.id = public.messages.conversation_id
        and ((select auth.uid()) = c.client_id or (select auth.uid()) = c.usta_id)
    )
  );

-- Bloklangan foydalanuvchi yozmaydi
drop policy if exists "messages_insert_own" on public.messages;

create policy "messages_insert_own" on public.messages
  for insert with check (
    (select auth.uid()) = sender_id
    and not public.is_blocked()
    and exists (
      select 1 from public.conversations c
      where c.id = public.messages.conversation_id
        and ((select auth.uid()) = c.client_id or (select auth.uid()) = c.usta_id)
    )
  );

drop policy if exists "conversations_insert_participant" on public.conversations;

create policy "conversations_insert_participant" on public.conversations
  for insert with check (
    ((select auth.uid()) = client_id or (select auth.uid()) = usta_id)
    and not public.is_blocked()
  );

-- ---------------------------------------------------------------------------
-- 6) order_events: `changed_by` tekshirilmagan edi — ishtirokchi tarixga
--    boshqa odam nomidan yozuv qoldirishi mumkin edi. Audit tarixi
--    nizolarda yagona haqiqat manbai (docs/04), demak qalbakilashtirilmasin.
-- ---------------------------------------------------------------------------
drop policy if exists "order_events_insert_participant" on public.order_events;

create policy "order_events_insert_participant" on public.order_events
  for insert with check (
    changed_by = (select auth.uid())
    and exists (
      select 1 from public.orders o
      where o.id = public.order_events.order_id
        and ((select auth.uid()) = o.client_id or (select auth.uid()) = o.usta_id)
    )
  );

-- ---------------------------------------------------------------------------
-- 7) Reytingni og'irlashtirilgan formula bilan hisoblash (docs/06, 2-bo'lim).
--    Oddiy o'rtacha bilan bitta 5-yulduzli sharh olgan usta 47 ta sharhli
--    ustadan yuqorida turadi — katalog boshidanoq buziladi.
--
--        reyting = (yig'indi + platforma_o'rtachasi * 5) / (soni + 5)
-- ---------------------------------------------------------------------------
create or replace function public.refresh_usta_rating()
returns trigger
language plpgsql
security definer set search_path = public
as $$
declare
  target       uuid;
  n            int;
  s            numeric;
  platform_avg numeric;
  m            constant int := 5;   -- ishonch chegarasi
begin
  target := coalesce(new.usta_id, old.usta_id);

  select count(*), coalesce(sum(r.rating), 0)
    into n, s
    from public.reviews r
   where r.usta_id = target;

  -- Platforma o'rtachasi; hali sharh bo'lmasa 4.5 deb olinadi
  select coalesce(avg(r.rating), 4.5) into platform_avg from public.reviews r;

  update public.usta_profiles up
     set rating_avg = case
                        when n = 0 then 0
                        else round(((s + platform_avg * m) / (n + m))::numeric, 2)
                      end,
         rating_count = n
   where up.user_id = target;

  return coalesce(new, old);
end;
$$;

revoke execute on function public.refresh_usta_rating() from public, anon, authenticated;

-- ---------------------------------------------------------------------------
-- 8) handle_new_user: email bo'sh bo'lsa (kelajakda telefon-OTP)
--    full_name null bo'lib, NOT NULL cheklovi tufayli ro'yxatdan o'tish
--    butunlay yiqilardi. Zaxira qiymat qo'shildi.
-- ---------------------------------------------------------------------------
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer set search_path = public
as $$
begin
  insert into public.profiles (id, full_name, role, phone)
  values (
    new.id,
    coalesce(
      nullif(new.raw_user_meta_data->>'full_name', ''),
      nullif(split_part(coalesce(new.email, ''), '@', 1), ''),
      'Foydalanuvchi'
    ),
    coalesce((new.raw_user_meta_data->>'role')::public.user_role, 'client'),
    new.raw_user_meta_data->>'phone'
  );
  return new;
end;
$$;

revoke execute on function public.handle_new_user() from public, anon, authenticated;

-- Rol qoidalarini baza darajasida majburlash + topilgan policy xatolarini
-- tuzatish. Ilgari bu qoidalar faqat UI'da edi, ya'ni PostgREST'ga to'g'ridan-
-- to'g'ri so'rov yuborib chetlab o'tish mumkin edi.
--
-- Manba: docs/01-mahsulot.md ("O'zgarmas cheklovlar"),
--        docs/04-buyurtma-oqimi.md ("Qat'iy qoidalar").

-- ---------------------------------------------------------------------------
-- 1) order_events: INSERT faqat ustaga ruxsat etilgan edi (0006), lekin
--    mijoz ham holat o'zgartiradi (narxni qabul qilish, topshirildi) —
--    uning harakatlari tarixga umuman yozilmayotgan edi.
-- ---------------------------------------------------------------------------
drop policy if exists "Ustas can insert order events" on public.order_events;
drop policy if exists "order_events_insert_participant" on public.order_events;

create policy "order_events_insert_participant"
  on public.order_events for insert
  with check (
    exists (
      select 1 from public.orders o
      where o.id = order_id
        and ((select auth.uid()) = o.client_id or (select auth.uid()) = o.usta_id)
    )
  );

-- ---------------------------------------------------------------------------
-- 2) orders: DELETE siyosati yo'q edi. Buyurtma yaratilib, order_items
--    yozilmasa, "yetim" buyurtma o'chirilmay qolib ketardi.
--    O'chirish faqat tarkibsiz va hali hech kim qabul qilmagan buyurtmaga.
-- ---------------------------------------------------------------------------
drop policy if exists "orders_delete_own_empty" on public.orders;

create policy "orders_delete_own_empty"
  on public.orders for delete
  using (
    (select auth.uid()) = client_id
    and status = 'pending'
    -- `orders.id` deb to'liq yozilishi shart: aks holda `id` subquery ichida
    -- `order_items.id` (bigint) ga bog'lanib, tip xatosi beradi.
    and not exists (
      select 1 from public.order_items i where i.order_id = public.orders.id
    )
  );

-- ---------------------------------------------------------------------------
-- 3) Buyurtma holati va to'lov holatini kim o'zgartira olishini majburlash.
--
--    Usta:  pending → accepted | cancelled
--           accepted → in_progress | cancelled
--           in_progress → ready
--    Mijoz: pending → cancelled
--           ready → completed
--
--    Ya'ni usta buyurtmani YAKUNLAY OLMAYDI, mijoz esa ish jarayonini
--    o'zgartira olmaydi. To'lovni faqat usta belgilaydi (pulni u oladi).
--
--    auth.uid() bo'sh bo'lsa (service_role, cron) tekshiruv o'tkazilmaydi —
--    server tomonidagi avtomatik jarayonlar ishonchli hisoblanadi.
-- ---------------------------------------------------------------------------
create or replace function public.enforce_order_transition()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
  uid       uuid := auth.uid();
  is_admin  boolean;
  is_client boolean;
  is_usta   boolean;
begin
  if uid is null then
    return new;
  end if;

  select coalesce(p.is_admin, false) into is_admin
  from public.profiles p where p.id = uid;

  if coalesce(is_admin, false) then
    return new;
  end if;

  is_client := uid = old.client_id;
  is_usta   := uid = old.usta_id;

  if new.status is distinct from old.status then
    if is_usta then
      if not (
        (old.status = 'pending'     and new.status in ('accepted', 'cancelled')) or
        (old.status = 'accepted'    and new.status in ('in_progress', 'cancelled')) or
        (old.status = 'in_progress' and new.status = 'ready')
      ) then
        raise exception 'usta cannot move order from % to %', old.status, new.status
          using errcode = 'check_violation';
      end if;
    elsif is_client then
      if not (
        (old.status = 'pending' and new.status = 'cancelled') or
        (old.status = 'ready'   and new.status = 'completed')
      ) then
        raise exception 'client cannot move order from % to %', old.status, new.status
          using errcode = 'check_violation';
      end if;
    else
      raise exception 'not an order participant'
        using errcode = 'check_violation';
    end if;
  end if;

  if new.payment_status is distinct from old.payment_status and not is_usta then
    raise exception 'only usta can change payment status'
      using errcode = 'check_violation';
  end if;

  return new;
end;
$$;

-- Trigger funksiyasi RPC orqali tashqaridan chaqirilmasligi kerak (0005 kabi)
revoke execute on function public.enforce_order_transition()
  from public, anon, authenticated;

drop trigger if exists trg_enforce_order_transition on public.orders;

create trigger trg_enforce_order_transition
  before update on public.orders
  for each row
  execute function public.enforce_order_transition();

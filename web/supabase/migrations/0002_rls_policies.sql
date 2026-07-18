-- Tikuvchi: Row Level Security siyosatlari
alter table public.profiles enable row level security;
alter table public.usta_profiles enable row level security;
alter table public.service_categories enable row level security;
alter table public.usta_services enable row level security;
alter table public.portfolio_items enable row level security;
alter table public.reviews enable row level security;
alter table public.orders enable row level security;
alter table public.order_items enable row level security;
alter table public.measurements enable row level security;
alter table public.conversations enable row level security;
alter table public.messages enable row level security;

-- profiles: ism/avatar hammaga ochiq (sharh mualliflari, ustalar uchun kerak), yozish faqat o'ziniki
create policy "profiles_select_all" on public.profiles
  for select using (true);
create policy "profiles_insert_own" on public.profiles
  for insert with check ((select auth.uid()) = id);
create policy "profiles_update_own" on public.profiles
  for update using ((select auth.uid()) = id) with check ((select auth.uid()) = id);

-- usta_profiles: hammaga ochiq, yozish faqat o'ziniki
create policy "usta_profiles_select_all" on public.usta_profiles
  for select using (true);
create policy "usta_profiles_insert_own" on public.usta_profiles
  for insert with check ((select auth.uid()) = user_id);
create policy "usta_profiles_update_own" on public.usta_profiles
  for update using ((select auth.uid()) = user_id) with check ((select auth.uid()) = user_id);

-- service_categories: faqat o'qish (yozish service role orqali)
create policy "service_categories_select_all" on public.service_categories
  for select using (true);

-- usta_services: hammaga ochiq, yozish faqat egasi
create policy "usta_services_select_all" on public.usta_services
  for select using (true);
create policy "usta_services_write_own" on public.usta_services
  for all using ((select auth.uid()) = usta_id) with check ((select auth.uid()) = usta_id);

-- portfolio_items: hammaga ochiq, yozish faqat egasi
create policy "portfolio_select_all" on public.portfolio_items
  for select using (true);
create policy "portfolio_write_own" on public.portfolio_items
  for all using ((select auth.uid()) = usta_id) with check ((select auth.uid()) = usta_id);

-- reviews: hammaga ochiq, mijoz faqat o'z nomidan yozadi
create policy "reviews_select_all" on public.reviews
  for select using (true);
create policy "reviews_insert_own" on public.reviews
  for insert with check ((select auth.uid()) = client_id);
create policy "reviews_update_own" on public.reviews
  for update using ((select auth.uid()) = client_id) with check ((select auth.uid()) = client_id);
create policy "reviews_delete_own" on public.reviews
  for delete using ((select auth.uid()) = client_id);

-- orders: faqat ishtirokchilar (mijoz yoki usta)
create policy "orders_select_participant" on public.orders
  for select using ((select auth.uid()) = client_id or (select auth.uid()) = usta_id);
create policy "orders_insert_participant" on public.orders
  for insert with check ((select auth.uid()) = client_id or (select auth.uid()) = usta_id);
create policy "orders_update_participant" on public.orders
  for update using ((select auth.uid()) = client_id or (select auth.uid()) = usta_id)
  with check ((select auth.uid()) = client_id or (select auth.uid()) = usta_id);

-- order_items: buyurtma ishtirokchilari orqali
create policy "order_items_select_participant" on public.order_items
  for select using (exists (
    select 1 from public.orders o
    where o.id = order_id and ((select auth.uid()) = o.client_id or (select auth.uid()) = o.usta_id)
  ));
create policy "order_items_write_participant" on public.order_items
  for all using (exists (
    select 1 from public.orders o
    where o.id = order_id and ((select auth.uid()) = o.client_id or (select auth.uid()) = o.usta_id)
  )) with check (exists (
    select 1 from public.orders o
    where o.id = order_id and ((select auth.uid()) = o.client_id or (select auth.uid()) = o.usta_id)
  ));

-- measurements: to'liq shaxsiy
create policy "measurements_own" on public.measurements
  for all using ((select auth.uid()) = client_id) with check ((select auth.uid()) = client_id);

-- conversations: faqat ishtirokchilar
create policy "conversations_select_participant" on public.conversations
  for select using ((select auth.uid()) = client_id or (select auth.uid()) = usta_id);
create policy "conversations_insert_participant" on public.conversations
  for insert with check ((select auth.uid()) = client_id or (select auth.uid()) = usta_id);
create policy "conversations_update_participant" on public.conversations
  for update using ((select auth.uid()) = client_id or (select auth.uid()) = usta_id)
  with check ((select auth.uid()) = client_id or (select auth.uid()) = usta_id);

-- messages: suhbat ishtirokchilari; yuborish faqat o'z nomidan
create policy "messages_select_participant" on public.messages
  for select using (exists (
    select 1 from public.conversations c
    where c.id = conversation_id and ((select auth.uid()) = c.client_id or (select auth.uid()) = c.usta_id)
  ));
create policy "messages_insert_own" on public.messages
  for insert with check (
    (select auth.uid()) = sender_id and exists (
      select 1 from public.conversations c
      where c.id = conversation_id and ((select auth.uid()) = c.client_id or (select auth.uid()) = c.usta_id)
    )
  );
-- narx taklifini qabul qilish/rad etish uchun ishtirokchi xabarni yangilay oladi
create policy "messages_update_participant" on public.messages
  for update using (exists (
    select 1 from public.conversations c
    where c.id = conversation_id and ((select auth.uid()) = c.client_id or (select auth.uid()) = c.usta_id)
  )) with check (exists (
    select 1 from public.conversations c
    where c.id = conversation_id and ((select auth.uid()) = c.client_id or (select auth.uid()) = c.usta_id)
  ));

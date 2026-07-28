-- Admin panel: is_admin/is_blocked bayroqlari, ularni himoya qiluvchi
-- funksiyalar, va admin uchun qo'shimcha (mavjudlarini o'zgartirmaydigan)
-- select policy'lar + yozish RPC'lari.

alter table public.profiles
  add column if not exists is_admin   boolean not null default false,
  add column if not exists is_blocked boolean not null default false;

-- profiles_select_all (using true) tufayli bu ikki ustun hammaga ko'rinib
-- qolmasligi uchun ustun darajasida yashiramiz. Faqat is_admin()/
-- current_profile_flags() orqali (SECURITY DEFINER) o'qiladi.
revoke select (is_admin, is_blocked) on public.profiles from authenticated, anon;

create or replace function public.is_admin() returns boolean
language sql stable security definer set search_path = public as $$
  select coalesce((select is_admin from public.profiles where id = auth.uid()), false);
$$;

create or replace function public.current_profile_flags()
returns table(is_admin boolean, is_blocked boolean)
language sql stable security definer set search_path = public as $$
  select is_admin, is_blocked from public.profiles where id = auth.uid();
$$;

revoke execute on function public.is_admin() from public, anon;
grant execute on function public.is_admin() to authenticated;
revoke execute on function public.current_profile_flags() from public, anon;
grant execute on function public.current_profile_flags() to authenticated;

-- is_admin/is_blocked ustun darajasida yashirilgani uchun admin ham
-- bevosita select orqali o'qiy olmaydi (bu barcha authenticated'ga bir xil
-- taalluqli, satr darajasida emas) — shuning uchun ro'yxat sahifasi uchun
-- alohida sanksiyalangan funksiya kerak.
create or replace function public.admin_list_profiles()
returns setof public.profiles
language plpgsql stable security definer set search_path = public as $$
begin
  if not public.is_admin() then
    raise exception 'not authorized';
  end if;
  return query select * from public.profiles order by created_at desc;
end;
$$;

revoke execute on function public.admin_list_profiles() from public, anon;
grant execute on function public.admin_list_profiles() to authenticated;

-- Admin uchun qo'shimcha o'qish huquqi: mavjud participant-only
-- policy'larga ustma-ust (OR) qo'shiladi, ularni almashtirmaydi.
create policy "orders_select_admin" on public.orders
  for select using (public.is_admin());
create policy "order_items_select_admin" on public.order_items
  for select using (public.is_admin());
create policy "order_events_select_admin" on public.order_events
  for select using (public.is_admin());

-- Admin yozish harakatlari: har biri o'zi is_admin()ni qayta tekshiradi.
create or replace function public.admin_set_user_blocked(target_id uuid, blocked boolean)
returns void language plpgsql security definer set search_path = public as $$
begin
  if not public.is_admin() then
    raise exception 'not authorized';
  end if;
  update public.profiles set is_blocked = blocked where id = target_id;
end;
$$;

create or replace function public.admin_set_usta_visibility(
  target_id uuid, p_visible boolean, p_available boolean
)
returns void language plpgsql security definer set search_path = public as $$
begin
  if not public.is_admin() then
    raise exception 'not authorized';
  end if;
  update public.usta_profiles
    set visible = p_visible, available = p_available
    where user_id = target_id;
end;
$$;

create or replace function public.admin_force_order_status(
  p_order_id uuid, p_new_status order_status, p_comment text default null
)
returns void language plpgsql security definer set search_path = public as $$
declare
  v_old order_status;
begin
  if not public.is_admin() then
    raise exception 'not authorized';
  end if;
  select status into v_old from public.orders where id = p_order_id;
  update public.orders set status = p_new_status where id = p_order_id;
  insert into public.order_events(order_id, from_status, to_status, changed_by, comment)
    values (p_order_id, v_old, p_new_status, auth.uid(), p_comment);
end;
$$;

revoke execute on function public.admin_set_user_blocked(uuid, boolean) from public, anon;
grant execute on function public.admin_set_user_blocked(uuid, boolean) to authenticated;
revoke execute on function public.admin_set_usta_visibility(uuid, boolean, boolean) from public, anon;
grant execute on function public.admin_set_usta_visibility(uuid, boolean, boolean) to authenticated;
revoke execute on function public.admin_force_order_status(uuid, order_status, text) from public, anon;
grant execute on function public.admin_force_order_status(uuid, order_status, text) to authenticated;

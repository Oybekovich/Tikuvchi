-- Tikuvchi: asosiy sxema
create extension if not exists "pgcrypto";

-- Enum turlari
create type public.user_role as enum ('client', 'usta');
create type public.gender_segment as enum ('women', 'men', 'unisex');
create type public.order_source as enum ('catalog', 'chat_negotiation');
create type public.order_status as enum ('pending','accepted','in_progress','ready','completed','cancelled');
create type public.payment_status as enum ('pending','partial','paid');
create type public.message_type as enum ('text','price_offer','image');
create type public.price_offer_status as enum ('pending','accepted','declined');

-- Profillar (auth.users bilan 1:1)
create table public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  full_name text not null,
  avatar_url text,
  role public.user_role not null default 'client',
  phone text,
  created_at timestamptz not null default now()
);

-- Usta profillari
create table public.usta_profiles (
  user_id uuid primary key references public.profiles(id) on delete cascade,
  bio text,
  cover_image_url text,
  rating_avg numeric(3,2) not null default 0,
  rating_count int not null default 0,
  location_text text,
  district text,
  work_hours_start time,
  work_hours_end time,
  tags text[] not null default '{}',
  gender_segment public.gender_segment not null default 'women'
);

create table public.service_categories (
  id bigint generated always as identity primary key,
  name text not null,
  icon text,
  gender_segment public.gender_segment not null default 'women'
);

create table public.usta_services (
  id bigint generated always as identity primary key,
  usta_id uuid not null references public.usta_profiles(user_id) on delete cascade,
  category_id bigint references public.service_categories(id) on delete set null,
  title text not null,
  description text,
  base_price bigint not null
);

create table public.portfolio_items (
  id bigint generated always as identity primary key,
  usta_id uuid not null references public.usta_profiles(user_id) on delete cascade,
  image_url text not null,
  caption text,
  sort_order int not null default 0
);

create table public.reviews (
  id bigint generated always as identity primary key,
  usta_id uuid not null references public.usta_profiles(user_id) on delete cascade,
  client_id uuid not null references public.profiles(id) on delete cascade,
  rating int not null check (rating between 1 and 5),
  comment text,
  created_at timestamptz not null default now()
);

create table public.orders (
  id uuid primary key default gen_random_uuid(),
  client_id uuid not null references public.profiles(id) on delete cascade,
  usta_id uuid not null references public.usta_profiles(user_id) on delete cascade,
  source public.order_source not null default 'catalog',
  status public.order_status not null default 'pending',
  total_price bigint not null default 0,
  payment_status public.payment_status not null default 'pending',
  estimated_ready_at date,
  created_at timestamptz not null default now()
);

create table public.order_items (
  id bigint generated always as identity primary key,
  order_id uuid not null references public.orders(id) on delete cascade,
  title text not null,
  material text,
  image_url text,
  size_note text,
  model_note text,
  price bigint not null default 0
);

create table public.measurements (
  id uuid primary key default gen_random_uuid(),
  client_id uuid not null references public.profiles(id) on delete cascade,
  label text not null,
  chest numeric(5,1),
  waist numeric(5,1),
  hips numeric(5,1),
  height numeric(5,1),
  shoulder numeric(5,1),
  sleeve_length numeric(5,1),
  notes text,
  updated_at timestamptz not null default now()
);

create table public.conversations (
  id uuid primary key default gen_random_uuid(),
  client_id uuid not null references public.profiles(id) on delete cascade,
  usta_id uuid not null references public.usta_profiles(user_id) on delete cascade,
  last_message_at timestamptz not null default now(),
  unique (client_id, usta_id)
);

create table public.messages (
  id uuid primary key default gen_random_uuid(),
  conversation_id uuid not null references public.conversations(id) on delete cascade,
  sender_id uuid not null references public.profiles(id) on delete cascade,
  content text,
  message_type public.message_type not null default 'text',
  price_offer_amount bigint,
  price_offer_duration_days int,
  price_offer_note text,
  price_offer_status public.price_offer_status,
  created_at timestamptz not null default now()
);

-- Indekslar
create index idx_usta_services_usta on public.usta_services(usta_id);
create index idx_usta_services_category on public.usta_services(category_id);
create index idx_portfolio_usta on public.portfolio_items(usta_id, sort_order);
create index idx_reviews_usta on public.reviews(usta_id);
create index idx_reviews_client on public.reviews(client_id);
create index idx_orders_client on public.orders(client_id, created_at desc);
create index idx_orders_usta on public.orders(usta_id, created_at desc);
create index idx_order_items_order on public.order_items(order_id);
create index idx_measurements_client on public.measurements(client_id);
create index idx_conversations_client on public.conversations(client_id, last_message_at desc);
create index idx_conversations_usta on public.conversations(usta_id, last_message_at desc);
create index idx_messages_conversation on public.messages(conversation_id, created_at);
create index idx_messages_sender on public.messages(sender_id);

-- Yangi auth foydalanuvchi uchun profil yaratish
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer set search_path = public
as $$
begin
  insert into public.profiles (id, full_name, role, phone)
  values (
    new.id,
    coalesce(new.raw_user_meta_data->>'full_name', split_part(new.email, '@', 1)),
    coalesce((new.raw_user_meta_data->>'role')::public.user_role, 'client'),
    new.raw_user_meta_data->>'phone'
  );
  return new;
end;
$$;

create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.handle_new_user();

-- Sharh o'zgarganda usta reytingini yangilash
create or replace function public.refresh_usta_rating()
returns trigger
language plpgsql
security definer set search_path = public
as $$
declare
  target uuid;
begin
  target := coalesce(new.usta_id, old.usta_id);
  update public.usta_profiles up
  set rating_avg = coalesce((select round(avg(r.rating)::numeric, 2) from public.reviews r where r.usta_id = target), 0),
      rating_count = (select count(*) from public.reviews r where r.usta_id = target)
  where up.user_id = target;
  return coalesce(new, old);
end;
$$;

create trigger on_review_change
  after insert or update or delete on public.reviews
  for each row execute function public.refresh_usta_rating();

-- Yangi xabarda suhbatning last_message_at ni yangilash
create or replace function public.touch_conversation()
returns trigger
language plpgsql
security definer set search_path = public
as $$
begin
  update public.conversations set last_message_at = new.created_at where id = new.conversation_id;
  return new;
end;
$$;

create trigger on_message_insert
  after insert on public.messages
  for each row execute function public.touch_conversation();

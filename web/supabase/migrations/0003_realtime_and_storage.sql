-- Realtime: chat xabarlari va suhbatlar uchun
alter publication supabase_realtime add table public.messages;
alter publication supabase_realtime add table public.conversations;

-- Storage bucketlari: portfolio va avatarlar (hammaga o'qish ochiq)
insert into storage.buckets (id, name, public)
values ('portfolio', 'portfolio', true), ('avatars', 'avatars', true)
on conflict (id) do nothing;

create policy "storage_public_read" on storage.objects
  for select using (bucket_id in ('portfolio', 'avatars'));

create policy "storage_insert_own_folder" on storage.objects
  for insert with check (
    bucket_id in ('portfolio', 'avatars')
    and (select auth.uid()) is not null
    and (storage.foldername(name))[1] = (select auth.uid())::text
  );

create policy "storage_update_own_folder" on storage.objects
  for update using (
    bucket_id in ('portfolio', 'avatars')
    and (storage.foldername(name))[1] = (select auth.uid())::text
  );

create policy "storage_delete_own_folder" on storage.objects
  for delete using (
    bucket_id in ('portfolio', 'avatars')
    and (storage.foldername(name))[1] = (select auth.uid())::text
  );

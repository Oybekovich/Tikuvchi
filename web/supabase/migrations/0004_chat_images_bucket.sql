-- Chatda rasm yuborish uchun bucket (yuboruvchi o'z papkasiga yozadi, o'qish ochiq)
insert into storage.buckets (id, name, public)
values ('chat-images', 'chat-images', true)
on conflict (id) do nothing;

create policy "chat_images_public_read" on storage.objects
  for select using (bucket_id = 'chat-images');

create policy "chat_images_insert_own_folder" on storage.objects
  for insert with check (
    bucket_id = 'chat-images'
    and (select auth.uid()) is not null
    and (storage.foldername(name))[1] = (select auth.uid())::text
  );

create policy "chat_images_delete_own_folder" on storage.objects
  for delete using (
    bucket_id = 'chat-images'
    and (storage.foldername(name))[1] = (select auth.uid())::text
  );

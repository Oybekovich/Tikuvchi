-- Xavfsizlikni mustahkamlash (Supabase security advisor tavsiyalari bo'yicha)

-- 1) Trigger funksiyalari SECURITY DEFINER bilan ishlaydi, ya'ni jadval egasi
--    huquqlari bilan. Ular /rest/v1/rpc/... orqali tashqaridan chaqirilmasligi kerak.
--    EXECUTE'ni olib tashlash triggerlarga ta'sir qilmaydi: trigger funksiyasi
--    chaqiruvchi huquqini emas, jadval egasi huquqini tekshiradi.
revoke execute on function public.handle_new_user() from public, anon, authenticated;
revoke execute on function public.refresh_usta_rating() from public, anon, authenticated;
revoke execute on function public.touch_conversation() from public, anon, authenticated;

-- 2) Storage: bucketlar public, ya'ni fayllar
--    /storage/v1/object/public/... orqali RLS'siz ochiladi. Shuning uchun keng
--    SELECT siyosati kerak emas — u faqat bucket ichini ro'yxatlash (listing)
--    imkonini berardi, bu esa boshqalarning fayl nomlarini fosh qiladi.
drop policy if exists "storage_public_read" on storage.objects;
drop policy if exists "chat_images_public_read" on storage.objects;

-- 3) Ammo SELECT butunlay yo'q bo'lsa, o'chirish/yangilash ishlamaydi:
--    storage delete/update avval SELECT qiladi. Shu sababli SELECT'ni
--    foydalanuvchining o'z papkasi bilan cheklaymiz.
create policy "storage_select_own_folder"
on storage.objects for select to authenticated
using (
  bucket_id in ('portfolio', 'avatars', 'chat-images')
  and (storage.foldername(name))[1] = (select auth.uid())::text
);

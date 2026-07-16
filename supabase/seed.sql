-- ============================================================
-- Tikuvchi seed ma'lumotlari
-- Demo hisoblar (hammasi uchun parol: demo1234):
--   mijoz@demo.uz  — mijoz (Malika Umarova)
--   usta1@demo.uz … usta5@demo.uz — ustalar
-- Eslatma: bu fayl Supabase SQL Editor'da yoki `supabase db reset`
-- jarayonida ishga tushiriladi. Migratsiyalar avval qo'llangan bo'lishi shart.
-- ============================================================

-- Demo foydalanuvchilar (auth.users + auth.identities)
do $$
declare
  demo_users constant jsonb := '[
    {"id":"a0000000-0000-4000-8000-000000000001","email":"usta1@demo.uz","name":"Dilnoza Karimova","role":"usta","phone":"+998901234501"},
    {"id":"a0000000-0000-4000-8000-000000000002","email":"usta2@demo.uz","name":"Madina Yusupova","role":"usta","phone":"+998901234502"},
    {"id":"a0000000-0000-4000-8000-000000000003","email":"usta3@demo.uz","name":"Gulnora Rashidova","role":"usta","phone":"+998901234503"},
    {"id":"a0000000-0000-4000-8000-000000000004","email":"usta4@demo.uz","name":"Zilola Tosheva","role":"usta","phone":"+998901234504"},
    {"id":"a0000000-0000-4000-8000-000000000005","email":"usta5@demo.uz","name":"Nafisa Abdullayeva","role":"usta","phone":"+998901234505"},
    {"id":"c0000000-0000-4000-8000-000000000001","email":"mijoz@demo.uz","name":"Malika Umarova","role":"client","phone":"+998901234511"},
    {"id":"c0000000-0000-4000-8000-000000000002","email":"mijoz2@demo.uz","name":"Nodira Saidova","role":"client","phone":"+998901234512"},
    {"id":"c0000000-0000-4000-8000-000000000003","email":"mijoz3@demo.uz","name":"Sevara Islomova","role":"client","phone":"+998901234513"},
    {"id":"c0000000-0000-4000-8000-000000000004","email":"mijoz4@demo.uz","name":"Feruza Qodirova","role":"client","phone":"+998901234514"}
  ]';
  u jsonb;
begin
  for u in select * from jsonb_array_elements(demo_users) loop
    insert into auth.users (
      instance_id, id, aud, role, email, encrypted_password, email_confirmed_at,
      raw_app_meta_data, raw_user_meta_data, created_at, updated_at,
      confirmation_token, recovery_token, email_change_token_new, email_change
    ) values (
      '00000000-0000-0000-0000-000000000000',
      (u->>'id')::uuid,
      'authenticated', 'authenticated',
      u->>'email',
      crypt('demo1234', gen_salt('bf')),
      now(),
      '{"provider":"email","providers":["email"]}'::jsonb,
      jsonb_build_object('full_name', u->>'name', 'role', u->>'role', 'phone', u->>'phone'),
      now(), now(), '', '', '', ''
    ) on conflict (id) do nothing;

    insert into auth.identities (
      id, user_id, provider_id, identity_data, provider,
      last_sign_in_at, created_at, updated_at
    ) values (
      gen_random_uuid(),
      (u->>'id')::uuid,
      u->>'id',
      jsonb_build_object('sub', u->>'id', 'email', u->>'email', 'email_verified', true, 'phone_verified', false),
      'email', now(), now(), now()
    ) on conflict do nothing;
  end loop;
end $$;

-- Usta profillari
insert into public.usta_profiles (user_id, bio, cover_image_url, location_text, district, work_hours_start, work_hours_end, tags, gender_segment) values
('a0000000-0000-4000-8000-000000000001', '15 yildan beri milliy liboslar tikaman. Atlas va adrasdan zamonaviy hamda an''anaviy modellar — har bir mijozga individual andoza asosida.', '/seed/covers/u1.svg', 'Chilonzor tumani, Toshkent', 'Chilonzor', '09:00', '18:00', '{"milliy libos","atlas","adras"}', 'women'),
('a0000000-0000-4000-8000-000000000002', 'Kechki va bayram liboslari bo''yicha mutaxassisman. Biser, payetka va qo''lda tikilgan bezaklar bilan ishlayman. Har bir ko''ylak — san''at asari.', '/seed/covers/u2.svg', 'Yunusobod tumani, Toshkent', 'Yunusobod', '10:00', '19:00', '{"kechki ko''ylak","biser bezak","individual andoza"}', 'women'),
('a0000000-0000-4000-8000-000000000003', 'Muslima ayollar uchun yopiq, bejirim va zamonaviy liboslar tikaman. Abaya, hijob to''plamlari va yopiq kechki liboslar — sifatli matolardan.', '/seed/covers/u3.svg', 'Mirzo Ulug''bek tumani, Toshkent', 'Mirzo Ulug''bek', '09:00', '17:00', '{"abaya","hijob","yopiq libos"}', 'women'),
('a0000000-0000-4000-8000-000000000004', 'Kundalik va ofis kiyimlari ustasi. Qulay, sifatli va har kuni kiyish mumkin bo''lgan liboslar — aniq o''lcham va tez muddatda.', '/seed/covers/u4.svg', 'Sergeli tumani, Toshkent', 'Sergeli', '10:00', '18:00', '{"kundalik","ofis uslubi","klassik"}', 'women'),
('a0000000-0000-4000-8000-000000000005', 'To''y va marosim liboslari bo''yicha 10 yillik tajriba. Kelin liboslari, milliy to''y to''plamlari va zamonaviy modellar.', '/seed/covers/u5.svg', 'Olmazor tumani, Toshkent', 'Olmazor', '09:00', '19:00', '{"to''y libosi","milliy","zamonaviy"}', 'women');

-- Xizmat kategoriyalari
insert into public.service_categories (name, icon, gender_segment) values
('Milliy liboslar', 'milliy', 'women'),
('Kechki ko''ylaklar', 'kechki', 'women'),
('Kundalik kiyim', 'kundalik', 'women'),
('Muslima kiyimlari', 'muslima', 'women'),
('To''y liboslari', 'toy', 'women');

-- Usta xizmatlari
insert into public.usta_services (usta_id, category_id, title, description, base_price) values
('a0000000-0000-4000-8000-000000000001', (select id from public.service_categories where name='Milliy liboslar'), 'Atlas ko''ylak (milliy)', 'Xonatlas matodan klassik yoki zamonaviy bichimda, astar bilan', 1450000),
('a0000000-0000-4000-8000-000000000001', (select id from public.service_categories where name='Milliy liboslar'), 'Adras libos to''plami', 'Adrasdan ko''ylak va lozim to''plami, qo''lda ishlangan detallar', 1800000),
('a0000000-0000-4000-8000-000000000001', (select id from public.service_categories where name='To''y liboslari'), 'Zamonaviy milliy libos', 'Milliy mato + zamonaviy bichim, marosimlar uchun', 1600000),
('a0000000-0000-4000-8000-000000000002', (select id from public.service_categories where name='Kechki ko''ylaklar'), 'Kechki ko''ylak (klassik)', 'Shifon yoki atlasdan uzun kechki ko''ylak', 2200000),
('a0000000-0000-4000-8000-000000000002', (select id from public.service_categories where name='Kechki ko''ylaklar'), 'Biser bezakli kechki libos', 'Qo''lda tikilgan biser va payetka bezaklari bilan', 2800000),
('a0000000-0000-4000-8000-000000000002', (select id from public.service_categories where name='Kechki ko''ylaklar'), 'Kokteyl ko''ylagi', 'Qisqa bayram ko''ylagi, zamonaviy bichim', 1700000),
('a0000000-0000-4000-8000-000000000003', (select id from public.service_categories where name='Muslima kiyimlari'), 'Abaya (klassik)', 'Krep yoki nida matodan erkin bichimli abaya', 950000),
('a0000000-0000-4000-8000-000000000003', (select id from public.service_categories where name='Muslima kiyimlari'), 'Hijob to''plami', 'Ro''mol va ichlik to''plami, sifatli matodan', 450000),
('a0000000-0000-4000-8000-000000000003', (select id from public.service_categories where name='Muslima kiyimlari'), 'Yopiq kechki libos', 'Marosimlar uchun yopiq, bejirim kechki ko''ylak', 1500000),
('a0000000-0000-4000-8000-000000000004', (select id from public.service_categories where name='Kundalik kiyim'), 'Ofis ko''ylagi', 'Klassik bichimli, qulay ofis ko''ylagi', 850000),
('a0000000-0000-4000-8000-000000000004', (select id from public.service_categories where name='Kundalik kiyim'), 'Kundalik ko''ylak', 'Paxta yoki viskoza matodan yengil ko''ylak', 650000),
('a0000000-0000-4000-8000-000000000004', (select id from public.service_categories where name='Kundalik kiyim'), 'Klassik yubka-kostyum', 'Yubka va jaket to''plami, ofis uslubida', 1250000),
('a0000000-0000-4000-8000-000000000005', (select id from public.service_categories where name='To''y liboslari'), 'Kelin libosi', 'Individual andoza asosida to''y ko''ylagi, bezaklar bilan', 3500000),
('a0000000-0000-4000-8000-000000000005', (select id from public.service_categories where name='To''y liboslari'), 'Milliy to''y to''plami', 'Milliy uslubdagi to''y libosi to''plami', 2400000),
('a0000000-0000-4000-8000-000000000005', (select id from public.service_categories where name='Kundalik kiyim'), 'Zamonaviy ko''ylak', 'Har kunlik va tadbirlar uchun zamonaviy ko''ylak', 1100000);

-- Portfolio (rasm yo'llari /public/seed/portfolio ichida)
insert into public.portfolio_items (usta_id, image_url, caption, sort_order) values
('a0000000-0000-4000-8000-000000000001', '/seed/portfolio/u1-1.svg', 'Xonatlas ko''ylak — to''y marosimi uchun', 1),
('a0000000-0000-4000-8000-000000000001', '/seed/portfolio/u1-2.svg', 'Adras libos to''plami', 2),
('a0000000-0000-4000-8000-000000000001', '/seed/portfolio/u1-3.svg', 'Zamonaviy milliy ko''ylak', 3),
('a0000000-0000-4000-8000-000000000001', '/seed/portfolio/u1-4.svg', 'Atlas lozim-ko''ylak to''plami', 4),
('a0000000-0000-4000-8000-000000000002', '/seed/portfolio/u2-1.svg', 'Qora shifon kechki ko''ylak', 1),
('a0000000-0000-4000-8000-000000000002', '/seed/portfolio/u2-2.svg', 'Biser bezakli libos', 2),
('a0000000-0000-4000-8000-000000000002', '/seed/portfolio/u2-3.svg', 'Yashil atlas kechki ko''ylak', 3),
('a0000000-0000-4000-8000-000000000002', '/seed/portfolio/u2-4.svg', 'Kokteyl ko''ylagi — kumush payetka', 4),
('a0000000-0000-4000-8000-000000000003', '/seed/portfolio/u3-1.svg', 'Klassik qora abaya', 1),
('a0000000-0000-4000-8000-000000000003', '/seed/portfolio/u3-2.svg', 'Yopiq kechki libos — to''q ko''k', 2),
('a0000000-0000-4000-8000-000000000003', '/seed/portfolio/u3-3.svg', 'Hijob to''plami — bej', 3),
('a0000000-0000-4000-8000-000000000003', '/seed/portfolio/u3-4.svg', 'Kundalik abaya — jigarrang', 4),
('a0000000-0000-4000-8000-000000000004', '/seed/portfolio/u4-1.svg', 'Ofis ko''ylagi — kulrang klassik', 1),
('a0000000-0000-4000-8000-000000000004', '/seed/portfolio/u4-2.svg', 'Yozgi kundalik ko''ylak', 2),
('a0000000-0000-4000-8000-000000000004', '/seed/portfolio/u4-3.svg', 'Yubka-kostyum to''plami', 3),
('a0000000-0000-4000-8000-000000000005', '/seed/portfolio/u5-1.svg', 'Kelin libosi — oq atlas', 1),
('a0000000-0000-4000-8000-000000000005', '/seed/portfolio/u5-2.svg', 'Milliy to''y to''plami', 2),
('a0000000-0000-4000-8000-000000000005', '/seed/portfolio/u5-3.svg', 'Fotosessiya uchun libos', 3),
('a0000000-0000-4000-8000-000000000005', '/seed/portfolio/u5-4.svg', 'Zamonaviy to''y ko''ylagi', 4),
('a0000000-0000-4000-8000-000000000005', '/seed/portfolio/u5-5.svg', 'Marosim libosi — pushti', 5);

-- Sharhlar (trigger reytingni avtomatik yangilaydi)
insert into public.reviews (usta_id, client_id, rating, comment, created_at) values
('a0000000-0000-4000-8000-000000000001', 'c0000000-0000-4000-8000-000000000002', 5, 'Atlas ko''ylagim juda chiroyli chiqdi! O''lchamlari aniq, muddatida tayyor bo''ldi. Rahmat!', now() - interval '20 days'),
('a0000000-0000-4000-8000-000000000001', 'c0000000-0000-4000-8000-000000000003', 5, 'Onam uchun adras libos tiktirgan edim, hamma maqtadi. Ish sifati zo''r.', now() - interval '45 days'),
('a0000000-0000-4000-8000-000000000001', 'c0000000-0000-4000-8000-000000000004', 4, 'Chiroyli tikilgan, faqat bir kun kechikdi. Umuman olganda tavsiya qilaman.', now() - interval '60 days'),
('a0000000-0000-4000-8000-000000000002', 'c0000000-0000-4000-8000-000000000003', 5, 'Kechki ko''ylagim aynan xohlagandek chiqdi. Biser bezaklari ajoyib!', now() - interval '15 days'),
('a0000000-0000-4000-8000-000000000002', 'c0000000-0000-4000-8000-000000000001', 5, 'Juda professional usta. O''lchov olishdan tortib topshirishgacha hammasi aniq.', now() - interval '30 days'),
('a0000000-0000-4000-8000-000000000002', 'c0000000-0000-4000-8000-000000000004', 4, 'Ko''ylak chiroyli, narxi biroz baland lekin sifatiga arziydi.', now() - interval '50 days'),
('a0000000-0000-4000-8000-000000000003', 'c0000000-0000-4000-8000-000000000001', 5, 'Abayam juda qulay va bejirim. Mato sifati a''lo darajada.', now() - interval '25 days'),
('a0000000-0000-4000-8000-000000000003', 'c0000000-0000-4000-8000-000000000002', 5, 'Yopiq kechki libos tiktirgandim, to''yda hamma so''radi kim tikkanini!', now() - interval '40 days'),
('a0000000-0000-4000-8000-000000000003', 'c0000000-0000-4000-8000-000000000004', 5, 'Gulnora opa bilan ishlash juda oson. Xohishlarimni to''liq inobatga oldi.', now() - interval '55 days'),
('a0000000-0000-4000-8000-000000000004', 'c0000000-0000-4000-8000-000000000002', 4, 'Ofis ko''ylagim qulay va sifatli. Narxi ham hamyonbop.', now() - interval '10 days'),
('a0000000-0000-4000-8000-000000000004', 'c0000000-0000-4000-8000-000000000003', 5, 'Tez va sifatli. Uch kunda kundalik ko''ylak tikib berdi!', now() - interval '35 days'),
('a0000000-0000-4000-8000-000000000004', 'c0000000-0000-4000-8000-000000000001', 4, 'Yaxshi usta, yubka-kostyum chiroyli chiqdi.', now() - interval '48 days'),
('a0000000-0000-4000-8000-000000000005', 'c0000000-0000-4000-8000-000000000004', 5, 'Kelinlik libosim orzuimdagidek bo''ldi! Har bir detali mukammal.', now() - interval '18 days'),
('a0000000-0000-4000-8000-000000000005', 'c0000000-0000-4000-8000-000000000002', 5, 'Singlimning to''y libosini tiktirgan edik. Juda ham chiroyli va sifatli ish.', now() - interval '42 days'),
('a0000000-0000-4000-8000-000000000005', 'c0000000-0000-4000-8000-000000000003', 5, 'Nafisa opaning qo''li gul! Muddatidan oldin tayyor qildi.', now() - interval '65 days');

-- Buyurtmalar (demo mijoz: Malika)
insert into public.orders (id, client_id, usta_id, source, status, total_price, payment_status, estimated_ready_at, created_at) values
('d0000000-0000-4000-8000-000000000001', 'c0000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000002', 'catalog', 'in_progress', 2200000, 'partial', current_date + 9, now() - interval '5 days'),
('d0000000-0000-4000-8000-000000000002', 'c0000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000003', 'catalog', 'completed', 950000, 'paid', current_date - 20, now() - interval '35 days'),
('d0000000-0000-4000-8000-000000000003', 'c0000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000005', 'catalog', 'ready', 1100000, 'partial', current_date + 1, now() - interval '12 days'),
('d0000000-0000-4000-8000-000000000004', 'c0000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000004', 'catalog', 'pending', 850000, 'pending', current_date + 14, now() - interval '1 day');

insert into public.order_items (order_id, title, material, image_url, size_note, model_note, price) values
('d0000000-0000-4000-8000-000000000001', 'Kechki ko''ylak (klassik)', 'Shifon, qora', '/seed/portfolio/u2-1.svg', 'Asosiy o''lchamlarim', 'Yenglari uzun, orqa tomoni yopiq bo''lsin', 2200000),
('d0000000-0000-4000-8000-000000000002', 'Abaya (klassik)', 'Krep, to''q ko''k', '/seed/portfolio/u3-2.svg', 'Asosiy o''lchamlarim', 'Yon cho''ntaklar qo''shilsin', 950000),
('d0000000-0000-4000-8000-000000000003', 'Zamonaviy ko''ylak', 'Viskoza, terrakota', '/seed/portfolio/u5-3.svg', 'Asosiy o''lchamlarim', 'Tizza ostidan, belida kamar', 1100000),
('d0000000-0000-4000-8000-000000000004', 'Ofis ko''ylagi', 'Paxta aralash, kulrang', '/seed/portfolio/u4-1.svg', 'Asosiy o''lchamlarim', 'Klassik yoqali, tugmali', 850000);

-- O'lchamlar
insert into public.measurements (client_id, label, chest, waist, hips, height, shoulder, sleeve_length, notes, updated_at) values
('c0000000-0000-4000-8000-000000000001', 'Asosiy o''lchamlarim', 92, 74, 100, 165, 38, 58, 'Erkin o''tirishini afzal ko''raman', now() - interval '10 days'),
('c0000000-0000-4000-8000-000000000001', 'Singlim uchun', 84, 66, 92, 158, 36, 55, 'To''y ko''ylagi uchun olingan', now() - interval '30 days');

-- Suhbatlar va xabarlar
insert into public.conversations (id, client_id, usta_id, last_message_at) values
('e0000000-0000-4000-8000-000000000001', 'c0000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000001', now() - interval '1 day'),
('e0000000-0000-4000-8000-000000000002', 'c0000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000002', now() - interval '3 hours');

insert into public.messages (conversation_id, sender_id, content, message_type, price_offer_amount, price_offer_duration_days, price_offer_note, price_offer_status, created_at) values
('e0000000-0000-4000-8000-000000000001', 'c0000000-0000-4000-8000-000000000001', 'Assalomu alaykum! Atlas ko''ylak tiktirmoqchi edim, to''yga 2 hafta qoldi. Ulgurasizmi?', 'text', null, null, null, null, now() - interval '2 days'),
('e0000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000001', 'Vaalaykum assalom! Albatta, atlas ko''ylakni 10-12 kunda tikib beraman. Qanday model o''ylagansiz?', 'text', null, null, null, null, now() - interval '2 days' + interval '5 minutes'),
('e0000000-0000-4000-8000-000000000001', 'c0000000-0000-4000-8000-000000000001', 'Klassik uslubda bo''lsin, yenglari uzun. Mato o''zimda bor.', 'text', null, null, null, null, now() - interval '2 days' + interval '12 minutes'),
('e0000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000001', null, 'price_offer', 1450000, 12, 'Atlas ko''ylak, klassik model, astar bilan. Mato sizniki bo''lgani uchun chegirmali narx.', 'pending', now() - interval '1 day'),
('e0000000-0000-4000-8000-000000000002', 'c0000000-0000-4000-8000-000000000001', 'Assalomu alaykum! Ko''ylagim qachon tayyor bo''ladi?', 'text', null, null, null, null, now() - interval '5 hours'),
('e0000000-0000-4000-8000-000000000002', 'a0000000-0000-4000-8000-000000000002', 'Assalomu alaykum, Malika! Bezak ishlari qoldi xolos, juma kuniga tayyor qilib beramiz 😊', 'text', null, null, null, null, now() - interval '3 hours');

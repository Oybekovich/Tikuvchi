# Tikuvchi — Admin panel

Platformani boshqarish uchun alohida, mustaqil Next.js ilovasi. `web/` va
`android/`dan butunlay ajratilgan — oddiy mijoz/usta ilovasida bu haqda
hech qanday ma'lumot yo'q, faqat manzilni bilganlar kirishga urinadi, va
faqat `is_admin=true` bo'lgan hisoblar haqiqatan kira oladi.

## Sozlash

1. `.env.local` (git'ga tushmaydi) — `web/.env.local`dagi bilan bir xil,
   umumiy Supabase loyihasi:
   ```
   NEXT_PUBLIC_SUPABASE_URL=...
   NEXT_PUBLIC_SUPABASE_ANON_KEY=...
   ```
2. `web/supabase/migrations/0007_admin_panel.sql`ni Supabase Dashboard →
   SQL Editor orqali qo'llang (agar hali qilinmagan bo'lsa).
3. O'zingizni birinchi admin qilib belgilang (bir martalik, SQL Editor'da):
   ```sql
   update public.profiles set is_admin = true
   where id = (select id from auth.users where email = 'sizning-emailingiz');
   ```
4. Oddiy Tikuvchi hisobingiz (email/parol) bilan `/login`dan kiring.

## Qurish

```bash
npm install
npm run dev     # http://localhost:3000
npm run build
```

## Tuzilishi

- `src/proxy.ts` — butun ilovani yopadi: sessiya yo'q → `/login`,
  `is_admin=false` → `/not-authorized`.
- `src/app/` — Umumiy (`/`), Foydalanuvchilar (`/users`), Ustalar
  (`/ustas`), Buyurtmalar (`/orders`, `/orders/[id]`).
- Barcha yozish harakatlari (`admin_set_user_blocked`,
  `admin_set_usta_visibility`, `admin_force_order_status`) `SECURITY
  DEFINER` RPC funksiyalar orqali — service-role kalit ishlatilmaydi,
  har bir chaqiruv o'zi `is_admin()`ni qayta tekshiradi.
- `src/lib/format.ts` — `web/src/lib/format.ts`ning kerakli qismi qo'lda
  ko'chirilgan (natija bir xil bo'lishi shart).

## Deploy

Yangi Vercel loyihasi kerak, **Root Directory** = `admin` (web loyihasidan
alohida). Asosiy ilova bilan bog'liq emas, bog'lanmagan.

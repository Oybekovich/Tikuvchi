# Tikuvchi 🪡

O'zbekistondagi ayol tikuvchi-ustalar bilan mijozlarni bog'laydigan marketplace PWA ilovasi.

**Stek:** Next.js 16 (App Router, TypeScript) · Tailwind CSS 4 · Supabase (Postgres, Auth, Realtime, Storage) · PWA (manifest + service worker)

## Imkoniyatlar

- 🔎 Usta qidiruvi va filtrlar (narx, reyting, tuman, kategoriya)
- 👩‍🎨 Usta profili: portfolio galereyasi, xizmatlar/narxlar, mijozlar sharhlari
- 🧾 **Buyurtma berishning 2 yo'li:**
  - **(A) Katalog orqali** — 3 bosqichli forma: Tur → O'lcham → Yakun (bu vaqtda pastki tab-bar yashirinadi)
  - **(B) Chat orqali kelishuv** — usta yuborgan "Narx taklifi" kartasi qabul qilinsa, avtomatik buyurtma yaratiladi
- 📦 Buyurtma holati: `Qabul qilindi → Tayyorlanmoqda → Tayyor` stepperi + to'lov bosqichlari (`Kutilmoqda → Bo'nak (30%) → To'liq to'langan`)
- 💬 Realtime chat (matn, rasm, narx-taklif kartalari) — Supabase Realtime
- 📏 Tana o'lchovlarini saqlash va boshqarish
- 🌐 Butun interfeys o'zbek tilida (`src/locales/uz.json` — i18n tuzilma, hardcoded matn yo'q)
- 💰 Narxlar yagona formatda: `1 450 000 so'm` (`formatCurrency()` — `src/lib/format.ts`)
- 📱 PWA: o'rnatish (Add to Home Screen), offline fallback sahifa, ikonkalar

## O'rnatish

### 1. Talablar

- Node.js 20+
- Supabase hisobi ([supabase.com](https://supabase.com) — bepul tarif yetarli)

### 2. Bog'liqliklar

```bash
cd Tikuvchi
npm install
```

### 3. Supabase loyihasini ulash

1. [supabase.com/dashboard](https://supabase.com/dashboard) da yangi loyiha yarating
2. **SQL Editor** ochib, quyidagi fayllarni **tartib bilan** ishga tushiring:
   - `supabase/migrations/0001_initial_schema.sql`
   - `supabase/migrations/0002_rls_policies.sql`
   - `supabase/migrations/0003_realtime_and_storage.sql`
   - `supabase/migrations/0004_chat_images_bucket.sql`
   - `supabase/migrations/0005_security_hardening.sql`
   - `supabase/seed.sql` (demo ma'lumotlar: 5 usta, sharhlar, buyurtmalar, chat)
3. **Project Settings → API** dan URL va anon kalitni oling

> Supabase CLI ishlatsangiz: `supabase db push` + SQL Editor orqali `seed.sql`.

### 4. Muhit o'zgaruvchilari

```bash
cp .env.local.example .env.local
```

`.env.local` faylini to'ldiring:

```
NEXT_PUBLIC_SUPABASE_URL=https://SIZNING-LOYIHANGIZ.supabase.co
NEXT_PUBLIC_SUPABASE_ANON_KEY=SIZNING_ANON_KALITINGIZ
```

### 5. Ishga tushirish

```bash
npm run dev        # development (http://localhost:3000)
# yoki
npm run build && npm run start   # production + service worker
```

> Service worker faqat production rejimida ro'yxatdan o'tadi.

## Demo hisoblar

`supabase/seed.sql` dan keyin (hammasi uchun parol: **demo1234**):

| Rol | Email |
|---|---|
| Mijoz | `mijoz@demo.uz` |
| Usta (Dilnoza) | `usta1@demo.uz` |
| Usta (Madina … Nafisa) | `usta2@demo.uz` … `usta5@demo.uz` |

`mijoz@demo.uz` bilan kirsangiz: tayyor buyurtmalar (turli holatlarda), saqlangan o'lchamlar va Dilnoza bilan chatda **kutilayotgan narx taklifi** bor — "Qabul qilish" tugmasini bossangiz, B-yo'l bo'yicha avtomatik buyurtma yaratiladi.

> **Eslatma:** Yangi ro'yxatdan o'tishda Supabase'da "Confirm email" yoqilgan bo'lsa, foydalanuvchi emailini tasdiqlashi kerak. Buni o'chirish: **Authentication → Sign In / Up → Email → Confirm email** ni o'chiring.

## Loyiha tuzilishi

```
src/
  app/                 # App Router sahifalari
    page.tsx           # Bosh sahifa (qidiruv, kategoriyalar, ustalar)
    search/            # Qidiruv + filtrlar
    usta/[id]/         # Usta profili
    usta/[id]/buyurtma # 3 bosqichli buyurtma oqimi (A yo'l)
    orders/            # Buyurtmalar ro'yxati va holati
    chat/              # Suhbatlar va chat oynasi (B yo'l)
    measurements/      # O'lchamlar
    profile/           # Profil va sozlamalar
    auth/              # Kirish / ro'yxatdan o'tish
    manifest.ts        # PWA manifest
    offline/           # Offline fallback
  components/          # BottomNav, AppHeader, Avatar, RatingBadge, PriceTag,
                       # UstaCard, CategoryCard, ReviewCard, OrderStatusStepper,
                       # PriceOfferBubble, EmptyState, LoadingSkeleton …
  lib/
    supabase/          # Browser/server klientlar (@supabase/ssr)
    format.ts          # formatCurrency, formatDate …
    i18n.ts            # t() — tarjima funksiyasi
    payments.ts        # Payme/Click uchun joy (hozircha holat modeli)
    database.types.ts  # Supabase'dan generatsiya qilingan tiplar
  locales/uz.json      # Barcha interfeys matnlari
  proxy.ts             # Auth himoyasi (Next 16 proxy, sobiq middleware)
supabase/
  migrations/          # SQL migratsiyalar (sxema, RLS, realtime, storage)
  seed.sql             # Demo ma'lumotlar
public/
  seed/                # Demo portfolio/cover SVG rasmlari
  icons/               # PWA ikonkalari
  sw.js                # Service worker
```

## Arxitektura eslatmalari

- **Kengaytirish:** kategoriya va usta profillarida `gender_segment` (`women`|`men`|`unisex`) maydoni bor — erkaklar/unisex segmentini qo'shish uchun tayyor.
- **RLS:** foydalanuvchi faqat o'z buyurtmalari/chatlari/o'lchamlarini ko'radi; usta profillari, portfolio va sharhlar hammaga ochiq.
- **Ikkala buyurtma yo'li** bitta `orders` jadvaliga yoziladi, `source` maydoni bilan farqlanadi (`catalog` | `chat_negotiation`).
- **To'lov:** hozircha `payment_status` maydoni sifatida modellashtirilgan; haqiqiy Payme/Click integratsiyasi uchun `src/lib/payments.ts` tayyor.
- **Telefon-OTP:** auth hozir email+parol; Supabase Phone Auth yoqilsa, `AuthForm` ga qo'shish oson.
- **Storage:** bucketlar public — fayllar `/storage/v1/object/public/...` orqali RLS'siz ochiladi, lekin bucket ichini ro'yxatlash faqat o'z papkangiz bilan cheklangan (`0005_security_hardening.sql`).

### Dashboard'dan qo'lda yoqiladigan sozlama

Supabase security advisor bitta ogohlantirish qoldiradi — uni faqat dashboard'dan yoqish mumkin:
**Authentication → Policies → Leaked password protection** (parollarni HaveIBeenPwned bazasiga tekshiradi).

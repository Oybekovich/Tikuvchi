# Tikuvchi — Loyiha holati (AI kontekst hujjati)

> Bu fayl boshqa AI/assistant loyihaga birinchi marta kirganda, hech qanday
> qo'shimcha tushuntirishsiz to'liq kontekstni olishi uchun yozilgan.
> Yangilangan sana: 2026-07-27. Yangilanishi kerak: har safar katta
> funksionallik commit qilinganda yoki rejalar o'zgarganda.

---

## 1. Loyiha nima

**Tikuvchi** — O'zbekistonda buyurtma asosida tikuvchilik xizmati
ko'rsatadigan **usta**larni (tikuvchi-ayollarni) va ularga buyurtma beruvchi
**mijoz**larni bog'lovchi marketplace. To'liq talablar
[PRD_Tikuvchi.md](PRD_Tikuvchi.md) faylida (o'zbek tilida).

Uch platforma, bitta repo, bitta Supabase backend:

```
web/       Next.js 16 ilova → tikuvchi-uz.vercel.app   (ISHLAB CHIQILGAN)
android/   Kotlin + Jetpack Compose                     (ISHLAB CHIQILGAN)
ios/       Swift                                        (HALI YO'Q, faqat reja)
```

Web'da yaratilgan buyurtma Android'da darhol ko'rinadi va aksincha —
chunki ikkalasi ham bitta Supabase loyihasiga ulanadi (auth, DB, storage,
realtime umumiy).

---

## 2. Texnologiyalar

| Qatlam | Tech |
|---|---|
| Web frontend | Next.js 16 (App Router) + React 19 + TypeScript, Tailwind CSS v4 |
| Android | Kotlin + Jetpack Compose, Ktor (Supabase Kotlin SDK), Coil3, Kotlinx Serialization |
| Backend | Supabase: PostgreSQL + PostgREST + Auth (email/password) + Realtime + Storage |
| Xavfsizlik | Row Level Security (RLS) barcha jadvallarda |

**MUHIM:** `web/AGENTS.md` (= `web/CLAUDE.md`) shuni ogohlantiradi: bu
**Next.js 16** — training datadagi Next.js'dan farq qiladi (masalan,
`middleware.ts` endi `proxy.ts` deb ataladi). Web'da kod yozishdan oldin
`web/node_modules/next/dist/docs/` ichidagi hujjatlarni tekshirish tavsiya
etiladi.

---

## 3. Ma'lumotlar bazasi (Supabase, `web/supabase/migrations/`)

6 ta migratsiya fayli, xronologik tartibda o'qilishi kerak:

1. **0001_initial_schema.sql** — asosiy sxema. Jadvallar: `profiles` (1:1
   `auth.users` bilan, trigger orqali avtomatik yaratiladi),
   `usta_profiles`, `service_categories`, `usta_services`,
   `portfolio_items`, `reviews`, `orders`, `order_items` (allaqachon
   `material`, `model_note`, `size_note`, `image_url`, `price` ustunlari
   bor), `measurements` (faqat egasi ko'radi), `conversations` (unique
   `client_id+usta_id`), `messages`.
   Enumlar: `user_role`(client/usta), `gender_segment`(women/men/unisex),
   `order_source`(catalog/chat_negotiation),
   `order_status`(pending→accepted→in_progress→ready→completed, yoki
   cancelled), `payment_status`(pending/partial/paid),
   `message_type`(text/price_offer/image),
   `price_offer_status`(pending/accepted/declined).
   Triggerlar (hammasi `SECURITY DEFINER`): `handle_new_user`,
   `refresh_usta_rating`, `touch_conversation`.
2. **0002_rls_policies.sql** — RLS barcha jadvallarda. `profiles`/
   `usta_profiles`/servis/portfolio/review — public read, owner write.
   `orders`/`order_items`/`conversations`/`messages` — faqat ishtirokchilar
   (`client_id` YOKI `usta_id` = `auth.uid()`).
3. **0003 / 0004** — Storage bucketlar: `portfolio`, `avatars`,
   `chat-images` — hammasi `public: true`, papka egasi orqali yozish
   cheklangan (`storage.foldername(name)[1] = auth.uid()`).
4. **0005_security_hardening.sql** — trigger funksiyalarni to'g'ridan-to'g'ri
   RPC orqali chaqirishni bloklaydi; storage listing'ni faqat o'z papkasiga
   cheklaydi (lekin fayllar public URL orqali baribir hammaga ochiq —
   ataylab shunday, chunki ilova rasmlarni shu yo'l bilan ko'rsatadi).
5. **0006_usta_interface.sql** — `usta_profiles.available` /
   `.visible` (boolean) va yangi `order_events` audit jadvali
   (from_status/to_status/changed_by/comment) qo'shildi. **Bu migratsiya
   kodda hali TO'LIQ ishlatilmagan** — pastdagi 6-bo'limga qarang.

---

## 4. Bajarilgan ishlar (MVP, `main`ga commit qilingan)

PRD'dagi "Bosqich 1 — MVP" ro'yxatining barchasi ishlaydi:

- Email/parol bilan ro'yxatdan o'tish va kirish (web: `proxy.ts` orqali
  butun ilova auth ortida yopiladi; Android: `TikuvchiRoot.kt` sessionni
  tekshiradi)
- Usta katalogi, kategoriya/tuman/narx/reyting bo'yicha qidiruv va filtr
- Usta profili: portfolio, xizmatlar, reyting, reviewlar
- Real-time chat (matn, rasm, price-offer) — Supabase Realtime orqali
- Price-offer qabul qilinganda avtomatik order yaratish
  (`web/src/components/ChatWindow.tsx`, `respondToOffer()`)
- O'lchamlar CRUD (mijozga xos, RLS bilan himoyalangan)
- Buyurtma yaratish (3-bosqichli wizard), status kuzatish, to'lov bosqichi
  (pending→partial 30%→paid — haqiqiy Payme/Click integratsiyasi hali
  stub, ko'ring `web/src/lib/payments.ts`)
- Web PWA (offline sahifa, service worker)

---

## 5. HOZIR ISHLANAYOTGAN (commit qilinmagan, working tree'da)

Ikkala platformada (**web va Android**) bir vaqtda, bir xil g'oya asosida
**parallel** ishlanmoqda: **"Usta buyurtma oqimi" refaktoringi** —
avvalgi "buyurtma → chatga shablon xabar" oqimi o'rniga
**"buyurtma → to'g'ridan-to'g'ri `orders` jadvaliga `pending` sifatida
yoziladi, usta uni ro'yxatdan qabul/rad etadi"** modeliga o'tish, hamda
**client-lokal unread-chat belgisi** qo'shilishi.

Hech biri hali commit qilinmagan — `git status` / `git diff` orqali
tekshirish mumkin.

### 5.1. Order yaratish oqimi o'zgardi (web + Android)

- **Eski oqim:** `OrderWizard` mijoz ma'lumotlarini (mato, model, o'lcham,
  narx taklifi) yig'ib, ustaga **chatga shablon matnli xabar** yuborardi
  (`"📋 Yangi buyurtma taklifi: ..."`), keyin chatga yo'naltirardi. Haqiqiy
  `orders` qatori faqat usta price-offer yuborib, mijoz qabul qilgandan
  keyin yaratilardi.
- **Yangi oqim:** `web/src/components/OrderWizard.tsx` endi wizard
  oxirida to'g'ridan-to'g'ri `orders` (status=`pending`,
  source=`catalog`) va `order_items` (title, material, model_note,
  size_note, price) qatorlarini yozadi va `/orders/[id]` sahifasiga
  yo'naltiradi — chat orqali o'tmaydi. Android'da xuddi shu o'zgarish
  `OrderWizardScreen.kt` / `OrderWizardViewModel.kt`da — narx endi
  xizmat-katalogidan emas, erkin matn (free-text) sifatida kiritiladi.
- Bu degani: chat orqali kelishuv (`chat_negotiation`, price-offer) hali
  ham ishlaydi va alohida oqim sifatida qoladi, lekin katalogdan
  to'g'ridan-to'g'ri buyurtma berish endi chatni chetlab o'tadi.

### 5.2. Usta uchun "buyurtmalarni qabul/rad etish" UI qo'shildi

- **Web:** yangi komponent
  [`web/src/components/OrderCardActions.tsx`](web/src/components/OrderCardActions.tsx) —
  `orders` ro'yxatidagi har bir pending buyurtma kartochkasida (faqat
  usta ko'rganda) Qabul/Rad tugmalari. `web/src/app/orders/page.tsx` endi
  `.eq("client_id", ...)` o'rniga `.or("client_id.eq...,usta_id.eq...")`
  bilan **ikkala rol uchun** buyurtmalarni tortadi va `isUsta` ni
  hisoblaydi.
  `web/src/components/OrderActions.tsx` (detail sahifa) ga `isUsta` va
  `ustaId` propslari qo'shildi: usta pending buyurtmani ko'rsa
  Qabul/Rad (ikki bosqichli tasdiqlash bilan) ko'radi, mijoz bo'lsa
  eski Bekor qilish tugmasi qoladi.
- **Android:** mos ravishda `OrdersScreen.kt`/`OrdersViewModel.kt`,
  `OrderDetailScreen.kt`/`OrderDetailViewModel.kt` kengaytirilgan, va
  butunlay yangi **Dashboard** ekrani qo'shilgan —
  `android/.../ui/screens/dashboard/DashboardScreen.kt` +
  `DashboardViewModel.kt`: usta uchun statistika (active/pending soni,
  daromad) + pending buyurtmalar ro'yxati, inline Qabul/Rad bilan. Bu
  ustalar uchun Home o'rnini bosadi (`TikuvchiRoot.kt`da rolga qarab
  boshlang'ich yo'nalish tanlanadi: mijoz → `HOME`, usta → `DASHBOARD`).

### 5.3. Usta "do'kon" (Shop) boshqaruvi — faqat Android'da hozircha

- Yangi fayllar: `android/.../data/ShopRepository.kt`,
  `ui/screens/shop/ShopScreen.kt` + `ShopViewModel.kt`. 3 ta tab: Profil
  (bio, manzil, tuman, ish vaqti, teglar, gender segment),
  Xizmatlar (CRUD), Sozlamalar (`available`/`visible` bayroqlari —
  bular 0006 migratsiyada qo'shilgan ustunlar, va nihoyat shu yerda
  ishlatilmoqda).
- **Web tomonida bu funksiya hali YO'Q** — usta o'z profili/xizmatlarini
  faqat Android orqali boshqarishi mumkin. Bu PRD "Bosqich 2"dagi
  "Usta xizmatlarini boshqarish (web UI)" va "Portfolio boshqaruvi
  (web UI)" bandlariga to'g'ri keladi — **web tarafi hali qilinmagan**.
- `order_events` jadvali (0006) esa **hech qayerda** (na web, na
  Android) ishlatilmayapti — status o'zgarishlari hamon faqat
  `orders.status`ni yangilaydi, audit yozuvi yaratilmaydi.

### 5.4. Chat: unread (o'qilmagan) belgisi

- **Web:** yangi hook
  [`web/src/hooks/useUnreadChat.ts`](web/src/hooks/useUnreadChat.ts) —
  `localStorage`da (`tikuvchi:chat_last_read`) suhbat bo'yicha oxirgi
  o'qilgan vaqtni saqlaydi, Realtime orqali yangi xabarlarni kuzatadi.
  `web/src/components/BottomNav.tsx`dagi Chat tabida qizil nuqta
  ko'rsatadi. Yangi
  [`web/src/components/ChatListClient.tsx`](web/src/components/ChatListClient.tsx)
  suhbatlar ro'yxatida o'qilmagan suhbatlarni **qalin** shrift bilan
  ajratadi.
  `web/src/app/chat/page.tsx` ham mos ravishda `client_id` bo'yicha
  filtrlashdan `.or(client_id/usta_id)`ga o'tdi — **avval usta chat
  ro'yxatini to'g'ri ko'ra olmasdi** (faqat mijoz sifatida so'rov
  yuborardi), endi tuzatildi.
- **Android:** parallel yechim — `android/.../data/ChatUnreadManager.kt`
  (yangi fayl): `ChatUnreadStore` SharedPreferences'da JSON xarita
  saqlaydi (`conversationId → lastReadIso`), `fetchUnreadConversations()`
  esa serverdan solishtirib chiqadi.
- **Ikkala tomonda ham bir xil cheklov:** bu **faqat lokal/client-side**
  yechim — DB'da `read_at`/`last_read_at` ustuni yo'q. Demak: (a)
  qurilmalar/brauzerlar orasida sinxronlanmaydi, (b) qayta o'rnatilsa
  yo'qoladi, (c) push-notifikatsiya kabi server-tomon funksiyalar buning
  ustiga qurilolmaydi. Agar bu doimiy funksiya bo'lishi kerak bo'lsa,
  DB darajasida `read_at` ustuni/jadvali kerak bo'ladi.

---

## 6. Ma'lum kamchiliklar / potentsial buglar (kod o'qish orqali topilgan)

Hech biri hali tuzatilmagan, faqat qayd etilgan:

1. **Tranzaksiya yo'q order yaratishda** — ham web
   (`OrderWizard.tsx` submit()), ham Android (`OrdersRepository`)da
   `orders` va `order_items` ketma-ket, alohida insert qilinadi. Ikkinchi
   insert xato bersa, "yetim" (bo'sh) order qolib ketadi.
2. **Price-offer'ni ikki marta qabul qilish** —
   `web/src/components/ChatWindow.tsx` `respondToOffer()`da tugma tez-tez
   bosilsa, bitta price-offer uchun ikkita `orders` qatori yaratilishi
   mumkin (idempotentlik/holat tekshiruvi yo'q, tugma insert tugagunga
   qadar disable qilinmaydi).
3. **Android `OrdersRepository.reject()`** — oldingi statusni
   so'ramasdan har doim `insertEvent(id, "pending", "cancelled")` deb
   yozadi (`progressStatus()` esa to'g'ri oldingi statusni oladi —
   nomuvofiqlik).
4. **Android `ShopRepository`** — `ProfileUpdate.coverImageUrl`
   maydoni typed qilingan (`JsonNull? = null`) lekin hech qayerda
   to'ldirilmaydi — agar kelajakda kutubxona `encodeDefaults`ni yoqsa,
   profil saqlashda `cover_image_url` sukut bo'yicha `null`ga
   tushib qolishi mumkin.
5. **`OrderWizard`da `source` hamon `"catalog"`ga qattiq yozilgan** —
   endi buyurtma yaratishning yagona yo'li shu bo'lgani uchun to'g'ri,
   lekin agar kelajakda chat orqali ham to'g'ridan-to'g'ri order yaratish
   yo'li qo'shilsa, bu qiymatni tekshirish kerak bo'ladi.
6. **`web/src/app/page.tsx` va `search/page.tsx`** — kategoriya so'rovi
   qattiq `.eq("gender_segment", "women")` bilan cheklangan; `men`/
   `unisex` DB'da bor, lekin UI orqali hech qachon ko'rinmaydi.
7. **`search/page.tsx`** — matn/kategoriya/narx filtri server component
   ichida **client-side** (`matches()` funksiyasi) barcha ustalarni
   tortib olgandan keyin bajariladi — hozircha kichik ma'lumot hajmida
   muammo emas, lekin haqiqiy qidiruv indeksi emas.

---

## 7. Keyingi bosqichlar (PRD roadmap, hali BOSHLANMAGAN)

PRD "Bosqich 2 — Yaqin muddatli"dan hali qolganlari:

- [ ] Usta onboarding (ro'yxatdan o'tishda usta profilini yaratish oqimi)
- [ ] Usta xizmatlarini boshqarish — **web UI** (Android'da bor, web'da yo'q)
- [ ] Portfolio boshqaruvi — **web UI** (Android'da yo'q, web'da ham yo'q)
- [ ] Review qoldirish UI (ko'rish bor, yozish UI yo'q)
- [ ] Avatar yuklash UI
- [ ] To'lov tizimi integratsiyasi (Payme / Click) — hozir faqat qo'lda
      bosiladigan status stepper, haqiqiy to'lov yo'q

"Bosqich 3 — Uzoq muddatli": iOS ilova, telefon-OTP, push-bildirishnoma,
geolokatsiya tracking, admin panel, erkaklar kategoriyasi, ko'p tillilik —
hech biri boshlanmagan.

---

## 8. Tezkor navigatsiya — muhim fayllar

| Mavzu | Web | Android |
|---|---|---|
| Auth/session | `src/proxy.ts`, `src/lib/auth.ts`, `src/lib/supabase/{client,server}.ts` | `data/Supabase.kt`, `ui/TikuvchiRoot.kt` |
| Order yaratish | `src/components/OrderWizard.tsx` | `ui/screens/order/OrderWizard{Screen,ViewModel}.kt` |
| Order boshqarish | `src/components/{OrderActions,OrderCardActions}.tsx` | `data/OrdersRepository.kt`, `ui/screens/orders/*`, `ui/screens/dashboard/*` |
| Chat | `src/components/ChatWindow.tsx`, `src/hooks/useUnreadChat.ts` | `data/ChatRepository.kt`, `data/ChatUnreadManager.kt`, `ui/screens/chat/*` |
| Usta do'kon boshqaruvi | — (yo'q) | `data/ShopRepository.kt`, `ui/screens/shop/*` |
| Formatlash (pul/sana/telefon) | `src/lib/format.ts` | `util/Format.kt` (qo'lda ko'chirilgan, natija bir xil bo'lishi shart) |
| Dizayn tokenlari | `src/app/globals.css` (`@theme`) | `ui/theme/Color.kt`, `Type.kt` |
| DB sxema | `supabase/migrations/0001…0006*.sql` | `data/model/Models.kt` (qo'lda mos keltirilgan) |

---

## 9. Ishga tushirish

- Web: `web/.env.local` kerak (Supabase URL/key), `cd web && npm run dev`.
- Android: `android/local.properties` kerak (`SUPABASE_URL`,
  `SUPABASE_ANON_KEY`), `export JAVA_HOME=~/android-studio/jbr && ./gradlew :app:assembleDebug`.
- Ikkalasi ham **bitta** Supabase loyihasiga ulanishi kerak — kalitlar
  git'ga tushmaydi, qo'lda kiritiladi.

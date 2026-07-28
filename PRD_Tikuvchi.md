# Product Requirements Document (PRD)

**Loyiha:** Tikuvchi — Tikuvchi-ustalar va mijozlarni bog'lovchi platforma  
**Versiya:** 1.0  
**Status:** Ishlab chiqilmoqda  

---

## 1. Kirish

### 1.1. Muammo

O'zbekistonda buyurtma asosida tikuvchilik xizmati ko'rsatadigan ustalarni topish, ularning portfelini ko'rish, o'lchamlarni yetkazish va buyurtmani kuzatish uchun yagona platforma mavjud emas. Mijozlar ustalarni og'zaki tavsiyalar orqali topadi, o'lchamlarni qayta-qayta yetkazishga majbur bo'ladi, buyurtma holatini kuzatish imkoniga ega bo'lmaydi.

### 1.2. Yechim

Tikuvchi — bu uch platformada (Web, Android, iOS) ishlaydigan marketplace bo'lib, unda:

- Mijozlar ustalarni kategoriya, tuman, reyting va narx bo'yicha qidirishi mumkin
- Ustalarning portfeli, xizmatlari va reytingini ko'rish mumkin
- O'lchamlarni bir marta kiritib, barcha buyurtmalarda ishlatish mumkin
- Buyurtma jarayoni shaffof: status, to'lov, chat orqali aloqa
- To'lov va chat orqali narx kelishuvi bilan buyurtma yaratish

### 1.3. Maqsadli Auditoriya

- **Mijozlar:** 18–45 yoshli ayollar, buyurtma asosida kiyim tiktiradiganlar
- **Ustalar:** 25–55 yoshli professional tikuvchi-ayollar, o'z portfelini namoyish qilish va buyurtma olishni xohlovchilar

---

## 2. Foydalanuvchi Rollari

### 2.1. Client (Mijoz)

| Imkoniyat | Tavsif |
|---|---|
| Ro'yxatdan o'tish / Kirish | Email + parol orqali |
| Katalogni ko'rish | Barcha ustalar, kategoriyalar bo'yicha |
| Qidiruv va filtr | Tuman, kategoriya, narx oralig'i, reyting |
| Usta profili | Portfel, reyting, xizmatlar, chat |
| Chat (matn, rasm) | Real-time xabar almashish |
| O'lchamlar CRUD | O'z o'lchamlarini boshqarish |
| Buyurtma yaratish | Usta profilidan yoki chat orqali |
| Buyurtmani bekor qilish | Faqat pending statusda |
| To'lov statusi | Avans va qoldiqni kuzatish |
| Ustaga review berish | Tugallangan buyurtmadan so'ng |

### 2.2. Usta (Tikuvchi)

| Imkoniyat | Tavsif |
|---|---|
| Ro'yxatdan o'tish / Kirish | Email + parol orqali (ustalik profilini yaratish alohida) |
| Chat (matn, rasm, price offer) | Mijoz bilan muzokara |
| Buyurtmani qabul qilish / rad etish | Pending buyurtmalar |
| Buyurtmani statusda olib borish | pending → accepted → in_progress → ready → completed |
| To'lov statusini yangilash | pending → partial → paid |
| O'z profilini boshqarish | Bio, ish vaqti, manzil, teglar |
| Xizmatlar va portfel | Xizmatlar ro'yxati, portfolio rasmlari |

---

## 3. Funksional Talablar

### 3.1. Autentifikatsiya va Profil

| ID | Talab | Prioritet |
|---|---|---|
| F-01 | Email va parol orqali ro'yxatdan o'tish | High |
| F-02 | Profil ma'lumotlarini tahrirlash (ism, telefon, avatar) | High |
| F-03 | Telefon raqamni avtomatik formatlash (+998 XX XXX XX XX) | High |
| F-04 | Demo-akkaunt bilan kirish (mijoz@demo.uz / demo1234) | Medium |
| F-05 | Telefon-OTP orqali autentifikatsiya (keyingi bosqich) | Low |

### 3.2. Katalog va Qidiruv

| ID | Talab | Prioritet |
|---|---|---|
| F-06 | Ustalarni kartochkalar ko'rinishida ko'rsatish | High |
| F-07 | Kategoriyalar bo'yicha filtrlash (ayollar kiyimi) | High |
| F-08 | Tuman bo'yicha filtrlash | High |
| F-09 | Narx oralig'i bo'yicha filtrlash | High |
| F-10 | Minimal reyting bo'yicha filtrlash | Medium |
| F-11 | Matn bo'yicha qidiruv (ism, tuman, bio, teglar) | High |

### 3.3. Usta Profili

| ID | Talab | Prioritet |
|---|---|---|
| F-12 | Usta haqida ma'lumot: avatar, cover, bio, ish vaqti, manzil | High |
| F-13 | Reyting va reviewlar | High |
| F-14 | Portfolio galereyasi (rasm + caption) | High |
| F-15 | Xizmatlar ro'yxati (nomi, tavsifi, narxi) | Medium |
| F-16 | "Yozish" va "Buyurtma berish" tugmalari | High |

### 3.4. Chat

| ID | Talab | Prioritet |
|---|---|---|
| F-17 | Real-time matnli xabar almashish | High |
| F-18 | Rasm yuborish (Supabase storage) | High |
| F-19 | Price offer (narx, muddat, izoh) yuborish | High |
| F-20 | Price offerni qabul qilish / rad etish | High |
| F-21 | Price offer qabul qilinganda avtomatik buyurtma yaratish | High |
| F-22 | Suhbatlar ro'yxati (oxirgi xabar, vaqt) | High |

### 3.5. O'lchamlar

| ID | Talab | Prioritet |
|---|---|---|
| F-23 | O'lchamlar ro'yxatini ko'rish (label, ko'krak, bel, son, bo'y, yelka, yeng) | High |
| F-24 | Yangi o'lcham qo'shish | High |
| F-25 | O'lchamni tahrirlash | High |
| F-26 | O'lchamni o'chirish | High |
| F-27 | Buyurtma yaratishda mavjud o'lchamlarni tanlash yoki yangi kiritish | High |

### 3.6. Buyurtmalar

| ID | Talab | Prioritet |
|---|---|---|
| F-28 | Buyurtma yaratish (3 bosqichli wizard) | High |
| F-29 | Buyurtmani status bo'yicha kuzatish | High |
| F-30 | Buyurtma raqamini ko'rsatish (UUID oxirgi 6 hex, uppercase) | High |
| F-31 | "Active" va "Finished" tablari | High |
| F-32 | Usta: pending buyurtmani qabul qilish / rad etish | High |
| F-33 | Mijoz: pending buyurtmani bekor qilish | High |
| F-34 | Buyurtma detallari: material, model, narx, o'lcham, sana | High |
| F-35 | To'lov statusi: pending → partial (30%) → paid | Medium |
| F-36 | Taxminiy tayyor bo'lish sanasi | Medium |

### 3.7. Reviewlar

| ID | Talab | Prioritet |
|---|---|---|
| F-37 | Reviewlarni ko'rish (reyting, matn, sana) | High |
| F-38 | Review qoldirish (1–5 yulduz + matn) | Low |
| F-39 | Reytingni avtomatik hisoblash (trigger) | High |

---

## 4. Nofunksional Talablar

| ID | Talab | Prioritet |
|---|---|---|
| NF-01 | Mobile-first dizayn (web va nativ ilova) | High |
| NF-02 | PWA qo'llab-quvvatlash (offline sahifa, service worker) | Medium |
| NF-03 | Minimal SDK: Android 8.0 (API 26) | High |
| NF-04 | Maxfiy ma'lumotlar: o'lchamlar faqat mijozga ko'rinadi (RLS) | High |
| NF-05 | Real-time chat: Supabase Realtime kanallar | High |
| NF-06 | Rasm yuklash: WebP yoki JPEG, maksimal 5 MB | Medium |
| NF-07 | Tarmoq uzilganda avtomatik qayta ulanish (Android) | Medium |
| NF-08 | Izchil UI: Web va Android bir xil rang sxemasi, shrift, terminologiya | High |
| NF-09 | SEO: usta profillari va katalog indeksatsiya qilinadi | Medium |

---

## 5. Texnik Stack

| Tarmoq | Texnologiya | Versiya |
|---|---|---|
| **Frontend (Web)** | Next.js (App Router) + React + TypeScript | 16.x / 19.x / ^5 |
| **Styling (Web)** | Tailwind CSS | ^4 |
| **Mobile (Android)** | Kotlin + Jetpack Compose | 2.1.x / BOM 2024.12.01 |
| **Mobile (iOS)** | Rejalashtirilgan | — |
| **Backend** | Supabase (PostgreSQL + PostgREST + Auth + Realtime + Storage) | — |
| **Auth** | Supabase Auth (Email/Password) | — |
| **Database** | PostgreSQL + pgcrypto + Row Level Security | — |
| **HTTP Client (Android)** | Ktor (OkHttp engine) | 3.1.x |
| **Image Loading (Android)** | Coil3 | 3.1.x |
| **Serialization** | Kotlinx Serialization | 1.8.x |
| **Async** | Kotlin Coroutines | 1.10.x |
| **Build (Android)** | Gradle + AGP | 8.10.2 / 8.7.3 |

---

## 6. Ma'lumotlar Bazasi Sxemasi

### 6.1. Jadval va Munosabatlar

```
auth.users (Supabase)
    ↓ 1:1 (trigger)
profiles (id, full_name, avatar_url, role, phone)
    ↓ 1:1
usta_profiles (user_id, bio, cover_image, rating_avg, district, tags, ish_vaqti)
    ├── 1:N → usta_services (category_id → service_categories)
    ├── 1:N → portfolio_items
    ├── 1:N ← reviews (client_id → profiles)
    ├── 1:N ← orders (client_id → profiles)
    └── 1:N ← conversations (client_id → profiles)

orders (client_id, usta_id, source, status, total_price, payment_status)
    └── 1:N → order_items

conversations (client_id, usta_id) [UNIQUE pair]
    └── 1:N → messages

measurements (client_id) [private — faqat egasi ko'radi]
```

### 6.2. Enumlar

| Enum | Qiymatlari |
|---|---|
| `user_role` | `client`, `usta` |
| `gender_segment` | `women`, `men`, `unisex` |
| `order_source` | `catalog`, `chat_negotiation` |
| `order_status` | `pending`, `accepted`, `in_progress`, `ready`, `completed`, `cancelled` |
| `payment_status` | `pending`, `partial`, `paid` |
| `message_type` | `text`, `price_offer`, `image` |
| `price_offer_status` | `pending`, `accepted`, `declined` |

### 6.3. Storage Buckets

| Bucket | Maqsad | Status |
|---|---|---|
| `portfolio` | Usta portfolio rasmlari | Public read, owner write |
| `avatars` | Foydalanuvchi avatar rasmlari | Public read, owner write |
| `chat-images` | Chatda yuborilgan rasmlar | Public read, participant write |

---

## 7. UI/UX Printsiplari

### 7.1. Dizayn tizimi

- **Ranglar:** Cream (fon), Terra (asosiy), Ink (matn), Gold (aksent), Sage (yashil)
- **Shrift:** Manrope (web), Default (Android)
- **Pastki navigatsiya:** 5 tab — Home, Orders, Measurements, Chat, Profile
- **Order raqami formati:** UUID oxirgi 6 ta belgi, katta harf (masalan: `A3F8C2`)

### 7.2. Platformalar o'rtasidagi izchillik

- Pastki navigatsiya web va Android'da bir xil tartibda
- Sana va vaqt formati bir xil (o'zbekcha oy nomlari, "Kecha")
- Pul formati: probel bilan ajratilgan mingliklar, "so'm" qo'shimchasi
- Xatolik, loading, empty state'lar bir xil ko'rinishda
- Telefon raqam formati bir xil logika bilan

### 7.3. Maxsus holatlar

- Pastki navigatsiya order wizard, auth va chat conversation sahifalarida yashirinadi
- Tarmoq uzilganda Android'da avtomatik qayta ulanish
- Web'da PWA offline sahifa ko'rsatiladi
- Chatda real-time yangilanishlar (Supabase Realtime kanallar)

---

## 8. Xavfsizlik

### 8.1. Row Level Security (RLS)

- Barcha jadvallarda RLS yoqilgan
- `profiles` va `usta_profiles`: hamma o'qiy oladi, faqat egasi yozadi
- `orders`, `order_items`, `conversations`, `messages`: faqat ishtirokchilar (client yoki usta)
- `measurements`: faqat egasi ko'radi (shaxsiy ma'lumot)
- `reviews`: hamma o'qiy oladi, faqat review egasi yozadi/o'chiradi
- Storage: fayllar `{userId}/` papkasida, listing faqat egasi uchun, URL orqali hamma ko'radi

### 8.2. Autentifikatsiya

- Supabase Auth (email/password) orqali sessiyalarni boshqarish
- Web: SSR proxy orqali session cookie
- Android: Session status orqali auth state
- Trigger orqali ro'yxatdan o'tganda avtomatik profile yaratish

### 8.3. Trigger funksiyalari

- `refresh_usta_rating()` — review INSERT/UPDATE/DELETE da ratingni qayta hisoblash
- `touch_conversation_ts()` — yangi xabarda `last_message_at` ni yangilash
- `handle_new_user()` — yangi foydalanuvchi ro'yxatdan o'tganda profile yaratish

---

## 9. Holatlar va Oqimlar

### 9.1. Buyurtma hayot sikh

```
Mijoz buyurtma yaratadi (catalog) ──→ pending
                                         │
                          ┌──────────────┴──────────────┐
                          ↓                              ↓
                     Usta qabul qiladi              Usta rad etadi
                          │                              │
                          ↓                              ↓
                     accepted                       cancelled
                          │
                          ↓
                   Usta: in_progress
                          │
                          ↓
                     Usta: ready
                          │
                          ↓
                    Mijoz: completed
```

### 9.2. Chat orqali buyurtma

```
Usta price offer yuboradi ──→ pending
                                  │
                     ┌────────────┴────────────┐
                     ↓                          ↓
              Mijoz qabul qiladi          Mijoz rad etadi
                     │                          │
                     ↓                          ↓
          Order yaratiladi (accepted)      declined
```

### 9.3. To'lov oqimi

```
pending ──→ partial (30% deposit) ──→ paid
```

---

## 10. Keyingi Bosqichlar (Roadmap)

### Bosqich 1 — MVP (hozirgi holat) ✅

- [x] Autentifikatsiya (email/password)
- [x] Usta katalogi va qidiruv
- [x] Usta profili (portfel, review, xizmatlar)
- [x] Chat (matn, rasm, price offer)
- [x] O'lchamlar CRUD
- [x] Buyurtma yaratish va boshqarish
- [x] Web PWA
- [x] Android ilova

### Bosqich 2 — Ya qin muddatli

- [ ] Usta onboarding (ro'yxatdan o'tishda usta profilini yaratish)
- [ ] Usta xizmatlarini boshqarish (web UI)
- [ ] Portfolio boshqaruvi (web UI)
- [ ] Review qoldirish UI
- [ ] Avatar yuklash UI
- [ ] To'lov tizimi integratsiyasi (Payme / Click)

### Bosqich 3 — Uzoq muddatli

- [ ] iOS ilova
- [ ] Telefon OTP orqali autentifikatsiya
- [ ] Push-bildirishnomalar
- [ ] Buyurtma tracking (geolokatsiya)
- [ ] Admin panel
- [ ] Erlar uchun kiyim kategoriyalari
- [ ] Ko'p tillilik (o'zbek, rus, ingliz)

---

## 11. Metrikalar va KPI

| Metrika | Maqsad |
|---|---|
| Ustalar soni | 100+ (6 oy) |
| Mijozlar soni | 1000+ (6 oy) |
| O'rtacha kunlik buyurtmalar | 10+ (6 oy) |
| Chat response vaqti | < 30 daqiqa |
| App crash rate | < 0.1% |
| Sahifa yuklanish tezligi | < 3 soniya (LCP) |

---

## 12. Xulosa

Tikuvchi platformasi O'zbekistonda buyurtma asosida tikuvchilik xizmatini raqamlashtirish, ustalar va mijozlar o'rtasidagi aloqani soddalashtirish, buyurtma jarayonini shaffof va kuzatiladigan qilish maqsadida yaratilmoqda. MVP bosqichi yakunlangan bo'lib, keyingi bosqichlarda to'lov integratsiyasi, usta onboarding va review tizimi to'liq ishga tushiriladi.

---

*Hujjat versiyasi: 1.0 | Oxirgi yangilanish: 24 Iyul 2026*

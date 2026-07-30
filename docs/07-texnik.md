# 07 — Texnik natija: sxema, delta, yo'l xaritasi

> Tikuvchi hujjatlari · 8-bosqich natijasi
> Oxirgi yangilanish: 2026-07-30

01–06 hujjatlardagi qarorlarning bazaga, kodga va yo'l xaritasiga aylangan
ko'rinishi.

---

## 0. Nomlash qoidasi

Baza identifikatorlari **inglizcha** qoladi (hozirgi kod shunday), o'zbekcha
matnlar `web/src/locales/uz.json` da:

```
status = 'queued'  →  ekranda "Navbatda"
```

## 1. Mavjud jadvallarga o'zgarishlar

### `orders`

| Maydon | O'zgarish |
|---|---|
| `status` | Enum qayta yozildi: `new · offered · queued · in_progress · ready · completed · cancelled` |
| `total_price` | `not null default 0` → **nullable** (boshida narx yo'q) |
| `estimated_ready_at` | ❌ **O'chiriladi** — tugatish sanasi yo'q |
| `start_date` | ➕ Boshlash sanasi (usta belgilaydi) |
| `deadline` | ➕ Mijoz so'ragan sana (nullable = "shoshilinch emas") |
| `meets_deadline` | ➕ Ustaning "yetkazaman / yetkazolmayman" javobi |
| `cancel_reason` | ➕ Enum: `usta_declined · price_declined · client_cancelled · no_response · admin_cancelled` |
| `cancel_note` | ➕ Matn |
| `cancel_requested_by` | ➕ "Tikilmoqda" holatidagi bekor qilish so'rovi |
| `measurements_snapshot` | ➕ `jsonb` — o'lchamlar **nusxasi**, keyin o'zgarmaydi |
| `commission` | ➕ `numeric default 0` — v2 ga tayyorlik |
| `source` | ⚠️ Ishlatilmaydi (oqim bittaga birlashdi), ustun qoladi |

### `order_items`

| Maydon | O'zgarish |
|---|---|
| `reference_images` | ➕ `text[]` — namuna rasmlari (3 tagacha) |
| `material_source` | ➕ Enum: `client · usta · undecided` |
| `price` | ⚠️ Ishlatilmaydi — narx buyurtma darajasida |

### `usta_profiles`

| Maydon | O'zgarish |
|---|---|
| `district` (text) | ❌ O'chiriladi → `district_id` FK |
| `region_id`, `district_id` | ➕ FK, **majburiy** |
| `location_text` | ⚠️ Ma'nosi o'zgardi: endi **mo'ljal** (ixtiyoriy) |
| `next_free_date` | ➕ "Eng yaqin bo'sh sanam" |
| `approval_status` | ➕ `pending · approved · rejected` |
| `rejection_reason`, `approved_at` | ➕ |
| `avg_response_minutes` | ➕ Hisoblangan |
| `completed_orders` | ➕ Hisoblangan |
| `on_time_rate` | ➕ Hisoblangan (ichki) |
| `trusted` | ➕ "Ishonchli usta" — avtomatik |
| `rating_avg` | ⚠️ Trigger **og'irlashtirilgan formulaga** o'tadi |

### `profiles`

| Maydon | O'zgarish |
|---|---|
| `region_id`, `district_id` | ➕ Mijozning tanlagan hududi |
| `is_blocked` | ⚠️ Bitta bayroq yetmaydi → `restriction_level` (`none · warned · restricted · blocked`) + sabab |

### `reviews`

| Maydon | O'zgarish |
|---|---|
| `order_id` | ➕ **Majburiy** + `unique` — soxta sharhni to'sadi |
| `image_url` | ➕ Natija rasmi |
| `updated_at` | ➕ 7 kunlik tahrirlash oynasi uchun |

### `messages`

| Maydon | O'zgarish |
|---|---|
| `price_offer_amount / _duration_days / _note / _status` | ❌ **Hammasi o'chiriladi** → `order_offers` ga ko'chadi |
| `message_type` | `text · image · system` (`price_offer` olib tashlanadi) |
| `order_id` | ➕ Tizim kartasi qaysi buyurtmaga tegishli |

### `order_events` — 🐛 tuzatish kerak

Mavjud INSERT policy (`0006_usta_interface.sql`) **faqat ustaga** ruxsat
beradi:

```sql
CREATE POLICY "Ustas can insert order events" ... AND orders.usta_id = auth.uid()
```

Lekin mijoz ham holat o'zgartiradi (narxni qabul qilish, topshirildi). Policy
`client_id OR usta_id` ga kengaytirilishi shart, aks holda mijozning har bir
harakati tarixga yozilmaydi.

## 2. Yangi jadvallar

| Jadval | Nima uchun | Manba hujjat |
|---|---|---|
| `regions` (14) | Viloyatlar | 01 |
| `districts` (~210) | Tuman/shaharlar + markaz koordinatasi | 01 |
| `order_offers` | Narx takliflari tarixi: narx, boshlash sanasi, `meets_deadline`, izoh, holat (`active · superseded · accepted · declined`) | 04 |
| `order_payments` | Har bir to'lov alohida yozuv: miqdor, sana, turi, kim yozdi | 05 |
| `complaints` | Shikoyatlar: sabab, izoh, rasmlar, holat, natija | 04 |
| `admin_chat_access_log` | Admin chatni o'qiganida jurnal | 04 |
| `client_flags` | Ustaning mijoz haqidagi ichki belgisi | 06 |
| `user_restrictions` | Ogohlantirish → cheklov → blok tarixi | 06 |
| `conversation_reads` | `(conversation_id, user_id, last_read_at)` — localStorage o'rniga | 04 |
| `device_tokens` | Push uchun FCM tokenlar | 04 |
| `notifications` | Ilova ichidagi bildirishnomalar ro'yxati | 04 |

### Ishlatilmaydigan jadvallar

`service_categories` va `usta_services` — 1-versiyada kategoriya va xizmat
narxlari yo'q. Jadvallar o'chirilmaydi (kelajakda kerak bo'ladi), faqat
UI'dan olib tashlanadi.

## 3. Avtomatik jarayonlar

[04-buyurtma-oqimi.md](04-buyurtma-oqimi.md) dagi 11 ta timerni **Supabase
`pg_cron` + Edge Function** bajaradi:

```
Har 15 daqiqada:  javobsiz so'rovlarni tekshirish (4s / 24s / 72s)
                  narx taklifi javobsizligini tekshirish (48s / 7 kun)
                  "tayyor" 7 kunlik avtomatik yakunlash

Har kuni 09:00:   boshlash sanasi kelgan buyurtmalar → ustaga eslatma
                  3 kun kechikkan navbatlar → ikkalasiga eslatma
                  7 kun javob bermagan ustani "qabul qilmayapman" ga o'tkazish

Voqea asosida:    reyting qayta hisoblash (trigger)
                  "ishonchli usta" belgisi (trigger)
                  javob tezligi (narx taklifi yozilganda)
```

## 4. Platformalar bo'linishi

**Qaror: hamma yangilik WEB'da birinchi qilinadi, Android keyin
tenglashtiriladi.**

Sabablari:

1. Bu reja katta o'zgarish talab qiladi (statuslar, offers, geo, onboarding,
   konsol). Ikki platformada parallel qilish — ikki barobar xato va vaqt
2. Web'da iteratsiya tez, deploy bir daqiqa, do'kon tekshiruvi yo'q
3. Usta web'da ham ishlashi kerak: profil to'ldirish va portfolio yuklash
   katta ekranda ancha qulay
4. Birinchi 20 ustaning profilini qo'lda to'ldirish — web'da bo'ladi

**Narxi:** R0–R4 davomida Android eskirgan holatda qoladi (yangi statuslarni
tanimaydi). **Android hali Play Store'da yo'q** — shuning uchun bu muammo
emas.

| Funksiya | Web | Android | Admin |
|---|---|---|---|
| Mijoz oqimi | ✅ 1-navbat | R5 | — |
| Usta konsoli | ✅ 1-navbat | R5 | — |
| Onboarding | ✅ 1-navbat | R5 | — |
| Moderatsiya, shikoyat | — | — | ✅ |
| iOS | — | — | — |

## 5. Hozirgi kodga nisbatan delta

| Fayl / soha | Nima bo'ladi |
|---|---|
| `web/src/components/OrderWizard.tsx` | 🔄 **Qayta yozish** — narx olib tashlanadi, rasm / mato / muddat qadamlari qo'shiladi |
| `web/src/components/ChatWindow.tsx` | 🔄 `price_offer` mantiqi olib tashlanadi, tizim kartalari qo'shiladi |
| `web/src/app/orders/page.tsx`, `orders/[id]` | 🔄 Yangi 7 holat, "Navbatda", to'lov ro'yxati |
| `web/src/components/OrderActions.tsx`, `OrderCardActions.tsx` | 🔄 Narx berish, boshlash sanasi, bekor qilish so'rovi |
| `web/src/lib/payments.ts` | 🔄 30% mantiqi o'chiriladi |
| `web/src/proxy.ts` | 🔄 Katalog va usta profili mehmonga ochiladi |
| `web/src/app/page.tsx`, `search/page.tsx` | 🔄 Kategoriya olib tashlanadi, geo-saralash qo'shiladi |
| `web/src/hooks/useUnreadChat.ts` | 🔄 localStorage → `conversation_reads` |
| `refresh_usta_rating()` (migratsiya) | 🔄 Og'irlashtirilgan formula |
| **Usta konsoli (web)** | 🆕 Butunlay yangi |
| **Usta onboarding** | 🆕 Butunlay yangi |
| **Geo (regions/districts + GPS)** | 🆕 Butunlay yangi |
| **Sharh yozish UI** | 🆕 Butunlay yangi |
| **Portfolio / avatar yuklash** | 🆕 Butunlay yangi |
| **Shikoyat va moderatsiya** | 🆕 Butunlay yangi |
| **Push (FCM)** | 🆕 Butunlay yangi |
| **Timerlar (cron)** | 🆕 Butunlay yangi |

## 6. Reliz yo'l xaritasi

| Reliz | Mazmuni | Natija |
|---|---|---|
| **R0 · Poydevor** | Migratsiyalar: geo jadvallar, yangi statuslar, `order_offers`, `order_payments`, `conversation_reads`, RLS tuzatishlari | Baza yangi modelga tayyor. Ekranlar hali eski |
| **R1 · Mijoz oqimi** | So'rov formasi (rasm/mato/o'lcham/muddat), narx kutish, qabul/rad, yangi holatlar, geo-saralash, mehmonga ochiq katalog | Mijoz uchun to'liq yangi oqim |
| **R2 · Usta konsoli** | Onboarding → admin tasdiq → konsol → narx berish → holat yuritish → pul hisobi → portfolio yuklash | **Platforma "tirik" bo'ladi** |
| **R3 · Ishonch** | Reyting formulasi, sharh yozish, ko'rsatkichlar, "ishonchli usta", moderatsiya lentasi | Katalog sifatli ko'rina boshlaydi |
| **R4 · Operatsion** | Shikoyat, cheklov pog'onalari, timerlar, push, admin vositalari | Pilotga tayyor |
| **R5 · Android** | Android'ni yangi modelga keltirish | Ikki platforma teng |

**R2 tugagach pilot boshlanadi** — 20 usta qo'lda jalb qilinadi. R3 va R4
pilot davomida qilinadi.

## 7. Ochiq texnik savollar

Kod boshlanishidan oldin aniqlanishi kerak:

| # | Savol |
|---|---|
| 1 | `districts` ma'lumotini (nom + markaz koordinatasi) qayerdan olamiz — qo'lda kiritamizmi yoki ochiq manbadan? |
| 2 | Push uchun FCM: web (VAPID) va Android bitta loyihada bo'ladimi? |
| 3 | Rasm yuklash: hozirgi `portfolio` / `chat-images` bucketlariga qo'shimcha `order-references` bucket kerakmi? |
| 4 | Statuslar enumini o'zgartirish — mavjud ma'lumotni migratsiya qilamizmi yoki test ma'lumot tozalanadimi? |

---

**Birinchi hujjat:** [01-mahsulot.md](01-mahsulot.md)

# 04 — Buyurtma oqimi, chat va nizolar

> Tikuvchi hujjatlari · 5-bosqich natijasi
> Oxirgi yangilanish: 2026-07-30

Bu hujjat mijoz va usta yo'llarining **kesishgan joyi** va kod uchun
**yagona haqiqat manbai**.

---

## 1. Holat mashinasi

```
                    ┌─────────────┐
   mijoz so'rov ───▶│  1. YANGI   │  narx yo'q
                    └──────┬──────┘
                           │ usta narx + boshlash sanasi beradi
                    ┌──────▼──────────┐
                    │ 2. NARX TAKLIFI │◀──┐ usta yangi narx yuborishi mumkin
                    └──────┬──────────┘   │ (muzokaradan keyin)
                           │ mijoz qabul  │
                           │ qiladi       └── eski taklif bekor bo'ladi
                    ┌──────▼──────┐
                    │ 3. NAVBATDA │  "5-avgustda boshlanadi"
                    └──────┬──────┘
                           │ usta boshladi
                    ┌──────▼──────────┐
                    │ 4. TIKILMOQDA   │
                    └──────┬──────────┘
                           │ usta tugatdi
                    ┌──────▼──────┐
                    │  5. TAYYOR  │
                    └──────┬──────┘
                           │ mijoz oldi va tasdiqladi
                    ┌──────▼──────────┐
                    │ 6. TOPSHIRILDI  │  ← sharh yozish ochiladi
                    └─────────────────┘

   1–4 holatlarning har biridan  ──▶  ┌──────────────────┐
                                      │ 7. BEKOR QILINDI │
                                      └──────────────────┘
```

**Jami 7 holat.**

| # | Baza qiymati | Ekranda |
|---|---|---|
| 1 | `new` | Yangi so'rov · *"Narx kutilmoqda"* |
| 2 | `offered` | Narx taklifi |
| 3 | `queued` | Navbatda |
| 4 | `in_progress` | Tikilmoqda |
| 5 | `ready` | Tayyor |
| 6 | `completed` | Topshirildi |
| 7 | `cancelled` | Bekor qilindi |

## 2. Bekor qilish sabablari

Bekor qilishning 5 turi bor. Alohida holat qilinmaydi — bitta holat +
**sabab** maydoni:

| Sabab | Kim | Mijozga qanday ko'rinadi |
|---|---|---|
| `usta_declined` | Usta | *"Usta so'rovni qabul qilmadi: band"* |
| `price_declined` | Mijoz | *"Siz narxni rad etdingiz"* |
| `client_cancelled` | Mijoz | *"Siz bekor qildingiz"* |
| `no_response` | Tizim | *"Usta javob bermadi, so'rov yopildi"* |
| `admin_cancelled` | Admin | *"Nizo bo'yicha bekor qilindi"* |

`no_response` ulushi — platformaning eng og'riqli metrikasi.

## 3. Har bir o'tishning egasi

| O'tish | Kim bajaradi | Shart |
|---|---|---|
| — → Yangi | **Mijoz** | So'rov formasi to'ldirilgan |
| Yangi → Narx taklifi | **Usta** | Narx + boshlash sanasi majburiy |
| Narx taklifi → Narx taklifi | **Usta** | Yangi taklif, eskisi bekor bo'ladi |
| Narx taklifi → Navbatda | **Mijoz** | Narxni qabul qiladi |
| Navbatda → Tikilmoqda | **Usta** | — |
| Tikilmoqda → Tayyor | **Usta** | — |
| Tayyor → Topshirildi | **Mijoz** | Yoki tizim, 7 kundan keyin |
| Yangi / Narx taklifi / Navbatda → Bekor | Mijoz yoki Usta | Sabab majburiy |
| **Tikilmoqda → Bekor** | ⚠️ **Faqat admin** | Mato kesilgan, pul masalasi bor |
| Har qanday holat → Bekor | Admin | Nizo bo'yicha |

### Qat'iy qoidalar (kodda o'zgarmas)

1. Usta hech qachon **Topshirildi** qila olmaydi
2. Mijoz hech qachon **Tikilmoqda** yoki **Tayyor** qila olmaydi
3. **Tikilmoqda** holatidan orqaga qaytish yo'q (faqat admin)
4. **Topshirildi** — terminal holat, o'zgarmaydi
5. Narx **Navbatda** holatiga o'tgach qotib qoladi

## 4. Buyurtma tarixi

Har bir o'tish yozib boriladi — bittasi ham tashlab ketilmaydi:

```
Buyurtma #A3F8C2 tarixi
─────────────────────────────────────────
01-avg 14:20  Mijoz    so'rov yubordi
01-avg 16:45  Usta     narx: 250 000 · boshlash: 5-avg
01-avg 17:02  Mijoz    narxni qabul qildi        → Navbatda
04-avg 09:10  Usta     boshlash sanasi 7-avgustga surildi
                       sabab: "mato kechikdi"
07-avg 10:00  Usta     ishni boshladi            → Tikilmoqda
18-avg 15:30  Usta     tayyor deb belgiladi      → Tayyor
19-avg 11:00  Mijoz    qabul qildi               → Topshirildi
```

Ro'yxat **ikkala tomonga ham ko'rinadi**. Nizo chiqsa — admin shu yerga
qaraydi, boshqa hech qayerga emas.

## 5. Chat va buyurtma qanday bog'lanadi

> **Buyurtma — yozuv. Chat — suhbat.** Kelishuv chatda gaplashiladi, lekin
> **qaror har doim buyurtmada qat'iylashadi.**

Usta chatda *"250 minga qilaman"* deb yozsa — bu hech narsani o'zgartirmaydi.
Narx faqat **narx taklifi** yuborilganda kuchga kiradi.

### Bitta suhbat, bitta lenta

Har bir mijoz–usta juftligi uchun **bitta** suhbat (bir necha buyurtma bo'lsa
ham). Buyurtma voqealari o'sha lentada **tizim kartasi** sifatida ko'rinadi:

```
┌──────────────────────────────────────┐
│ Nilufar Karimova                     │
├──────────────────────────────────────┤
│  ┌────────────────────────────────┐  │
│  │ 📋 Yangi so'rov #A3F8C2        │  │  ← tizim kartasi
│  │ 15-avgustga                    │  │
│  └────────────────────────────────┘  │
│                                      │
│ Nilufar: Assalomu alaykum, matoni   │
│ o'zingiz olib kelasizmi?             │
│                       Siz: Ha ▸      │
│                                      │
│  ┌────────────────────────────────┐  │
│  │ 💰 Narx taklifi   #A3F8C2      │  │  ← tizim kartasi
│  │ 250 000 so'm                   │  │
│  │ 5-avgustda boshlanadi          │  │
│  │ ✅ 15-avgustga yetkazaman      │  │
│  │ [ Qabul qilaman ] [ Rad etaman]│  │
│  └────────────────────────────────┘  │
│                                      │
│  ┌────────────────────────────────┐  │
│  │ ✂️ Ish boshlandi — #A3F8C2     │  │
│  └────────────────────────────────┘  │
└──────────────────────────────────────┘
```

Kartani bosganda buyurtma sahifasi ochiladi. Aksincha ham — buyurtma
sahifasidagi "💬 Yozish" tugmasi shu suhbatga olib boradi.

Foydasi: ikkala tomon ham hech qayerga o'tmasdan hamma narsani bitta joyda
ko'radi. Buyurtma sahifasi esa "rasmiy hujjat" sifatida qoladi.

## 6. Muzokara: takroriy narx taklifi

```
Usta: 250 000 taklif qildi
   ↓
Mijoz: [ 💬 Savolim bor ] → chat
   ↓
Chatda gaplashadilar: "matoni o'zim olsam arzonroq bo'ladimi?"
   ↓
Usta yangi taklif yuboradi: 200 000
   ↓
Eski taklif avtomatik BEKOR bo'ladi
(lentada "eskirgan" deb ko'rinadi, o'chirilmaydi — tarix saqlanadi)
   ↓
Mijoz yangi taklifni qabul qiladi → Navbatda
```

**Qoidalar:**

- Bir vaqtda **faqat bitta faol taklif**
- Barcha takliflar tarixda saqlanadi
- Taklif soni cheklanmaydi
- Usta faqat **Yangi** va **Narx taklifi** holatlarida taklif yubora oladi

> **Narx qabul qilingandan keyin o'zgartirish — 1-versiyada YO'Q.** Usta
> chatda gaplashadi, kelishilmasa buyurtma bekor qilinadi. Narxni
> o'zgartirish imkoni ochiq qolsa — bu suiiste'molning eng katta teshigi
> bo'ladi.

## 7. Bekor qilish — uchta holat

### A. Ish boshlanmagan (Yangi / Narx taklifi / Navbatda)

Mijoz **erkin bekor qiladi**. Sabab so'raladi (ixtiyoriy), ustaga xabar
ketadi. Jarima yo'q.

### B. Ish boshlangan (Tikilmoqda)

Mijoz o'zi bekor qila **olmaydi**. U **bekor qilish so'rovi** yuboradi:

```
Mijoz: [ Bekor qilishni so'rash ]  → sabab yozadi
   ↓
Ustaga xabar: "Aziza buyurtmani bekor qilishni so'rayapti: ..."
   ↓
   ┌── Usta rozi     → bekor qilinadi
   └── Usta rad etadi → mijoz shikoyat ochishi mumkin → admin hal qiladi
```

Sababi: mato kesilgan bo'lishi mumkin, ustaning mehnati va xarajati ketgan.

### C. Usta bekor qilmoqchi

| Holat | Ruxsat |
|---|---|
| Yangi / Narx taklifi | ✅ Rad etadi, sabab bilan |
| Navbatda | ✅ Bekor qiladi, sabab bilan — lekin ko'rsatkichiga tushadi |
| Tikilmoqda | ❌ Yo'q. Faqat mijoz bilan kelishib yoki admin orqali |

## 8. Shikoyat va nizolar

**Kim va qachon:** ikkala tomon ham, istalgan holatda — hatto
**Topshirildi** dan keyin ham **14 kun** ichida.

```
[ ⚠️ Shikoyat qilish ]
   ↓
Sabab tanlanadi:
   ○ Usta ishni bajarmadi / g'oyib bo'ldi
   ○ Sifat kelishilgandek emas
   ○ Muddat juda kechikdi
   ○ Bekor qilish bo'yicha kelisha olmadik
   ○ Boshqa: ...
   ↓
Izoh + rasm (ixtiyoriy)
   ↓
Shikoyat: OCHIQ
```

Holatlar: `ochiq → ko'rilmoqda → yopildi` (natija va izoh bilan). Ikkala
tomon natijani ko'radi.

### Admin vositalari

| Vosita | Izoh |
|---|---|
| Buyurtma tarixini ko'rish | Har doim ochiq |
| **Chatni o'qish** | ⚠️ Faqat shikoyat ochiq bo'lganda + **jurnalga yoziladi** (kim, qachon, qaysi shikoyat bo'yicha). Taraflarga xabar berilmaydi |
| Buyurtmani majburan bekor qilish | Sabab: `admin_cancelled` |
| Holatni majburan o'zgartirish | Tarixda "admin tomonidan" deb ko'rinadi |
| Sharhni o'chirish | Haqoratli / soxta bo'lsa |
| Cheklov qo'yish | Takroriy buzilishda |

## 9. Barcha avtomatik harakatlar

Tizim (odam emas) bajaradigan **hamma** narsa shu yerda. Boshqa avtomatik
harakat yo'q:

| Vaqt | Shart | Harakat |
|---|---|---|
| 4 soat | Yangi, javobsiz | Ustaga eslatma |
| 24 soat | Yangi, javobsiz | Mijozga xabar + o'xshash ustalar |
| **72 soat** | Yangi, javobsiz | Yopiladi (`no_response`) + ustaning javob tezligi pasayadi |
| 48 soat | Narx taklifi, mijoz javob bermayapti | Mijozga eslatma |
| **7 kun** | Narx taklifi, mijoz javob bermayapti | Yopiladi (`client_cancelled`) |
| Boshlash sanasi kelganda | Navbatda | Ustaga: *"boshlash vaqti keldi"* |
| 3 kun kechikdi | Navbatda, sana o'tdi | Ikkalasiga eslatma |
| **7 kun** | Tayyor, mijoz javob bermayapti | Avtomatik **Topshirildi** |
| Topshirildi bo'lganda | — | Sharh so'rovi |
| 7 kun javobsiz | Usta hech bir so'rovga javob bermadi | Avtomatik "qabul qilmayapman" |
| Sharh yozilganda | — | Reyting qayta hisoblanadi |

## 10. Bildirishnomalar xaritasi

| Voqea | Mijoz | Usta |
|---|---|---|
| Yangi so'rov | — | 🔴 |
| Narx taklifi keldi | 🔴 | — |
| Narx qabul qilindi | — | 🔴 |
| Narx rad etildi | — | 🟡 |
| Ish boshlandi | 🟡 | — |
| Boshlash sanasi surildi | 🔴 | — |
| Tayyor | 🔴 | — |
| Topshirildi | — | 🟡 |
| Yangi chat xabari | 🟡 | 🟡 |
| Yangi sharh | — | 🟢 |
| Shikoyat ochildi | 🔴 | 🔴 |
| Shikoyat yopildi | 🔴 | 🔴 |

🔴 majburiy push · 🟡 push + ilova ichida · 🟢 faqat ilova ichida

---

**Keyingi hujjat:** [05-pul.md](05-pul.md)

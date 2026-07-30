# 02 — Mijoz yo'li

> Tikuvchi hujjatlari · 3-bosqich natijasi
> Oxirgi yangilanish: 2026-07-30

Ilovani birinchi ochishdan buyurtmani qo'liga olgunga qadar har bir qadam.

---

## 0. Umumiy ko'rinish

```
   MEHMON                    RO'YXAT             SO'ROV              KELISHUV
┌───────────┐            ┌───────────┐      ┌───────────┐       ┌───────────┐
│ 1 Ochilish│            │ 4 Ro'yxat │      │ 5 So'rov  │       │ 6 Narxni  │
│ 2 Katalog │───────────▶│   dan     │─────▶│   yuborish│──────▶│   kutish  │
│ 3 Profil  │            │   o'tish  │      │           │       │ 7 Kelishuv│
└───────────┘            └───────────┘      └───────────┘       └─────┬─────┘
                                                                      │
   QAYTISH                  BAHOLASH            YAKUN            ▼ ISH
┌───────────┐            ┌───────────┐      ┌───────────┐       ┌───────────┐
│11 Ikkinchi│◀───────────│10 Sharh   │◀─────│ 9 Qabul   │◀──────│ 8 Kuzatuv │
│   buyurtma│            │   yozish  │      │   qilish  │       │           │
└───────────┘            └───────────┘      └───────────┘       └───────────┘
```

---

## 1. Birinchi ochilish

```
Ilova ochiladi
   ↓
📍 "Tikuvchi joylashuvingizni bilishga ruxsat bering —
    yaqin atrofdagi ustalarni ko'rsatamiz"   [Ruxsat berish] [Keyinroq]
   ↓
Ruxsat berildi → koordinata → eng yaqin tuman aniqlanadi
Rad etildi     → hudud tanlash oynasi: viloyat → tuman
   ↓
BOSH SAHIFA
```

Ruxsat **faqat bir marta** so'raladi. Keyingi kirishlarda joylashuv avtomatik
olinadi, ilova yopilganda kuzatuv to'xtaydi.

### Bosh sahifa

```
┌────────────────────────────────────────┐
│ 📍 Chilonzor tumani ▾        🔍 Qidiruv│
├────────────────────────────────────────┤
│  Sizga yaqin ustalar                   │
│  ┌──────┐ ┌──────┐ ┌──────┐            │
│  │ usta │ │ usta │ │ usta │            │
│  └──────┘ └──────┘ └──────┘            │
├────────────────────────────────────────┤
│  Yuqori reytingli ustalar              │
│  ...                                   │
└────────────────────────────────────────┘
```

**Bo'sh holat:** mijozning tumanida usta bo'lmasa, bo'sh ekran o'rniga doira
kengayadi va sababi yoziladi:

> *"Chilonzor tumanida hozircha usta yo'q — Toshkent shahri bo'ylab
> ko'rsatyapmiz"*

## 2. Qidiruv va filtr

| Filtr | Qiymatlar |
|---|---|
| Hudud | Viloyat → tuman (ikki bosqichli tanlov) |
| Reyting | 4+ , 4.5+ |
| Holat | "Hozir buyurtma qabul qilayotganlar" |
| Matn | Ism, teg, bio bo'yicha |

**Kategoriya filtri yo'q** (1-versiyada). Ustani topish **teglar va matn
qidiruvi** orqali: *"to'y libosi"*, *"milliy kiyim"*, *"ko'ylak"*.

**Narx filtri ham yo'q** — narx faqat so'rovga javoban paydo bo'ladi.

Saralash: yaqinlik → reyting → yakunlangan buyurtmalar soni.

## 3. Usta profili

```
┌────────────────────────────────────────┐
│         [muqova rasmi]                 │
│  🖼  Nilufar Karimova        ⭐ 4.8 (23)│
│  📍 Toshkent, Chilonzor tumani         │
│     Bunyodkor metrosi yaqinida         │
│  🕘 09:00 – 18:00                      │
│  ✅ Buyurtma qabul qilmoqda            │
│  ⏳ 5-avgustdan bo'sh                  │
│  ⚡ O'rtacha 1 soat 20 daqiqada javob  │
│  ✅ 6 buyurtma yakunlagan              │
│  📅 Iyun 2026 dan beri                 │
├────────────────────────────────────────┤
│  Men haqimda: 12 yildan beri ayollar...│
├────────────────────────────────────────┤
│  Teglar: ayollar kiyimi · ko'ylak ·    │
│          to'y libosi                   │
├────────────────────────────────────────┤
│  Ishlarim (24)   [rasm][rasm][rasm]... │
├────────────────────────────────────────┤
│  Sharhlar (23)                         │
│  ⭐⭐⭐⭐⭐ Aziza — "Juda chiroyli tikdi"│
├────────────────────────────────────────┤
│   [ 💬 Yozish ]   [ 📋 So'rov yuborish ]│
└────────────────────────────────────────┘
```

**Xizmatlar va narxlar bo'limi yo'q** (1-versiyada). Mehmon bu sahifani
to'liq ko'radi.

## 4. Ro'yxatdan o'tish

**Printsip:** ro'yxatdan o'tish faqat harakat qilmoqchi bo'lganda so'raladi,
ilovani ochishda emas.

```
[So'rov yuborish] bosildi (mehmon)
   ↓
"Davom etish uchun ro'yxatdan o'ting"
   ism · telefon · parol
   ↓
Ro'yxatdan o'tgach — BOSILGAN JOYGA QAYTARILADI
(kiritilgan ma'lumotlar yo'qolmaydi)
```

Oxirgi qator muhim — ko'p ilova shu yerda foydalanuvchini yo'qotadi.

## 5. So'rov yuborish

Narx yo'q, shuning uchun forma **"men nima xohlayman"** ga aylanadi:

```
─── 1/4  NIMA TIKILSIN? ──────────────────
  Tavsif:      ┌──────────────────────┐
               │ Uzun yengli, yozgi,  │
               │ bel qismi tor...     │
               └──────────────────────┘
  Namuna rasmi: [+ rasm qo'shish]   (3 tagacha)
                💡 Instagram yoki Pinterest'dan
                   yoqqan modelni yuklang

─── 2/4  MATO ───────────────────────────
  ○ Matom bor, o'zim beraman
  ○ Usta topib bersin
  ○ Hali hal qilmadim — usta bilan maslahatlashaman

─── 3/4  O'LCHAM ────────────────────────
  ○ Mening o'lchamlarim:  [ Yozgi ko'ylak ▾ ]
  ○ Yangi o'lcham kiritaman
  ○ O'lchamimni bilmayman — ustaga borib o'lchataman
       💡 Bu variantda usta siz bilan uchrashuv
          vaqtini kelishadi

─── 4/4  MUDDAT ─────────────────────────
  Qachonga kerak?  [ 📅 sana ]  yoki  ○ Shoshilinch emas

              [ So'rovni yuborish ]
```

**Uchta muhim jihat:**

1. **Namuna rasmi** — O'zbekistondagi eng real oqim. Mijoz Instagramda ko'rgan
   modelni ko'rsatadi. Bu asosiy element, ixtiyoriy qo'shimcha emas.
2. **Mato savoli** — narxni ikki barobar o'zgartiradi. Usta narx taklif
   qilishdan oldin buni bilishi shart.
3. **"O'lchamimni bilmayman"** — mijozlarning katta qismi shunday. Bu variant
   bo'lmasa, ular formada tiqilib qoladi.

Yuborilgach:

```
✅ So'rovingiz Nilufar Karimovaga yuborildi
   Usta odatda 2 soat ichida javob beradi
   [ Buyurtmani ko'rish ]
```

## 6. Narxni kutish

```
Buyurtma  #A3F8C2
🕐 Narx kutilmoqda
────────────────────────
Nilufar Karimova
Ko'ylak · Matom bor · Yozgi ko'ylak o'lchami
Muddat: 15-avgust
[namuna rasmlari]
```

**Usta javob bermasa:**

| Vaqt | Nima bo'ladi |
|---|---|
| 4 soat | Ustaga eslatma |
| 24 soat | Mijozga xabar + o'xshash ustalar ro'yxati. **So'rov ochiq qoladi** |
| 72 soat | So'rov avtomatik yopiladi. Ustaning javob tezligi pasayadi |

## 7. Narx keldi — kelishuv

```
💰 Narx taklifi
   250 000 so'm
   📅 5-avgustda boshlanadi
   ✅ 15-avgustga yetkazaman
   Izoh: "Hozir 3 ta ishim bor, 5-avgustda bo'shayman"

   [ Qabul qilaman ]   [ Rad etaman ]   [ 💬 Savolim bor ]
```

| Tanlov | Natija |
|---|---|
| **Qabul qilaman** | Buyurtma tasdiqlanadi → **Navbatda** holati |
| **Savolim bor** | Chatga o'tadi. Muzokaradan keyin usta yangi narx yuborishi mumkin (eskisi bekor bo'ladi) |
| **Rad etaman** | Buyurtma yopiladi, boshqa ustalar taklif qilinadi |

**Tugatish sanasi ko'rsatilmaydi** — usta faqat boshlash sanasini belgilaydi.
Mijozning muddati bo'lsa, usta unga *"yetkazaman / yetkazolmayman"* deb javob
beradi.

Har bir narx taklifi buyurtma tarixida saqlanadi.

**Mijoz javob bermasa:** 48 soatda eslatma, 7 kunda taklif yopiladi.

## 8. Ish jarayonini kuzatish

```
✅ Qabul qilindi ──▶ ⏳ Navbatda ──▶ ✂️ Tikilmoqda ──▶ 📦 Tayyor ──▶ ✔️ Topshirildi
```

| Holat | Mijoz nima ko'radi |
|---|---|
| ⏳ Navbatda | *"5-avgustda boshlanadi"* |
| ✂️ Tikilmoqda | *"5-avgustda boshlandi"* + ustaning ixtiyoriy izohi |
| 📦 Tayyor | Xabar: *"Buyurtmangiz tayyor"* |

**Boshlash sanasi surilsa** — mijozga xabar ketadi, sabab ko'rsatiladi va
buyurtma tarixiga yoziladi.

Buyurtma sahifasida doim: qaysi bosqichda, qachon o'zgargan, to'lov holati
va chatga o'tish tugmasi.

## 9. Topshirish va yakunlash

Usta "Tayyor" deb belgilaydi → mijoz boradi, oladi, tekshiradi → **mijoz
"Topshirildi" tugmasini bosadi**.

> Mijoz unutib qo'ysa: "Tayyor" holatidan **7 kun** o'tgach buyurtma
> avtomatik yakunlanadi.

## 10. Sharh

Yakunlanishidan **darhol keyin** so'raladi:

```
Buyurtmangiz yakunlandi 🎉
Nilufar Karimovani baholang:
   ⭐ ⭐ ⭐ ⭐ ⭐
   ┌──────────────────────────┐
   │ Fikringiz (ixtiyoriy)    │
   └──────────────────────────┘
   [+ Natija rasmini qo'shish]     (ixtiyoriy)

   [ Yuborish ]        [ Keyinroq ]
```

| Qoida | Qiymat |
|---|---|
| Oyna | Topshirilgandan keyin 30 kun |
| Tahrirlash | 7 kun ichida |
| ⭐ | Majburiy |
| Matn va rasm | Ixtiyoriy |

Natija rasmi ustaning portfeliga *"✅ Mijoz yuborgan rasm"* belgisi bilan
tushadi.

## 11. Ikkinchi buyurtma

Platformaning haqiqiy qiymati shu yerda ochiladi — qaytgan mijoz uchun yo'l
qisqaradi:

- O'lchamlari allaqachon saqlangan (usta o'lchov olgan bo'lsa ham)
- Usta tanish → chatdan yoki "yana buyurtma berish" tugmasidan
- Oldingi buyurtmani nusxalash: *"O'sha ko'ylakdan yana, boshqa matoda"*

## 12. Nosoz holatlar

| Holat | Yechim |
|---|---|
| Usta so'rovni rad etdi | Sabab ko'rsatiladi + o'xshash 3 ta usta taklif qilinadi |
| Usta javob bermadi | 72 soatdan keyin yopiladi, boshqa ustalar taklif qilinadi |
| Usta ishni yarim yo'lda tashladi | Mijoz shikoyat ochadi → admin aralashadi |
| Mijoz g'oyib bo'ldi (kiyimni olmayapti) | 7 kundan keyin avtomatik yakunlanadi |
| Natija yoqmadi | Chat orqali hal qilinadi → hal bo'lmasa shikoyat |
| Narx qabul qilingandan keyin bekor qilmoqchi | Usta boshlamagan bo'lsa — bekor qilinadi; boshlagan bo'lsa — bekor qilish so'rovi |

---

**Keyingi hujjat:** [03-usta-yoli.md](03-usta-yoli.md)

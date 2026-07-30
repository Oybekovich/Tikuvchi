# 03 — Usta yo'li va navbat modeli

> Tikuvchi hujjatlari · 4-bosqich natijasi
> Oxirgi yangilanish: 2026-07-30

---

## 0. Umumiy ko'rinish

```
┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐
│ 1 Keladi │──▶│ 2 Ro'yxat│──▶│ 3 Profil │──▶│ 4 Admin  │
│          │   │   dan    │   │   to'ldi-│   │   tasdiq-│
│          │   │   o'tadi │   │   radi   │   │   laydi  │
└──────────┘   └──────────┘   └──────────┘   └────┬─────┘
                                                  │
┌──────────┐   ┌──────────┐   ┌──────────┐   ┌────▼─────┐
│ 8 Pul va │◀──│ 7 Ishni  │◀──│ 6 Narx   │◀──│ 5 Konsol │
│  hisob   │   │  yuritadi│   │  beradi  │   │          │
└──────────┘   └──────────┘   └──────────┘   └──────────┘
```

---

## 1. Usta qanday keladi

1-versiyada usta o'z-o'zidan kelmaydi. Buni ochiq tan olamiz:

| Kanal | Izoh |
|---|---|
| **Qo'lda jalb qilish** | Birinchi 20–30 usta Instagram/Telegramdagi tikuvchilardan qo'lda topiladi. Hisob **oddiy ro'yxatdan o'tish sahifasi orqali** ochib beriladi va profil o'zimiz to'ldiriladi |
| Mijoz taklifi | *"Ustangiz Tikuvchida yo'qmi? Taklif qiling"* |
| Og'izdan-og'izga | Usta ustaga aytadi — eng kuchli, lekin keyinroq ishlaydi |

> Admin panelga "usta nomidan profil yaratish" funksiyasi **qo'shilmaydi** —
> oddiy ro'yxatdan o'tish yetadi.

## 2. Ro'yxatdan o'tish

```
[Ro'yxatdan o'tish] → "Kim sifatida?" → ● Ustaman
   ↓
   Ism · Telefon · Parol
   ↓
   Darhol → PROFIL TO'LDIRISH (onboarding)
```

## 3. Onboarding — eng kritik ekran

**Qoida: 10 daqiqadan oshmasin.** Uzun forma — ustani yo'qotishning eng tez
yo'li.

```
─── 1/4  QAYERDA ISHLAYSIZ? ──────────────
  Viloyat:  [ Toshkent shahri ▾ ]
  Tuman:    [ Chilonzor ▾ ]
  Mo'ljal:  ┌────────────────────────┐
            │ Bunyodkor metrosi ya...│  (ixtiyoriy)
            └────────────────────────┘

─── 2/4  ISHLARINGIZ ────────────────────
  Tikkan kiyimlaringiz rasmini yuklang
  [+][+][+]                    kamida 3 ta
  💡 Telefoningizdagi eski rasmlar ham bo'ladi

─── 3/4  NIMA TIKASIZ? ──────────────────
  [ayollar kiyimi] [ko'ylak] [to'y libosi]
  [milliy kiyim]   [+ qo'shish]
  ← teglar; mijozlar sizni shu so'zlar bo'yicha topadi

─── 4/4  O'ZINGIZ HAQINGIZDA ────────────
  Bio:       ┌────────────────────────┐  (ixtiyoriy)
             │ 12 yildan beri...      │
             └────────────────────────┘
  Ish vaqti: [09:00] – [18:00]          (ixtiyoriy)
  Rasm:      [+ profil rasmi]           (ixtiyoriy)

              [ Tekshiruvga yuborish ]
```

| Majburiy | Ixtiyoriy |
|---|---|
| Ism, telefon | Bio |
| Viloyat + tuman | Mo'ljal / manzil |
| **Kamida 3 ta portfolio rasmi** | Ish vaqti |
| Kamida 1 ta teg | Profil va muqova rasmi |

> **3 ta rasm nega majburiy:** rasmsiz profil katalogda hech qachon
> bosilmaydi. Ustani boshidanoq shunga majbur qilish — keyin "profilingizni
> to'ldiring" deb ta'qib qilishdan yaxshiroq.

## 4. Admin tasdig'ini kutish

```
┌────────────────────────────────────────┐
│  ⏳ Profilingiz tekshirilmoqda         │
│                                        │
│  Odatda 24 soat ichida tasdiqlanadi.   │
│  Tasdiqlangach, mijozlar sizni katalog-│
│  dan topa boshlaydi.                   │
│                                        │
│  [ Profilni tahrirlash ]               │
└────────────────────────────────────────┘
```

| Natija | Nima bo'ladi |
|---|---|
| ✅ Tasdiqlandi | Katalogga chiqadi → ustaga xabar |
| ❌ Rad etildi | **Sabab ko'rsatiladi** (*"rasmlar sifatsiz"*) → tuzatib qayta yuboradi |

Rad etish har doim **sabab bilan** bo'lishi shart — aks holda usta qaytmaydi.

## 5. Usta konsoli

```
┌────────────────────────────────────────┐
│  Assalomu alaykum, Nilufar             │
│  ✅ Buyurtma qabul qilyapman  [o'zgart]│
│  📅 Eng yaqin bo'sh sanam: 5-avgust    │
├────────────────────────────────────────┤
│  🔴 YANGI SO'ROVLAR (2)                │
│  ┌────────────────────────────────────┐│
│  │ Aziza R. · 2 soat oldin            ││
│  │ [rasm][rasm]  Mato: mijozniki      ││
│  │ Muddat: 15-avgust                  ││
│  │        [ Narx berish ]  [ Rad et ] ││
│  └────────────────────────────────────┘│
├────────────────────────────────────────┤
│  ⏳ NAVBATDA (3)                       │
│  Malika · 7-avgustda boshlanadi        │
├────────────────────────────────────────┤
│  ✂️ HOZIR ISHLAYAPMAN (2)              │
│  Dilnoza · 1-avgustda boshlangan       │
├────────────────────────────────────────┤
│  📦 TAYYOR (1) — topshirilishi kutiladi│
├────────────────────────────────────────┤
│  📊 SHU OY                             │
│  Yakunlangan: 6   Daromad: 1 450 000   │
│  ⚡ Javob tezligi: 1 soat 20 daqiqa    │
└────────────────────────────────────────┘
```

Tepada **harakat talab qiladigan narsa** turadi — usta ilovani ochganda
birinchi javobsiz so'rovlarni ko'radi.

## 6. Narx berish

```
So'rov: Aziza Rahimova
────────────────────────────────
"Uzun yengli, yozgi, bel qismi tor"
[namuna rasmlari — bosilsa kattalashadi]

Mato:     Mijozniki
O'lcham:  ko'krak 88 · bel 68 · son 94 ·
          bo'y 165 · yelka 38 · yeng 58
Muddat:   15-avgustgacha
   ℹ️ Sizning navbatingiz: 5-avgust — yetadi ✅
────────────────────────────────
  💰 Narx:        [ 250 000 ] so'm
  📅 Boshlayman:  [ 5-avgust ]

  Mijoz 15-avgustga so'ragan:
     ● Yetkazaman
     ○ Yetkazolmayman

  💬 Izoh: ┌──────────────────┐  (ixtiyoriy)
           │ Mato sifatiga... │
           └──────────────────┘

  [ Narxni yuborish ]    [ 💬 Avval savol berish ]
```

**Usta faqat boshlash sanasini belgilaydi.** Tugatish sanasi yo'q — u
buyurtma holati orqali ma'lum bo'ladi. Mijozning muddati bo'lsa, usta
unga bitta belgi bilan javob beradi. Mijoz "shoshilinch emas" deb
belgilagan bo'lsa, bu savol chiqmaydi.

### Uch xil holat

| Mijoz nima dedi | Usta nima ko'radi |
|---|---|
| O'lchamini kiritgan | Raqamlar tayyor → darhol narx beradi |
| *"O'lchamimni bilmayman"* | ⚠️ *"O'lcham yo'q — mijoz siz bilan uchrashmoqchi"* → chatda vaqt kelishadi, uchrashgach **o'lchamni o'zi kiritadi** |
| *"Mato hal qilinmagan"* | Chatda maslahatlashadi, keyin narx beradi |

### O'lchamni usta kiritsa

Usta o'lchov olib, raqamlarni kiritadi → **mijozning o'lcham kutubxonasiga
saqlanadi** → mijozga xabar ketadi. **Tasdiqlash majburiy emas** — mijoz
ko'radi, xato bo'lsa chatda aytadi.

Foydasi: mijozning ikkinchi buyurtmasi allaqachon o'lchamli bo'ladi. Bir
marta o'lchov olinadi, umrbod ishlatiladi.

### Rad etish

Sabab bilan: *band man / bu turdagi ishni qilmayman / muddat yetmaydi*.
Sabab mijozga ko'rinadi.

## 7. Ishni yuritish

```
Qabul qilindi ──▶ ⏳ Navbatda ──▶ ✂️ Tikilmoqda ──▶ 📦 Tayyor
   (mijoz          (usta hali        (usta            (usta
    narxni          boshlamagan)      bosadi)          bosadi)
    qabul qildi)
                                                        ↓
                                        ✔️ Topshirildi — MIJOZ bosadi
```

Usta har bosqichda ixtiyoriy izoh qoldira oladi (*"mato kelishini
kutyapman"*) — mijozga ko'rinadi va "qanday bo'lyapti?" qo'ng'iroqlarini
kamaytiradi.

Har bir o'zgarish **tarixga yoziladi**: kim, qachon, qaysi holatdan qaysiga.
Nizo chiqsa — bu yagona haqiqat manbai.

## 8. Pul hisobi

1-versiyada pul platformadan o'tmaydi ([05-pul.md](05-pul.md)). Konsolda:

```
📊 PUL HISOBI · Avgust
──────────────────────────────────────
Kelishilgan (faol buyurtmalar)  1 850 000
   ⏳ Navbatda (3)                750 000
   ✂️ Tikilmoqda (2)              600 000
   📦 Tayyor (1)                  500 000

Olingan avanslar                  400 000
──────────────────────────────────────
⚠️  QARZDORLIK                    350 000
   Yakunlangan, lekin to'liq
   to'lanmagan (2 buyurtma)
   • Malika R.  ·  200 000
   • Dilnoza S. ·  150 000
──────────────────────────────────────
Shu oy olingan (jami)           1 200 000
```

**Qarzdorlik ro'yxati** — ustaning eng katta og'rig'i va u bepul yechiladi.
Bu funksiya ustani ilovaga har kuni qaytaradi.

## 9. Do'kon boshqaruvi

| Bo'lim | Nima qiladi |
|---|---|
| Profil | Ism, rasm, muqova, bio, viloyat, tuman, mo'ljal, ish vaqti, teglar, **eng yaqin bo'sh sana** |
| Portfolio | Rasm qo'shish / o'chirish / tartiblash. Sharh bilan kelgan natija rasmlari ham shu yerda ko'rinadi |
| Holat | Buyurtma qabul qilish (yoq/o'chiq) · Katalogdan yashirish (ta'til rejimi) |

Ikkalasining farqi:

```
Qabul qilmayapman  → profil ko'rinadi, "hozir buyurtma olmayapti" yozuvi bilan
Katalogdan yashir  → profil umuman ko'rinmaydi, mavjud mijozlar bilan aloqa saqlanadi
```

## 10. Bildirishnomalar

| Voqea | Muhimligi |
|---|---|
| 🔴 Yangi so'rov keldi | Eng yuqori — daromad shu yerdan boshlanadi |
| 🔴 Mijoz narxni qabul qildi | Ish boshlash vaqti |
| 🔴 Boshlash sanasi keldi | *"Azizaning buyurtmasini boshlash vaqti"* |
| 🟡 Yangi chat xabari | |
| 🟡 So'rov 4 soatdan beri javobsiz | Eslatma |
| 🟢 Mijoz buyurtmani yakunladi | |
| 🟢 Yangi sharh | |

## 11. Nosoz holatlar

| Holat | Yechim |
|---|---|
| So'rovlarga javob bermayapti | Javob tezligi pasayadi → katalogda pastroq chiqadi → 7 kun javobsiz qolsa avtomatik "qabul qilmayapman" ga o'tadi |
| Ishni tashlab ketdi | Mijoz shikoyat ochadi → admin buyurtmani majburan bekor qiladi → takrorlansa cheklov |
| Sifatsiz ish / soxta portfolio | Sharhlar orqali ko'rinadi → admin portfolio rasmini o'chira oladi |
| Ta'tilga chiqdi | "Katalogdan yashirish" ni o'zi yoqadi, faol buyurtmalari saqlanadi |
| Akkauntni o'chirmoqchi | Faol buyurtmasi bo'lsa — ruxsat yo'q. Yo'q bo'lsa — o'chiriladi, sharhlar anonim qoladi |

---

# 12. Navbat modeli

Bu — usta yo'lining eng nozik joyi. Muammo: usta ish bilan to'lib ketgan
bo'lsa, unga kelgan so'rovlar javobsiz qolib avtomatik bekor bo'lib ketishi
mumkin — demak talab yo'qoladi.

## 12.1. Muammoning ildizi: uch xil soat

| Soat | Nimani o'lchaydi | Qancha bo'lishi tabiiy |
|---|---|---|
| ⏱ **Javob soati** | So'rov keldi → usta javob berdi | Soatlar |
| 📅 **Navbat** | Qabul qilindi → usta ishni boshladi | Kunlar, haftalar |
| ✂️ **Ish soati** | Boshlandi → tayyor | Kunlar |

Uchtasini bittaga qo'shib qo'yish — xato. U **band ustani jazolaydi**.

> **Asosiy printsip: javob berish ≠ ishni boshlash.** Usta 5 daqiqada javob
> bera oladi, lekin ishni 5 kundan keyin boshlashi mumkin. Bu rad etish emas,
> bu — **navbat**.

## 12.2. To'rt yechim

### 1. Narx taklifida "boshlash sanasi"

Usta narxni va **qachon boshlashini** aytadi. Mijoz to'liq rasmni ko'rib o'zi
qaror qiladi: kutamanmi yoki boshqa usta topamanmi.

### 2. Yangi holat: "Navbatda"

"Qabul qilindi" dan darhol "Tikilmoqda" ga o'tish — yolg'on, chunki usta hali
boshlamagan. Oraga **Navbatda** holati qo'yiladi.

Foydasi:
- Mijoz *"nega hali tikilmayapti?"* deb xafa bo'lmaydi
- Ustaning konsolida buyurtmalar ajraladi: *Navbatda* / *Hozir ishlayapman*
- Navbat sanasi kelganda tizim ustaga eslatadi

### 3. Avtomatik yopilish faqat sukutga tegishli

72 soatlik qoida **javob bermaydigan** ustani jazolaydi, **band** ustani emas.
Usta javob bergach — narx qanchalik uzoq sanani ko'rsatsa ham — hech narsa
avtomatik bekor bo'lmaydi. Qarorni mijoz qabul qiladi.

### 4. Klapan: "eng yaqin bo'sh sanam"

Ustaning profilida **bitta maydon**:

```
📅 Eng yaqin bo'sh sanam:  [ 5-avgust ]
```

Uch joyda ishlaydi:

| Joy | Qanday |
|---|---|
| **Katalog kartochkasi** | *"⏳ 5-avgustdan bo'sh"* — mijoz so'rov yuborishdan **oldin** biladi |
| **So'rov formasi** | Mijoz "1-avgustga kerak" deb yozsa: ⚠️ *"Bu usta 5-avgustdan bo'shaydi — baribir yuborasizmi?"* |
| **Ustaning so'rov ekrani** | Tizim hisoblab beradi: *"Mijoz 15-avgustga so'rayapti · navbatingiz 5-avgust · yetadi ✅"* |

Uch daraja qoladi:

```
1. Bo'sh              → so'rov erkin keladi
2. Navbat bilan       → keladi, lekin mijoz kutish kerakligini biladi
3. Qabul qilmayapman  → so'rov tugmasi o'chadi, umuman kelmaydi
```

> **Sig'im raqami (masalan "men 3 ta buyurtma olaman") 1-versiyada YO'Q.**
> Avtomatik hisoblash va cheklash keyinroq. Hozir bitta sana maydoni — usta
> o'zi yuritadi.

## 12.3. Tekshiruv senariysi

Nilufarning 3 ta buyurtmasi bor, 5-avgustda bo'shaydi. Profilida:
*"5-avgustdan bo'sh"*.

| Kun | Voqea | Natija |
|---|---|---|
| 1-avg | Aziza so'rov yuboradi, 20-avgustga kerak | Katalogda "5-avgustdan bo'sh" ni ko'rgan, ataylab yuborgan |
| 1-avg +2s | Nilufar javob beradi: 250 000 · 5-avgustda boshlayman · yetkazaman | ✅ Javob soati bajarildi |
| 1-avg | Aziza qabul qiladi | → **Navbatda** |
| 2-avg | Malika so'rov yuboradi, **3-avgustga** kerak | Nilufar 10 daqiqada rad etadi: *"navbatim 5-avgustdan"* |
| — | Malika boshqa ustaga o'tadi | Ikki soat yo'qotdi, uch kun emas |
| 5-avg | Tizim: *"Azizaning buyurtmasini boshlash vaqti"* | Nilufar → **Tikilmoqda** |
| 18-avg | → **Tayyor** → Aziza oladi → **Topshirildi** | ✅ |

**Hech bir so'rov noto'g'ri bekor bo'lmadi. Band usta jazolanmadi. Mijoz
aldanmadi.**

## 12.4. Boshlash sanasi surilsa

Bloklanmaydi (real hayotda kechikish bo'ladi), lekin **ko'rinadigan**
qilinadi:

```
Usta boshlash sanasini surdi
   ↓
Mijozga xabar: "Boshlash sanasi 10-avgustga surildi. Sabab: ..."
   ↓
Buyurtma tarixiga yoziladi
   ↓
Ustaning "boshlash sanasiga rioya" ko'rsatkichiga tushadi (ichki)
```

Mijoz rozi bo'lmasa — bekor qilish so'rovi qiladi.

---

**Keyingi hujjat:** [04-buyurtma-oqimi.md](04-buyurtma-oqimi.md)

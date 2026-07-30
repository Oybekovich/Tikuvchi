# 06 — Ishonch va sifat

> Tikuvchi hujjatlari · 7-bosqich natijasi
> Oxirgi yangilanish: 2026-07-30

Pul platformadan o'tmagani uchun ([05-pul.md](05-pul.md)) bizning yagona
vositamiz — **ishonch**.

---

## 1. Ishonch nima bilan qurilarli

| Ustun | Nimadan | Kuchi |
|---|---|---|
| 🖼 **Portfolio** | Ustaning haqiqiy ishlari | Eng kuchli — birinchi qaraladigan narsa |
| ⭐ **Sharhlar** | Oldingi mijozlar | Eng ishonchli, lekin yig'ilishi vaqt oladi |
| 📊 **Ko'rsatkichlar** | Tizim hisoblaydi, yolg'on bo'lmaydi | Kuchli va **birinchi kundan** ishlaydi |
| ✅ **Tasdiqlash** | Admin tekshirgan | Boshlanish uchun zarur, lekin o'zi yetmaydi |

## 2. Reyting formulasi

Oddiy o'rtacha ishlamaydi:

| Usta | Sharhlar | Oddiy o'rtacha | Adolatlimi |
|---|---|---|---|
| A | 1 ta × ⭐5 | **5.00** | ❌ Bitta sharh bilan birinchi o'rinda |
| B | 47 ta, o'rtacha 4.8 | **4.80** | ❌ A dan pastda turadi |

**Og'irlashtirilgan reyting** — kam sharhli usta platforma o'rtachasiga
"tortiladi":

```
reyting = (sharhlar_yig'indisi + platforma_o'rtachasi × 5)
          ─────────────────────────────────────────────────
                     (sharhlar_soni + 5)
```

Natija (platforma o'rtachasi 4.5 deb olsak):

| Usta | Sharhlar | Yangi reyting |
|---|---|---|
| A | 1 × ⭐5 | **4.58** |
| B | 47, o'rt. 4.8 | **4.77** ✅ |

Sharhlar ko'paygan sari formula haqiqiy o'rtachaga yaqinlashadi. `5` raqami —
"ishonch chegarasi", keyinchalik sozlanadi.

### Ko'rsatish qoidasi

```
0–2 sharh   →  ⭐ ko'rsatilmaydi.  "🆕 Yangi usta" yozuvi
3+ sharh    →  ⭐ 4.77 (12 sharh)
```

`⭐ 5.0 (1)` mijozni chalg'itadi va soxta ko'rinadi. `🆕 Yangi usta` halolroq
va portfolio'ga e'tiborni qaratadi.

## 3. Sharh tizimi

| Qoida | Qiymat |
|---|---|
| Kim yozadi | **Faqat buyurtmasi "Topshirildi" bo'lgan mijoz** |
| Qancha | Bir buyurtma = bir sharh |
| Nima | ⭐ 1–5 (majburiy) · matn (ixtiyoriy) · natija rasmi (ixtiyoriy) |
| Oyna | Topshirilgandan keyin **30 kun** |
| Tahrirlash | Yozgandan keyin **7 kun** ichida |
| O'chirish | Mijoz o'chira olmaydi. Faqat admin (haqoratli / soxta bo'lsa) |
| Ustaning javobi | 1-versiyada **yo'q** |

> **Eng muhim himoya:** sharh faqat haqiqiy, yakunlangan buyurtmadan keyin
> yozilishi mumkin. Soxta sharh uchun soxta buyurtma yaratib, uni oxirigacha
> olib borish kerak — bu qimmat. Bitta qoida soxta sharhlarning katta qismini
> to'sadi.

## 4. Ko'rsatkichlar

Sharhlar yig'ilishini kutmasdan ishonch beradi:

```
Nilufar Karimova              🆕 Yangi usta
📍 Toshkent, Chilonzor
⚡ O'rtacha 1 soat 20 daqiqada javob beradi
✅ 6 buyurtma yakunlagan
📅 Iyun 2026 dan beri
```

| Ko'rsatkich | Ko'rinishi | Izoh |
|---|---|---|
| Javob tezligi | 🌐 Ochiq | Mijozga foydali, ustaga kuchli motivatsiya |
| Yakunlangan buyurtmalar | 🌐 Ochiq | Eng halol raqam |
| Platformada qancha vaqt | 🌐 Ochiq | Arzon, lekin ishonch beradi |
| **Boshlash sanasiga rioya** | 🔒 Ichki | 1-versiyada faqat saralashda ishlatiladi. Boshida kechikish ko'p bo'ladi — ustani ochiq jazolash noto'g'ri |

## 5. "Ishonchli usta" belgisi

Avtomatik beriladi, admin qo'lda bermaydi:

```
Shart:  10+ yakunlangan buyurtma  VA  reyting 4.5+
Natija: profilda va katalog kartochkasida  ✅ Ishonchli usta
        katalogda yuqoriroq chiqadi
```

Shart bajarilmay qolsa — belgi avtomatik olinadi. Ustaga aniq maqsad beradi
va deyarli hech narsaga turmaydi.

## 6. Yangi ustaning muammosi (cold start)

0 sharhli ustaga hech kim buyurtma bermaydi → u hech qachon sharh olmaydi.
Zanjirni uzish:

| Chora | Qanday |
|---|---|
| 🆕 belgisi jazo emas, **taklif** | *"Yangi usta — birinchi mijozlaridan bo'ling"* |
| Portfolio urg'usi | Sharhi yo'q ustaning kartochkasida **rasm kattaroq** ko'rsatiladi |
| Saralashda joy | Yangi ustalar butunlay pastga tushib ketmaydi — katalogda ularga ma'lum ulush ajratiladi |
| Javob tezligi | Yangi usta tez javob berib **darhol** ishonch qura oladi — bu uning yagona quroli |

## 7. Soxta portfolio bilan kurash

Eng ko'p uchraydigan yolg'on: usta internetdan rasm olib "mening ishim" deb
qo'yadi.

| Chora | Qachon |
|---|---|
| Onboarding'da admin tekshiradi | Har bir yangi usta |
| **Sharh bilan kelgan natija rasmlari** ajratiladi | *"✅ Mijoz yuborgan rasm"* belgisi bilan — soxta bo'lishi mumkin emas |
| Shikoyat | Mijoz *"bu rasm uning ishi emas"* deb bildira oladi |
| Keyin qo'shilgan rasmlar — **post-moderatsiya** | Darhol chiqadi (usta kutmaydi), admin panelida lenta ko'rinadi, shubhali bo'lsa o'chiriladi |

> Har bir rasmni admin tasdig'ini kutish ustani bo'g'adi. Faqat **birinchi
> kirish** tekshiriladi, keyin ishonch asosida.

## 8. Mijoz tomonining sifati

Mijoz ham muammo bo'lishi mumkin: g'oyib bo'ladi, kiyimni olmaydi, so'rov
yuborib javob bermaydi.

**1-versiyada mijozning ochiq reytingi YO'Q.** Sabablari:

- Ochiq reyting mijozni qo'rqitadi va so'rov yuborishdan tiyadi — talab tomoni
  siqiladi
- Marketplace boshida talab taklifdan qimmatroq turadi

Uning o'rniga — **ichki belgi**:

```
Usta buyurtmada:  [ ⚠️ Muammo bildirish ]
                     ○ Mijoz kelmadi / kiyimni olmadi
                     ○ Javob bermayapti
                     ○ Muomala qo'pol edi
                  → faqat ADMIN ko'radi
```

Takrorlansa admin mijozni cheklaydi. Mijoz bu belgilarni ko'rmaydi.

## 9. Moderatsiya

| Obyekt | Qachon | Kim |
|---|---|---|
| Yangi usta profili | **Oldindan** (katalogga chiqishdan avval) | Admin |
| Portfolio rasmlari (keyingi) | **Keyin** (shikoyat / lenta orqali) | Admin |
| Sharhlar | **Keyin** (shikoyat orqali) | Admin |
| Chat xabarlari | ❌ Hech qachon oldindan. Faqat shikoyat bo'lganda | Admin |
| So'rov matni va rasmlari | ❌ Moderatsiya qilinmaydi | — |

## 10. Cheklov — bosqichma-bosqich

Darhol blok qilinmaydi, uch pog'ona:

```
1. OGOHLANTIRISH
   Admin xabar yuboradi. Hech narsa cheklanmaydi.
        ↓ takrorlandi
2. CHEKLOV
   Usta: katalogdan olinadi, faol buyurtmalari saqlanadi
   Mijoz: yangi so'rov yubora olmaydi, faol buyurtmalari saqlanadi
        ↓ takrorlandi yoki qo'pol buzilish
3. BLOK
   Kira oladi, faol buyurtmalarini ko'radi (majburiyati bor),
   lekin yangi hech narsa yarata olmaydi
```

Har pog'onada **sabab yoziladi** va foydalanuvchi uni ko'radi.

**Darhol blok qilinadigan holatlar:** soxta shaxs, haqorat/tahdid, boshqa
odamning portfoliosini o'zlashtirish, pulni olib g'oyib bo'lish.

---

**Keyingi hujjat:** [07-texnik.md](07-texnik.md)

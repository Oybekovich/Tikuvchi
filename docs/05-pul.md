# 05 — Pul oqimi

> Tikuvchi hujjatlari · 6-bosqich natijasi
> Oxirgi yangilanish: 2026-07-30

---

## 1. Realiya — hozir qanday to'lanadi

Dizayn shunga moslashishi kerak:

| Qanday | Ulushi (taxminan) |
|---|---|
| Naqd pul, qo'lga | Ko'pchilik |
| Karta orqali o'tkazma (Humo/Uzcard P2P) | O'sib boryapti |
| Avans + qoldiq | Deyarli har doim, ayniqsa mato ustaniki bo'lsa |
| Ilova / onlayn to'lov orqali | Deyarli yo'q |

Avans odatda **mato va ip xarajati** uchun olinadi — foiz emas, **haqiqiy
xarajat** miqdorida. Shuning uchun "30% avans" degan qat'iy raqam realiyaga
mos kelmaydi.

## 2. Asosiy qaror: 1-versiyada pul platformadan o'tmaydi

| Yo'l | Nima | Baho |
|---|---|---|
| **A. Kuzatuv rejimi** | Pul mijoz→usta to'g'ridan-to'g'ri. Platforma faqat **yozib boradi** | ✅ **Tanlandi** |
| B. Avans escrow | Avans platforma orqali → komissiya ushlanadi → qolgani ustaga | Yuridik shaxs, Payme/Click shartnomasi, payout tizimi — oylar |
| C. To'liq escrow | Hamma pul platformadan | Eng murakkab, ishonch qurish yillar oladi |

### Nega A

1. **B va C ning to'sig'i mahsulotda emas** — yuridik shaxs, to'lov tizimi
   shartnomasi, soliq, pulni ustalarga qaytarib o'tkazish mexanizmi. Bularning
   hammasi bir necha oy oladi va mahsulot rivojiga hech narsa qo'shmaydi.
2. **Avval aylanma bormi-yo'qmi bilish kerak.** Oyiga 20 buyurtma bo'lsa,
   escrow qurish — bo'sh mehnat.
3. **Eng kuchli sabab:** 1-versiyada **komissiya yo'q** → chetlab o'tishga
   sabab ham yo'q → **ma'lumot toza bo'ladi.** Usta narxni yashirmaydi, mijoz
   *"ilovadan tashqarida kelishaylik"* demaydi. Biz haqiqiy aylanmani
   ko'ramiz.

> 1-versiyada Tikuvchi pul ishlamaydi. Bu ataylab qilingan tanlov, kamchilik
> emas.

## 3. To'lov holatlari

Buyurtma holatidan **alohida o'q** — ular bir-biriga bog'liq emas:

```
To'lanmagan  ──▶  Avans to'landi  ──▶  To'liq to'landi
```

| Holat | Ma'nosi |
|---|---|
| `pending` | Hech narsa olinmagan |
| `partial` | Qisman olingan — qancha ekani yozilgan |
| `paid` | Hammasi olingan |

**Kim belgilaydi:** **usta** — pulni u oladi. Belgilaganda **miqdorni va
sanani** yozadi:

```
💰 To'lov qo'shish
   Miqdor:  [ 100 000 ] so'm
   Sana:    [ 05-avgust ]
   Turi:    ● Avans   ○ Qoldiq   ○ To'liq
   [ Saqlash ]
```

Mijozga darhol xabar ketadi. Rozi bo'lmasa — chatda aytadi yoki shikoyat
qiladi.

> **Qat'iy 30% foiz yo'q.** Usta haqiqiy olingan summani kiritadi.

## 4. Mijoz nima ko'radi

Buyurtma sahifasida:

```
💰 To'lov
────────────────────────────
Kelishilgan narx:   250 000 so'm
To'langan:          100 000 so'm  (5-avgust, avans)
Qoldiq:             150 000 so'm
```

## 5. Ustaning pul hisobi

1-versiyaning eng qadrli funksiyalaridan biri. **Ko'p usta kim qancha
qarzdor ekanini daftarda ham yuritmaydi va adashadi.**

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

Bu ro'yxat — ustani ilovaga har kuni qaytaradigan sabab. Va pul platformadan
o'tmasa ham to'liq ishlaydi.

## 6. Monetizatsiya yo'li

| Bosqich | Model | Qachon | Nega shunday |
|---|---|---|---|
| **v1** | Bepul, 0 komissiya | Hozir | Aylanma va ishonchni o'lchash. Ma'lumot toza bo'lishi |
| **v2** | **Usta obunasi** (oylik) | Haftada ~50 tugallangan buyurtmaga yetganda | Undirish oson · chetlab o'tib bo'lmaydi · ustaning daromadiga aralashmaydi · pul o'tkazish tizimi kerak emas |
| **v3** | Avans escrow + komissiya | Aylanma katta bo'lsa va ishonch qurilsa | Eng yuqori daromad, lekin yuridik va texnik yuk bilan |

### Nega v2 da komissiya emas, obuna

- Komissiya undirish uchun **pul platformadan o'tishi shart** — aks holda usta
  pulni tashqarida oladi va yashiradi
- Obuna buyurtmaga bog'liq emas: usta katalogda ko'rinish uchun to'laydi.
  Yashirish imkoni yo'q
- Birinchi ustalardan obunani hatto **qo'lda** (o'tkazma orqali) yig'ish
  mumkin — integratsiya kutilmaydi

## 7. Qaytarish (refund)

Platforma pul ushlab turmagani uchun qaytarish ham platformadan tashqarida:

| Holat | Yechim |
|---|---|
| Mijoz avans berdi, keyin bekor qildi | Usta va mijoz o'zaro hal qiladi. Kelisha olmasalar → shikoyat |
| Admin shikoyatni ko'rib chiqdi | Admin **qaror yozadi** (*"avansning 50% qaytarilishi kerak"*), lekin pulni o'zi qaytarmaydi |
| Usta qaytarmadi | Admin ustani cheklaydi/bloklaydi. Boshqa vositasi yo'q |

> Bu 1-versiyaning **eng zaif joyi.** v3 (escrow) aynan shu muammoni yechish
> uchun keladi.

## 8. Bugundan tayyorlanish

v2/v3 ga o'tishda hammasini buzib qaytadan qurmaslik uchun **hozir** shu
ikki narsa qilinadi:

1. **Har bir to'lov alohida yozuv bo'lsin** — buyurtmada "to'langan:
   100 000" degan bitta raqam emas, balki *to'lovlar ro'yxati* (miqdor, sana,
   turi, kim yozdi). Keyin haqiqiy to'lov tizimi qo'shilsa, o'sha ro'yxatga
   "Payme orqali" degan yozuv qo'shiladi — sxema o'zgarmaydi.
2. **Har buyurtmada komissiya maydoni bo'sh turadi** (hozircha `0`). v2
   kelganda to'ldiriladi, migratsiya kerak bo'lmaydi.

Ikkalasi ham bugun deyarli hech narsaga turmaydi, keyin katta ishni tejaydi.

---

**Keyingi hujjat:** [06-ishonch.md](06-ishonch.md)

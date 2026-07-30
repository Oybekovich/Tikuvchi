# 01 — Mahsulot: loyiha, auditoriya, rollar

> Tikuvchi hujjatlari · 1 va 2-bosqich natijasi
> Oxirgi yangilanish: 2026-07-30

---

## 1. Loyiha nima

> **Tikuvchi** — O'zbekistonda buyurtma asosida kiyim tikadigan ustani topish,
> u bilan narxni kelishish va buyurtmani boshidan oxirigacha kuzatish uchun
> mo'ljallangan marketplace.

## 2. Muammo

Ikki tomonning og'rig'i bir-birining ko'zgusi:

| Mijoz tomoni | Usta tomoni |
|---|---|
| Ishonchli ustani faqat tanish-bilish orqali topadi | Yangi mijoz faqat og'izdan-og'izga keladi, oqim beqaror |
| Ustaning oldingi ishlarini ko'ra olmaydi — sifat lotereya | Portfeli bor, lekin uni ko'rsatadigan joy yo'q |
| O'lchamlarni har safar qaytadan yetkazadi | O'lchamlarni daftarda saqlaydi, adashadi |
| "Qachon tayyor bo'ladi?" — javob yo'q | Kuniga o'nlab "tayyormi?" xabariga javob beradi |
| Narx oldindan noaniq, kelishuv og'zaki | Og'zaki kelishuv → "bunday demagandingiz" nizosi |
| Buyurtmalar Telegram yozishmalarida aralashadi | Nechta faol buyurtma borligini boshida saqlaydi |

**Asl muammo bitta:** bu bozorda **yozma yozuv yo'q**. Na kim bilan
kelishilgani, na nimaga, na qachonga, na qanchaga. Hamma narsa xotira va
shaxsiy ishonchda.

## 3. Yechim g'oyasi

Tikuvchi uch narsani raqamlashtiradi:

1. **Ko'rinish** — ustaning portfeli, hududi va reytingi qidiriladigan katalogda
2. **Kelishuv** — mijoz so'rov yuboradi, usta narx taklif qiladi, mijoz
   qabul qiladi → og'zaki kelishuv o'rniga **yozma yozuv**
3. **Kuzatuv** — buyurtma holati ikkala tomonga bir xil ko'rinadi, o'lchamlar
   bir marta olinadi va qayta ishlatiladi

## 4. Auditoriya

**Qamrov: butun O'zbekiston.** Toshkent bilan cheklanmaydi.

**Mijoz (asosiy):** 20–40 yoshli ayol. To'y, bayram yoki ish uchun kiyim
tiktiradi. Instagram va Telegramda faol. Yiliga 2–6 marta buyurtma beradi.
Narxdan ko'ra **sifat va muddatga ishonch** muhim.

**Usta (asosiy):** 25–55 yoshli tikuvchi ayol. Uyda yoki kichik ateleda
ishlaydi, 1–3 kishilik jamoa. Oyiga 10–40 buyurtma. Telefondan ishlaydi.
Instagram sahifasi bor, lekin uni yuritishga vaqti yo'q.

> Ikkalasidan **usta** kritik: mijoz katalog bo'sh bo'lsa ketadi, usta esa
> mijoz bo'lmasa ham profilini qoldiradi. Shuning uchun butun reja usta
> tomonini birinchi qilib qurilishi kerak.

## 5. Geo-model

### 5.1. Ma'lumotnoma

```
regions    (14 ta)  — 12 viloyat + Qoraqalpog'iston Respublikasi + Toshkent shahri
districts  (~210)   — har viloyatning tuman/shaharlari + markaz koordinatasi
```

| Kimda | Nima saqlanadi |
|---|---|
| **Usta** | viloyat + tuman (**majburiy**) · mo'ljal/manzil matni (ixtiyoriy) |
| **Mijoz** | viloyat + tuman (o'zi tanlaydi, profilida saqlanadi) |

### 5.2. Mijozga ustalar qanday tartibda chiqadi

Bosqichma-bosqich kengayadigan doira — hech qachon bo'sh ekran chiqmasligi
uchun. Doira kengayganda **sababi doim yozib qo'yiladi**:

```
1. Mening tumanimdagi ustalar
2. yetarli emas → viloyatimdagi ustalar   ("Andijon viloyati bo'ylab")
3. yetarli emas → butun O'zbekiston       ("Butun mamlakat bo'ylab")
```

Saralash: yaqinlik → reyting → yakunlangan buyurtmalar soni.

### 5.3. Mijozning joylashuvi qayerdan olinadi

| Manba | Qachon |
|---|---|
| 1. Qurilma geolokatsiyasi | **Asosiy manba.** Ruxsat birinchi kirishda so'raladi, keyingi kirishlarda avtomatik olinadi. Ilova yopilganda kuzatuv to'xtaydi (fon rejimida ishlamaydi) |
| 2. Foydalanuvchi tanlagan hudud | Ruxsat berilmasa yoki GPS o'chiq bo'lsa. Profilda saqlanadi |
| 3. Hech biri yo'q | Butun mamlakat + "Hududingizni tanlang" tugmasi |

**Koordinatani tumanga aylantirish:** har tuman uchun bitta markaz nuqtasi
bazada turadi, GPS koordinatasiga eng yaqini tanlanadi. Tashqi geocoding
xizmati kerak emas — bepul, internetsiz ishlaydi, shahar darajasida yetarli
aniq. Chegara yaqinida adashishi mumkin, shuning uchun tanlangan hudud
ekranning tepasida doim ko'rinib turadi va bir bosishda o'zgartiriladi:

```
📍 Chilonzor tumani  ▾
```

### 5.4. Ustaning manzili

1-versiyada ustaning hududi va manzili **ochiq ko'rinadi**. Manzil maydoni
**erkin matn va ixtiyoriy** — usta xohlasa to'liq yozadi
(*"Chilonzor 19-kvartal, 5-uy"*), xohlasa faqat mo'ljal yozadi
(*"Bunyodkor metrosi yaqinida"*).

Yashirish / taxminiy ko'rsatish funksiyalari keyingi versiyada ko'rib
chiqiladi.

## 6. Qamrov

### Kiradi (1-versiya)

- Usta katalogi, hudud bo'yicha qidiruv, usta profili, portfolio
- Chat va narx kelishuvi
- O'lchamlar kutubxonasi
- Buyurtma va uning holati
- Reyting va sharhlar
- Admin / moderatsiya paneli

### Kirmaydi (ataylab)

| Nima | Nega |
|---|---|
| Tayyor kiyim sotish (e-commerce) | Boshqa biznes. Bu — **xizmat** bozori |
| Mato / furnitura sotish | Ombor va yetkazib berish talab qiladi |
| Yetkazib berish, kuryer | O'lchov olish uchun baribir uchrashish kerak |
| Fabrika / optom (B2B) | Boshqa oqim: shartnoma, partiya, muddat |
| Erkaklar va bolalar kiyimi | Bazada joy bor, lekin fokus tarqatilmaydi. 2-versiya |
| Xizmat kategoriyalari | 1-versiyada yo'q — ustani **teglar va matn qidiruvi** orqali topiladi |
| Xizmat narxlari ro'yxati | Narx faqat so'rovga javoban paydo bo'ladi |
| iOS | Auditoriya asosan Android. Web PWA vaqtincha o'rnini bosadi |
| Ko'p tillilik | Faqat o'zbek. Rus tili — 2-versiya |

## 7. Muvaffaqiyat metrikalari

**Shimoliy yulduz:** *haftada tugallangan buyurtmalar soni* — bu bitta raqam
ichida usta ham, mijoz ham, ishonch ham bor.

| Yordamchi metrika | Nima haqida gapiradi |
|---|---|
| Profilini to'liq to'ldirgan usta soni (portfolio ≥ 5 rasm) | Katalog haqiqiy sifati |
| So'rovning buyurtmaga o'tish ulushi | Kelishuv oqimi ishlayaptimi |
| **Javobsiz yopilgan so'rovlar ulushi** | Platformaning eng og'riqli metrikasi |
| Buyurtmaning bekor qilinish ulushi | Kutish va reallik farqi |
| Ikkinchi marta buyurtma bergan mijoz ulushi | Platforma qiymati bormi |

## 8. Asosiy tavakkalchiliklar

1. **Tovuq-tuxum.** Usta yo'q → mijoz kelmaydi → usta qolmaydi.
   *Yechim:* birinchi 20–30 ustani qo'lda jalb qilish, profilini o'zimiz
   to'ldirish.
2. **Platformadan chetlab o'tish.** Mijoz va usta bir marta tanishgach,
   keyingi safar Telegramda kelishadi. Bu komissiya modelini o'ldiradi.
   *Yechim:* platformada qolish foydali bo'lishi kerak — o'lchamlar tarixi,
   buyurtma yozuvi, reyting, nizo hal qilish. 1-versiyada komissiya
   bo'lmagani uchun chetlab o'tishga sabab ham yo'q.
3. **Ishonch.** Oldindan pul berish madaniyati yo'q.
4. **Ustaning raqamli savodxonligi.** Onboarding 10 daqiqadan oshsa — usta
   tashlab ketadi.
5. **Sifat nazorati.** Bitta yomon tajriba butun platformaga yopishadi.
   Moderatsiya va sharh tizimi 1-versiyada bo'lishi shart.

---

# 2. Aktyorlar va rollar

## 2.1. Aktyorlar

| Aktyor | Kim | Uy ekrani |
|---|---|---|
| **Mehmon** | Ro'yxatdan o'tmagan | Katalog (faqat ko'rish) |
| **Mijoz** | Buyurtma beruvchi | Katalog / bosh sahifa |
| **Usta** | Xizmat ko'rsatuvchi tikuvchi | Buyurtmalar konsoli |
| **Admin** | Platforma xodimi | Alohida admin panel |
| **Tizim** | Avtomatik jarayonlar | — |

## 2.2. Rol modeli

**Ikki xil akkaunt: mijoz va usta — alohida.** Ro'yxatdan o'tishda tanlanadi.

```
[Ro'yxatdan o'tish]
   ↓
"Kim sifatida?"   ┌─ Mijozman  → ism, telefon, parol → tayyor
                  └─ Ustaman   → ism, telefon, parol → usta onboarding
```

`profiles.role` — asosiy manba. 1-versiyada usta buyurtma bera olmaydi:
unda buyurtma tugmasi, o'lchamlar bo'limi va katalogdan buyurtma berish
oqimi yo'q.

**Kelajakka bitta shart:** kodda hech qayerda *"usta = mijoz emas"* degan
taxmin qattiq yozilmasin. Tekshiruv har doim *"bu odamning usta profili
bormi?"* savoli orqali qilinsin. Shunda keyinchalik bitta akkauntga ikki
qobiliyat berish arzon bo'ladi.

**Mijoz keyinchalik usta bo'lishni xohlasa:** 1-versiyada alohida akkaunt
ochadi. Bitta telefon raqam bilan ikkala akkaunt bo'lishi mumkin — akkauntlar
email bilan ajratiladi.

## 2.3. Mehmon nimani ko'radi

| Sahifa | Mehmon |
|---|---|
| Katalog, qidiruv, filtr | ✅ Ochiq |
| Usta profili, portfolio, sharhlar | ✅ Ochiq |
| "Yozish" / "So'rov yuborish" tugmalari | ⚠️ Ko'rinadi, bosilsa → ro'yxatdan o'tish |
| Chat, buyurtmalar, o'lchamlar, profil | 🔒 Yopiq |

Sabab: SEO indeksatsiyasi va ustaning profilini havola orqali ulashish
imkoni. Ro'yxatdan o'tish **faqat harakat qilmoqchi bo'lganda** so'raladi.

## 2.4. Huquqlar matritsasi

| Obyekt | Mehmon | Mijoz | Usta | Admin |
|---|---|---|---|---|
| Katalog, qidiruv, filtr | ko'rish | ko'rish | ko'rish | hammasi |
| Usta profili, portfolio, sharhlar | ko'rish | ko'rish | ko'rish | tahrirlash, yashirish |
| **O'z** usta profili / portfolio | — | — | to'liq CRUD | ko'rish, yashirish |
| Joylashuv (viloyat, tuman, manzil) | ko'rish | ko'rish | o'ziniki | tahrirlash |
| O'lchamlar kutubxonasi | — | o'ziniki: CRUD | ❌ | ❌ |
| Buyurtmaga biriktirilgan o'lcham nusxasi | — | ko'rish | ko'rish | ko'rish |
| Chat | — | o'z suhbatlari | o'z suhbatlari | faqat shikoyat + jurnal |
| So'rov yaratish | — | ✅ | ❌ | ❌ |
| **Narx va boshlash sanasi** | — | ❌ | ✅ | tuzatish |
| Narxni qabul qilish / rad etish | — | ✅ | ❌ | — |
| Status: tikilmoqda → tayyor | — | ❌ | ✅ | majburiy o'zgartirish |
| Status: topshirildi | — | ✅ | ❌ | majburiy o'zgartirish |
| Bekor qilish | — | narx tasdiqlanmaguncha | so'rovni rad etish | har doim |
| Sharh yozish | — | ✅ (yakunlangandan keyin) | ❌ | o'chirish |
| To'lov qayd qilish | — | ko'rish | ✅ | tuzatish |
| Shikoyat qilish | — | ✅ | ✅ | ko'rib chiqish |
| Bloklash | — | — | — | ✅ |
| Akkauntni o'chirish | — | o'ziniki | o'ziniki | har kimniki |

## 2.5. O'zgarmas cheklovlar

Bular ataylab qo'yilgan devorlar:

1. **Usta mijozning o'lcham kutubxonasini ko'rmaydi** — faqat o'ziga berilgan
   buyurtmaga biriktirilgan **nusxani**. Mijoz o'lchamini keyin o'zgartirsa,
   eski buyurtmadagi raqamlar o'zgarmaydi.
2. **Usta buyurtmani yakunlay olmaydi** — faqat "Tayyor" deb belgilaydi.
   Yakunlashni mijoz tasdiqlaydi.
3. **Usta o'ziga yozilgan sharhni o'chira olmaydi** va reytingini
   o'zgartira olmaydi. Sharhga javob yozish — 2-versiya.
4. **Mijoz buyurtmani faqat usta ishni boshlamaguncha bekor qila oladi.**
   Undan keyin — bekor qilish so'rovi.
5. **Admin chatni faqat shikoyat ochilganda o'qiy oladi**, va o'qigani
   jurnalga yoziladi. Taraflarga xabar berilmaydi.
6. **Admin foydalanuvchi nomidan yoza olmaydi** va parolini ko'ra olmaydi.
7. **Yangi usta profili admin tasdig'idan keyin katalogga chiqadi.**

## 2.6. Cheklangan holatlar

| Holat | Ma'nosi |
|---|---|
| Foydalanuvchi cheklangan | Kira oladi va mavjud buyurtmalarini ko'radi (majburiyati bor), lekin yangi buyurtma / chat / sharh yarata olmaydi |
| Usta katalogdan yashirilgan | Katalogda ko'rinmaydi, mavjud mijozlar bilan aloqasi uzilmaydi |
| Usta buyurtma qabul qilmayapti | Profil ko'rinadi, lekin *"Hozir yangi buyurtma qabul qilmayapti"* — so'rov tugmasi o'chirilgan |

## 2.7. Admin akkaunti

Faqat baza orqali qo'lda beriladi (`is_admin = true`). Ilovada "admin bo'lish"
yo'li umuman bo'lmaydi.

Birinchi ustalarga hisob **oddiy ro'yxatdan o'tish sahifasi orqali** ochib
beriladi — admin panelga "usta nomidan profil yaratish" funksiyasi qo'shilmaydi.

## 2.8. Akkauntni o'chirish

Foydalanuvchi o'z akkauntini o'chira oladi (Play Store talabi). Faol
buyurtmasi bo'lsa — o'chirishga ruxsat berilmaydi. O'chirilgach ustaning
sharhlari anonim holda qoladi.

---

**Keyingi hujjat:** [02-mijoz-yoli.md](02-mijoz-yoli.md)

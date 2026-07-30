# Tikuvchi

O'zbekistonda buyurtma asosida kiyim tikadigan **ustalar** va ularga buyurtma
beruvchi **mijozlar**ni bog'lovchi marketplace. Bir mahsulot, uch platforma —
kod bitta repozitoriyada, har biri o'z papkasida.

```
web/       Next.js ilova → tikuvchi-uz.vercel.app   (asosiy ishlanish maydoni)
android/   Kotlin + Jetpack Compose                  (yangi modelga R5 da o'tadi)
ios/       Swift                                     (rejalashtirilmagan)
admin/     Next.js — superadmin paneli (alohida ilova, faqat is_admin=true
           hisoblar kiradi)
```

Barchasi bitta Supabase loyihasidan foydalanadi: ma'lumotlar bazasi,
autentifikatsiya va fayl saqlash umumiy.

## Hujjatlar

Mahsulotning to'liq ta'rifi `docs/` papkasida. **Kod yozishdan oldin shu
hujjatlar o'qilishi kerak** — ular oqimlar va qoidalar bo'yicha yagona
haqiqat manbai.

| # | Hujjat | Mazmuni |
|---|---|---|
| 01 | [Mahsulot](docs/01-mahsulot.md) | Loyiha nima, muammo, auditoriya, geo-model, qamrov, metrikalar, aktyorlar va huquqlar matritsasi |
| 02 | [Mijoz yo'li](docs/02-mijoz-yoli.md) | Ilovani ochishdan buyurtmani olgunga qadar 11 qadam |
| 03 | [Usta yo'li](docs/03-usta-yoli.md) | Ro'yxatdan o'tish, onboarding, konsol, narx berish + **navbat modeli** |
| 04 | [Buyurtma oqimi](docs/04-buyurtma-oqimi.md) | Holat mashinasi, chat, muzokara, bekor qilish, shikoyatlar, timerlar |
| 05 | [Pul](docs/05-pul.md) | To'lov holatlari, ustaning pul hisobi, monetizatsiya yo'li |
| 06 | [Ishonch](docs/06-ishonch.md) | Reyting formulasi, sharhlar, ko'rsatkichlar, moderatsiya, cheklovlar |
| 07 | [Texnik](docs/07-texnik.md) | Sxema o'zgarishlari, yangi jadvallar, kod delta, **reliz yo'l xaritasi** |

### Asosiy qarorlarning qisqa xulosasi

- Buyurtma oqimi bitta: **mijoz so'rov yuboradi (narxsiz) → usta narx va
  boshlash sanasini beradi → mijoz qabul qiladi**
- Buyurtma holatlari: `new · offered · queued · in_progress · ready ·
  completed · cancelled`
- Tugatish sanasi yo'q — usta faqat **boshlash sanasini** belgilaydi
- Mijoz va usta — **alohida akkaunt turlari**
- Katalog va usta profili **mehmonlarga ochiq**
- Yangi usta profili **admin tasdig'idan** keyin katalogga chiqadi
- 1-versiyada pul platformadan **o'tmaydi**, komissiya **yo'q**
- Buyurtmani yakunlash huquqi **faqat mijozda**

## Ishga tushirish

Har bir papkaning o'z ko'rsatmasi bor:

- [`web/README.md`](web/README.md)
- [`android/README.md`](android/README.md)
- [`admin/README.md`](admin/README.md)

Barchasi Supabase kalitlarini talab qiladi va ular git'ga tushmaydi:
web/admin uchun `.env.local`, Android uchun `android/local.properties`.
Namunalar mos papkalarda.

## Vercel

Vercel loyihasining **Root Directory** sozlamasi `web` bo'lishi kerak — web
ilova repozitoriya ildizida emas, `web/` ichida turadi.

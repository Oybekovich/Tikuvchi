# Tikuvchi

Ayol tikuvchi-ustalar uchun marketplace. Bir mahsulot, uch platforma —
kod bitta repozitoriyda, har biri o'z papkasida.

```
web/       Next.js ilova → tikuvchi-uz.vercel.app
android/   Kotlin + Jetpack Compose (mahalliy ilova)
ios/       Swift (rejalashtirilgan)
```

Uchalasi ham bitta Supabase loyihasidan foydalanadi: ma'lumotlar bazasi,
autentifikatsiya va fayl saqlash umumiy. Ya'ni web'da yaratilgan buyurtma
Android'da ham darhol ko'rinadi.

## Ishga tushirish

Har bir papkaning o'z ko'rsatmasi bor:

- [`web/README.md`](web/README.md)
- [`android/README.md`](android/README.md)

Ikkalasi ham Supabase kalitlarini talab qiladi va ular git'ga tushmaydi:
web uchun `web/.env.local`, Android uchun `android/local.properties`.
Namunalar mos papkalarda.

## Vercel

Vercel loyihasining **Root Directory** sozlamasi `web` bo'lishi kerak —
web ilova repozitoriya ildizida emas, `web/` ichida turadi.

# Tikuvchi — Android

Tikuvchi (tikuvchi-uz.vercel.app) ning native Android ilovasi: Kotlin + Jetpack Compose,
ma'lumotlar web bilan bir xil Supabase loyihasidan olinadi.

## Sozlash

`local.properties` git'ga tushmaydi (ichida kalitlar bor). Loyihani birinchi marta
ochganda uni qo'lda yaratish kerak:

```properties
sdk.dir=/path/to/Android/Sdk
SUPABASE_URL=https://<project-ref>.supabase.co
SUPABASE_ANON_KEY=<anon key>
```

Qiymatlarni web loyihaning `.env.local` faylidan yoki Supabase panelidan olish mumkin.
Kalitlar `BuildConfig` orqali o'qiladi — kodga yozilmaydi.

## Qurish

```bash
export JAVA_HOME=~/android-studio/jbr
./gradlew :app:assembleDebug     # APK
./gradlew :app:testDebugUnitTest # testlar
```

## Tuzilishi

- `data/` — Supabase klienti va modellar (DB sxemasi bilan bir xil, `@SerialName` ochiq yozilgan)
- `ui/theme/` — ranglar va tipografiya, web'dagi `@theme` dan aynan ko'chirilgan
- `ui/components/` — umumiy komponentlar
- `ui/screens/` — ekranlar
- `util/Format.kt` — web'dagi `src/lib/format.ts` ning ko'chirmasi (bir xil natija berishi shart)

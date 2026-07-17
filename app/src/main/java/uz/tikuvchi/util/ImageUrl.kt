package uz.tikuvchi.util

import uz.tikuvchi.BuildConfig

/**
 * DB'dagi rasm manzilini yuklab bo'ladigan ko'rinishga keltiradi.
 *
 * Seed ma'lumotlari web ilovaning public/ papkasiga nisbiy yo'l bilan yozilgan
 * ("/seed/covers/u1.svg") — brauzerda ishlaydi, ilovada esa yo'q. Foydalanuvchi
 * yuklagan rasmlar Supabase Storage'ning to'liq manzili bo'ladi, ularga tegilmaydi.
 */
fun imageUrl(raw: String?): String? {
    val value = raw?.trim().orEmpty()
    if (value.isEmpty()) return null
    if (value.startsWith("http://") || value.startsWith("https://")) return value
    return BuildConfig.WEB_ORIGIN.trimEnd('/') + "/" + value.trimStart('/')
}

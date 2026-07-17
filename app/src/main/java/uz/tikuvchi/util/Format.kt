package uz.tikuvchi.util

import java.time.OffsetDateTime
import java.time.ZoneId
import kotlin.math.abs
import kotlin.math.roundToLong

/** Web'dagi src/lib/format.ts ning aynan ko'chirmasi — ikki platformada bir xil natija. */

const val PHONE_PREFIX = "+998 "

/** Narx: 1 450 000 so'm */
fun formatCurrency(amount: Long): String {
    val sign = if (amount < 0) "-" else ""
    val spaced = abs(amount).toString().reversed().chunked(3).joinToString(" ").reversed()
    return "$sign$spaced so'm"
}

fun formatCurrency(amount: Double): String = formatCurrency(amount.roundToLong())

/**
 * Buyurtma raqami: UUID'dan qisqa kod — 0A3F12
 * UUID boshi versiya/variant bitlari sabab bir xil bo'lishi mumkin, shuning
 * uchun oxirgi belgilardan olinadi.
 */
fun formatOrderNumber(id: String): String =
    id.replace("-", "").takeLast(6).uppercase()

private val UZ_MONTHS = listOf(
    "yanvar", "fevral", "mart", "aprel", "may", "iyun",
    "iyul", "avgust", "sentabr", "oktabr", "noyabr", "dekabr",
)

/** Sana: 16-iyul, 2026. Web'dagi kabi mahalliy vaqt mintaqasida ko'rsatiladi. */
fun formatDate(iso: String): String {
    val d = OffsetDateTime.parse(iso).atZoneSameInstant(ZoneId.systemDefault())
    return "${d.dayOfMonth}-${UZ_MONTHS[d.monthValue - 1]}, ${d.year}"
}

/** Ish vaqti: DB'da "09:00:00" — "09:00" ko'rinishiga keltiriladi. */
fun formatTime(value: String?): String {
    if (value.isNullOrEmpty()) return "—"
    return value.take(5)
}

/**
 * Boshidagi 998 mamlakat kodimi? Maydonda prefiks turgan bo'lsa — ha. Tashqaridan
 * nusxalanganda esa faqat raqam 9 tadan uzun bo'lsagina: "99 812 34 56" kabi
 * abonent raqamlari ham 998 bilan boshlanadi (99 — operator kodi).
 */
private fun hasCountryCode(raw: String, digits: String): Boolean =
    digits.startsWith("998") &&
        (raw.startsWith(PHONE_PREFIX.trim()) || digits.length > 9)

/** Abonent raqami — mamlakat kodisiz, ko'pi bilan 9 ta raqam. */
private fun subscriberDigits(raw: String): String {
    val digits = raw.filter { it.isDigit() }
    return (if (hasCountryCode(raw, digits)) digits.drop(3) else digits).take(9)
}

/**
 * Telefonni yozilayotgan payt formatlaydi: +998 90 123 45 67
 * Bo'sh satr qaytsa — maydon tozalangan degani. Prefiksning o'zi o'chirilsa
 * ("+998") maydon bo'shaydi; boshqa holatda prefiks o'zi qo'yib boriladi.
 */
fun formatPhone(raw: String): String {
    if (raw == "+998" || raw.none(Char::isDigit)) return ""

    val rest = subscriberDigits(raw)
    val groups = listOf(
        rest.take(2),
        rest.drop(2).take(3),
        rest.drop(5).take(2),
        rest.drop(7).take(2),
    ).filter { it.isNotEmpty() }

    return PHONE_PREFIX + groups.joinToString(" ")
}

/** Formatlangan telefon matni va undagi kursor o'rni. */
data class PhoneInput(val text: String, val cursor: Int)

/** `digitCount`-raqamdan keyingi o'rin. */
private fun offsetAfterDigits(text: String, digitCount: Int): Int {
    if (digitCount <= 0) return 0
    var seen = 0
    text.forEachIndexed { i, c ->
        if (c.isDigit()) {
            seen++
            if (seen == digitCount) return i + 1
        }
    }
    return text.length
}

/**
 * Xom kiritmani formatlaydi va kursorni joyida ushlab qoladi.
 *
 * Formatlash matn uzunligini o'zgartirgani uchun kursorni o'zimiz hisoblashimiz
 * shart — aks holda u sudralib, keyingi raqamlar noto'g'ri joyga tushadi va
 * raqam aralashib ketadi. O'lchov sifatida kursordan oldingi abonent raqamlari
 * soni olinadi: formatlashda ular soni o'zgarmaydi, faqat orasidagi bo'shliqlar
 * suriladi.
 */
fun formatPhoneInput(raw: String, cursor: Int): PhoneInput {
    val text = formatPhone(raw)
    if (text.isEmpty()) return PhoneInput("", 0)

    val digits = raw.filter { it.isDigit() }
    val before = raw.take(cursor).count(Char::isDigit)
    // Mamlakat kodi kursordan oldida bo'lsa, u abonent raqamiga kirmaydi
    val subscriberBefore =
        if (hasCountryCode(raw, digits)) (before - 3).coerceAtLeast(0) else before

    // Kursor prefiks ichiga tushmasligi kerak — aks holda backspace abonent
    // raqami o'rniga mamlakat kodini o'chira boshlaydi
    val offset = offsetAfterDigits(text, 3 + subscriberBefore)
        .coerceAtLeast(PHONE_PREFIX.length)
    return PhoneInput(text, offset)
}

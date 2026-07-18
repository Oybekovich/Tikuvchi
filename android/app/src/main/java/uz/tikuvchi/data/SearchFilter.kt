package uz.tikuvchi.data

import uz.tikuvchi.data.model.UstaSearchRow

/**
 * Qidiruv filtri. Tuman va reyting server tomonda qo'llanadi (CatalogRepository),
 * qolgani shu yerda — web'dagi search/page.tsx dagi matches() bilan bir xil.
 */
data class SearchQuery(
    val text: String = "",
    val categoryId: Long? = null,
    val district: String? = null,
    val minRating: Double? = null,
    val minPrice: Long? = null,
    val maxPrice: Long? = null,
)

/** Matn ism, tuman, bio va teglar bo'yicha qidiriladi. */
fun matches(usta: UstaSearchRow, q: SearchQuery): Boolean {
    if (q.text.isNotBlank()) {
        val haystack = buildList {
            add(usta.profiles.fullName)
            add(usta.district.orEmpty())
            add(usta.bio.orEmpty())
            addAll(usta.tags)
        }.joinToString(" ").lowercase()
        if (!haystack.contains(q.text.trim().lowercase())) return false
    }

    if (q.categoryId != null &&
        usta.services.none { it.categoryId == q.categoryId }
    ) {
        return false
    }

    if (q.minPrice != null || q.maxPrice != null) {
        // Kamida bitta xizmat narx oralig'iga tushsa — usta ko'rsatiladi
        val inRange = usta.services.any { s ->
            (q.minPrice == null || s.basePrice >= q.minPrice) &&
                (q.maxPrice == null || s.basePrice <= q.maxPrice)
        }
        if (!inRange) return false
    }

    return true
}

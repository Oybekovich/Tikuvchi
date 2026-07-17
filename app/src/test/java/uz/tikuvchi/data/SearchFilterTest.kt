package uz.tikuvchi.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import uz.tikuvchi.data.model.ProfileBrief
import uz.tikuvchi.data.model.ServicePriceCategory
import uz.tikuvchi.data.model.UstaSearchRow

/** Filtr web'dagi search/page.tsx dagi matches() bilan bir xil ishlashi shart. */
class SearchFilterTest {

    private val gulnora = UstaSearchRow(
        userId = "u1",
        district = "Mirzo Ulug'bek",
        bio = "Muslima ayollar uchun yopiq liboslar tikaman",
        ratingAvg = 5.0,
        tags = listOf("abaya", "hijob"),
        profiles = ProfileBrief(fullName = "Gulnora Rashidova"),
        services = listOf(
            ServicePriceCategory(basePrice = 950_000, categoryId = 4),
            ServicePriceCategory(basePrice = 450_000, categoryId = 4),
        ),
    )

    @Test
    fun `bo'sh so'rov hammasini o'tkazadi`() {
        assertTrue(matches(gulnora, SearchQuery()))
    }

    @Test
    fun `matn ism bo'yicha topadi`() {
        assertTrue(matches(gulnora, SearchQuery(text = "gulnora")))
    }

    @Test
    fun `matn teg bo'yicha topadi`() {
        assertTrue(matches(gulnora, SearchQuery(text = "abaya")))
    }

    @Test
    fun `matn bio bo'yicha topadi`() {
        assertTrue(matches(gulnora, SearchQuery(text = "muslima")))
    }

    @Test
    fun `matn tuman bo'yicha topadi`() {
        assertTrue(matches(gulnora, SearchQuery(text = "ulug'bek")))
    }

    @Test
    fun `mos kelmagan matn chiqarib tashlaydi`() {
        assertFalse(matches(gulnora, SearchQuery(text = "kostyum")))
    }

    @Test
    fun `kategoriya bo'yicha filtrlaydi`() {
        assertTrue(matches(gulnora, SearchQuery(categoryId = 4)))
        assertFalse(matches(gulnora, SearchQuery(categoryId = 1)))
    }

    @Test
    fun `narx oralig'i - kamida bitta xizmat tushsa yetarli`() {
        // 450 000 pastki chegaradan o'tadi, 950 000 esa yo'q — usta ko'rinishi kerak
        assertTrue(matches(gulnora, SearchQuery(minPrice = 400_000, maxPrice = 500_000)))
        assertTrue(matches(gulnora, SearchQuery(minPrice = 900_000)))
        assertFalse(matches(gulnora, SearchQuery(minPrice = 2_000_000)))
        assertFalse(matches(gulnora, SearchQuery(maxPrice = 100_000)))
    }

    @Test
    fun `filtrlar birga qo'llanadi`() {
        assertTrue(matches(gulnora, SearchQuery(text = "abaya", categoryId = 4, minPrice = 400_000)))
        assertFalse(matches(gulnora, SearchQuery(text = "abaya", categoryId = 1)))
    }
}

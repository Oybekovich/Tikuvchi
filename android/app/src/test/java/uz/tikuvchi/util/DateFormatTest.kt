package uz.tikuvchi.util

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * DB'da sana ustunlari ikki xil turda: timestamptz va oddiy date. Ikkalasi ham
 * o'qilishi shart — faqat timestamptz kutilgani uchun buyurtmalar ekrani
 * qulab tushgan edi (orders.estimated_ready_at = "2026-07-19").
 */
class DateFormatTest {

    @Test
    fun `timestamptz o'qiladi`() {
        assertEquals("21-iyun, 2026", formatDate("2026-06-21T05:43:56.722385+00:00"))
    }

    @Test
    fun `oddiy date o'qiladi`() {
        assertEquals("19-iyul, 2026", formatDate("2026-07-19"))
    }

    @Test
    fun `yanvar va dekabr chegaralari`() {
        assertEquals("1-yanvar, 2026", formatDate("2026-01-01"))
        assertEquals("31-dekabr, 2026", formatDate("2026-12-31"))
    }

    @Test
    fun `ish vaqti soniyasiz ko'rsatiladi`() {
        assertEquals("09:00", formatTime("09:00:00"))
        assertEquals("—", formatTime(null))
        assertEquals("—", formatTime(""))
    }

    @Test
    fun `narx bo'sh joy bilan ajratiladi`() {
        assertEquals("1 450 000 so'm", formatCurrency(1_450_000))
        assertEquals("450 000 so'm", formatCurrency(450_000))
        assertEquals("0 so'm", formatCurrency(0))
    }

    @Test
    fun `buyurtma raqami oxirgi 6 belgidan`() {
        assertEquals("0A3F12", formatOrderNumber("b1000000-0000-4000-8000-0000000a3f12"))
    }
}

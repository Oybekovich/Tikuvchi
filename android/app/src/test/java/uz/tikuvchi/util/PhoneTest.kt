package uz.tikuvchi.util

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Telefon maydonining xatti-harakati. Emulyatorda `adb input text` bilan sinash
 * ishonchsiz bo'lgani uchun terish shu yerda simulyatsiya qilinadi: har bir belgi
 * kursor turgan joyga qo'yiladi va natija qayta formatlanadi — maydon aynan
 * shunday ishlaydi.
 */
class PhoneTest {

    /** Bo'sh maydonga belgilarni bittalab teradi. */
    private fun type(chars: String, start: PhoneInput = PhoneInput("", 0)): PhoneInput {
        var state = start
        for (c in chars) {
            val raw = state.text.take(state.cursor) + c + state.text.drop(state.cursor)
            state = formatPhoneInput(raw, state.cursor + 1)
        }
        return state
    }

    /** Kursordan oldingi bitta belgini o'chiradi (backspace). */
    private fun backspace(state: PhoneInput): PhoneInput {
        if (state.cursor == 0) return state
        val raw = state.text.take(state.cursor - 1) + state.text.drop(state.cursor)
        return formatPhoneInput(raw, state.cursor - 1)
    }

    @Test
    fun `bittalab terilganda raqam aralashmaydi`() {
        assertEquals("+998 90 123 45 67", type("901234567").text)
    }

    @Test
    fun `prefiks qo'yilgandan keyin terilganda ham to'g'ri`() {
        val start = PhoneInput(PHONE_PREFIX, PHONE_PREFIX.length)
        assertEquals("+998 91 333 44 55", type("913334455", start).text)
    }

    @Test
    fun `kursor har doim terilgan raqamdan keyin turadi`() {
        val s = type("901")
        assertEquals("+998 90 1", s.text)
        assertEquals(s.text.length, s.cursor)
    }

    @Test
    fun `99 bilan boshlanadigan abonent raqami mamlakat kodi deb olinmaydi`() {
        // 99 — operator kodi, shuning uchun 998123456 to'liq abonent raqami
        assertEquals("+998 99 812 34 56", formatPhone("998123456"))
    }

    @Test
    fun `mamlakat kodi bilan nusxalangan raqam`() {
        assertEquals("+998 90 123 45 67", formatPhone("998901234567"))
        assertEquals("+998 90 123 45 67", formatPhone("+998901234567"))
        assertEquals("+998 90 123 45 67", formatPhone("+998 90 123 45 67"))
    }

    @Test
    fun `9 tadan ortiq raqam qabul qilinmaydi`() {
        assertEquals("+998 90 123 45 67", type("9012345678999").text)
    }

    @Test
    fun `backspace raqamlarni bittalab o'chiradi`() {
        var s = type("901234567")
        s = backspace(s)
        assertEquals("+998 90 123 45 6", s.text)
        s = backspace(s)
        assertEquals("+998 90 123 45", s.text)
        s = backspace(s)
        assertEquals("+998 90 123 4", s.text)
    }

    @Test
    fun `prefiksni o'chirib bo'lganda maydon bo'shaydi`() {
        var s = type("9")
        assertEquals("+998 9", s.text)
        repeat(10) { s = backspace(s) }
        assertEquals("", s.text)
    }

    @Test
    fun `bo'shatilgandan keyin qayta terish mumkin`() {
        var s = type("9")
        repeat(10) { s = backspace(s) }
        // Prefiks o'chirilgach maydon "yopilib" qolmasligi kerak
        assertEquals("+998 77 777", type("77777", s).text)
    }

    @Test
    fun `o'rtaga qo'yilgan raqam o'sha yerga tushadi`() {
        val start = formatPhoneInput("+998 90 123", 7) // kursor "90" dan keyin
        val s = type("5", start)
        assertEquals("+998 90 512 3", s.text)
        // Kursor terilgan 5 dan keyin turishi kerak
        assertEquals("+998 90 5", s.text.take(s.cursor))
    }

    @Test
    fun `formatlash barqaror - qayta formatlaganda o'zgarmaydi`() {
        val once = formatPhone("901234567")
        assertEquals(once, formatPhone(once))
    }
}

import { describe, expect, it } from "vitest";
import {
  formatChatTime,
  formatCurrency,
  formatDate,
  formatOrderNumber,
  formatTime,
} from "@/lib/format";

/**
 * Bu testlarning bir qismi Android'dagi DateFormatTest.kt bilan AYNAN bir xil
 * kutilgan qiymatlarga ega — hujjat (docs/01) web va Android natijalari bir
 * xil bo'lishini talab qiladi. Biri o'zgarsa, ikkinchisi ham o'zgarishi kerak.
 */

describe("formatCurrency", () => {
  it("mingliklarni bo'sh joy bilan ajratadi (Android bilan bir xil)", () => {
    expect(formatCurrency(1_450_000)).toBe("1 450 000 so'm");
    expect(formatCurrency(450_000)).toBe("450 000 so'm");
    expect(formatCurrency(0)).toBe("0 so'm");
  });

  it("kichik sonlarni ajratmaydi", () => {
    expect(formatCurrency(1)).toBe("1 so'm");
    expect(formatCurrency(999)).toBe("999 so'm");
    expect(formatCurrency(1000)).toBe("1 000 so'm");
  });

  it("kasr sonni yaxlitlaydi", () => {
    expect(formatCurrency(1499.4)).toBe("1 499 so'm");
    expect(formatCurrency(1499.5)).toBe("1 500 so'm");
  });

  it("manfiy sonda ishora yo'qolmaydi", () => {
    expect(formatCurrency(-250_000)).toBe("-250 000 so'm");
  });
});

describe("formatOrderNumber", () => {
  it("UUID oxirgi 6 belgisini katta harfda beradi (Android bilan bir xil)", () => {
    expect(formatOrderNumber("b1000000-0000-4000-8000-0000000a3f12")).toBe(
      "0A3F12"
    );
  });

  it("defislar hisobga olinmaydi", () => {
    expect(formatOrderNumber("00000000-0000-0000-0000-0000000abcde")).toBe(
      "0ABCDE"
    );
  });

  it("bir xil UUID har doim bir xil raqam beradi", () => {
    const id = "3f2504e0-4f89-41d3-9a0c-0305e82c3301";
    expect(formatOrderNumber(id)).toBe(formatOrderNumber(id));
  });
});

describe("formatDate", () => {
  it("timestamptz o'qiladi (Android bilan bir xil)", () => {
    expect(formatDate("2026-06-21T05:43:56.722385+00:00")).toBe(
      "21-iyun, 2026"
    );
  });

  it("oddiy date o'qiladi — orders.estimated_ready_at shu turda", () => {
    expect(formatDate("2026-07-19")).toBe("19-iyul, 2026");
  });

  it("yanvar va dekabr chegaralari", () => {
    expect(formatDate("2026-01-01")).toBe("1-yanvar, 2026");
    expect(formatDate("2026-12-31")).toBe("31-dekabr, 2026");
  });

  it("Date obyektini ham qabul qiladi", () => {
    expect(formatDate(new Date("2026-07-19T12:00:00Z"))).toBe("19-iyul, 2026");
  });

  it("o'zbekcha oy nomlari to'liq 12 oy uchun", () => {
    const oylar = [
      "yanvar", "fevral", "mart", "aprel", "may", "iyun",
      "iyul", "avgust", "sentabr", "oktabr", "noyabr", "dekabr",
    ];
    oylar.forEach((oy, i) => {
      const oyRaqami = String(i + 1).padStart(2, "0");
      expect(formatDate(`2026-${oyRaqami}-15`)).toBe(`15-${oy}, 2026`);
    });
  });

  /**
   * Sahifalar serverda (Vercel — UTC) render qilinadi, foydalanuvchi esa
   * Toshkentda (UTC+5). Kechasi yaratilgan buyurtma serverda bir kun oldin
   * ko'rinib qolmasligi kerak.
   */
  it("Toshkent vaqtida hisoblanadi, server mintaqasida emas", () => {
    // 2026-07-19 21:30 UTC = 2026-07-20 02:30 Toshkent
    expect(formatDate("2026-07-19T21:30:00Z")).toBe("20-iyul, 2026");
    // 2026-07-20 00:30 UTC = 2026-07-20 05:30 Toshkent
    expect(formatDate("2026-07-20T00:30:00Z")).toBe("20-iyul, 2026");
    // 2026-07-19 18:30 UTC = 2026-07-19 23:30 Toshkent
    expect(formatDate("2026-07-19T18:30:00Z")).toBe("19-iyul, 2026");
  });

  it("sof sana vaqt mintaqasidan mutlaqo mustaqil", () => {
    // "date" ustuni kalendar sanasi — unda vaqt mintaqasi tushunchasi yo'q
    expect(formatDate("2026-01-01")).toBe("1-yanvar, 2026");
    expect(formatDate("2026-12-31")).toBe("31-dekabr, 2026");
    expect(formatDate("2026-07-19")).toBe("19-iyul, 2026");
  });
});

describe("formatTime", () => {
  it("soniyani olib tashlaydi (Android bilan bir xil)", () => {
    expect(formatTime("09:00:00")).toBe("09:00");
    expect(formatTime("18:30:00")).toBe("18:30");
  });

  it("bo'sh qiymat uchun chiziqcha", () => {
    expect(formatTime(null)).toBe("—");
    expect(formatTime("")).toBe("—");
  });
});

describe("formatChatTime", () => {
  const SOAT = 60 * 60 * 1000;
  const KUN = 24 * SOAT;

  it("bugungi xabar uchun soat ko'rsatiladi", () => {
    expect(formatChatTime(new Date().toISOString())).toMatch(/^\d{2}:\d{2}$/);
  });

  it("kechagi xabar uchun 'Kecha'", () => {
    const kecha = new Date(Date.now() - KUN);
    expect(formatChatTime(kecha.toISOString())).toBe("Kecha");
  });

  it("eski xabar uchun kun va oy", () => {
    const eski = new Date(Date.now() - 30 * KUN);
    expect(formatChatTime(eski.toISOString())).toMatch(
      /^\d{1,2}-(yanvar|fevral|mart|aprel|may|iyun|iyul|avgust|sentabr|oktabr|noyabr|dekabr)$/
    );
  });

  it("soat Toshkent vaqtida, 24 soatlik formatda", () => {
    // 2026-07-19 21:30 UTC = 02:30 Toshkent (keyingi kun) — eski xabar
    expect(formatChatTime("2026-07-19T21:30:00Z")).toBe("20-iyul");
  });
});

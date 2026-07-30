import { describe, expect, it } from "vitest";
import { t } from "@/lib/i18n";

describe("t() — tarjima olish", () => {
  it("oddiy kalitni topadi", () => {
    expect(t("common.save")).toBe("Saqlash");
    expect(t("orders.title")).toBe("Buyurtmalarim");
  });

  it("o'zgaruvchini almashtiradi", () => {
    expect(t("orders.estimatedReady", { date: "19-iyul" })).toBe(
      "Taxminiy tayyor: 19-iyul"
    );
    expect(t("search.found", { count: 5 })).toContain("5");
  });

  it("son o'zgaruvchi ham qabul qilinadi", () => {
    expect(t("chat.offerDurationDays", { days: 12 })).toBe("12 kun");
  });

  it("berilmagan o'zgaruvchi o'z joyida qoladi", () => {
    // Yo'qolib ketgandan ko'ra ko'rinib turgani yaxshi — xato darrov sezilади
    expect(t("orders.estimatedReady")).toBe("Taxminiy tayyor: {date}");
    expect(t("orders.estimatedReady", { boshqa: "x" })).toBe(
      "Taxminiy tayyor: {date}"
    );
  });

  it("topilmagan kalit o'zini qaytaradi", () => {
    expect(t("yoq.kalit.umuman")).toBe("yoq.kalit.umuman");
    expect(t("orders.yoqBunday")).toBe("orders.yoqBunday");
  });

  it("bo'lim nomini kalit sifatida so'rasa, o'zini qaytaradi", () => {
    // "orders" — obyekt, satr emas
    expect(t("orders")).toBe("orders");
  });

  it("bo'sh kalit yiqilmaydi", () => {
    expect(() => t("")).not.toThrow();
  });
});

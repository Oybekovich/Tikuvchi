import { describe, expect, it } from "vitest";
import { PHONE_PREFIX, formatPhone } from "@/lib/format";

/**
 * Android'dagi PhoneTest.kt bilan bir xil holatlar. Kursor bilan bog'liq
 * testlar Android'ga xos (u yerda formatPhoneInput bor), qolgani ikkala
 * platformada bir xil natija berishi shart.
 */

describe("formatPhone", () => {
  it("9 xonali abonent raqamini formatlaydi", () => {
    expect(formatPhone("901234567")).toBe("+998 90 123 45 67");
  });

  it("99 bilan boshlanadigan abonent raqami mamlakat kodi deb olinmaydi", () => {
    // 99 — operator kodi, shuning uchun 998123456 to'liq abonent raqami
    expect(formatPhone("998123456")).toBe("+998 99 812 34 56");
  });

  it("mamlakat kodi bilan nusxalangan raqamlarni tanidi", () => {
    expect(formatPhone("998901234567")).toBe("+998 90 123 45 67");
    expect(formatPhone("+998901234567")).toBe("+998 90 123 45 67");
    expect(formatPhone("+998 90 123 45 67")).toBe("+998 90 123 45 67");
  });

  it("9 tadan ortiq raqam qabul qilinmaydi", () => {
    expect(formatPhone("9012345678999")).toBe("+998 90 123 45 67");
  });

  it("qisman terilgan raqam ham formatlanadi", () => {
    expect(formatPhone("9")).toBe("+998 9");
    expect(formatPhone("90")).toBe("+998 90");
    expect(formatPhone("901")).toBe("+998 90 1");
    expect(formatPhone("90123")).toBe("+998 90 123");
    expect(formatPhone("9012345")).toBe("+998 90 123 45");
  });

  it("prefiksni o'chirganda maydon bo'shaydi", () => {
    // Aks holda raqamni butunlay olib tashlab bo'lmaydi
    expect(formatPhone("+998")).toBe("");
    expect(formatPhone("")).toBe("");
  });

  it("raqamsiz belgilar tashlab yuboriladi", () => {
    expect(formatPhone("(90) 123-45-67")).toBe("+998 90 123 45 67");
    expect(formatPhone("abc")).toBe("");
  });

  it("formatlash barqaror — qayta formatlaganda o'zgarmaydi", () => {
    const bir = formatPhone("901234567");
    expect(formatPhone(bir)).toBe(bir);
    expect(formatPhone(formatPhone(bir))).toBe(bir);
  });

  it("prefiks o'zgarmas qiymat sifatida eksport qilinadi", () => {
    expect(PHONE_PREFIX).toBe("+998 ");
    expect(formatPhone("901234567").startsWith(PHONE_PREFIX)).toBe(true);
  });
});

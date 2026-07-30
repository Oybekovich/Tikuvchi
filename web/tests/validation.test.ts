import { describe, expect, it } from "vitest";
import {
  PASSWORD_MIN,
  suggestEmailFix,
  validateEmail,
  validateFullName,
  validatePassword,
  validatePhone,
} from "@/lib/validation";
import { t } from "@/lib/i18n";

describe("validateEmail", () => {
  it("to'g'ri emailni o'tkazadi", () => {
    expect(validateEmail("mijoz1@demo.uz")).toBeNull();
    expect(validateEmail("ism.familiya@mail.co.uk")).toBeNull();
    expect(validateEmail("  aziza@mail.uz  ")).toBeNull();
    expect(validateEmail("a+etiket@gmail.com")).toBeNull();
  });

  it("bo'sh maydonni ushlaydi", () => {
    expect(validateEmail("")?.key).toBe("auth.errEmailRequired");
    expect(validateEmail("   ")?.key).toBe("auth.errEmailRequired");
  });

  it("@ belgisi yo'qligini aytadi", () => {
    expect(validateEmail("mijoz1demo.uz")?.key).toBe("auth.errEmailNoAt");
  });

  it("nuqta o'rniga vergul — tuzatishni taklif qiladi", () => {
    // Foydalanuvchi shikoyat qilgan aynan shu holat
    const xato = validateEmail("mijoz1@demo,uz");
    expect(xato?.key).toBe("auth.errEmailComma");
    expect(xato?.vars?.taklif).toBe("mijoz1@demo.uz");
  });

  it("domensiz emailni rad etadi", () => {
    expect(validateEmail("mijoz1@demo")?.key).toBe("auth.errEmailInvalid");
    expect(validateEmail("mijoz1@")?.key).toBe("auth.errEmailInvalid");
    expect(validateEmail("@demo.uz")?.key).toBe("auth.errEmailInvalid");
  });

  it("bo'sh joyli emailni rad etadi", () => {
    expect(validateEmail("mijoz 1@demo.uz")?.key).toBe("auth.errEmailInvalid");
  });
});

describe("suggestEmailFix", () => {
  it("vergulni nuqtaga almashtiradi", () => {
    expect(suggestEmailFix("mijoz1@demo,uz")).toBe("mijoz1@demo.uz");
    expect(suggestEmailFix("a@b,co,uk")).toBe("a@b.co.uk");
  });

  it("vergul bo'lmasa taklif yo'q", () => {
    expect(suggestEmailFix("mijoz1@demo.uz")).toBeNull();
  });

  it("tuzatilgani ham noto'g'ri bo'lsa taklif bermaydi", () => {
    expect(suggestEmailFix("mijoz1,demo")).toBeNull();
  });
});

describe("validatePassword", () => {
  it("yetarli uzunlikdagi parolni o'tkazadi", () => {
    expect(validatePassword("demo1234")).toBeNull();
    expect(validatePassword("123456")).toBeNull();
  });

  it("bo'sh parolni ushlaydi", () => {
    expect(validatePassword("")?.key).toBe("auth.errPasswordRequired");
  });

  it("qisqa parolda eng kam uzunlikni aytadi", () => {
    const xato = validatePassword("12345");
    expect(xato?.key).toBe("auth.errPasswordShort");
    expect(xato?.vars?.min).toBe(String(PASSWORD_MIN));
  });
});

describe("validateFullName", () => {
  it("normal ismni o'tkazadi", () => {
    expect(validateFullName("Aziza Rahimova")).toBeNull();
  });

  it("bo'sh va juda qisqa ismni ushlaydi", () => {
    expect(validateFullName("")?.key).toBe("auth.errNameRequired");
    expect(validateFullName("   ")?.key).toBe("auth.errNameRequired");
    expect(validateFullName("A")?.key).toBe("auth.errNameShort");
  });
});

describe("validatePhone", () => {
  it("telefon ixtiyoriy — bo'sh bo'lsa xato yo'q", () => {
    expect(validatePhone("")).toBeNull();
    expect(validatePhone("+998")).toBeNull();
  });

  it("to'liq raqamni o'tkazadi", () => {
    expect(validatePhone("+998 90 123 45 67")).toBeNull();
  });

  it("chala raqamni ushlaydi", () => {
    expect(validatePhone("+998 90 123")?.key).toBe("auth.errPhoneIncomplete");
  });
});

describe("xato matnlari o'zbekcha va tarjimasi bor", () => {
  it("har bir xato kaliti uz.json da mavjud", () => {
    const xatolar = [
      validateEmail(""),
      validateEmail("mijoz1demo.uz"),
      validateEmail("mijoz1@demo,uz"),
      validateEmail("mijoz1@demo"),
      validatePassword(""),
      validatePassword("123"),
      validateFullName(""),
      validateFullName("A"),
      validatePhone("+998 90"),
    ];

    for (const x of xatolar) {
      expect(x).not.toBeNull();
      const matn = t(x!.key, x!.vars);
      // t() topilmagan kalitni o'zini qaytaradi — demak tarjima bor bo'lsa
      // natija kalitdan farq qiladi
      expect(matn).not.toBe(x!.key);
      expect(matn.length).toBeGreaterThan(5);
    }
  });

  it("vergul xabari tuzatilgan variantni ko'rsatadi", () => {
    const x = validateEmail("mijoz1@demo,uz")!;
    expect(t(x.key, x.vars)).toContain("mijoz1@demo.uz");
  });
});

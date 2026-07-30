import { PHONE_PREFIX } from "@/lib/format";

/**
 * Forma tekshiruvi.
 *
 * Brauzerning o'z tekshiruvi (`required`, `type="email"`) ishlatilmaydi:
 * u xabarni brauzer/OS tilida chiqaradi ("A part following '@' should not
 * contain the symbol ','") va uni tarjima qilib ham, ko'rinishini
 * o'zgartirib ham bo'lmaydi. Butun ilova o'zbekcha bo'lgani uchun
 * tekshiruv o'zimizda bo'lishi kerak.
 *
 * Funksiyalar tarjima KALITINI qaytaradi, matnni emas — shunda mantiq
 * tildan mustaqil bo'ladi va sinash oson.
 */

export type ValidationError = { key: string; vars?: Record<string, string> };

// Ataylab qat'iy emas: maqsad — xatoni ushlab qolish, RFC'ni bajarish emas.
// Supabase baribir o'z tekshiruvini o'tkazadi.
const EMAIL_RE = /^[^\s@]+@[^\s@.]+(\.[^\s@.]+)+$/;

/**
 * Vergul nuqta o'rniga yozilgani — eng ko'p uchraydigan xato
 * (klaviaturada yonma-yon). Tuzatilgan variant to'g'ri bo'lsa, uni
 * foydalanuvchiga taklif qilamiz.
 */
export function suggestEmailFix(raw: string): string | null {
  const value = raw.trim();
  if (!value.includes(",")) return null;
  const fixed = value.replace(/,/g, ".");
  return EMAIL_RE.test(fixed) ? fixed : null;
}

export function validateEmail(raw: string): ValidationError | null {
  const value = raw.trim();
  if (!value) return { key: "auth.errEmailRequired" };

  const suggestion = suggestEmailFix(value);
  if (suggestion) return { key: "auth.errEmailComma", vars: { taklif: suggestion } };

  if (!value.includes("@")) return { key: "auth.errEmailNoAt" };
  if (!EMAIL_RE.test(value)) return { key: "auth.errEmailInvalid" };
  return null;
}

export const PASSWORD_MIN = 6;

export function validatePassword(value: string): ValidationError | null {
  if (!value) return { key: "auth.errPasswordRequired" };
  if (value.length < PASSWORD_MIN) {
    return { key: "auth.errPasswordShort", vars: { min: String(PASSWORD_MIN) } };
  }
  return null;
}

export function validateFullName(raw: string): ValidationError | null {
  const value = raw.trim();
  if (!value) return { key: "auth.errNameRequired" };
  if (value.length < 2) return { key: "auth.errNameShort" };
  return null;
}

/** Telefon ixtiyoriy — kiritilgan bo'lsagina to'liqligi tekshiriladi */
export function validatePhone(raw: string): ValidationError | null {
  const value = raw.trim();
  if (!value || value === PHONE_PREFIX.trim()) return null;
  const digits = value.replace(/\D/g, "");
  // +998 + 9 xonali abonent raqami
  if (digits.length !== 12) return { key: "auth.errPhoneIncomplete" };
  return null;
}

import { t } from "@/lib/i18n";

/**
 * Narxlarni yagona formatda chiqaradi: 1 450 000 so'm
 * Butun ilovada narx faqat shu funksiya orqali ko'rsatilishi shart.
 */
export function formatCurrency(amount: number): string {
  const rounded = Math.round(amount);
  const spaced = String(Math.abs(rounded)).replace(
    /\B(?=(\d{3})+(?!\d))/g,
    " "
  );
  const sign = rounded < 0 ? "-" : "";
  return `${sign}${spaced} ${t("app.currency")}`;
}

/**
 * Buyurtma raqami: UUID'dan qisqa, o'qiladigan kod — 0A3F12
 * UUID'ning boshi versiya/variant bitlari sabab bir xil bo'lishi mumkin,
 * shuning uchun oxirgi belgilardan olinadi.
 */
export function formatOrderNumber(id: string): string {
  return id.replace(/-/g, "").slice(-6).toUpperCase();
}

/** Telefon maydoni fokuslanganda avtomatik qo'yiladigan boshlanish */
export const PHONE_PREFIX = "+998 ";

/**
 * Telefon raqamni yozilayotgan payt formatlaydi: +998 90 123 45 67
 * Kiritilgan har qanday ko'rinish (998901234567, +998901234567, 901234567)
 * bir xil natijaga keltiriladi.
 *
 * Bo'sh satr qaytsa — maydon tozalangan degani: foydalanuvchi "+998" ni ham
 * o'chira olishi kerak, aks holda raqamni butunlay olib tashlab bo'lmaydi.
 * Boshqa holatda prefiks o'zi qo'yib boriladi — maydon tozalangandan keyin
 * ham (fokus yo'qolmagani uchun onFocus qayta ishlamaydi) yozish mumkin
 * bo'lishi kerak.
 */
export function formatPhone(raw: string): string {
  const digits = raw.replace(/\D/g, "");

  // Prefiksning o'zi o'chirildi yoki hech narsa yozilmagan — maydon bo'shaydi
  if (raw === "+998" || digits.length === 0) return "";

  // Maydonda prefiks turgan bo'lsa, boshidagi 998 — mamlakat kodi.
  // Tashqaridan nusxalanganda esa u faqat raqam 9 tadan uzun bo'lsagina
  // mamlakat kodi: "99 812 34 56" kabi abonent raqamlari ham 998 bilan
  // boshlanadi (99 — operator kodi).
  const hasPrefix = raw.startsWith(PHONE_PREFIX.trim());
  const isCountryCode =
    digits.startsWith("998") && (hasPrefix || digits.length > 9);
  const rest = (isCountryCode ? digits.slice(3) : digits).slice(0, 9);
  const groups = [
    rest.slice(0, 2),
    rest.slice(2, 5),
    rest.slice(5, 7),
    rest.slice(7, 9),
  ].filter(Boolean);

  return PHONE_PREFIX + groups.join(" ");
}

const UZ_MONTHS = [
  "yanvar",
  "fevral",
  "mart",
  "aprel",
  "may",
  "iyun",
  "iyul",
  "avgust",
  "sentabr",
  "oktabr",
  "noyabr",
  "dekabr",
];

/** Faqat sana: "2026-07-19" (timestamptz emas) */
const DATE_ONLY = /^\d{4}-\d{2}-\d{2}$/;

/**
 * Barcha sana va vaqtlar Toshkent vaqtida ko'rsatiladi.
 *
 * Sabab: sahifalar serverda render qilinadi (Vercel — UTC), foydalanuvchi
 * esa O'zbekistonda (UTC+5). Mahalliy vaqtga tayanilsa, Toshkent bo'yicha
 * kechasi 02:00 da yaratilgan buyurtma serverda BIR KUN OLDIN ko'rinadi.
 * Android ham qurilma vaqtida, ya'ni Toshkentda formatlaydi — natijalar
 * bir xil bo'lishi shart (docs/01).
 */
const TASHKENT = "Asia/Tashkent";

function tashkentParts(d: Date): { day: number; month: number; year: number } {
  const parts = new Intl.DateTimeFormat("en-US", {
    timeZone: TASHKENT,
    year: "numeric",
    month: "numeric",
    day: "numeric",
  }).formatToParts(d);
  const get = (type: string) =>
    Number(parts.find((p) => p.type === type)?.value ?? 0);
  return { day: get("day"), month: get("month") - 1, year: get("year") };
}

/**
 * Sana: 16-iyul, 2026
 *
 * DB'da sana ustunlari ikki xil turda: `timestamptz` va oddiy `date`
 * (masalan `orders.estimated_ready_at`). Sof sana — bu kalendar sanasi,
 * unda vaqt mintaqasi umuman yo'q, shuning uchun matnning o'zidan
 * o'qiladi: `new Date("2026-07-19")` uni UTC yarim tuni deb talqin qilib,
 * UTC'dan orqadagi mintaqada bir kunga orqaga surib yuboradi.
 */
export function formatDate(value: string | Date): string {
  if (typeof value === "string" && DATE_ONLY.test(value)) {
    const [year, month, day] = value.split("-").map(Number);
    return `${day}-${UZ_MONTHS[month - 1]}, ${year}`;
  }
  const d = typeof value === "string" ? new Date(value) : value;
  const { day, month, year } = tashkentParts(d);
  return `${day}-${UZ_MONTHS[month]}, ${year}`;
}

/**
 * Chat ro'yxati uchun qisqa vaqt: bugun bo'lsa soat, aks holda sana.
 * Kun chegarasi ham Toshkent vaqtida hisoblanadi — aks holda server
 * (UTC) va foydalanuvchining qurilmasi "bugun"ni har xil tushunadi.
 */
export function formatChatTime(value: string): string {
  const d = new Date(value);
  const now = new Date();

  const kun = (x: Date) => {
    const { day, month, year } = tashkentParts(x);
    return `${year}-${month}-${day}`;
  };

  if (kun(d) === kun(now)) {
    return new Intl.DateTimeFormat("en-GB", {
      timeZone: TASHKENT,
      hour: "2-digit",
      minute: "2-digit",
      hour12: false,
    }).format(d);
  }

  const kecha = new Date(now.getTime() - 24 * 60 * 60 * 1000);
  if (kun(d) === kun(kecha)) return t("common.yesterday");

  const { day, month } = tashkentParts(d);
  return `${day}-${UZ_MONTHS[month]}`;
}

/** Ish vaqtini "09:00" ko'rinishiga keltiradi (DB'da "09:00:00") */
export function formatTime(value: string | null): string {
  if (!value) return "—";
  return value.slice(0, 5);
}

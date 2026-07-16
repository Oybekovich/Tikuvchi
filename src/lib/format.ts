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

/** Sana: 16-iyul, 2026 */
export function formatDate(value: string | Date): string {
  const d = typeof value === "string" ? new Date(value) : value;
  return `${d.getDate()}-${UZ_MONTHS[d.getMonth()]}, ${d.getFullYear()}`;
}

/** Chat ro'yxati uchun qisqa vaqt: bugun bo'lsa soat, aks holda sana */
export function formatChatTime(value: string): string {
  const d = new Date(value);
  const now = new Date();
  const sameDay = d.toDateString() === now.toDateString();
  if (sameDay) {
    return d.toLocaleTimeString("uz-UZ", {
      hour: "2-digit",
      minute: "2-digit",
      hour12: false,
    });
  }
  const yesterday = new Date(now);
  yesterday.setDate(now.getDate() - 1);
  if (d.toDateString() === yesterday.toDateString()) return t("common.yesterday");
  return `${d.getDate()}-${UZ_MONTHS[d.getMonth()]}`;
}

/** Ish vaqtini "09:00" ko'rinishiga keltiradi (DB'da "09:00:00") */
export function formatTime(value: string | null): string {
  if (!value) return "—";
  return value.slice(0, 5);
}

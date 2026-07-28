/**
 * Bu funksiyalar web/src/lib/format.ts ning qisqartirilgan ko'chirmasi —
 * natija ikkala ilovada bir xil bo'lishi kerak.
 */

export function formatCurrency(amount: number): string {
  const rounded = Math.round(amount);
  const spaced = String(Math.abs(rounded)).replace(
    /\B(?=(\d{3})+(?!\d))/g,
    " "
  );
  const sign = rounded < 0 ? "-" : "";
  return `${sign}${spaced} so'm`;
}

export function formatOrderNumber(id: string): string {
  return id.replace(/-/g, "").slice(-6).toUpperCase();
}

const UZ_MONTHS = [
  "yanvar", "fevral", "mart", "aprel", "may", "iyun",
  "iyul", "avgust", "sentabr", "oktabr", "noyabr", "dekabr",
];

export function formatDate(value: string | Date): string {
  const d = typeof value === "string" ? new Date(value) : value;
  return `${d.getDate()}-${UZ_MONTHS[d.getMonth()]}, ${d.getFullYear()}`;
}

export function formatDateTime(value: string): string {
  const d = new Date(value);
  const time = d.toLocaleTimeString("uz-UZ", {
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  });
  return `${formatDate(d)}, ${time}`;
}

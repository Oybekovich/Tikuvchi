/**
 * Havola joriy sahifaga mos keladimi.
 *
 * Bosh sahifa faqat aniq mos kelganda faol bo'ladi — aks holda `/` prefiksi
 * hamma narsaga to'g'ri kelib, barcha tugmalar bir vaqtda yonib turardi.
 * Qolganlari uchun ichki sahifalar ham hisobga olinadi: `/orders/123`
 * ochilganda "Buyurtmalar" faol qoladi.
 *
 * Pastki panel (`BottomNav`) va desktop menyusi (`AppHeader`) bitta qoidadan
 * foydalanadi — aks holda bir xil sahifada ikki xil faol holat chiqishi
 * mumkin edi.
 */
export function isActivePath(pathname: string, href: string): boolean {
  if (href === "/") return pathname === "/";
  return pathname === href || pathname.startsWith(href + "/");
}

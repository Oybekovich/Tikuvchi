import type { Enums } from "@/lib/database.types";

/**
 * To'lov integratsiyasi uchun joy (Payme / Click).
 *
 * Hozircha to'lov faqat `orders.payment_status` maydoni orqali modellashtirilgan:
 * pending → partial (bo'nak, ~30%) → paid.
 *
 * Haqiqiy integratsiyada bu modul provayder API'siga so'rov yuboradi va
 * webhook orqali payment_status'ni yangilaydi.
 */

export const DEPOSIT_RATE = 0.3;

export function depositAmount(totalPrice: number): number {
  return Math.round(totalPrice * DEPOSIT_RATE);
}

export function nextPaymentStatus(
  current: Enums<"payment_status">
): Enums<"payment_status"> | null {
  if (current === "pending") return "partial";
  if (current === "partial") return "paid";
  return null;
}

import { createClient } from "@/lib/supabase/client";
import type { Enums } from "@/lib/database.types";

type OrderStatus = Enums<"order_status">;

/**
 * Ustaning ilgari suradigan bosqichlari: accepted -> in_progress -> ready.
 *
 * `ready -> completed` ataylab YO'Q: buyurtmani yakunlash huquqi faqat
 * mijozda (docs/01-mahsulot.md, "O'zgarmas cheklovlar" 2-band). Usta faqat
 * "tayyor" deb belgilaydi, qabul qilganini mijoz tasdiqlaydi.
 */
export function nextUstaStatus(
  status: OrderStatus
): { next: OrderStatus; labelKey: string } | null {
  switch (status) {
    case "accepted":
      return { next: "in_progress", labelKey: "orders.start" };
    case "in_progress":
      return { next: "ready", labelKey: "orders.markReady" };
    default:
      return null;
  }
}

/** Mijoz buyurtmani faqat "tayyor" holatida yakunlay oladi */
export function canClientComplete(status: OrderStatus): boolean {
  return status === "ready";
}

/**
 * Audit tarixi. Xato bo'lsa ham asosiy amalni to'xtatmaydi — tarix yozuvi
 * buyurtmaning o'zidan muhimroq emas.
 */
export async function insertOrderEvent(
  orderId: string,
  from: OrderStatus,
  to: OrderStatus
): Promise<void> {
  try {
    const supabase = createClient();
    const {
      data: { user },
    } = await supabase.auth.getUser();
    if (!user) return;
    await supabase.from("order_events").insert({
      order_id: orderId,
      from_status: from,
      to_status: to,
      changed_by: user.id,
    });
  } catch {
    // Audit yozuvi muvaffaqiyatsiz bo'lsa ham status o'zgarishi turadi
  }
}

/**
 * Statusni almashtirish. `from` holati hamon o'zgarmaganini shart qilib
 * qo'yadi — shu bilan ikki qurilma (yoki ikki marta bosilgan tugma) bitta
 * buyurtmani ikki xil yo'nalishga olib ketishining oldi olinadi.
 *
 * Har bir muvaffaqiyatli o'zgarish `order_events`ga yoziladi.
 */
export async function changeOrderStatus(
  orderId: string,
  from: OrderStatus,
  to: OrderStatus
): Promise<{ ok: boolean; stale: boolean }> {
  const supabase = createClient();
  const { data, error } = await supabase
    .from("orders")
    .update({ status: to })
    .eq("id", orderId)
    .eq("status", from)
    .select("id");

  if (error) return { ok: false, stale: false };
  // 0 qator: buyurtma allaqachon boshqa holatga o'tgan
  if (!data || data.length === 0) return { ok: false, stale: true };

  await insertOrderEvent(orderId, from, to);
  return { ok: true, stale: false };
}

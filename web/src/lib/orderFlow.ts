import { createClient } from "@/lib/supabase/client";
import type { Enums } from "@/lib/database.types";

/**
 * android/.../data/OrdersRepository.kt dagi progressStatus() bilan bir xil
 * ketma-ketlik: accepted -> in_progress -> ready -> completed.
 */
export function nextOrderStatus(
  status: Enums<"order_status">
): { next: Enums<"order_status">; labelKey: string } | null {
  switch (status) {
    case "accepted":
      return { next: "in_progress", labelKey: "orders.start" };
    case "in_progress":
      return { next: "ready", labelKey: "orders.markReady" };
    case "ready":
      return { next: "completed", labelKey: "orders.markCompleted" };
    default:
      return null;
  }
}

/**
 * android/.../data/OrdersRepository.kt dagi insertEvent() bilan bir xil —
 * audit tarixi uchun, xato bo'lsa ham asosiy amalni to'xtatmaydi.
 */
export async function insertOrderEvent(
  orderId: string,
  from: Enums<"order_status">,
  to: Enums<"order_status">
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
    // Audit yozuvi muvaffaqiyatsiz bo'lsa ham asosiy status o'zgarishi turadi
  }
}

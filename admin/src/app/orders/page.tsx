import Link from "next/link";
import { createClient } from "@/lib/supabase/server";
import { formatCurrency, formatDate, formatOrderNumber } from "@/lib/format";

const STATUSES = [
  "pending",
  "accepted",
  "in_progress",
  "ready",
  "completed",
  "cancelled",
] as const;

export default async function OrdersPage({
  searchParams,
}: {
  searchParams: Promise<{ status?: string }>;
}) {
  const { status } = await searchParams;
  const supabase = await createClient();

  let query = supabase
    .from("orders")
    .select(
      "id, status, payment_status, total_price, created_at, client_id, usta_id"
    )
    .order("created_at", { ascending: false })
    .limit(100);

  if (status && (STATUSES as readonly string[]).includes(status)) {
    query = query.eq("status", status);
  }

  const { data: orders, error } = await query;

  return (
    <div>
      <h1 className="text-xl font-extrabold text-ink-900">Buyurtmalar</h1>

      {error && (
        <p className="mt-4 rounded-xl bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">
          Ma&apos;lumot yuklashda xatolik: {error.message}
        </p>
      )}

      <div className="mt-4 flex flex-wrap gap-2">
        <Link
          href="/orders"
          className={`rounded-full px-3 py-1.5 text-xs font-bold ${
            !status ? "bg-terra-600 text-white" : "bg-white text-ink-700 shadow-card"
          }`}
        >
          Barchasi
        </Link>
        {STATUSES.map((s) => (
          <Link
            key={s}
            href={`/orders?status=${s}`}
            className={`rounded-full px-3 py-1.5 text-xs font-bold ${
              status === s ? "bg-terra-600 text-white" : "bg-white text-ink-700 shadow-card"
            }`}
          >
            {s}
          </Link>
        ))}
      </div>

      <div className="mt-4 overflow-x-auto rounded-2xl bg-white shadow-card">
        <table className="w-full min-w-[640px] text-left text-sm">
          <thead>
            <tr className="border-b border-cream-200 text-xs font-bold uppercase text-ink-500">
              <th className="px-4 py-3">Raqam</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3">To&apos;lov</th>
              <th className="px-4 py-3">Narx</th>
              <th className="px-4 py-3">Sana</th>
              <th className="px-4 py-3" />
            </tr>
          </thead>
          <tbody>
            {(orders ?? []).map((o) => (
              <tr key={o.id} className="border-b border-cream-100 last:border-0">
                <td className="px-4 py-3 font-semibold text-ink-900">
                  {formatOrderNumber(o.id)}
                </td>
                <td className="px-4 py-3 text-ink-700">{o.status}</td>
                <td className="px-4 py-3 text-ink-700">{o.payment_status}</td>
                <td className="px-4 py-3 text-ink-700">
                  {formatCurrency(o.total_price)}
                </td>
                <td className="px-4 py-3 text-ink-700">
                  {formatDate(o.created_at)}
                </td>
                <td className="px-4 py-3">
                  <Link
                    href={`/orders/${o.id}`}
                    className="text-xs font-bold text-terra-700 hover:underline"
                  >
                    Ko&apos;rish
                  </Link>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {orders && orders.length === 0 && (
          <p className="px-4 py-6 text-center text-sm text-ink-500">
            Buyurtma topilmadi
          </p>
        )}
      </div>
    </div>
  );
}

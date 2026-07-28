import { notFound } from "next/navigation";
import { createClient } from "@/lib/supabase/server";
import { formatCurrency, formatDateTime, formatOrderNumber } from "@/lib/format";
import ForceStatusForm from "@/components/ForceStatusForm";

export default async function OrderDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const supabase = await createClient();

  const [{ data: order }, { data: items }, { data: events }] = await Promise.all([
    supabase.from("orders").select("*").eq("id", id).maybeSingle(),
    supabase.from("order_items").select("*").eq("order_id", id),
    supabase
      .from("order_events")
      .select("*")
      .eq("order_id", id)
      .order("created_at", { ascending: false }),
  ]);

  if (!order) notFound();

  const [{ data: client }, { data: usta }] = await Promise.all([
    supabase
      .from("profiles")
      .select("full_name, phone")
      .eq("id", order.client_id)
      .maybeSingle(),
    supabase
      .from("profiles")
      .select("full_name, phone")
      .eq("id", order.usta_id)
      .maybeSingle(),
  ]);

  return (
    <div className="max-w-2xl">
      <h1 className="text-xl font-extrabold text-ink-900">
        Buyurtma {formatOrderNumber(order.id)}
      </h1>

      <div className="mt-4 grid gap-3 rounded-2xl bg-white p-4 shadow-card sm:grid-cols-2">
        <div>
          <p className="text-xs font-bold uppercase text-ink-500">Mijoz</p>
          <p className="font-semibold text-ink-900">{client?.full_name ?? "—"}</p>
          <p className="text-sm text-ink-500">{client?.phone ?? "—"}</p>
        </div>
        <div>
          <p className="text-xs font-bold uppercase text-ink-500">Usta</p>
          <p className="font-semibold text-ink-900">{usta?.full_name ?? "—"}</p>
          <p className="text-sm text-ink-500">{usta?.phone ?? "—"}</p>
        </div>
        <div>
          <p className="text-xs font-bold uppercase text-ink-500">Status</p>
          <p className="font-semibold text-ink-900">{order.status}</p>
        </div>
        <div>
          <p className="text-xs font-bold uppercase text-ink-500">To&apos;lov</p>
          <p className="font-semibold text-ink-900">{order.payment_status}</p>
        </div>
        <div>
          <p className="text-xs font-bold uppercase text-ink-500">Narx</p>
          <p className="font-semibold text-ink-900">
            {formatCurrency(order.total_price)}
          </p>
        </div>
        <div>
          <p className="text-xs font-bold uppercase text-ink-500">Manba</p>
          <p className="font-semibold text-ink-900">{order.source}</p>
        </div>
      </div>

      {items && items.length > 0 && (
        <div className="mt-4 rounded-2xl bg-white p-4 shadow-card">
          <p className="text-xs font-bold uppercase text-ink-500">Buyurtma tafsilotlari</p>
          {items.map((it) => (
            <div key={it.id} className="mt-2 text-sm text-ink-700">
              <p className="font-semibold text-ink-900">{it.title}</p>
              {it.material && <p>Mato: {it.material}</p>}
              {it.model_note && <p>Model: {it.model_note}</p>}
              {it.size_note && <p>Izoh: {it.size_note}</p>}
            </div>
          ))}
        </div>
      )}

      <div className="mt-4 rounded-2xl bg-white p-4 shadow-card">
        <p className="text-xs font-bold uppercase text-ink-500">
          Statusni majburiy o&apos;zgartirish
        </p>
        <ForceStatusForm orderId={order.id} currentStatus={order.status} />
      </div>

      <div className="mt-4 rounded-2xl bg-white p-4 shadow-card">
        <p className="text-xs font-bold uppercase text-ink-500">Tarix</p>
        {(events ?? []).length === 0 ? (
          <p className="mt-2 text-sm text-ink-500">Hali o&apos;zgarish yo&apos;q</p>
        ) : (
          <ul className="mt-2 space-y-2 text-sm text-ink-700">
            {(events ?? []).map((e) => (
              <li key={e.id}>
                <span className="font-semibold text-ink-900">
                  {e.from_status ?? "—"} → {e.to_status}
                </span>{" "}
                — {formatDateTime(e.created_at)}
                {e.comment && <p className="text-ink-500">{e.comment}</p>}
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}

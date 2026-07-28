import { createClient } from "@/lib/supabase/server";
import { formatCurrency } from "@/lib/format";

const ORDER_STATUSES = [
  "pending",
  "accepted",
  "in_progress",
  "ready",
  "completed",
  "cancelled",
] as const;

function StatCard({ label, value }: { label: string; value: string | number }) {
  return (
    <div className="rounded-2xl bg-white p-4 shadow-card">
      <p className="text-2xl font-extrabold text-ink-900">{value}</p>
      <p className="mt-1 text-sm text-ink-500">{label}</p>
    </div>
  );
}

export default async function OverviewPage() {
  const supabase = await createClient();

  const [usersCount, ustasCount, orders] = await Promise.all([
    supabase.from("profiles").select("*", { count: "exact", head: true }),
    supabase.from("usta_profiles").select("*", { count: "exact", head: true }),
    supabase.from("orders").select("status, total_price"),
  ]);

  const orderRows = orders.data ?? [];
  const byStatus = Object.fromEntries(
    ORDER_STATUSES.map((s) => [s, orderRows.filter((o) => o.status === s).length])
  );
  const revenue = orderRows
    .filter((o) => o.status === "ready" || o.status === "completed")
    .reduce((sum, o) => sum + (o.total_price ?? 0), 0);

  const loadError = usersCount.error || ustasCount.error || orders.error;

  return (
    <div>
      <h1 className="text-xl font-extrabold text-ink-900">Umumiy holat</h1>

      {loadError && (
        <p className="mt-4 rounded-xl bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">
          Ma&apos;lumot yuklashda xatolik: {loadError.message}. Agar
          0007_admin_panel.sql migratsiyasi hali qo&apos;llanilmagan bo&apos;lsa,
          shuni bajaring.
        </p>
      )}

      <div className="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-3">
        <StatCard label="Foydalanuvchilar" value={usersCount.count ?? "—"} />
        <StatCard label="Ustalar" value={ustasCount.count ?? "—"} />
        <StatCard label="Buyurtmalar (jami)" value={orderRows.length} />
      </div>

      <h2 className="mt-8 text-sm font-extrabold uppercase tracking-wide text-ink-500">
        Status bo&apos;yicha buyurtmalar
      </h2>
      <div className="mt-3 grid grid-cols-2 gap-3 sm:grid-cols-3">
        {ORDER_STATUSES.map((s) => (
          <StatCard key={s} label={s} value={byStatus[s]} />
        ))}
      </div>

      <h2 className="mt-8 text-sm font-extrabold uppercase tracking-wide text-ink-500">
        Daromad
      </h2>
      <div className="mt-3 grid grid-cols-2 gap-3 sm:grid-cols-3">
        <StatCard label="Ready + Completed" value={formatCurrency(revenue)} />
      </div>
    </div>
  );
}

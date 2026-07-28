import { createClient } from "@/lib/supabase/server";
import UstaVisibilityToggle from "@/components/UstaVisibilityToggle";

type UstaRow = {
  user_id: string;
  district: string | null;
  rating_avg: number | null;
  rating_count: number | null;
  available: boolean;
  visible: boolean;
  profiles: { full_name: string | null; phone: string | null } | null;
};

export default async function UstasPage() {
  const supabase = await createClient();
  const { data, error } = await supabase
    .from("usta_profiles")
    .select(
      "user_id, district, rating_avg, rating_count, available, visible, profiles!inner(full_name, phone)"
    )
    .order("rating_avg", { ascending: false });
  const ustas = data as unknown as UstaRow[] | null;

  return (
    <div>
      <h1 className="text-xl font-extrabold text-ink-900">Ustalar</h1>

      {error && (
        <p className="mt-4 rounded-xl bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">
          Ma&apos;lumot yuklashda xatolik: {error.message}
        </p>
      )}

      <div className="mt-4 overflow-x-auto rounded-2xl bg-white shadow-card">
        <table className="w-full min-w-[720px] text-left text-sm">
          <thead>
            <tr className="border-b border-cream-200 text-xs font-bold uppercase text-ink-500">
              <th className="px-4 py-3">Ism</th>
              <th className="px-4 py-3">Tuman</th>
              <th className="px-4 py-3">Reyting</th>
              <th className="px-4 py-3">Holat</th>
              <th className="px-4 py-3" />
            </tr>
          </thead>
          <tbody>
            {(ustas ?? []).map((u) => (
              <tr key={u.user_id} className="border-b border-cream-100 last:border-0">
                <td className="px-4 py-3 font-semibold text-ink-900">
                  {u.profiles?.full_name ?? "—"}
                </td>
                <td className="px-4 py-3 text-ink-700">{u.district ?? "—"}</td>
                <td className="px-4 py-3 text-ink-700">
                  {u.rating_avg?.toFixed(1) ?? "0.0"} ({u.rating_count ?? 0})
                </td>
                <td className="px-4 py-3">
                  <div className="flex flex-col gap-1 text-xs font-bold">
                    <span className={u.available ? "text-sage-500" : "text-ink-300"}>
                      {u.available ? "Buyurtma qabul qiladi" : "Band"}
                    </span>
                    <span className={u.visible ? "text-sage-500" : "text-red-700"}>
                      {u.visible ? "Ko'rinadi" : "Yashirilgan"}
                    </span>
                  </div>
                </td>
                <td className="px-4 py-3">
                  <UstaVisibilityToggle
                    ustaId={u.user_id}
                    visible={u.visible}
                    available={u.available}
                  />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {ustas && ustas.length === 0 && (
          <p className="px-4 py-6 text-center text-sm text-ink-500">
            Usta topilmadi
          </p>
        )}
      </div>
    </div>
  );
}

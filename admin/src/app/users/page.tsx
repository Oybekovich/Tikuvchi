import { createClient } from "@/lib/supabase/server";
import { formatDate } from "@/lib/format";
import BlockUserButton from "@/components/BlockUserButton";

type AdminProfileRow = {
  id: string;
  full_name: string | null;
  phone: string | null;
  role: string;
  created_at: string;
  is_admin: boolean;
  is_blocked: boolean;
};

export default async function UsersPage() {
  const supabase = await createClient();
  const { data: users, error } = (await supabase.rpc("admin_list_profiles")) as {
    data: AdminProfileRow[] | null;
    error: { message: string } | null;
  };

  return (
    <div>
      <h1 className="text-xl font-extrabold text-ink-900">Foydalanuvchilar</h1>

      {error && (
        <p className="mt-4 rounded-xl bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">
          Ma&apos;lumot yuklashda xatolik: {error.message}
        </p>
      )}

      <div className="mt-4 overflow-x-auto rounded-2xl bg-white shadow-card">
        <table className="w-full min-w-[640px] text-left text-sm">
          <thead>
            <tr className="border-b border-cream-200 text-xs font-bold uppercase text-ink-500">
              <th className="px-4 py-3">Ism</th>
              <th className="px-4 py-3">Telefon</th>
              <th className="px-4 py-3">Rol</th>
              <th className="px-4 py-3">Ro&apos;yxatdan o&apos;tgan</th>
              <th className="px-4 py-3">Holat</th>
              <th className="px-4 py-3" />
            </tr>
          </thead>
          <tbody>
            {(users ?? []).map((u) => (
              <tr key={u.id} className="border-b border-cream-100 last:border-0">
                <td className="px-4 py-3 font-semibold text-ink-900">
                  {u.full_name ?? "—"}
                </td>
                <td className="px-4 py-3 text-ink-700">{u.phone ?? "—"}</td>
                <td className="px-4 py-3 text-ink-700">{u.role}</td>
                <td className="px-4 py-3 text-ink-700">
                  {u.created_at ? formatDate(u.created_at) : "—"}
                </td>
                <td className="px-4 py-3">
                  {u.is_blocked ? (
                    <span className="rounded-full bg-red-50 px-2.5 py-1 text-xs font-bold text-red-700">
                      Bloklangan
                    </span>
                  ) : (
                    <span className="rounded-full bg-sage-500/10 px-2.5 py-1 text-xs font-bold text-sage-500">
                      Faol
                    </span>
                  )}
                </td>
                <td className="px-4 py-3">
                  <BlockUserButton userId={u.id} isBlocked={u.is_blocked} />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {users && users.length === 0 && (
          <p className="px-4 py-6 text-center text-sm text-ink-500">
            Foydalanuvchi topilmadi
          </p>
        )}
      </div>
    </div>
  );
}

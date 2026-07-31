import Link from "next/link";
import type { Metadata } from "next";
import AppHeader from "@/components/AppHeader";
import Avatar from "@/components/Avatar";
import EmptyState from "@/components/EmptyState";
import PriceTag from "@/components/PriceTag";
import StatusChip from "@/components/StatusChip";
import OrderCardActions from "@/components/OrderCardActions";
import { PhPackage } from "@/components/PhosphorIcons";
import { createClient } from "@/lib/supabase/server";
import { formatDate, formatOrderNumber } from "@/lib/format";
import { t } from "@/lib/i18n";

export const metadata: Metadata = { title: "Buyurtmalarim" };

const ACTIVE_STATUSES = ["pending", "accepted", "in_progress", "ready"] as const;
const FINISHED_STATUSES = ["completed", "cancelled"] as const;

export default async function OrdersPage({
  searchParams,
}: {
  searchParams: Promise<{ tab?: string }>;
}) {
  const { tab } = await searchParams;
  const finished = tab === "finished";
  const supabase = await createClient();

  const {
    data: { user },
  } = await supabase.auth.getUser();
  if (!user) return null;

  const { data: orders } = await supabase
    .from("orders")
    .select(
      `id, status, payment_status, total_price, estimated_ready_at, created_at, source,
       usta_id, client_id,
       usta_profiles!inner(profiles!inner(full_name, avatar_url)),
       order_items(title)`
    )
    .or(`client_id.eq.${user.id},usta_id.eq.${user.id}`)
    .in("status", finished ? [...FINISHED_STATUSES] : [...ACTIVE_STATUSES])
    .order("created_at", { ascending: false });

  const orderList = orders ?? [];

  // Usta o'z buyurtmalarini ko'rganda kartochkada MIJOZning ismi turishi
  // kerak — o'z ismi emas. Usta profili so'rovda allaqachon bor, mijoz
  // profillari alohida tortiladi.
  const clientIds = Array.from(
    new Set(
      orderList
        .filter((o) => o.usta_id === user.id && o.client_id !== user.id)
        .map((o) => o.client_id)
    )
  );

  const clientMap: Record<
    string,
    { full_name: string; avatar_url: string | null }
  > = {};
  if (clientIds.length > 0) {
    const { data: clients } = await supabase
      .from("profiles")
      .select("id, full_name, avatar_url")
      .in("id", clientIds);
    for (const c of clients ?? []) {
      clientMap[c.id] = { full_name: c.full_name, avatar_url: c.avatar_url };
    }
  }

  return (
    <>
      <AppHeader />
      <main className="mx-auto max-w-5xl px-4 pt-4 pb-6 md:px-6">
        <h1 className="text-xl font-extrabold text-ink-900">
          {t("orders.title")}
        </h1>

        {/* Holat bo'yicha filtr — ikki tab keng ekranda cho'zilib ketmasin */}
        <div className="mt-3 flex gap-2 rounded-2xl bg-cream-200 p-1 sm:max-w-sm">
          {[
            { href: "/orders", label: t("orders.active"), active: !finished },
            {
              href: "/orders?tab=finished",
              label: t("orders.finished"),
              active: finished,
            },
          ].map((tabItem) => (
            <Link
              key={tabItem.href}
              href={tabItem.href}
              className={`flex-1 rounded-xl py-2 text-center text-sm font-bold transition-colors ${
                tabItem.active
                  ? "bg-white text-terra-700 shadow-card"
                  : "text-ink-500 hover:text-ink-700"
              }`}
            >
              {tabItem.label}
            </Link>
          ))}
        </div>

        {/* Bo'sh holat kartasi grid ichida cho'zilib ketmasligi uchun grid
            faqat buyurtmalar bo'lganda qo'llanadi. Buyurtma kartasi usta
            kartasidan zichroq, shuning uchun ko'pi bilan ikki ustun. */}
        <div
          className={
            orderList.length > 0
              ? "mt-4 grid items-start gap-3 md:grid-cols-2 md:gap-4"
              : "mt-4"
          }
        >
          {orderList.length > 0 ? (
            orderList.map((order) => {
              const isUsta = user.id === order.usta_id;
              const counterparty = isUsta
                ? clientMap[order.client_id]
                : order.usta_profiles.profiles;
              const name = counterparty?.full_name ?? "—";
              const avatarUrl = counterparty?.avatar_url ?? null;

              return (
                <div key={order.id}>
                  <Link
                    href={`/orders/${order.id}`}
                    className="block rounded-2xl bg-white p-4 shadow-card transition-transform hover:-translate-y-0.5"
                  >
                    <div className="flex items-center justify-between gap-2">
                      <span className="text-xs font-semibold text-ink-500">
                        {t("orders.orderNumber")}
                        {formatOrderNumber(order.id)}
                      </span>
                      <StatusChip status={order.status} />
                    </div>
                    <div className="mt-3 flex items-center gap-3">
                      <Avatar name={name} src={avatarUrl} size="md" />
                      <div className="min-w-0 flex-1">
                        <p className="truncate font-bold text-ink-900">
                          {order.order_items[0]?.title ?? "—"}
                        </p>
                        <p className="text-xs text-ink-500">
                          {isUsta ? `${t("orders.client")}: ${name}` : name}
                        </p>
                      </div>
                      <PriceTag amount={order.total_price} size="sm" />
                    </div>
                    {order.estimated_ready_at && (
                      <p className="mt-2 text-xs text-ink-500">
                        {t("orders.estimatedReady", {
                          date: formatDate(order.estimated_ready_at),
                        })}
                      </p>
                    )}
                  </Link>
                  {order.status !== "cancelled" &&
                    order.status !== "completed" && (
                      <div className="mt-2 px-4">
                        <OrderCardActions
                          orderId={order.id}
                          status={order.status}
                          isUsta={isUsta}
                        />
                      </div>
                    )}
                </div>
              );
            })
          ) : (
            <EmptyState
              icon={<PhPackage size={30} />}
              title={t("orders.empty")}
              hint={t("orders.emptyHint")}
              actionLabel={t("orders.goHome")}
              actionHref="/"
            />
          )}
        </div>
      </main>
    </>
  );
}

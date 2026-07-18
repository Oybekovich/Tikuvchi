import Link from "next/link";
import { notFound } from "next/navigation";
import type { Metadata } from "next";
import AppHeader from "@/components/AppHeader";
import Avatar from "@/components/Avatar";
import OrderActions from "@/components/OrderActions";
import OrderStatusStepper from "@/components/OrderStatusStepper";
import PriceTag from "@/components/PriceTag";
import StatusChip from "@/components/StatusChip";
import { IconChat, IconChevronRight } from "@/components/Icons";
import { createClient } from "@/lib/supabase/server";
import { formatDate, formatOrderNumber } from "@/lib/format";
import { t } from "@/lib/i18n";

export const metadata: Metadata = { title: "Buyurtma holati" };

export default async function OrderDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const supabase = await createClient();

  const { data: order } = await supabase
    .from("orders")
    .select(
      `id, status, payment_status, source, total_price, estimated_ready_at, created_at, usta_id,
       usta_profiles!inner(district, profiles!inner(full_name, avatar_url)),
       order_items(id, title, material, image_url, size_note, model_note, price)`
    )
    .eq("id", id)
    .maybeSingle();

  if (!order) notFound();

  const ustaProfile = order.usta_profiles.profiles;

  return (
    <>
      <AppHeader back backHref="/orders" />
      <main className="mx-auto max-w-3xl space-y-4 px-4 pt-4 pb-6">
        <div className="flex items-center justify-between gap-2">
          <div>
            <h1 className="text-xl font-extrabold text-ink-900">
              {t("orders.detailsTitle")}
            </h1>
            <p className="mt-0.5 text-xs font-semibold text-ink-500">
              {t("orders.orderNumber")}
              {formatOrderNumber(order.id)} ·{" "}
              {t(`orders.source_${order.source}`)}
            </p>
          </div>
          <StatusChip status={order.status} />
        </div>

        {/* Holat stepper */}
        <section className="rounded-2xl bg-white px-4 py-5 shadow-card">
          <OrderStatusStepper status={order.status} />
          {order.estimated_ready_at && (
            <p className="mt-4 text-center text-sm font-semibold text-ink-500">
              {t("orders.estimatedReady", {
                date: formatDate(order.estimated_ready_at),
              })}
            </p>
          )}
        </section>

        {/* Usta kartasi */}
        <div className="flex items-center gap-3 rounded-2xl bg-white p-4 shadow-card">
          <Link
            href={`/usta/${order.usta_id}`}
            className="flex min-w-0 flex-1 items-center gap-3"
          >
            <Avatar
              name={ustaProfile.full_name}
              src={ustaProfile.avatar_url}
              size="lg"
            />
            <div className="min-w-0 flex-1">
              <p className="font-bold text-ink-900">{ustaProfile.full_name}</p>
              {order.usta_profiles.district && (
                <p className="text-xs text-ink-500">
                  {order.usta_profiles.district}
                </p>
              )}
            </div>
            <IconChevronRight size={18} className="text-ink-300" />
          </Link>
          <Link
            href={`/chat/${order.usta_id}`}
            aria-label={t("usta.chatCta")}
            className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-terra-50 text-terra-700 hover:bg-terra-100"
          >
            <IconChat size={19} />
          </Link>
        </div>

        {/* Buyurtma tarkibi */}
        <section className="rounded-2xl bg-white p-4 shadow-card">
          <h2 className="text-sm font-extrabold text-ink-900">
            {t("orders.composition")}
          </h2>
          <div className="mt-3 space-y-3">
            {order.order_items.map((item) => (
              <div key={item.id} className="flex gap-3">
                {item.image_url && (
                  // eslint-disable-next-line @next/next/no-img-element
                  <img
                    src={item.image_url}
                    alt={item.title}
                    className="h-20 w-16 shrink-0 rounded-xl object-cover"
                  />
                )}
                <div className="min-w-0 flex-1">
                  <p className="font-bold text-ink-900">{item.title}</p>
                  {item.material && (
                    <p className="mt-0.5 text-xs text-ink-500">
                      {t("orderFlow.material")}: {item.material}
                    </p>
                  )}
                  {item.size_note && (
                    <p className="text-xs text-ink-500">
                      {t("orderFlow.measurement")}: {item.size_note}
                    </p>
                  )}
                  {item.model_note && (
                    <p className="text-xs text-ink-500">{item.model_note}</p>
                  )}
                </div>
                <PriceTag amount={item.price} size="sm" />
              </div>
            ))}
          </div>
          <div className="mt-4 flex items-center justify-between border-t border-cream-200 pt-3">
            <span className="font-bold text-ink-900">{t("orders.total")}</span>
            <PriceTag amount={order.total_price} size="lg" />
          </div>
          <p className="mt-2 text-xs text-ink-500">
            {t("orders.createdAt", { date: formatDate(order.created_at) })}
          </p>
        </section>

        {/* To'lov holati va amallar */}
        <OrderActions
          orderId={order.id}
          status={order.status}
          paymentStatus={order.payment_status}
          totalPrice={order.total_price}
        />
      </main>
    </>
  );
}

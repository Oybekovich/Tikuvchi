import Link from "next/link";
import type { Metadata } from "next";
import AppHeader from "@/components/AppHeader";
import Avatar from "@/components/Avatar";
import EmptyState from "@/components/EmptyState";
import { createClient } from "@/lib/supabase/server";
import { formatChatTime } from "@/lib/format";
import { t } from "@/lib/i18n";

export const metadata: Metadata = { title: "Suhbatlar" };

export default async function ChatListPage() {
  const supabase = await createClient();

  const {
    data: { user },
  } = await supabase.auth.getUser();

  const { data: conversations } = await supabase
    .from("conversations")
    .select(
      `id, usta_id, last_message_at,
       usta_profiles!inner(profiles!inner(full_name, avatar_url)),
       messages(content, message_type, created_at)`
    )
    .eq("client_id", user!.id)
    .order("last_message_at", { ascending: false })
    .order("created_at", { referencedTable: "messages", ascending: false })
    .limit(1, { referencedTable: "messages" });

  function preview(message?: {
    content: string | null;
    message_type: string;
  }): string {
    if (!message) return "";
    if (message.message_type === "price_offer") return `💰 ${t("chat.priceOffer")}`;
    if (message.message_type === "image") return `🖼️ ${t("chat.photo")}`;
    return message.content ?? "";
  }

  return (
    <>
      <AppHeader />
      <main className="mx-auto max-w-3xl px-4 pt-4 pb-6">
        <h1 className="text-xl font-extrabold text-ink-900">{t("chat.title")}</h1>

        <div className="mt-4 space-y-2">
          {conversations && conversations.length > 0 ? (
            conversations.map((conv) => {
              const profile = conv.usta_profiles.profiles;
              const last = conv.messages[0];
              return (
                <Link
                  key={conv.id}
                  href={`/chat/${conv.usta_id}`}
                  className="flex items-center gap-3 rounded-2xl bg-white p-4 shadow-card transition-transform hover:-translate-y-0.5"
                >
                  <Avatar
                    name={profile.full_name}
                    src={profile.avatar_url}
                    size="lg"
                  />
                  <div className="min-w-0 flex-1">
                    <div className="flex items-center justify-between gap-2">
                      <p className="truncate font-bold text-ink-900">
                        {profile.full_name}
                      </p>
                      <span className="shrink-0 text-xs text-ink-500">
                        {formatChatTime(conv.last_message_at)}
                      </span>
                    </div>
                    <p className="mt-0.5 truncate text-sm text-ink-500">
                      {preview(last)}
                    </p>
                  </div>
                </Link>
              );
            })
          ) : (
            <EmptyState
              icon="💬"
              title={t("chat.empty")}
              hint={t("chat.emptyHint")}
              actionLabel={t("orders.goHome")}
              actionHref="/"
            />
          )}
        </div>
      </main>
    </>
  );
}

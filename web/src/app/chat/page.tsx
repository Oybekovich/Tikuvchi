import type { Metadata } from "next";
import AppHeader from "@/components/AppHeader";
import ChatListClient from "@/components/ChatListClient";
import EmptyState from "@/components/EmptyState";
import { PhChat } from "@/components/PhosphorIcons";
import { createClient } from "@/lib/supabase/server";
import { t } from "@/lib/i18n";

export const metadata: Metadata = { title: "Suhbatlar" };

type ConvRaw = {
  id: string;
  client_id: string;
  usta_id: string;
  last_message_at: string;
  messages: { content: string | null; message_type: string; created_at: string }[];
};

export default async function ChatListPage() {
  const supabase = await createClient();
  const { data: { user } } = await supabase.auth.getUser();
  if (!user) return null;

  const { data: conversations } = await supabase
    .from("conversations")
    .select(
      `id, client_id, usta_id, last_message_at,
       messages(content, message_type, created_at)`
    )
    .or(`client_id.eq.${user.id},usta_id.eq.${user.id}`)
    .order("last_message_at", { ascending: false })
    .order("created_at", { referencedTable: "messages", ascending: false })
    .limit(1, { referencedTable: "messages" });

  const convList = (conversations ?? []) as ConvRaw[];

  let profileMap: Record<string, { id: string; full_name: string; avatar_url: string | null }> = {};
  if (convList.length > 0) {
    const otherIds = convList.map((c) =>
      c.client_id === user.id ? c.usta_id : c.client_id
    );
    const { data: profiles } = await supabase
      .from("profiles")
      .select("id, full_name, avatar_url")
      .in("id", otherIds);
    if (profiles) {
      for (const p of profiles) {
        profileMap[p.id] = p;
      }
    }
  }

  return (
    <>
      <AppHeader />
      <main className="mx-auto max-w-3xl px-4 pt-4 pb-6">
        <h1 className="text-xl font-extrabold text-ink-900">{t("chat.title")}</h1>

        {convList.length > 0 ? (
          <ChatListClient
            conversations={convList}
            profileMap={profileMap}
            userId={user.id}
          />
        ) : (
          <EmptyState
            icon={<PhChat size={30} />}
            title={t("chat.empty")}
            hint={t("chat.emptyHint")}
            actionLabel={t("orders.goHome")}
            actionHref="/"
          />
        )}
      </main>
    </>
  );
}

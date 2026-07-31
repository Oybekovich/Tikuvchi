"use client";

import Link from "next/link";
import Avatar from "@/components/Avatar";
import { PhImage, PhTag } from "@/components/PhosphorIcons";
import { formatChatTime } from "@/lib/format";
import { t } from "@/lib/i18n";
import { useUnreadChat } from "@/hooks/useUnreadChat";

import type { ChatProfile, ConversationRow } from "@/lib/chat";

export default function ChatListClient({
  conversations,
  profileMap,
  userId,
  activePeerId,
  variant = "page",
}: {
  conversations: ConversationRow[];
  profileMap: Record<string, ChatProfile>;
  userId: string;
  /** Ikki panelli ko'rinishda ochiq turgan suhbat ajratib ko'rsatiladi */
  activePeerId?: string;
  /** `page` — alohida sahifa; `pane` — suhbat oynasi yonidagi yon panel */
  variant?: "page" | "pane";
}) {
  const { isUnread } = useUnreadChat();

  function otherProfile(conv: ConversationRow): ChatProfile | undefined {
    const otherId = conv.client_id === userId ? conv.usta_id : conv.client_id;
    return profileMap[otherId];
  }

  function preview(message?: {
    content: string | null;
    message_type: string;
  }) {
    if (!message) return null;
    if (message.message_type === "price_offer") {
      return (
        <>
          <PhTag size={14} className="shrink-0" />
          <span className="truncate">{t("chat.priceOffer")}</span>
        </>
      );
    }
    if (message.message_type === "image") {
      return (
        <>
          <PhImage size={14} className="shrink-0" />
          <span className="truncate">{t("chat.photo")}</span>
        </>
      );
    }
    return <span className="truncate">{message.content ?? ""}</span>;
  }

  return (
    <div className={variant === "pane" ? "space-y-2 p-3" : "mt-4 space-y-2"}>
      {conversations.map((conv) => {
        const profile = otherProfile(conv);
        const last = conv.messages[0];
        const otherId = conv.client_id === userId ? conv.usta_id : conv.client_id;
        const unread = isUnread(conv.id);
        const active = otherId === activePeerId;
        return (
          <Link
            key={conv.id}
            href={`/chat/${otherId}`}
            aria-current={active ? "page" : undefined}
            className={`flex items-center gap-3 rounded-2xl p-4 shadow-card transition-transform hover:-translate-y-0.5 ${
              active ? "bg-terra-50 ring-2 ring-terra-300" : "bg-white"
            }`}
          >
            <Avatar
              name={profile?.full_name ?? ""}
              src={profile?.avatar_url ?? null}
              size="lg"
            />
            <div className="min-w-0 flex-1">
              <div className="flex items-center justify-between gap-2">
                <p
                  className={`truncate ${
                    unread ? "font-extrabold text-ink-900" : "font-semibold text-ink-800"
                  }`}
                >
                  {profile?.full_name ?? ""}
                </p>
                <span className="shrink-0 text-xs text-ink-500">
                  {formatChatTime(conv.last_message_at)}
                </span>
              </div>
              <p
                className={`mt-0.5 flex items-center gap-1 text-sm ${
                  unread ? "font-bold text-ink-700" : "text-ink-500"
                }`}
              >
                {preview(last)}
              </p>
            </div>
          </Link>
        );
      })}
    </div>
  );
}

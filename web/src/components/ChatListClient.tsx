"use client";

import Link from "next/link";
import Avatar from "@/components/Avatar";
import { PhImage, PhTag } from "@/components/PhosphorIcons";
import { formatChatTime } from "@/lib/format";
import { t } from "@/lib/i18n";
import { useUnreadChat } from "@/hooks/useUnreadChat";

type ConvRaw = {
  id: string;
  client_id: string;
  usta_id: string;
  last_message_at: string;
  messages: { content: string | null; message_type: string; created_at: string }[];
};

type Profile = { id: string; full_name: string; avatar_url: string | null };

export default function ChatListClient({
  conversations,
  profileMap,
  userId,
}: {
  conversations: ConvRaw[];
  profileMap: Record<string, Profile>;
  userId: string;
}) {
  const { isUnread } = useUnreadChat();

  function otherProfile(conv: ConvRaw): Profile | undefined {
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
    <div className="mt-4 space-y-2">
      {conversations.map((conv) => {
        const profile = otherProfile(conv);
        const last = conv.messages[0];
        const otherId = conv.client_id === userId ? conv.usta_id : conv.client_id;
        const unread = isUnread(conv.id);
        return (
          <Link
            key={conv.id}
            href={`/chat/${otherId}`}
            className="flex items-center gap-3 rounded-2xl bg-white p-4 shadow-card transition-transform hover:-translate-y-0.5"
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

import type { Metadata } from "next";
import AppHeader from "@/components/AppHeader";
import ChatListClient from "@/components/ChatListClient";
import EmptyState from "@/components/EmptyState";
import { PhChat } from "@/components/PhosphorIcons";
import { createClient } from "@/lib/supabase/server";
import { getConversations } from "@/lib/chat";
import { t } from "@/lib/i18n";

export const metadata: Metadata = { title: "Suhbatlar" };

export default async function ChatListPage() {
  const supabase = await createClient();
  const { data: { user } } = await supabase.auth.getUser();
  if (!user) return null;

  const { conversations, profileMap } = await getConversations(supabase, user.id);

  return (
    <>
      <AppHeader />
      {/* `lg` da suhbat sahifasi bilan bir xil ikki panelli ramka: chapda
          ro'yxat, o'ngda — hali suhbat tanlanmagani uchun — taklif matni.
          Shu tufayli ro'yxatdan suhbatga o'tganda maket sakramaydi.
          Bu yerda qat'iy balandlik yo'q: sahifa odatdagidek aylanadi
          (pastda mobil panel uchun bo'shliq ham bor). */}
      <div className="lg:flex lg:items-stretch">
        <div className="lg:w-96 lg:shrink-0 lg:border-e lg:border-cream-200">
          <div className="mx-auto max-w-3xl px-4 pt-4 pb-6 md:px-6 lg:px-4">
            <h1 className="text-xl font-extrabold text-ink-900">
              {t("chat.title")}
            </h1>

            {conversations.length > 0 ? (
              <ChatListClient
                conversations={conversations}
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
          </div>
        </div>

        <div className="hidden lg:flex lg:flex-1 lg:items-center lg:justify-center lg:p-8">
          <EmptyState
            icon={<PhChat size={30} />}
            title={t("chat.selectConversation")}
            hint={t("chat.selectConversationHint")}
          />
        </div>
      </div>
    </>
  );
}

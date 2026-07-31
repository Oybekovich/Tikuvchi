"use client";

import { usePathname } from "next/navigation";
import type { ReactNode } from "react";
import BottomNav from "@/components/BottomNav";
import { UnreadChatProvider } from "@/hooks/useUnreadChat";

/**
 * Pastki tab-navigatsiya qaysi sahifalarda yashirilishini boshqaradi:
 *  - buyurtma yaratish oqimi (to'liq ekranli, chalg'itmaslik uchun)
 *  - auth sahifalari
 *  - chat oynasi (pastda xabar yozish paneli turadi)
 */
export default function AppShell({ children }: { children: ReactNode }) {
  const pathname = usePathname();

  const hideNav =
    /^\/usta\/[^/]+\/buyurtma/.test(pathname) ||
    pathname.startsWith("/auth/") ||
    /^\/chat\/[^/]+/.test(pathname) ||
    pathname === "/offline";

  return (
    <UnreadChatProvider>
      {/* Panel kontent ustida suzadi, shuning uchun pastda ko'proq bo'shliq
          kerak — aks holda oxirgi element panel ostida qolib ketadi.
          `md` dan boshlab panel yashiriladi (navigatsiya header'ga ko'chadi),
          shuning uchun bu bo'shliq ham kichrayadi. */}
      <div className={hideNav ? "min-h-dvh" : "min-h-dvh pb-24 md:pb-10"}>
        {children}
      </div>
      {!hideNav && <BottomNav />}
    </UnreadChatProvider>
  );
}

"use client";

import { usePathname } from "next/navigation";
import type { ReactNode } from "react";
import BottomNav from "@/components/BottomNav";

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
    <>
      <div className={hideNav ? "min-h-dvh" : "min-h-dvh pb-20"}>{children}</div>
      {!hideNav && <BottomNav />}
    </>
  );
}

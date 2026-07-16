"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  IconChat,
  IconHome,
  IconOrders,
  IconRuler,
  IconUser,
} from "@/components/Icons";
import { t } from "@/lib/i18n";

const TABS = [
  { href: "/", label: "nav.home", icon: IconHome },
  { href: "/orders", label: "nav.orders", icon: IconOrders },
  { href: "/measurements", label: "nav.measurements", icon: IconRuler },
  { href: "/chat", label: "nav.chat", icon: IconChat },
  { href: "/profile", label: "nav.profile", icon: IconUser },
] as const;

/**
 * Butun ilova bo'ylab yagona pastki navigatsiya — 5 ta tab, faqat o'zbekcha.
 * Fokuslangan oqimlarda (buyurtma yaratish, auth, chat oynasi) AppShell uni yashiradi.
 */
export default function BottomNav() {
  const pathname = usePathname();

  function isActive(href: string): boolean {
    if (href === "/") return pathname === "/";
    return pathname === href || pathname.startsWith(href + "/");
  }

  return (
    <nav
      aria-label={t("menu.title")}
      className="fixed inset-x-0 bottom-0 z-40 border-t border-cream-200 bg-cream-50/95 backdrop-blur shadow-nav"
    >
      <div className="mx-auto grid max-w-3xl grid-cols-5 pb-safe">
        {TABS.map(({ href, label, icon: Icon }) => {
          const active = isActive(href);
          return (
            <Link
              key={href}
              href={href}
              aria-current={active ? "page" : undefined}
              className={`flex flex-col items-center gap-0.5 py-2 text-[11px] font-semibold transition-colors ${
                active ? "text-terra-600" : "text-ink-500 hover:text-ink-700"
              }`}
            >
              <Icon size={22} strokeWidth={active ? 2.4 : 1.9} />
              {t(label)}
            </Link>
          );
        })}
      </div>
    </nav>
  );
}

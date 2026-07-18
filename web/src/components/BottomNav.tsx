"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import type { ComponentType, SVGProps } from "react";
import {
  PhChat,
  PhHome,
  PhOrders,
  PhRuler,
  PhUser,
} from "@/components/PhosphorIcons";
import { t } from "@/lib/i18n";

type Tab = {
  href: string;
  label: string;
  icon: ComponentType<SVGProps<SVGSVGElement> & { size?: number }>;
};

/**
 * Bosh sahifa markazda, doira ichida — qolgan to'rttasi ikki chetda faqat
 * ikonka bo'lib turadi va tanlanganda kengayib yozuvi chiqadi. Shu tufayli
 * besh tabga tor ekranda ham joy yetadi.
 *
 * Tartib Android ilova bilan bir xil: foydalanuvchi ikkalasini ham
 * ishlatganda tugmalar o'rni almashib turmasligi kerak.
 */
const LEFT: Tab[] = [
  { href: "/orders", label: "nav.orders", icon: PhOrders },
  { href: "/measurements", label: "nav.measurements", icon: PhRuler },
];

const RIGHT: Tab[] = [
  { href: "/chat", label: "nav.chat", icon: PhChat },
  { href: "/profile", label: "nav.profile", icon: PhUser },
];

export default function BottomNav() {
  const pathname = usePathname();

  function isActive(href: string): boolean {
    if (href === "/") return pathname === "/";
    return pathname === href || pathname.startsWith(href + "/");
  }

  const homeActive = isActive("/");

  return (
    <nav
      aria-label={t("menu.title")}
      // Panel kontent ustida suzadi; nav butun kenglikni egallagani uchun
      // bosishlar faqat qutining o'ziga tushishi kerak
      className="pointer-events-none fixed inset-x-0 bottom-0 z-40 pb-safe"
    >
      <div className="mx-auto max-w-md px-4 pb-3">
        <div className="pointer-events-auto flex items-center justify-evenly rounded-[28px] bg-white p-2 shadow-float">
          {LEFT.map((tab) => (
            <SideTab key={tab.href} tab={tab} active={isActive(tab.href)} />
          ))}

          <Link
            href="/"
            aria-label={t("nav.home")}
            aria-current={homeActive ? "page" : undefined}
            // O'lcham emas, masshtab: transform qayta joylashtirishga sabab
            // bo'lmaydi, shuning uchun qo'shni tugmalar joyida qoladi
            className={`flex h-[52px] w-[52px] shrink-0 items-center justify-center rounded-full bg-terra-600 text-white shadow-[0_3px_8px_rgba(88,45,32,0.25)] transition-transform duration-200 ease-out ${
              homeActive ? "scale-100" : "scale-[0.92]"
            }`}
          >
            <PhHome size={24} />
          </Link>

          {RIGHT.map((tab) => (
            <SideTab key={tab.href} tab={tab} active={isActive(tab.href)} />
          ))}
        </div>
      </div>
    </nav>
  );
}

/** Chetdagi tab: tanlanmagan bo'lsa faqat ikonka, tanlanganda yozuvi kengayadi. */
function SideTab({ tab, active }: { tab: Tab; active: boolean }) {
  const Icon = tab.icon;
  const label = t(tab.label);

  return (
    <Link
      href={tab.href}
      // Yozuv yashiringanda ham ekran o'quvchisi tabni ayta olishi kerak
      aria-label={label}
      aria-current={active ? "page" : undefined}
      className={`flex items-center rounded-2xl py-2 transition-all duration-200 ease-out ${
        active ? "bg-terra-50 px-3 text-terra-600" : "px-2.5 text-ink-500"
      }`}
    >
      <Icon size={22} className="shrink-0" />
      {/* Fon, ichki bo'shliq va matn kengligi bir vaqtda harakatlanadi —
          aks holda yozuv sakrab chiqqandek ko'rinadi */}
      <span
        aria-hidden
        className={`overflow-hidden whitespace-nowrap text-[11px] font-bold leading-none transition-all duration-200 ease-out ${
          active ? "ms-1.5 max-w-28 opacity-100" : "ms-0 max-w-0 opacity-0"
        }`}
      >
        {label}
      </span>
    </Link>
  );
}

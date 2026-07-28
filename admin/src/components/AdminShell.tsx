"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useState, type ReactNode } from "react";
import LogoutButton from "@/components/LogoutButton";

const NAV_ITEMS = [
  { href: "/", label: "Umumiy" },
  { href: "/users", label: "Foydalanuvchilar" },
  { href: "/ustas", label: "Ustalar" },
  { href: "/orders", label: "Buyurtmalar" },
] as const;

export default function AdminShell({ children }: { children: ReactNode }) {
  const pathname = usePathname();
  const [menuOpen, setMenuOpen] = useState(false);

  const bareChrome = pathname === "/login" || pathname === "/not-authorized";
  if (bareChrome) return <>{children}</>;

  function isActive(href: string): boolean {
    if (href === "/") return pathname === "/";
    return pathname.startsWith(href);
  }

  return (
    <div className="flex min-h-dvh flex-col md:flex-row">
      {/* Mobil top bar */}
      <header className="flex items-center justify-between border-b border-cream-200 bg-white px-4 py-3 md:hidden">
        <span className="text-lg font-extrabold text-ink-900">Tikuvchi Admin</span>
        <button
          onClick={() => setMenuOpen((v) => !v)}
          aria-label="Menyu"
          className="rounded-lg px-3 py-2 text-sm font-bold text-ink-700 hover:bg-cream-200"
        >
          {menuOpen ? "Yopish" : "Menyu"}
        </button>
      </header>

      {/* Sidebar: desktopda doim ko'rinadi, mobilda ochilib-yopiladi */}
      <nav
        className={`w-full shrink-0 border-b border-cream-200 bg-white px-3 py-4 md:block md:w-60 md:border-b-0 md:border-r md:py-6 ${
          menuOpen ? "block" : "hidden"
        }`}
      >
        <div className="hidden px-2 pb-6 text-lg font-extrabold text-ink-900 md:block">
          Tikuvchi Admin
        </div>
        <div className="space-y-1">
          {NAV_ITEMS.map((item) => (
            <Link
              key={item.href}
              href={item.href}
              onClick={() => setMenuOpen(false)}
              className={`block rounded-xl px-3 py-2.5 text-sm font-semibold ${
                isActive(item.href)
                  ? "bg-terra-50 text-terra-700"
                  : "text-ink-700 hover:bg-cream-200"
              }`}
            >
              {item.label}
            </Link>
          ))}
        </div>
        <div className="mt-6 border-t border-cream-200 pt-4">
          <LogoutButton />
        </div>
      </nav>

      <main className="min-w-0 flex-1 px-4 py-6 md:px-8">{children}</main>
    </div>
  );
}

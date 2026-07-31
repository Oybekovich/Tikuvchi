"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useState } from "react";
import {
  IconBack,
  IconChat,
  IconClose,
  IconHome,
  IconLogout,
  IconMenu,
  IconOrders,
  IconRuler,
  IconScissors,
  IconSearch,
  IconUser,
} from "@/components/Icons";
import { signOut } from "@/lib/auth";
import { useUser } from "@/lib/useUser";
import { isActivePath } from "@/lib/nav";
import { t } from "@/lib/i18n";
import { useUnreadChat } from "@/hooks/useUnreadChat";

type Props = {
  /** Ichki sahifalarda orqaga strelka ko'rsatiladi */
  back?: boolean;
  /** Tarix bo'lmasa qaytish manzili */
  backHref?: string;
  /** Markazda logo o'rniga sahifa sarlavhasi (masalan, chat oynasida usta ismi) */
  title?: string;
};

function Logo() {
  return (
    <span className="inline-flex items-center gap-1.5 text-lg font-extrabold tracking-tight text-ink-900">
      <IconScissors size={19} className="text-terra-600" />
      {t("app.name")}
    </span>
  );
}

const MENU_ITEMS = [
  { href: "/", label: "nav.home", icon: IconHome },
  { href: "/search", label: "menu.search", icon: IconSearch },
  { href: "/orders", label: "nav.orders", icon: IconOrders },
  { href: "/measurements", label: "nav.measurements", icon: IconRuler },
  { href: "/chat", label: "nav.chat", icon: IconChat },
  { href: "/profile", label: "nav.profile", icon: IconUser },
] as const;

/**
 * Desktop menyusida "Profil" yo'q — u o'ngdagi ikonkada turadi va ikki marta
 * ko'rsatish joyni behuda egallardi.
 */
const DESKTOP_NAV = MENU_ITEMS.filter((item) => item.href !== "/profile");

/**
 * Yagona umumiy header.
 *
 *  - `< md`: chapda hamburger (bosh sahifalar) yoki orqaga strelka (ichki
 *    sahifalar), markazda logo yoki sahifa sarlavhasi, o'ngda profil.
 *  - `>= md`: chapda logo, markazda gorizontal menyu, o'ngda profil. Pastki
 *    suzuvchi panel bu kenglikda yashiriladi, shuning uchun navigatsiya shu
 *    yerga ko'chadi va hamburger kerak bo'lmaydi.
 */
export default function AppHeader({ back = false, backHref = "/", title }: Props) {
  const router = useRouter();
  const pathname = usePathname();
  const [menuOpen, setMenuOpen] = useState(false);
  const { user } = useUser();
  const { unreadCount } = useUnreadChat();

  function goBack() {
    if (typeof window !== "undefined" && window.history.length > 1) {
      router.back();
    } else {
      router.push(backHref);
    }
  }

  async function logout() {
    await signOut();
    setMenuOpen(false);
    router.push("/auth/login");
    router.refresh();
  }

  return (
    <>
      <header className="sticky top-0 z-40 border-b border-cream-200 bg-cream-50/95 backdrop-blur">
        {/* Ro'yxatli sahifalar kontenti bilan bir xil kenglik (`max-w-5xl`) —
            aks holda keng ekranda logo va profil ikonkasi kontentdan
            ichkarida qolib, tekislanmagandek ko'rinardi. */}
        <div className="mx-auto flex h-14 max-w-5xl items-center justify-between px-3 md:px-6">
          {/* Chap tomon: orqaga/hamburger + desktopdagi logo */}
          <div className="flex items-center gap-1 md:gap-3">
            {back ? (
              <button
                onClick={goBack}
                aria-label={t("common.back")}
                className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full text-ink-900 hover:bg-cream-200 active:bg-cream-300"
              >
                <IconBack />
              </button>
            ) : (
              <button
                onClick={() => setMenuOpen(true)}
                aria-label={t("menu.title")}
                className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full text-ink-900 hover:bg-cream-200 active:bg-cream-300 md:hidden"
              >
                <IconMenu />
              </button>
            )}
            <Link
              href="/"
              aria-label={t("app.name")}
              className="hidden md:inline-flex"
            >
              <Logo />
            </Link>
          </div>

          {/* Markaz — faqat telefonda: logo yoki sahifa sarlavhasi */}
          {title ? (
            <span className="max-w-[60%] truncate text-base font-bold text-ink-900 md:hidden">
              {title}
            </span>
          ) : (
            <Link href="/" aria-label={t("app.name")} className="md:hidden">
              <Logo />
            </Link>
          )}

          {/* Desktop navigatsiyasi */}
          <nav
            aria-label={t("menu.title")}
            className="hidden md:flex md:items-center md:gap-0.5 lg:gap-1"
          >
            {DESKTOP_NAV.map(({ href, label }) => {
              const active = isActivePath(pathname, href);
              return (
                <Link
                  key={href}
                  href={href}
                  aria-current={active ? "page" : undefined}
                  className={`relative rounded-xl px-2.5 py-2 text-sm font-bold transition-colors lg:px-3 ${
                    active
                      ? "bg-terra-50 text-terra-700"
                      : "text-ink-500 hover:bg-cream-200 hover:text-ink-900"
                  }`}
                >
                  {t(label)}
                  {href === "/chat" && unreadCount > 0 && (
                    <span
                      aria-hidden
                      className="absolute right-1 top-1 h-2 w-2 rounded-full bg-red-500"
                    />
                  )}
                </Link>
              );
            })}
          </nav>

          <Link
            href={user ? "/profile" : "/auth/login"}
            aria-label={t("nav.profile")}
            aria-current={isActivePath(pathname, "/profile") ? "page" : undefined}
            className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-full hover:bg-cream-200 active:bg-cream-300 ${
              isActivePath(pathname, "/profile")
                ? "bg-terra-50 text-terra-700"
                : "text-ink-900"
            }`}
          >
            <IconUser />
          </Link>
        </div>
      </header>

      {/* Yon menyu (drawer) — faqat telefonda. `md:hidden` kerak: drawer ochiq
          turganda oyna kengaytirilsa, u desktop menyusi ustida osilib
          qolmasligi uchun. */}
      {menuOpen && (
        <div
          className="fixed inset-0 z-50 md:hidden"
          role="dialog"
          aria-modal="true"
        >
          <div
            className="absolute inset-0 bg-ink-900/40"
            onClick={() => setMenuOpen(false)}
          />
          <div className="absolute left-0 top-0 flex h-full w-72 max-w-[80vw] flex-col bg-cream-50 shadow-2xl">
            <div className="flex h-14 items-center justify-between border-b border-cream-200 px-4">
              <Logo />
              <button
                onClick={() => setMenuOpen(false)}
                aria-label={t("common.close")}
                className="flex h-10 w-10 items-center justify-center rounded-full hover:bg-cream-200"
              >
                <IconClose />
              </button>
            </div>
            <nav className="flex-1 overflow-y-auto p-3">
              {MENU_ITEMS.map(({ href, label, icon: Icon }) => (
                <Link
                  key={href}
                  href={href}
                  onClick={() => setMenuOpen(false)}
                  className="flex items-center gap-3 rounded-xl px-3 py-3 font-semibold text-ink-700 hover:bg-cream-200"
                >
                  <Icon size={20} className="text-terra-600" />
                  {t(label)}
                </Link>
              ))}
            </nav>
            <div className="border-t border-cream-200 p-3 pb-safe">
              {user ? (
                <button
                  onClick={logout}
                  className="flex w-full items-center gap-3 rounded-xl px-3 py-3 font-semibold text-red-700 hover:bg-red-50"
                >
                  <IconLogout size={20} />
                  {t("menu.logout")}
                </button>
              ) : (
                <Link
                  href="/auth/login"
                  onClick={() => setMenuOpen(false)}
                  className="flex items-center gap-3 rounded-xl px-3 py-3 font-semibold text-terra-700 hover:bg-terra-50"
                >
                  <IconUser size={20} />
                  {t("menu.login")}
                </Link>
              )}
            </div>
          </div>
        </div>
      )}
    </>
  );
}

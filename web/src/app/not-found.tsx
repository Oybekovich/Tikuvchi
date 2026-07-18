import Link from "next/link";
import AppHeader from "@/components/AppHeader";
import EmptyState from "@/components/EmptyState";
import {
  PhSearchOff,
} from "@/components/PhosphorIcons";
import { t } from "@/lib/i18n";

export default function NotFound() {
  return (
    <>
      <AppHeader back backHref="/" />
      <main className="mx-auto max-w-3xl px-4 pt-8">
        <EmptyState
          icon={<PhSearchOff size={30} />}
          title={t("common.notFound")}
          actionLabel={t("nav.home")}
          actionHref="/"
        />
        <p className="sr-only">
          <Link href="/">{t("nav.home")}</Link>
        </p>
      </main>
    </>
  );
}

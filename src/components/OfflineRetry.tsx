"use client";

import Button from "@/components/Button";
import { t } from "@/lib/i18n";

export default function OfflineRetry() {
  return (
    <Button onClick={() => window.location.reload()} className="px-8">
      {t("offline.retry")}
    </Button>
  );
}

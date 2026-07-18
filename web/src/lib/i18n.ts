import uz from "@/locales/uz.json";

// Hozircha bitta til (uz). Yangi til qo'shish uchun /locales/<til>.json
// yaratib, shu yerda tanlash mexanizmini kengaytirish kifoya.
const dictionaries = { uz } as const;
const locale: keyof typeof dictionaries = "uz";

type Dict = Record<string, unknown>;

/**
 * Tarjima matnini oladi: t("orders.title"), t("search.found", { count: 5 })
 */
export function t(
  key: string,
  vars?: Record<string, string | number>
): string {
  const parts = key.split(".");
  let node: unknown = dictionaries[locale];
  for (const part of parts) {
    if (typeof node !== "object" || node === null) break;
    node = (node as Dict)[part];
  }
  if (typeof node !== "string") return key;
  if (!vars) return node;
  return node.replace(/\{(\w+)\}/g, (_, name) =>
    name in vars ? String(vars[name]) : `{${name}}`
  );
}

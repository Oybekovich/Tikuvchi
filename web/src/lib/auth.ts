import { createClient } from "@/lib/supabase/client";

/**
 * Hisobdan chiqish. Sessiya bilan birga service worker'ning sahifa keshi ham
 * tozalanadi — aks holda offline holatda kirmagan foydalanuvchiga
 * oldingi sessiyadan qolgan sahifalar ko'rinib qolardi.
 */
export async function signOut(): Promise<void> {
  const supabase = createClient();
  await supabase.auth.signOut();

  if (typeof caches !== "undefined") {
    try {
      const names = await caches.keys();
      await Promise.all(
        names
          .filter((n) => n.startsWith("tikuvchi-pages"))
          .map((n) => caches.delete(n))
      );
    } catch {
      // Kesh tozalanmasa ham chiqish davom etadi
    }
  }
}

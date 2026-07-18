"use client";

import { useEffect } from "react";

/** Service worker'ni ro'yxatdan o'tkazadi (faqat production'da) */
export default function SWRegister() {
  useEffect(() => {
    if (
      process.env.NODE_ENV === "production" &&
      "serviceWorker" in navigator
    ) {
      navigator.serviceWorker.register("/sw.js").catch(() => {
        // SW ro'yxatdan o'tmasa ham ilova oddiy sayt sifatida ishlayveradi
      });
    }
  }, []);

  return null;
}

import type { Metadata, Viewport } from "next";
import { Manrope } from "next/font/google";
import "./globals.css";
import AppShell from "@/components/AppShell";
import SWRegister from "@/components/SWRegister";
import { t } from "@/lib/i18n";

const manrope = Manrope({
  variable: "--font-manrope",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: {
    default: `${t("app.name")} — ${t("app.tagline")}`,
    template: `%s | ${t("app.name")}`,
  },
  description: t("app.tagline"),
  applicationName: t("app.name"),
  manifest: "/manifest.webmanifest",
  appleWebApp: {
    capable: true,
    statusBarStyle: "default",
    title: t("app.name"),
  },
  icons: {
    icon: "/icons/icon-192.png",
    apple: "/icons/icon-192.png",
  },
};

export const viewport: Viewport = {
  themeColor: "#a9533a",
  width: "device-width",
  initialScale: 1,
  viewportFit: "cover",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="uz" className={`${manrope.variable} antialiased`}>
      <body>
        <SWRegister />
        <AppShell>{children}</AppShell>
      </body>
    </html>
  );
}

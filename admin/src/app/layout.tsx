import type { Metadata } from "next";
import AdminShell from "@/components/AdminShell";
import "./globals.css";

export const metadata: Metadata = {
  title: "Tikuvchi Admin",
  description: "Tikuvchi platformasini boshqarish paneli",
};

export default function RootLayout({
  children,
}: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="uz">
      <body>
        <AdminShell>{children}</AdminShell>
      </body>
    </html>
  );
}

"use client";

import type { ButtonHTMLAttributes, ReactNode } from "react";

type Props = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: "primary" | "outline" | "ghost" | "danger";
  size?: "md" | "lg";
  loading?: boolean;
  children: ReactNode;
};

const VARIANTS = {
  primary:
    "bg-terra-600 text-white hover:bg-terra-700 active:bg-terra-800 disabled:bg-terra-300",
  outline:
    "border-2 border-terra-600 text-terra-700 hover:bg-terra-50 active:bg-terra-100 disabled:opacity-50",
  ghost: "text-terra-700 hover:bg-terra-50 active:bg-terra-100 disabled:opacity-50",
  danger:
    "bg-red-50 text-red-700 border border-red-200 hover:bg-red-100 active:bg-red-200 disabled:opacity-50",
} as const;

const SIZES = {
  md: "px-4 py-2.5 text-sm",
  lg: "w-full px-5 py-3.5 text-base",
} as const;

export function Spinner({ className = "h-4 w-4" }: { className?: string }) {
  return (
    <svg
      className={`animate-spin ${className}`}
      viewBox="0 0 24 24"
      fill="none"
      aria-hidden
    >
      <circle
        className="opacity-25"
        cx="12"
        cy="12"
        r="10"
        stroke="currentColor"
        strokeWidth="4"
      />
      <path
        className="opacity-90"
        fill="currentColor"
        d="M4 12a8 8 0 0 1 8-8v4a4 4 0 0 0-4 4H4z"
      />
    </svg>
  );
}

export default function Button({
  variant = "primary",
  size = "md",
  loading = false,
  disabled,
  children,
  className = "",
  ...rest
}: Props) {
  return (
    <button
      disabled={disabled || loading}
      className={`inline-flex items-center justify-center gap-2 rounded-xl font-bold transition-colors ${VARIANTS[variant]} ${SIZES[size]} ${className}`}
      {...rest}
    >
      {loading && <Spinner />}
      {children}
    </button>
  );
}

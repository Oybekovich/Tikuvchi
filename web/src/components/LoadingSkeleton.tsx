type Props = {
  variant?: "list" | "detail" | "chat";
};

/** Sahifa yuklanayotganda ko'rsatiladigan skeletlar */
export default function LoadingSkeleton({ variant = "list" }: Props) {
  if (variant === "detail") {
    return (
      <div className="space-y-4 p-4" aria-busy="true">
        <div className="skeleton h-48 rounded-2xl" />
        <div className="skeleton h-6 w-2/3 rounded-lg" />
        <div className="skeleton h-4 w-1/2 rounded-lg" />
        <div className="skeleton h-24 rounded-2xl" />
        <div className="skeleton h-24 rounded-2xl" />
      </div>
    );
  }

  if (variant === "chat") {
    return (
      <div className="space-y-3 p-4" aria-busy="true">
        <div className="skeleton ml-auto h-10 w-1/2 rounded-2xl" />
        <div className="skeleton h-10 w-2/3 rounded-2xl" />
        <div className="skeleton ml-auto h-10 w-2/5 rounded-2xl" />
        <div className="skeleton h-32 w-2/3 rounded-2xl" />
      </div>
    );
  }

  return (
    <div className="space-y-3 p-4" aria-busy="true">
      {[1, 2, 3, 4].map((i) => (
        <div key={i} className="rounded-2xl bg-white p-4 shadow-card">
          <div className="flex items-center gap-3">
            <div className="skeleton h-14 w-14 rounded-full" />
            <div className="flex-1 space-y-2">
              <div className="skeleton h-4 w-1/2 rounded-lg" />
              <div className="skeleton h-3 w-1/3 rounded-lg" />
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}

import LoadingSkeleton from "@/components/LoadingSkeleton";

export default function Loading() {
  return (
    <div className="mx-auto max-w-3xl">
      <LoadingSkeleton variant="list" />
    </div>
  );
}

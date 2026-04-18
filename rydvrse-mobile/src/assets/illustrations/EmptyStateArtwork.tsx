import React from "react";

export type EmptyStateArtworkVariant = "booking" | "jobs" | "support";

type EmptyStateArtworkProps = {
  variant?: EmptyStateArtworkVariant;
  width?: number;
  height?: number;
};

/**
 * Intentionally empty. The product team decided to drop placeholder
 * illustrations from every empty state — copy-only empty states read
 * cleaner and stay on-brand without the off-brand blue artwork.
 */
export function EmptyStateArtwork(_props: EmptyStateArtworkProps) {
  return null;
}

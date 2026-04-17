export type CustomerServiceType =
  | "SCHEDULED_LOCAL"
  | "ONE_WAY_DROP"
  | "ROUND_TRIP"
  | "AIRPORT"
  | "LATE_NIGHT_SAFE_RETURN";

export const serviceTypeOptions: Array<{
  id: CustomerServiceType;
  title: string;
  subtitle: string;
}> = [
  {
    id: "ONE_WAY_DROP",
    title: "One-way trip",
    subtitle: "Point A to B with transparent fare."
  },
  {
    id: "ROUND_TRIP",
    title: "Round trip",
    subtitle: "Same driver for return journey."
  }
];

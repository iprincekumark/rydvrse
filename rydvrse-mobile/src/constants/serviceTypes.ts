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
    id: "SCHEDULED_LOCAL",
    title: "Scheduled Local",
    subtitle: "Time-based driver booking for errands, meetings, and city travel."
  },
  {
    id: "ONE_WAY_DROP",
    title: "One-Way Drop",
    subtitle: "A direct driver service with clear return allowance built in."
  },
  {
    id: "ROUND_TRIP",
    title: "Round Trip",
    subtitle: "Keep the same driver with you across multiple stops."
  },
  {
    id: "AIRPORT",
    title: "Airport",
    subtitle: "Zone-based flat fares for dependable airport transfers."
  },
  {
    id: "LATE_NIGHT_SAFE_RETURN",
    title: "Late-Night Return",
    subtitle: "Trust-first booking for safe return rides after hours."
  }
];

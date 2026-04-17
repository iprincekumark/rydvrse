export type ActorRole = "CUSTOMER" | "DRIVER";

export type ApiEnvelope<T> = {
  data: T;
  meta: {
    request_id: string;
    timestamp: string;
    api_version: string;
  };
};

export type SessionPayload = {
  actor_type: ActorRole;
  access_token: string;
  refresh_token: string;
  expires_at: string;
  user: {
    id: string;
    full_name: string;
    mobile_number: string;
    status: string;
  };
};

export type QuotePayload = {
  quote_id: string;
  expires_at: string;
  service_type: string;
  commercial_model?: string;
  pricing_assumptions?: {
    rounded_distance_km?: number;
    predicted_drive_minutes?: number;
    included_distance_km?: number;
    included_minutes?: number;
    driver_pickup_distance_km?: number;
    driver_pickup_eta_minutes?: number;
    pickup_arrival_sla_minutes?: number;
    transmission_type?: string;
    car_type?: string;
    estimate_quality?: string;
  };
  fare_summary: {
    amount_paise: number;
    currency: "INR";
  };
  fare_components: Array<{
    code: string;
    label: string;
    amount_paise: number;
    is_tax?: boolean;
  }>;
  driver_payout_preview?: {
    total_payout_paise: number;
    components: Array<{
      code: string;
      label: string;
      amount_paise: number;
    }>;
  };
  savings_summary?: {
    reference_total_paise?: number;
    estimated_savings_paise?: number;
    message?: string;
  };
  assignment_note: string;
  cancellation_summary: string;
};

export type BookingPayload = {
  booking_id: string;
  service_type: string;
  status: string;
  schedule_at: string;
  pickup_label: string;
  drop_label?: string;
  fare_amount_paise: number;
  driver?: {
    name: string;
    rating: number;
    language: string;
    eta_minutes: number;
    verification_badge: string;
  };
};

export type SupportTicketPayload = {
  ticket_id: string;
  status: string;
  category: string;
  description: string;
  created_at: string;
};

export type DriverOfferPayload = {
  assignment_id: string;
  service_type: string;
  pickup_zone: string;
  scheduled_at: string;
  expected_duration_minutes: number;
  estimated_earning_paise: number;
  offer_expires_at: string;
};

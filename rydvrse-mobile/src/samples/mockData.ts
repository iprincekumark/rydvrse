import { BookingPayload, DriverOfferPayload, QuotePayload } from "@/services/api/types";

export const mockQuote: QuotePayload = {
  quote_id: "quote-001",
  expires_at: new Date(Date.now() + 1000 * 60 * 12).toISOString(),
  service_type: "AIRPORT",
  fare_summary: {
    amount_paise: 54900,
    currency: "INR"
  },
  fare_components: [
    { code: "base", label: "Base booking charge", amount_paise: 29900 },
    { code: "service", label: "Airport service charge", amount_paise: 18000 },
    { code: "tax", label: "Taxes", amount_paise: 7000 }
  ],
  assignment_note: "Best price for rides booked 3+ hours in advance.",
  cancellation_summary: "Free cancellation until 60 minutes before pickup."
};

export const mockBookings: BookingPayload[] = [
  {
    booking_id: "booking-upcoming-001",
    service_type: "AIRPORT",
    status: "DRIVER_ASSIGNED",
    schedule_at: new Date(Date.now() + 1000 * 60 * 85).toISOString(),
    pickup_label: "Koramangala 4th Block",
    drop_label: "Kempegowda International Airport",
    fare_amount_paise: 54900,
    driver: {
      name: "Arun K",
      rating: 4.9,
      language: "English, Kannada",
      eta_minutes: 17,
      verification_badge: "Verified + Trained"
    }
  },
  {
    booking_id: "booking-past-001",
    service_type: "SCHEDULED_LOCAL",
    status: "COMPLETED",
    schedule_at: new Date(Date.now() - 1000 * 60 * 60 * 24).toISOString(),
    pickup_label: "Indiranagar",
    drop_label: "Lavelle Road",
    fare_amount_paise: 39900
  }
];

export const mockDriverOffers: DriverOfferPayload[] = [
  {
    assignment_id: "assignment-001",
    service_type: "AIRPORT",
    pickup_zone: "Koramangala",
    scheduled_at: new Date(Date.now() + 1000 * 60 * 25).toISOString(),
    expected_duration_minutes: 75,
    estimated_earning_paise: 29000,
    offer_expires_at: new Date(Date.now() + 1000 * 20).toISOString()
  }
];

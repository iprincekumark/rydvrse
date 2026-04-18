import { BookingPayload, DriverOfferPayload, QuotePayload } from "@/services/api/types";

export const mockQuote: QuotePayload = {
  quote_id: "quote-001",
  expires_at: new Date(Date.now() + 1000 * 60 * 12).toISOString(),
  service_type: "SCHEDULED_ONE_WAY",
  commercial_model: "BLR_HYBRID_ONE_WAY_V1",
  pricing_assumptions: {
    rounded_distance_km: 31,
    predicted_drive_minutes: 105,
    included_distance_km: 20,
    included_minutes: 99,
    driver_pickup_distance_km: 8,
    driver_pickup_eta_minutes: 24,
    pickup_arrival_sla_minutes: 30,
    transmission_type: "AUTOMATIC",
    car_type: "SEDAN",
    estimate_quality: "CLIENT_OR_MAP_ESTIMATE"
  },
  fare_summary: {
    amount_paise: 59059,
    currency: "INR"
  },
  fare_components: [
    { code: "BLR_ONE_WAY_BASE", label: "Base fare: first 20 km + 75 min", amount_paise: 29900 },
    { code: "BLR_DISTANCE_20_35", label: "Distance fee: 11 km x Rs. 6.50", amount_paise: 7150 },
    { code: "BLR_TRAFFIC_TIME", label: "Traffic time buffer", amount_paise: 1000 },
    { code: "BLR_PICKUP_ACCESS", label: "Driver pickup access", amount_paise: 4900 },
    { code: "BLR_ONE_WAY_RELOCATION", label: "One-way relocation allowance", amount_paise: 5900 },
    { code: "RYD_SECURE", label: "Rydvrse Secure", amount_paise: 1200 },
    { code: "GST", label: "Tax", amount_paise: 9009, is_tax: true }
  ],
  driver_payout_preview: {
    total_payout_paise: 36920,
    components: [
      { code: "DRIVER_BASE", label: "Driver base payout", amount_paise: 21000 },
      { code: "DISTANCE_PAYOUT", label: "Distance payout", amount_paise: 4400 },
      { code: "TIME_PAYOUT", label: "Traffic-time payout", amount_paise: 720 },
      { code: "PICKUP_ACCESS_PAYOUT", label: "Pickup access pass-through", amount_paise: 4900 },
      { code: "RELOCATION_PAYOUT", label: "One-way relocation pass-through", amount_paise: 5900 }
    ]
  },
  savings_summary: {
    reference_total_paise: 66400,
    estimated_savings_paise: 7350,
    message: "Estimated lower than the reference one-way model for a 31 km Bengaluru daytime trip."
  },
  assignment_note: "Built for Bengaluru: distance, traffic time, pickup access, and driver relocation are visible before booking.",
  cancellation_summary: "Free cancellation until 60 minutes before pickup."
};

export const mockBookings: BookingPayload[] = [
  {
    booking_id: "booking-upcoming-001",
    service_type: "SCHEDULED_ONE_WAY",
    status: "DRIVER_ASSIGNED",
    schedule_at: new Date(Date.now() + 1000 * 60 * 85).toISOString(),
    pickup_label: "Koramangala 4th Block",
    drop_label: "Whitefield Main Road",
    fare_amount_paise: 59059,
    pickup_otp: "4821",
    driver: {
      name: "Arun K",
      rating: 4.9,
      language: "English, Kannada",
      eta_minutes: 17,
      verification_badge: "Verified + Trained",
      distance_km: 3.2,
      vehicle_model: "Maruti Dzire - White",
      vehicle_plate: "KA 05 AB 1234"
    }
  },
  {
    booking_id: "booking-past-001",
    service_type: "SCHEDULED_ROUND_TRIP",
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
    service_type: "SCHEDULED_ROUND_TRIP",
    pickup_zone: "Koramangala",
    scheduled_at: new Date(Date.now() + 1000 * 60 * 25).toISOString(),
    expected_duration_minutes: 75,
    estimated_earning_paise: 29000,
    offer_expires_at: new Date(Date.now() + 1000 * 20).toISOString()
  }
];

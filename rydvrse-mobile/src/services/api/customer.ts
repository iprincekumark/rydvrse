import { env } from "@/constants/env";
import { mockBookings, mockQuote } from "@/samples/mockData";
import { ApiEnvelope, BookingPayload, QuotePayload, SupportTicketPayload } from "@/services/api/types";
import { apiClient } from "@/services/api/client";

const meta = {
  request_id: "mock-request",
  timestamp: new Date().toISOString(),
  api_version: "v1"
};

export const customerApi = {
  async profile(token: string): Promise<ApiEnvelope<{ full_name: string; email: string; city_name: string }>> {
    if (env.useMocks) {
      return apiClient.simulate(() => ({
        data: {
          full_name: "Meera Singh",
          email: "meera@rydvrse.app",
          city_name: "Bengaluru"
        },
        meta
      }));
    }

    return apiClient.request("/customers/me", { token });
  },

  async updateProfile(token: string, body: { full_name: string; email?: string; city_name?: string }): Promise<ApiEnvelope<{ full_name: string; email?: string; city_name?: string }>> {
    if (env.useMocks) {
      return apiClient.simulate(() => ({
        data: body,
        meta
      }));
    }

    return apiClient.request("/customers/me", {
      method: "PATCH",
      token,
      body
    });
  },

  async home(token: string): Promise<ApiEnvelope<{ city_name: string; upcoming_bookings: BookingPayload[]; active_trip: unknown }>> {
    if (env.useMocks) {
      return apiClient.simulate(() => ({
        data: {
          city_name: "Bengaluru",
          upcoming_bookings: mockBookings.filter((booking) => booking.status !== "COMPLETED"),
          active_trip: null
        },
        meta
      }));
    }

    return apiClient.request("/customers/me/home", { token });
  },

  async quote(token: string, body: Record<string, unknown>) {
    if (env.useMocks) {
      return apiClient.simulate<ApiEnvelope<QuotePayload>>(() => ({
        data: mockQuote,
        meta
      }));
    }

    return apiClient.request<ApiEnvelope<QuotePayload>>("/quotes", {
      method: "POST",
      token,
      body,
      headers: {
        "Idempotency-Key": "quote-request-mobile"
      }
    });
  },

  async createBooking(token: string, body: Record<string, unknown>) {
    if (env.useMocks) {
      return apiClient.simulate<ApiEnvelope<BookingPayload>>(() => ({
        data: mockBookings[0],
        meta
      }));
    }

    return apiClient.request<ApiEnvelope<BookingPayload>>("/bookings", {
      method: "POST",
      token,
      body,
      headers: {
        "Idempotency-Key": "create-booking-mobile"
      }
    });
  },

  async listBookings(token: string) {
    if (env.useMocks) {
      return apiClient.simulate(() => ({
        data: mockBookings,
        meta
      }));
    }

    return apiClient.request<ApiEnvelope<BookingPayload[]>>("/bookings", { token });
  },

  async bookingDetail(token: string, bookingId: string) {
    if (env.useMocks) {
      return apiClient.simulate(() => ({
        data: mockBookings.find((booking) => booking.booking_id === bookingId) ?? mockBookings[0],
        meta
      }));
    }

    return apiClient.request<ApiEnvelope<BookingPayload>>(`/bookings/${bookingId}`, { token });
  },

  async confirmTripStart(token: string, bookingId: string) {
    if (env.useMocks) {
      return apiClient.simulate(() => ({
        data: {
          booking_id: bookingId,
          trip_status: "IN_PROGRESS"
        },
        meta
      }));
    }

    return apiClient.request(`/bookings/${bookingId}/start-confirmation`, {
      method: "POST",
      token,
      headers: {
        "Idempotency-Key": "start-confirmation-mobile"
      }
    });
  },

  async createPaymentOrder(token: string, bookingId: string): Promise<ApiEnvelope<{ payment_id: string; booking_id: string; amount_paise: number; status: string }>> {
    if (env.useMocks) {
      return apiClient.simulate(() => ({
        data: {
          payment_id: "payment-001",
          booking_id: bookingId,
          amount_paise: mockBookings[0].fare_amount_paise,
          status: "PENDING"
        },
        meta
      }));
    }

    return apiClient.request(`/bookings/${bookingId}/payment-orders`, {
      method: "POST",
      token,
      headers: {
        "Idempotency-Key": "payment-order-mobile"
      }
    });
  },

  async invoice(token: string, bookingId: string): Promise<ApiEnvelope<{ invoice_id: string; booking_id: string; amount_paise: number; line_items: QuotePayload["fare_components"] }>> {
    if (env.useMocks) {
      return apiClient.simulate(() => ({
        data: {
          invoice_id: "invoice-001",
          booking_id: bookingId,
          amount_paise: mockBookings[0].fare_amount_paise,
          line_items: mockQuote.fare_components
        },
        meta
      }));
    }

    return apiClient.request(`/bookings/${bookingId}/invoice`, { token });
  },

  async submitRating(token: string, bookingId: string, body: Record<string, unknown>): Promise<ApiEnvelope<{ booking_id: string; submitted: boolean }>> {
    if (env.useMocks) {
      return apiClient.simulate(() => ({
        data: {
          booking_id: bookingId,
          submitted: true
        },
        meta
      }));
    }

    return apiClient.request(`/bookings/${bookingId}/ratings`, {
      method: "POST",
      token,
      body,
      headers: {
        "Idempotency-Key": "rating-mobile"
      }
    });
  },

  async createSupportTicket(token: string, body: Record<string, unknown>) {
    if (env.useMocks) {
      return apiClient.simulate<ApiEnvelope<SupportTicketPayload>>(() => ({
        data: {
          ticket_id: "support-001",
          status: "OPEN",
          category: String(body.category ?? "Other"),
          description: String(body.description ?? ""),
          created_at: new Date().toISOString()
        },
        meta
      }));
    }

    return apiClient.request<ApiEnvelope<SupportTicketPayload>>("/support/tickets", {
      method: "POST",
      token,
      body,
      headers: {
        "Idempotency-Key": "support-mobile"
      }
    });
  }
};

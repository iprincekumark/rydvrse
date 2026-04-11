import { env } from "@/constants/env";
import { mockDriverOffers } from "@/samples/mockData";
import { ApiEnvelope, DriverOfferPayload, SupportTicketPayload } from "@/services/api/types";
import { apiClient } from "@/services/api/client";

const meta = {
  request_id: "mock-request",
  timestamp: new Date().toISOString(),
  api_version: "v1"
};

export const driverApi = {
  async dashboard(token: string): Promise<ApiEnvelope<{ earnings_today_paise: number; availability: string; upcoming_jobs: DriverOfferPayload[] }>> {
    if (env.useMocks) {
      return apiClient.simulate(() => ({
        data: {
          earnings_today_paise: 124000,
          availability: "AVAILABLE",
          upcoming_jobs: mockDriverOffers
        },
        meta
      }));
    }

    return apiClient.request("/drivers/me/dashboard", { token });
  },

  async onboardingStatus(token: string): Promise<ApiEnvelope<{ status: string; reason: string | null }>> {
    if (env.useMocks) {
      return apiClient.simulate(() => ({
        data: {
          status: "APPROVED",
          reason: null
        },
        meta
      }));
    }

    return apiClient.request("/drivers/onboarding/status", { token });
  },

  async submitOnboarding(token: string, body: Record<string, unknown>): Promise<ApiEnvelope<{ status: string; submitted: boolean; payload: Record<string, unknown> }>> {
    if (env.useMocks) {
      return apiClient.simulate(() => ({
        data: {
          status: "UNDER_REVIEW",
          submitted: true,
          payload: body
        },
        meta
      }));
    }

    return apiClient.request("/drivers/onboarding", {
      method: "PUT",
      token,
      body
    });
  },

  async updateAvailability(token: string, status: "AVAILABLE" | "UNAVAILABLE"): Promise<ApiEnvelope<{ availability: string }>> {
    if (env.useMocks) {
      return apiClient.simulate(() => ({
        data: {
          availability: status
        },
        meta
      }));
    }

    return apiClient.request("/drivers/me/availability", {
      method: "PATCH",
      token,
      body: { availability_status: status }
    });
  },

  async offers(token: string) {
    if (env.useMocks) {
      return apiClient.simulate<ApiEnvelope<DriverOfferPayload[]>>(() => ({
        data: mockDriverOffers,
        meta
      }));
    }

    return apiClient.request<ApiEnvelope<DriverOfferPayload[]>>("/drivers/assignments/offers", { token });
  },

  async acceptOffer(token: string, assignmentId: string): Promise<ApiEnvelope<{ assignment_id: string; status: string }>> {
    if (env.useMocks) {
      return apiClient.simulate(() => ({
        data: {
          assignment_id: assignmentId,
          status: "ACCEPTED"
        },
        meta
      }));
    }

    return apiClient.request(`/drivers/assignments/${assignmentId}/accept`, {
      method: "POST",
      token,
      headers: {
        "Idempotency-Key": "driver-accept-mobile"
      }
    });
  },

  async markArrived(token: string, tripId: string): Promise<ApiEnvelope<{ trip_id: string; status: string }>> {
    if (env.useMocks) {
      return apiClient.simulate(() => ({
        data: {
          trip_id: tripId,
          status: "ARRIVED"
        },
        meta
      }));
    }

    return apiClient.request(`/drivers/trips/${tripId}/arrived`, {
      method: "POST",
      token,
      headers: {
        "Idempotency-Key": "driver-arrived-mobile"
      }
    });
  },

  async completeTrip(token: string, tripId: string): Promise<ApiEnvelope<{ trip_id: string; status: string }>> {
    if (env.useMocks) {
      return apiClient.simulate(() => ({
        data: {
          trip_id: tripId,
          status: "COMPLETED"
        },
        meta
      }));
    }

    return apiClient.request(`/drivers/trips/${tripId}/complete`, {
      method: "POST",
      token,
      headers: {
        "Idempotency-Key": "driver-complete-mobile"
      }
    });
  },

  async earningsLedger(token: string): Promise<ApiEnvelope<Array<{ trip_id: string; title: string; amount_paise: number; payout_status: string }>>> {
    if (env.useMocks) {
      return apiClient.simulate(() => ({
        data: [
          {
            trip_id: "trip-001",
            title: "Airport Drop",
            amount_paise: 29000,
            payout_status: "PENDING"
          },
          {
            trip_id: "trip-002",
            title: "Local Ride",
            amount_paise: 18000,
            payout_status: "SETTLED"
          }
        ],
        meta
      }));
    }

    return apiClient.request("/drivers/earnings/ledger", { token });
  },

  async createSupportTicket(token: string, body: Record<string, unknown>) {
    if (env.useMocks) {
      return apiClient.simulate<ApiEnvelope<SupportTicketPayload>>(() => ({
        data: {
          ticket_id: "driver-support-001",
          status: "OPEN",
          category: String(body.category ?? "Other"),
          description: String(body.description ?? ""),
          created_at: new Date().toISOString()
        },
        meta
      }));
    }

    return apiClient.request<ApiEnvelope<SupportTicketPayload>>("/drivers/support/tickets", {
      method: "POST",
      token,
      body,
      headers: {
        "Idempotency-Key": "driver-support-mobile"
      }
    });
  }
};

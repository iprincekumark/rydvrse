import { env } from "@/constants/env";
import { mockBookings } from "@/samples/mockData";
import { ActorRole, ApiEnvelope, SessionPayload } from "@/services/api/types";
import { apiClient } from "@/services/api/client";

const makeMockSession = (actor: ActorRole): ApiEnvelope<SessionPayload> => ({
  data: {
    actor_type: actor,
    access_token: `${actor.toLowerCase()}-access-token`,
    refresh_token: `${actor.toLowerCase()}-refresh-token`,
    expires_at: new Date(Date.now() + 1000 * 60 * 15).toISOString(),
    user: {
      id: `${actor.toLowerCase()}-001`,
      full_name: actor === "CUSTOMER" ? "Meera Singh" : "Ravi Kumar",
      mobile_number: "+919999999999",
      status: "ACTIVE"
    }
  },
  meta: {
    request_id: "mock-request",
    timestamp: new Date().toISOString(),
    api_version: "v1"
  }
});

export const authApi = {
  async requestOtp(actor: ActorRole, mobileNumber: string) {
    if (env.useMocks) {
      return apiClient.simulate(() => ({
        data: {
          challenge_id: `challenge-${mobileNumber}`,
          actor_type: actor,
          resend_after_seconds: 24
        },
        meta: {
          request_id: "mock-request",
          timestamp: new Date().toISOString(),
          api_version: "v1"
        }
      }));
    }

    return apiClient.request("/auth/otp/request", {
      method: "POST",
      body: {
        actor_type: actor,
        mobile_number: mobileNumber
      }
    });
  },

  async verifyOtp(actor: ActorRole, mobileNumber: string, otp: string) {
    if (env.useMocks) {
      return apiClient.simulate(() => makeMockSession(actor));
    }

    return apiClient.request<ApiEnvelope<SessionPayload>>("/auth/otp/verify", {
      method: "POST",
      body: {
        actor_type: actor,
        mobile_number: mobileNumber,
        otp
      }
    });
  },

  async refresh(token: string) {
    if (env.useMocks) {
      return apiClient.simulate(() => makeMockSession("CUSTOMER"));
    }

    return apiClient.request<ApiEnvelope<SessionPayload>>("/auth/refresh", {
      method: "POST",
      body: {
        refresh_token: token
      }
    });
  },

  async logout(token: string) {
    if (env.useMocks) {
      return apiClient.simulate(() => ({ ok: true }));
    }

    return apiClient.request("/auth/logout", {
      method: "POST",
      token
    });
  },

  upcomingBookingsPreview() {
    return mockBookings.filter((booking) => booking.status !== "COMPLETED");
  }
};

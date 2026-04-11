import { createSlice, PayloadAction } from "@reduxjs/toolkit";

import { ActorRole, SessionPayload } from "@/services/api/types";

type SessionState = {
  status: "idle" | "authenticated";
  activeRole: ActorRole | null;
  accessToken: string | null;
  refreshToken: string | null;
  userName: string | null;
  mobileNumber: string | null;
};

const initialState: SessionState = {
  status: "idle",
  activeRole: null,
  accessToken: null,
  refreshToken: null,
  userName: null,
  mobileNumber: null
};

const sessionSlice = createSlice({
  name: "session",
  initialState,
  reducers: {
    hydrateSession(state, action: PayloadAction<SessionPayload>) {
      state.status = "authenticated";
      state.activeRole = action.payload.actor_type;
      state.accessToken = action.payload.access_token;
      state.refreshToken = action.payload.refresh_token;
      state.userName = action.payload.user.full_name;
      state.mobileNumber = action.payload.user.mobile_number;
    },
    switchRole(state, action: PayloadAction<ActorRole>) {
      state.activeRole = action.payload;
    },
    logout(state) {
      Object.assign(state, initialState);
    }
  }
});

export const { hydrateSession, switchRole, logout } = sessionSlice.actions;
export const sessionReducer = sessionSlice.reducer;

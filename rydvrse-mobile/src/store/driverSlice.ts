import { createSlice, PayloadAction } from "@reduxjs/toolkit";

import { DriverOfferPayload } from "@/services/api/types";

type DriverState = {
  onboardingStatus: "NOT_STARTED" | "UNDER_REVIEW" | "APPROVED" | "CORRECTION_REQUIRED";
  availability: "AVAILABLE" | "UNAVAILABLE";
  offers: DriverOfferPayload[];
  activeAssignmentId: string | null;
};

const initialState: DriverState = {
  onboardingStatus: "APPROVED",
  availability: "AVAILABLE",
  offers: [],
  activeAssignmentId: null
};

const driverSlice = createSlice({
  name: "driver",
  initialState,
  reducers: {
    setOnboardingStatus(state, action: PayloadAction<DriverState["onboardingStatus"]>) {
      state.onboardingStatus = action.payload;
    },
    setAvailability(state, action: PayloadAction<DriverState["availability"]>) {
      state.availability = action.payload;
    },
    setOffers(state, action: PayloadAction<DriverOfferPayload[]>) {
      state.offers = action.payload;
    },
    setActiveAssignmentId(state, action: PayloadAction<string | null>) {
      state.activeAssignmentId = action.payload;
    }
  }
});

export const { setOnboardingStatus, setAvailability, setOffers, setActiveAssignmentId } = driverSlice.actions;
export const driverReducer = driverSlice.reducer;

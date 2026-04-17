import { createSlice, PayloadAction } from "@reduxjs/toolkit";

import { BookingPayload, QuotePayload } from "@/services/api/types";
import { CustomerServiceType } from "@/constants/serviceTypes";

type BookingForm = {
  serviceType: CustomerServiceType;
  pickup: string;
  drop: string;
  scheduleAt: string;
  durationLabel: string;
  distanceKm: string;
  predictedDriveMinutes: string;
  driverPickupDistanceKm: string;
  driverPickupEtaMinutes: string;
  transmissionType: string;
  carType: string;
  carBrandModel: string;
  carNumber: string;
  instructions: string;
};

type CustomerState = {
  profileComplete: boolean;
  bookingForm: BookingForm;
  currentQuote: QuotePayload | null;
  bookings: BookingPayload[];
  activeBookingId: string | null;
};

const initialState: CustomerState = {
  profileComplete: false,
  bookingForm: {
    serviceType: "ONE_WAY_DROP",
    pickup: "Koramangala 4th Block",
    drop: "Whitefield Main Road",
    scheduleAt: new Date(Date.now() + 1000 * 60 * 90).toISOString(),
    durationLabel: "105 mins",
    distanceKm: "31",
    predictedDriveMinutes: "105",
    driverPickupDistanceKm: "8",
    driverPickupEtaMinutes: "24",
    transmissionType: "AUTOMATIC",
    carType: "SEDAN",
    carBrandModel: "Hyundai Verna",
    carNumber: "KA03AB1234",
    instructions: "Pickup from Gate 2"
  },
  currentQuote: null,
  bookings: [],
  activeBookingId: null
};

const customerSlice = createSlice({
  name: "customer",
  initialState,
  reducers: {
    markProfileComplete(state) {
      state.profileComplete = true;
    },
    updateBookingForm(state, action: PayloadAction<Partial<BookingForm>>) {
      state.bookingForm = { ...state.bookingForm, ...action.payload };
      state.currentQuote = null;
    },
    setQuote(state, action: PayloadAction<QuotePayload | null>) {
      state.currentQuote = action.payload;
    },
    setBookings(state, action: PayloadAction<BookingPayload[]>) {
      state.bookings = action.payload;
    },
    setActiveBooking(state, action: PayloadAction<string | null>) {
      state.activeBookingId = action.payload;
    }
  }
});

export const { markProfileComplete, updateBookingForm, setQuote, setBookings, setActiveBooking } = customerSlice.actions;
export const customerReducer = customerSlice.reducer;

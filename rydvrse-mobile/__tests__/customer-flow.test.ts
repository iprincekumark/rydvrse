import { customerReducer, markProfileComplete, setActiveBooking, setBookings, setQuote } from "@/store/customerSlice";
import { mockBookings, mockQuote } from "@/samples/mockData";

describe("customer booking flow reducer", () => {
  it("tracks the quote, booking list, active booking, and profile completion state", () => {
    let state = customerReducer(undefined, { type: "init" });

    state = customerReducer(state, setQuote(mockQuote));
    state = customerReducer(state, setBookings(mockBookings));
    state = customerReducer(state, setActiveBooking(mockBookings[0].booking_id));
    state = customerReducer(state, markProfileComplete());

    expect(state.currentQuote?.quote_id).toBe("quote-001");
    expect(state.bookings).toHaveLength(2);
    expect(state.activeBookingId).toBe("booking-upcoming-001");
    expect(state.profileComplete).toBe(true);
  });
});

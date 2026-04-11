import { configureStore } from "@reduxjs/toolkit";
import { TypedUseSelectorHook, useDispatch, useSelector } from "react-redux";

import { customerReducer } from "@/store/customerSlice";
import { driverReducer } from "@/store/driverSlice";
import { sessionReducer } from "@/store/sessionSlice";

export const store = configureStore({
  reducer: {
    session: sessionReducer,
    customer: customerReducer,
    driver: driverReducer
  }
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

export const useAppDispatch = () => useDispatch<AppDispatch>();
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector;

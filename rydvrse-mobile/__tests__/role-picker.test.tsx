import React from "react";
import { fireEvent, render } from "@testing-library/react-native";
import { NavigationContainer } from "@react-navigation/native";
import { Provider } from "react-redux";

import { RolePickerScreen } from "@/screens/shared/RolePickerScreen";
import { store } from "@/store";

describe("RolePickerScreen", () => {
  it("switches the workspace into customer mode", () => {
    const { getByText } = render(
      <Provider store={store}>
        <NavigationContainer>
          <RolePickerScreen />
        </NavigationContainer>
      </Provider>
    );

    fireEvent.press(getByText("Customer app"));

    expect(store.getState().session.activeRole).toBe("CUSTOMER");
  });
});

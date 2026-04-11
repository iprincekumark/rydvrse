import React from "react";
import { fireEvent, render } from "@testing-library/react-native";

import { PrimaryButton } from "@/components/common/PrimaryButton";

describe("PrimaryButton", () => {
  it("fires the press handler when enabled", () => {
    const onPress = jest.fn();
    const { getByText } = render(<PrimaryButton label="Continue" onPress={onPress} />);

    fireEvent.press(getByText("Continue"));

    expect(onPress).toHaveBeenCalledTimes(1);
  });
});

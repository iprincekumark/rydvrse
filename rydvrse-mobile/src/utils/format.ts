export const formatCurrency = (amountPaise: number) => {
  const value = amountPaise / 100;
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0
  }).format(value);
};

export const formatCompactTime = (isoString?: string | null) => {
  if (!isoString) {
    return "--";
  }

  const date = new Date(isoString);
  return new Intl.DateTimeFormat("en-IN", {
    hour: "numeric",
    minute: "2-digit"
  }).format(date);
};

export const delay = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

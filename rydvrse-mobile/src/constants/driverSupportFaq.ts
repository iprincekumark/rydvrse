export type DriverSupportFaq = {
  id: string;
  question: string;
  category: string;
  answer: string;
  tags: string[];
};

/**
 * Driver-side FAQ catalog. Same shape as customer's SupportFaq so the chat
 * screen can render either. Keep tags lowercase for case-insensitive search.
 */
export const DRIVER_SUPPORT_FAQS: DriverSupportFaq[] = [
  {
    id: "dfaq-payout",
    question: "When will I receive my payout?",
    category: "Payouts",
    answer:
      "Daily trip earnings settle to your linked bank account by 11 AM the next working day. Any flagged rides pause only that amount and release once reviewed — usually within 24 hours.",
    tags: ["payout", "payment", "money", "earnings", "bank"],
  },
  {
    id: "dfaq-customer-unreachable",
    question: "Customer is not reachable at pickup.",
    category: "Pickup",
    answer:
      "Tap Pickup help → Unable to reach. We text the customer on your behalf. If there is still no response after 5 minutes you can cancel with full pickup fee protection.",
    tags: ["customer", "unreachable", "pickup", "noshow"],
  },
  {
    id: "dfaq-cancel-penalty",
    question: "Will I be penalised for cancelling?",
    category: "Trips",
    answer:
      "Cancellations count against your acceptance rate, but pickup-side issues (customer no-show, unsafe location, vehicle issue) do not. Tap the reason that matches — our system records it correctly.",
    tags: ["cancel", "penalty", "acceptance", "rating"],
  },
  {
    id: "dfaq-docs-expiry",
    question: "My documents are about to expire.",
    category: "Compliance",
    answer:
      "Upload a fresh copy from Profile → Documents at least 3 days before expiry. Your account stays active during the 3-day grace window so you can keep earning while we verify.",
    tags: ["document", "expire", "license", "rc", "compliance"],
  },
  {
    id: "dfaq-fare-mismatch",
    question: "Customer paid less than the shown fare.",
    category: "Payouts",
    answer:
      "Sometimes waiting time or toll is added after payment. Open the trip in Earnings → it shows the split. If the total is still off, tap Report fare issue and we will reconcile within 48 hours.",
    tags: ["fare", "payout", "mismatch", "less"],
  },
  {
    id: "dfaq-location",
    question: "Navigation is taking me the wrong way.",
    category: "Trips",
    answer:
      "We use live traffic from Ola Maps — the fastest route sometimes looks longer. If you choose a different route, the fare is still protected as long as you don't deviate more than 2 km from the suggested path.",
    tags: ["navigation", "route", "map", "direction"],
  },
  {
    id: "dfaq-rating",
    question: "How do I improve my rating?",
    category: "Profile",
    answer:
      "Clean cab, on-time arrival, and a polite greeting are the biggest levers. Ratings below 4.5 for 10+ trips trigger a coaching nudge but don't pause your account unless they keep dropping.",
    tags: ["rating", "stars", "profile", "coaching"],
  },
  {
    id: "dfaq-break",
    question: "Can I take a break without going offline?",
    category: "Availability",
    answer:
      "Yes — tap Break on the home screen. Your status shows as ON_BREAK for up to 45 minutes and you won't receive offers. Tap End break to resume — no penalty.",
    tags: ["break", "offline", "pause", "availability"],
  },
];

export function searchDriverFaqs(
  query: string,
  faqs: DriverSupportFaq[] = DRIVER_SUPPORT_FAQS,
): DriverSupportFaq[] {
  const normalised = query.trim().toLowerCase();
  if (!normalised) {
    return faqs;
  }
  const words = normalised.split(/\s+/).filter(Boolean);
  return faqs
    .map((faq) => {
      const haystack =
        `${faq.question} ${faq.category} ${faq.tags.join(" ")} ${faq.answer}`.toLowerCase();
      const hits = words.filter((word) => haystack.includes(word)).length;
      return { faq, hits };
    })
    .filter((entry) => entry.hits > 0)
    .sort((a, b) => b.hits - a.hits)
    .map((entry) => entry.faq);
}

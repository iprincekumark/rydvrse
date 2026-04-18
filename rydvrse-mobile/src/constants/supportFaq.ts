export type SupportFaq = {
  id: string;
  question: string;
  category: string;
  answer: string;
  tags: string[];
};

/**
 * Catalog of frequently asked questions shown on the Help screen. Keep
 * `tags` lowercase so free-form search can match against them case-
 * insensitively. Order matters — the first five are the default list; the
 * rest show up when the user taps "View more".
 */
export const SUPPORT_FAQS: SupportFaq[] = [
  {
    id: "faq-fare",
    question: "Why is my fare different from the estimate?",
    category: "Billing",
    answer:
      "Fare can shift slightly if the route changes during the trip (detours, waiting, toll roads). The fare breakdown you saw at booking remains visible under the trip detail along with the final adjustments.",
    tags: ["fare", "billing", "price", "estimate", "cost"],
  },
  {
    id: "faq-driver-delay",
    question: "My driver is running late — what can I do?",
    category: "Driver",
    answer:
      "Open the trip from Bookings → you will see live ETA. If the driver is more than 10 minutes late you can switch to a backup driver from the trip screen, at no charge.",
    tags: ["driver", "late", "delay", "eta", "wait"],
  },
  {
    id: "faq-cancel",
    question: "How do I cancel a booking?",
    category: "Bookings",
    answer:
      "Open Bookings → select the trip → tap Cancel. Cancellations are free until 10 minutes before pickup. After that a small fee covers the driver's travel.",
    tags: ["cancel", "refund", "booking"],
  },
  {
    id: "faq-payment",
    question: "Which payment methods work?",
    category: "Payments",
    answer:
      "UPI, credit and debit cards, and Rydvrse wallet. You can add or change a method in Profile → Payment methods.",
    tags: ["payment", "upi", "card", "wallet", "pay"],
  },
  {
    id: "faq-safety",
    question: "How do I share my trip with family?",
    category: "Safety",
    answer:
      "During a live trip tap Safety → Share trip. A link with the live location and driver details is sent via SMS or WhatsApp. The link expires when the trip ends.",
    tags: ["safety", "share", "sos", "family"],
  },
  {
    id: "faq-lost",
    question: "I left something in the car.",
    category: "Safety",
    answer:
      "From Bookings → tap the finished trip → Report lost item. We connect you to the driver directly and keep a support agent on standby until the item is returned.",
    tags: ["lost", "item", "forgot", "belongings"],
  },
  {
    id: "faq-driver-not-arrived",
    question: "Driver never arrived — what do I do?",
    category: "Driver",
    answer:
      "If the driver marks arrived but is not at your pickup, open the trip and tap Pickup issue. We cancel the trip with no charge and dispatch a backup driver immediately.",
    tags: ["driver", "arrive", "pickup", "noshow"],
  },
  {
    id: "faq-receipt",
    question: "How do I get a GST invoice?",
    category: "Billing",
    answer:
      "Open the completed trip from Bookings → Invoice. If you need a company GSTIN on the invoice, add it in Profile → Business profile and future invoices will include it.",
    tags: ["invoice", "gst", "receipt", "tax", "billing"],
  },
  {
    id: "faq-round-trip",
    question: "How does a round trip work?",
    category: "Bookings",
    answer:
      "A round trip reserves the same driver for both legs, including waiting time at the destination. Fare includes return kilometres and the estimated wait minutes — visible before you confirm.",
    tags: ["round", "trip", "return", "wait"],
  },
  {
    id: "faq-schedule",
    question: "Can I schedule a trip for tomorrow?",
    category: "Bookings",
    answer:
      "Yes — on the home screen tap the time chip (Now / 30 min / 1 hour) and pick a custom time. You will get a confirmation 30 minutes before pickup.",
    tags: ["schedule", "tomorrow", "later", "book"],
  },
];

export function searchFaqs(query: string, faqs: SupportFaq[] = SUPPORT_FAQS): SupportFaq[] {
  const normalised = query.trim().toLowerCase();
  if (!normalised) {
    return faqs;
  }
  const words = normalised.split(/\s+/).filter(Boolean);
  return faqs
    .map((faq) => {
      const haystack = `${faq.question} ${faq.category} ${faq.tags.join(" ")} ${faq.answer}`.toLowerCase();
      const hits = words.filter((word) => haystack.includes(word)).length;
      return { faq, hits };
    })
    .filter((entry) => entry.hits > 0)
    .sort((a, b) => b.hits - a.hits)
    .map((entry) => entry.faq);
}

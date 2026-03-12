# RYDVRSE Module Details

## Module Dependency Graph

```
                    ┌──────────┐
                    │  Shared  │
                    │  Kernel  │
                    └────┬─────┘
                         │ (all modules depend on shared)
         ┌───────────────┼───────────────┐
         │               │               │
    ┌────▼────┐    ┌─────▼────┐    ┌─────▼────┐
    │  Auth   │    │   User   │    │  Driver  │
    └────┬────┘    └──────────┘    └──┬───────┘
         │                            │
    ┌────▼────────────────────────────▼──┐
    │              Trip Module           │
    │         (Core Business)            │
    └──┬────────┬────────┬──────────┬───┘
       │        │        │          │
  ┌────▼──┐ ┌──▼───┐ ┌──▼────┐ ┌──▼─────┐
  │Dispatch│ │Pricing│ │Payment│ │Location│
  └───────┘ └──────┘ └──┬────┘ └────────┘
                        │
                   ┌────▼───┐
                   │ Wallet │
                   └────────┘
```

## Module Communication Matrix

| Source → Target | Auth | User | Driver | Trip | Dispatch | Payment | Notification | Analytics |
|----------------|------|------|--------|------|----------|---------|-------------|-----------|
| **Auth** | — | Event | Event | — | — | — | Event | Event |
| **User** | — | — | — | — | — | — | — | — |
| **Driver** | — | — | — | — | — | — | Event | Event |
| **Trip** | — | — | Call | — | Event | Event | Event | Event |
| **Dispatch** | — | — | Call | Call | — | — | — | — |
| **Payment** | — | — | — | — | — | — | Event | Event |
| **Wallet** | — | — | — | — | — | — | — | — |

Legend: Event = Spring/Kafka event, Call = direct service method call

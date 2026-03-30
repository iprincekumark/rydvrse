# RYDVRSE Database Design

## Schema Overview

All tables use:
- **UUID** primary keys (distributed-ready)
- **created_at** and **updated_at** timestamps (auto-managed)
- **version** column for optimistic locking
- Appropriate indexes on query-heavy columns

## Table Definitions

### auth_users
| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PK |
| phone_number | VARCHAR(15) | UNIQUE, NOT NULL |
| email | VARCHAR(255) | |
| password_hash | VARCHAR | |
| role | VARCHAR(20) | NOT NULL (CUSTOMER/DRIVER/ADMIN) |
| profile_id | UUID | FK to customer/driver |
| is_active | BOOLEAN | DEFAULT true |
| is_phone_verified | BOOLEAN | DEFAULT false |
| last_login_at | TIMESTAMP | |
| failed_login_attempts | INT | DEFAULT 0 |
| locked_until | TIMESTAMP | |

### customers
| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PK |
| auth_user_id | UUID | UNIQUE, NOT NULL |
| phone_number | VARCHAR(15) | UNIQUE, NOT NULL |
| first_name | VARCHAR(100) | |
| last_name | VARCHAR(100) | |
| email | VARCHAR(255) | |
| total_trips | INT | DEFAULT 0 |
| average_rating | DOUBLE | DEFAULT 5.0 |

### drivers
| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PK |
| auth_user_id | UUID | UNIQUE, NOT NULL |
| phone_number | VARCHAR(15) | UNIQUE, NOT NULL |
| license_number | VARCHAR(50) | |
| status | VARCHAR(30) | NOT NULL (DriverStatus enum) |
| is_available | BOOLEAN | DEFAULT false |
| is_on_trip | BOOLEAN | DEFAULT false |
| current_lat/lng | DOUBLE | |
| operating_city | VARCHAR(100) | |
| total_trips | INT | DEFAULT 0 |
| average_rating | DOUBLE | DEFAULT 5.0 |
| acceptance_rate | DOUBLE | DEFAULT 100.0 |
| total_earnings | DOUBLE | DEFAULT 0.0 |

### trips
| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PK |
| trip_number | VARCHAR(20) | UNIQUE, NOT NULL |
| customer_id | UUID | NOT NULL |
| driver_id | UUID | |
| vehicle_id | UUID | |
| status | VARCHAR(20) | NOT NULL (TripStatus enum) |
| pickup_lat/lng | DOUBLE | |
| pickup_address | VARCHAR | |
| drop_lat/lng | DOUBLE | |
| estimated_distance_km | DOUBLE | |
| actual_distance_km | DOUBLE | |
| estimated_fare | DOUBLE | |
| final_fare | DOUBLE | |
| surge_multiplier | DOUBLE | DEFAULT 1.0 |
| start_otp | VARCHAR(4) | |
| driver_assigned_at | TIMESTAMP | |
| trip_started_at | TIMESTAMP | |
| trip_completed_at | TIMESTAMP | |
| customer_rating | DOUBLE | |
| driver_rating | DOUBLE | |

### payments
| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PK |
| trip_id | UUID | NOT NULL |
| transaction_id | VARCHAR(50) | UNIQUE |
| amount | DOUBLE | NOT NULL |
| platform_fee | DOUBLE | DEFAULT 0.0 |
| driver_payout | DOUBLE | |
| payment_method | VARCHAR(20) | NOT NULL |
| status | VARCHAR(20) | NOT NULL |
| gateway_reference | VARCHAR | |
| paid_at | TIMESTAMP | |

### wallets
| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PK |
| user_id | UUID | UNIQUE per user_type |
| user_type | VARCHAR(20) | NOT NULL |
| balance | DOUBLE | DEFAULT 0.0 |

## Key Indexes

```sql
CREATE INDEX idx_driver_available ON drivers (status, is_available, is_on_trip, operating_city);
CREATE INDEX idx_trip_lookup ON trips (customer_id, status, created_at DESC);
CREATE INDEX idx_payment_trip ON payments (trip_id);
CREATE INDEX idx_auth_phone ON auth_users (phone_number);
```

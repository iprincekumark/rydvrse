-- V1__init.sql — RYDVRSE MVP Schema
-- PostGIS extension
CREATE EXTENSION IF NOT EXISTS postgis;

-- ===== Auth =====
CREATE TABLE refresh_token (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    user_type VARCHAR(20) NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    is_revoked BOOLEAN NOT NULL DEFAULT false
);
CREATE INDEX idx_refresh_token ON refresh_token(token);

-- ===== Customer =====
CREATE TABLE customer (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(15) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    profile_image_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE UNIQUE INDEX idx_customer_phone ON customer(phone);
CREATE UNIQUE INDEX idx_customer_email ON customer(email);

CREATE TABLE customer_address (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customer(id) ON DELETE CASCADE,
    label VARCHAR(50) NOT NULL,
    address VARCHAR(500) NOT NULL,
    lat DOUBLE PRECISION NOT NULL,
    lng DOUBLE PRECISION NOT NULL
);

-- ===== Driver =====
CREATE TABLE driver (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(15) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    profile_image_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    average_rating NUMERIC(3,2) DEFAULT 0,
    total_trips INTEGER DEFAULT 0,
    acceptance_rate NUMERIC(3,2) DEFAULT 1.00,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE UNIQUE INDEX idx_driver_phone ON driver(phone);
CREATE UNIQUE INDEX idx_driver_email ON driver(email);

CREATE TABLE driver_document (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID NOT NULL REFERENCES driver(id) ON DELETE CASCADE,
    type VARCHAR(30) NOT NULL,
    document_url VARCHAR(500) NOT NULL,
    verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reviewed_by UUID,
    review_notes TEXT,
    expiry_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_driver_doc_driver_type ON driver_document(driver_id, type);

CREATE TABLE driver_location (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID NOT NULL UNIQUE,
    point geometry(Point, 4326) NOT NULL,
    heading DOUBLE PRECISION,
    speed DOUBLE PRECISION,
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_driver_location_driver ON driver_location(driver_id);
CREATE INDEX idx_driver_location_spatial ON driver_location USING GIST(point);

CREATE TABLE driver_availability (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID NOT NULL UNIQUE,
    is_online BOOLEAN NOT NULL DEFAULT false,
    current_trip_id UUID,
    last_online_at TIMESTAMP
);
CREATE UNIQUE INDEX idx_driver_avail_driver ON driver_availability(driver_id);

-- ===== Trip =====
CREATE TABLE trip (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    driver_id UUID,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    pickup_address VARCHAR(500) NOT NULL,
    pickup_lat DOUBLE PRECISION NOT NULL,
    pickup_lng DOUBLE PRECISION NOT NULL,
    drop_address VARCHAR(500) NOT NULL,
    drop_lat DOUBLE PRECISION NOT NULL,
    drop_lng DOUBLE PRECISION NOT NULL,
    estimated_fare NUMERIC(10,2) NOT NULL,
    actual_fare NUMERIC(10,2),
    estimated_distance NUMERIC(10,2) NOT NULL,
    actual_distance NUMERIC(10,2),
    estimated_duration INTEGER NOT NULL,
    scheduled_at TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    cancellation_reason VARCHAR(500),
    cancelled_by VARCHAR(20),
    is_night_trip BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_trip_customer_status ON trip(customer_id, status);
CREATE INDEX idx_trip_driver_status ON trip(driver_id, status);
CREATE INDEX idx_trip_scheduled ON trip(scheduled_at);

CREATE TABLE trip_location_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trip_id UUID NOT NULL,
    point geometry(Point, 4326) NOT NULL,
    heading DOUBLE PRECISION,
    speed DOUBLE PRECISION,
    timestamp TIMESTAMP NOT NULL
);
CREATE INDEX idx_trip_loc_trip_time ON trip_location_log(trip_id, timestamp);
CREATE INDEX idx_trip_loc_spatial ON trip_location_log USING GIST(point);

CREATE TABLE trip_rating (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trip_id UUID NOT NULL,
    rated_by VARCHAR(20) NOT NULL,
    rater_id UUID NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    review TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE trip_dispatch_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trip_id UUID NOT NULL,
    driver_id UUID NOT NULL,
    dispatch_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    sent_at TIMESTAMP NOT NULL,
    responded_at TIMESTAMP
);

CREATE TABLE fare_rule (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vehicle_type VARCHAR(50) NOT NULL,
    base_fare NUMERIC(10,2) NOT NULL,
    per_km_rate NUMERIC(10,2) NOT NULL,
    per_minute_rate NUMERIC(10,2) NOT NULL,
    minimum_fare NUMERIC(10,2) NOT NULL,
    night_surcharge_percent NUMERIC(5,2) NOT NULL DEFAULT 0,
    cancellation_fee NUMERIC(10,2) NOT NULL DEFAULT 0,
    effective_from DATE NOT NULL,
    effective_to DATE
);

-- ===== Payment =====
CREATE TABLE payment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trip_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    amount NUMERIC(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    method VARCHAR(30) NOT NULL,
    gateway_provider VARCHAR(50) NOT NULL DEFAULT 'RAZORPAY',
    gateway_transaction_id VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'INITIATED',
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0
);
CREATE UNIQUE INDEX idx_payment_idempotency ON payment(idempotency_key);

CREATE TABLE wallet (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL UNIQUE,
    balance NUMERIC(10,2) NOT NULL DEFAULT 0,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE wallet_transaction (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id UUID NOT NULL REFERENCES wallet(id),
    type VARCHAR(10) NOT NULL,
    amount NUMERIC(10,2) NOT NULL,
    reference_type VARCHAR(30) NOT NULL,
    reference_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- ===== Safety =====
CREATE TABLE sos_alert (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trip_id UUID NOT NULL,
    triggered_by VARCHAR(20) NOT NULL,
    triggered_by_id UUID NOT NULL,
    lat DOUBLE PRECISION NOT NULL,
    lng DOUBLE PRECISION NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    resolved_at TIMESTAMP,
    resolved_by UUID,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_sos_status ON sos_alert(status);

CREATE TABLE trip_share_link (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trip_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    share_token UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    recipient_phone VARCHAR(15) NOT NULL,
    recipient_name VARCHAR(100) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    expires_at TIMESTAMP NOT NULL
);

CREATE TABLE incident (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trip_id UUID,
    reported_by VARCHAR(20) NOT NULL,
    reporter_id UUID NOT NULL,
    type VARCHAR(30) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    priority VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE emergency_contact (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    user_type VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    relationship VARCHAR(50) NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- ===== Notification =====
CREATE TABLE notification_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_type VARCHAR(20) NOT NULL,
    recipient_id UUID NOT NULL,
    channel VARCHAR(20) NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SENT',
    sent_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE device_token (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    user_type VARCHAR(20) NOT NULL,
    platform VARCHAR(10) NOT NULL,
    fcm_token VARCHAR(500) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- ===== Admin =====
CREATE TABLE admin_user (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- ===== Common =====
CREATE TABLE audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_type VARCHAR(20) NOT NULL,
    actor_id UUID NOT NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    details JSONB,
    ip_address VARCHAR(45),
    timestamp TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE failed_event (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    error_message TEXT NOT NULL,
    retry_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- ===== Default Data =====
INSERT INTO fare_rule (id, vehicle_type, base_fare, per_km_rate, per_minute_rate, minimum_fare, night_surcharge_percent, cancellation_fee, effective_from)
VALUES (gen_random_uuid(), 'SEDAN', 50.00, 12.00, 2.00, 80.00, 25.00, 50.00, '2024-01-01');

INSERT INTO fare_rule (id, vehicle_type, base_fare, per_km_rate, per_minute_rate, minimum_fare, night_surcharge_percent, cancellation_fee, effective_from)
VALUES (gen_random_uuid(), 'SUV', 75.00, 16.00, 3.00, 120.00, 25.00, 75.00, '2024-01-01');

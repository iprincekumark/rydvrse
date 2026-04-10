CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS citext;
CREATE EXTENSION IF NOT EXISTS postgis;

CREATE SCHEMA IF NOT EXISTS iam;
CREATE SCHEMA IF NOT EXISTS master;
CREATE SCHEMA IF NOT EXISTS customer;
CREATE SCHEMA IF NOT EXISTS driver;
CREATE SCHEMA IF NOT EXISTS commercial;
CREATE SCHEMA IF NOT EXISTS booking;
CREATE SCHEMA IF NOT EXISTS trip;
CREATE SCHEMA IF NOT EXISTS finance;
CREATE SCHEMA IF NOT EXISTS support;
CREATE SCHEMA IF NOT EXISTS comms;
CREATE SCHEMA IF NOT EXISTS audit;
CREATE SCHEMA IF NOT EXISTS analytics;

CREATE OR REPLACE FUNCTION public.set_updated_at()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$;

CREATE OR REPLACE FUNCTION public.booking_sync_pickup_drop_geo()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.pickup_latitude IS NOT NULL AND NEW.pickup_longitude IS NOT NULL THEN
        NEW.pickup_geo = ST_SetSRID(ST_MakePoint(NEW.pickup_longitude, NEW.pickup_latitude), 4326)::geography;
    END IF;
    IF NEW.drop_latitude IS NOT NULL AND NEW.drop_longitude IS NOT NULL THEN
        NEW.drop_geo = ST_SetSRID(ST_MakePoint(NEW.drop_longitude, NEW.drop_latitude), 4326)::geography;
    END IF;
    RETURN NEW;
END;
$$;

CREATE OR REPLACE FUNCTION public.saved_location_sync_geo()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.latitude IS NOT NULL AND NEW.longitude IS NOT NULL THEN
        NEW.location_geo = ST_SetSRID(ST_MakePoint(NEW.longitude, NEW.latitude), 4326)::geography;
    END IF;
    RETURN NEW;
END;
$$;

CREATE OR REPLACE FUNCTION public.location_ping_sync_geo()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.latitude IS NOT NULL AND NEW.longitude IS NOT NULL THEN
        NEW.location_geo = ST_SetSRID(ST_MakePoint(NEW.longitude, NEW.latitude), 4326)::geography;
    END IF;
    RETURN NEW;
END;
$$;

CREATE TABLE iam.user_account (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_type text NOT NULL,
    mobile_country_code text NOT NULL,
    mobile_number_e164 text NOT NULL,
    email citext NULL,
    password_hash text NULL,
    status text NOT NULL,
    is_mobile_verified boolean NOT NULL DEFAULT false,
    last_login_at timestamptz NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    created_by_user_id uuid NULL,
    updated_by_user_id uuid NULL,
    row_version bigint NOT NULL DEFAULT 0,
    metadata jsonb NOT NULL DEFAULT '{}'::jsonb,
    CONSTRAINT uq_user_account_mobile UNIQUE (mobile_number_e164),
    CONSTRAINT ck_user_account_user_type CHECK (user_type IN ('CUSTOMER', 'DRIVER', 'INTERNAL', 'ADMIN')),
    CONSTRAINT ck_user_account_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'BLOCKED', 'SUSPENDED'))
);

CREATE UNIQUE INDEX uq_user_account_email_partial
    ON iam.user_account (email)
    WHERE email IS NOT NULL;
CREATE INDEX idx_user_account_user_type_status
    ON iam.user_account (user_type, status);

ALTER TABLE iam.user_account
    ADD CONSTRAINT fk_user_account_created_by
        FOREIGN KEY (created_by_user_id) REFERENCES iam.user_account(id);
ALTER TABLE iam.user_account
    ADD CONSTRAINT fk_user_account_updated_by
        FOREIGN KEY (updated_by_user_id) REFERENCES iam.user_account(id);

CREATE TABLE iam.otp_challenge (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_account_id uuid NULL REFERENCES iam.user_account(id),
    mobile_number_e164 text NOT NULL,
    channel text NOT NULL,
    purpose text NOT NULL,
    otp_hash text NOT NULL,
    status text NOT NULL,
    attempt_count integer NOT NULL DEFAULT 0,
    max_attempts integer NOT NULL DEFAULT 5,
    expires_at timestamptz NOT NULL,
    verified_at timestamptz NULL,
    request_ip inet NULL,
    device_fingerprint text NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT ck_otp_attempt_count CHECK (attempt_count >= 0),
    CONSTRAINT ck_otp_max_attempts CHECK (max_attempts > 0)
);

CREATE INDEX idx_otp_mobile_created_at
    ON iam.otp_challenge (mobile_number_e164, created_at DESC);
CREATE INDEX idx_otp_status_expires_at
    ON iam.otp_challenge (status, expires_at);
CREATE INDEX idx_otp_user_created_at
    ON iam.otp_challenge (user_account_id, created_at DESC)
    WHERE user_account_id IS NOT NULL;

CREATE TABLE iam.role (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    role_code text NOT NULL UNIQUE,
    role_name text NOT NULL,
    is_system_role boolean NOT NULL DEFAULT false,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE iam.permission (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    permission_code text NOT NULL UNIQUE,
    permission_name text NOT NULL,
    resource_type text NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE iam.role_permission (
    role_id uuid NOT NULL REFERENCES iam.role(id),
    permission_id uuid NOT NULL REFERENCES iam.permission(id),
    created_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE iam.user_role_binding (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_account_id uuid NOT NULL REFERENCES iam.user_account(id),
    role_id uuid NOT NULL REFERENCES iam.role(id),
    binding_status text NOT NULL,
    effective_from timestamptz NOT NULL,
    effective_to timestamptz NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_user_role_binding_user_status
    ON iam.user_role_binding (user_account_id, binding_status);
CREATE INDEX idx_user_role_binding_role_status
    ON iam.user_role_binding (role_id, binding_status);

CREATE TABLE iam.device_registration (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_account_id uuid NOT NULL REFERENCES iam.user_account(id),
    device_type text NOT NULL,
    push_token text NOT NULL,
    app_variant text NOT NULL,
    status text NOT NULL,
    device_id text NULL,
    last_seen_at timestamptz NULL,
    revoked_at timestamptz NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_device_registration_push UNIQUE (push_token)
);

CREATE INDEX idx_device_registration_user_status
    ON iam.device_registration (user_account_id, status);

CREATE TABLE iam.user_session (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_account_id uuid NOT NULL REFERENCES iam.user_account(id),
    refresh_token_hash text NOT NULL UNIQUE,
    session_status text NOT NULL,
    device_registration_id uuid NULL REFERENCES iam.device_registration(id),
    issued_at timestamptz NOT NULL,
    expires_at timestamptz NOT NULL,
    revoked_at timestamptz NULL,
    last_seen_at timestamptz NULL,
    request_ip inet NULL,
    user_agent text NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_user_session_user_status
    ON iam.user_session (user_account_id, session_status);
CREATE INDEX idx_user_session_expires_at
    ON iam.user_session (expires_at);

CREATE TABLE master.city (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    city_code text NOT NULL UNIQUE,
    city_name text NOT NULL,
    country_code text NOT NULL,
    timezone text NOT NULL,
    launch_status text NOT NULL,
    currency_code text NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    row_version bigint NOT NULL DEFAULT 0
);

CREATE INDEX idx_city_launch_status
    ON master.city (launch_status);

CREATE TABLE master.service_zone (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    city_id uuid NOT NULL REFERENCES master.city(id),
    zone_code text NOT NULL,
    zone_name text NOT NULL,
    zone_type text NOT NULL,
    boundary_geom geometry(MultiPolygon, 4326) NOT NULL,
    centroid_geo geography(Point, 4326) NULL,
    launch_status text NOT NULL,
    sort_order integer NOT NULL DEFAULT 0,
    service_types_enabled text[] NOT NULL DEFAULT ARRAY[]::text[],
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    row_version bigint NOT NULL DEFAULT 0,
    CONSTRAINT uq_service_zone_city_zone_code UNIQUE (city_id, zone_code)
);

CREATE INDEX idx_service_zone_city_status
    ON master.service_zone (city_id, launch_status);
CREATE INDEX idx_service_zone_city_type
    ON master.service_zone (city_id, zone_type);
CREATE INDEX idx_service_zone_boundary_geom
    ON master.service_zone USING gist (boundary_geom);

CREATE TABLE master.airport_zone_band (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    city_id uuid NOT NULL REFERENCES master.city(id),
    band_code text NOT NULL,
    band_name text NOT NULL,
    service_zone_id uuid NOT NULL REFERENCES master.service_zone(id),
    airport_code text NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_airport_zone_band UNIQUE (city_id, airport_code, service_zone_id)
);

CREATE INDEX idx_airport_zone_band_city_airport_band
    ON master.airport_zone_band (city_id, airport_code, band_code);

CREATE TABLE master.one_way_band (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    city_id uuid NOT NULL REFERENCES master.city(id),
    band_code text NOT NULL,
    band_name text NOT NULL,
    source_zone_id uuid NOT NULL REFERENCES master.service_zone(id),
    destination_zone_id uuid NOT NULL REFERENCES master.service_zone(id),
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_one_way_band UNIQUE (city_id, source_zone_id, destination_zone_id)
);

CREATE INDEX idx_one_way_band_city_band
    ON master.one_way_band (city_id, band_code);
CREATE INDEX idx_one_way_band_source_destination
    ON master.one_way_band (source_zone_id, destination_zone_id);

CREATE TABLE master.serviceability_rule (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    city_id uuid NOT NULL REFERENCES master.city(id),
    service_zone_id uuid NOT NULL REFERENCES master.service_zone(id),
    service_type text NOT NULL,
    is_enabled boolean NOT NULL,
    min_lead_minutes integer NOT NULL DEFAULT 0,
    operating_start_local time NULL,
    operating_end_local time NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    row_version bigint NOT NULL DEFAULT 0,
    CONSTRAINT uq_serviceability_rule UNIQUE (city_id, service_zone_id, service_type)
);

CREATE INDEX idx_serviceability_rule_city_service_enabled
    ON master.serviceability_rule (city_id, service_type, is_enabled);

CREATE TABLE master.feature_flag (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    flag_key text NOT NULL,
    scope_type text NOT NULL,
    scope_id uuid NULL,
    is_enabled boolean NOT NULL,
    payload jsonb NOT NULL DEFAULT '{}'::jsonb,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_feature_flag UNIQUE (flag_key, scope_type, scope_id)
);

CREATE TABLE master.business_config (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    config_key text NOT NULL,
    scope_type text NOT NULL,
    scope_id uuid NULL,
    config_value jsonb NOT NULL,
    version_no integer NOT NULL,
    is_active boolean NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_business_config UNIQUE (config_key, scope_type, scope_id, version_no)
);

CREATE TABLE customer.customer_profile (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_account_id uuid NOT NULL UNIQUE REFERENCES iam.user_account(id),
    first_name text NOT NULL,
    last_name text NULL,
    email citext NULL,
    default_city_id uuid NOT NULL REFERENCES master.city(id),
    status text NOT NULL,
    last_active_at timestamptz NULL,
    trust_score numeric(5,2) NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    row_version bigint NOT NULL DEFAULT 0,
    metadata jsonb NOT NULL DEFAULT '{}'::jsonb
);

CREATE INDEX idx_customer_profile_city_status
    ON customer.customer_profile (default_city_id, status);
CREATE INDEX idx_customer_profile_last_active
    ON customer.customer_profile (last_active_at DESC);

CREATE TABLE customer.saved_location (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_profile_id uuid NOT NULL REFERENCES customer.customer_profile(id),
    label text NOT NULL,
    address_line_1 text NOT NULL,
    address_line_2 text NULL,
    landmark text NULL,
    city_id uuid NOT NULL REFERENCES master.city(id),
    service_zone_id uuid NULL REFERENCES master.service_zone(id),
    latitude numeric(9,6) NOT NULL,
    longitude numeric(9,6) NOT NULL,
    location_geo geography(Point, 4326) NOT NULL,
    is_default boolean NOT NULL DEFAULT false,
    is_deleted boolean NOT NULL DEFAULT false,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    row_version bigint NOT NULL DEFAULT 0
);

CREATE INDEX idx_saved_location_customer_deleted_label
    ON customer.saved_location (customer_profile_id, is_deleted, label);
CREATE INDEX idx_saved_location_geo
    ON customer.saved_location USING gist (location_geo);

CREATE TABLE customer.emergency_contact (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_profile_id uuid NOT NULL REFERENCES customer.customer_profile(id),
    contact_name text NOT NULL,
    contact_mobile_e164 text NOT NULL,
    relationship_label text NULL,
    is_primary boolean NOT NULL DEFAULT false,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_emergency_contact_customer_primary
    ON customer.emergency_contact (customer_profile_id, is_primary DESC);

CREATE TABLE customer.trusted_share_contact (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_profile_id uuid NOT NULL REFERENCES customer.customer_profile(id),
    contact_name text NOT NULL,
    contact_mobile_e164 text NULL,
    contact_email citext NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_trusted_share_contact_customer
    ON customer.trusted_share_contact (customer_profile_id);

CREATE TABLE customer.preference (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_profile_id uuid NOT NULL UNIQUE REFERENCES customer.customer_profile(id),
    location_permission_status text NOT NULL,
    marketing_opt_in boolean NOT NULL DEFAULT false,
    push_opt_in boolean NOT NULL DEFAULT true,
    sms_opt_in boolean NOT NULL DEFAULT true,
    language_code text NULL,
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE customer.customer_flag (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_profile_id uuid NOT NULL REFERENCES customer.customer_profile(id),
    flag_type text NOT NULL,
    severity text NOT NULL,
    status text NOT NULL,
    reason text NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    resolved_at timestamptz NULL
);

CREATE INDEX idx_customer_flag_profile_status
    ON customer.customer_flag (customer_profile_id, status);
CREATE INDEX idx_customer_flag_type_status
    ON customer.customer_flag (flag_type, status);

CREATE TABLE driver.driver_profile (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_account_id uuid NOT NULL UNIQUE REFERENCES iam.user_account(id),
    first_name text NOT NULL,
    last_name text NULL,
    email citext NULL,
    date_of_birth date NULL,
    default_city_id uuid NOT NULL REFERENCES master.city(id),
    language_codes text[] NULL,
    emergency_contact_name text NULL,
    emergency_contact_mobile_e164 text NULL,
    onboarding_status text NOT NULL,
    compliance_status text NOT NULL,
    rating_avg numeric(4,2) NULL,
    rating_count integer NOT NULL DEFAULT 0,
    punctuality_score numeric(5,2) NULL,
    acceptance_rate numeric(5,2) NULL,
    cancellation_rate numeric(5,2) NULL,
    current_status text NOT NULL,
    approved_at timestamptz NULL,
    suspended_at timestamptz NULL,
    rejection_reason_code text NULL,
    reviewer_notes text NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    row_version bigint NOT NULL DEFAULT 0,
    metadata jsonb NOT NULL DEFAULT '{}'::jsonb
);

CREATE INDEX idx_driver_profile_city_onboarding
    ON driver.driver_profile (default_city_id, onboarding_status);
CREATE INDEX idx_driver_profile_city_compliance_runtime
    ON driver.driver_profile (default_city_id, compliance_status, current_status);
CREATE INDEX idx_driver_profile_rating_avg
    ON driver.driver_profile (rating_avg DESC);

CREATE TABLE driver.driver_document (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_profile_id uuid NOT NULL REFERENCES driver.driver_profile(id),
    document_type text NOT NULL,
    document_number_masked text NULL,
    storage_key text NOT NULL,
    status text NOT NULL,
    issued_at date NULL,
    expires_at date NULL,
    submitted_at timestamptz NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_driver_document_profile_type_status
    ON driver.driver_document (driver_profile_id, document_type, status);
CREATE INDEX idx_driver_document_expires_at
    ON driver.driver_document (expires_at)
    WHERE expires_at IS NOT NULL;

CREATE TABLE driver.driver_document_review (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_document_id uuid NOT NULL REFERENCES driver.driver_document(id),
    review_status text NOT NULL,
    reviewer_user_id uuid NULL REFERENCES iam.user_account(id),
    review_note text NULL,
    reviewed_at timestamptz NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_driver_document_review_document_reviewed
    ON driver.driver_document_review (driver_document_id, reviewed_at DESC);

CREATE TABLE driver.driver_training_record (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_profile_id uuid NOT NULL REFERENCES driver.driver_profile(id),
    training_code text NOT NULL,
    status text NOT NULL,
    completed_at timestamptz NULL,
    expires_at timestamptz NULL,
    score numeric(5,2) NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_driver_training UNIQUE (driver_profile_id, training_code)
);

CREATE TABLE driver.driver_bank_account (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_profile_id uuid NOT NULL REFERENCES driver.driver_profile(id),
    account_holder_name text NOT NULL,
    bank_name text NOT NULL,
    account_number_masked text NOT NULL,
    ifsc_code text NOT NULL,
    provider_reference text NULL,
    verification_status text NOT NULL,
    is_primary boolean NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_driver_bank_account_profile_primary
    ON driver.driver_bank_account (driver_profile_id, is_primary DESC);

CREATE TABLE driver.driver_availability_status (
    driver_profile_id uuid PRIMARY KEY REFERENCES driver.driver_profile(id),
    current_status text NOT NULL,
    current_booking_id uuid NULL,
    current_assignment_id uuid NULL,
    current_trip_id uuid NULL,
    updated_at timestamptz NOT NULL DEFAULT now(),
    updated_by_user_id uuid NULL REFERENCES iam.user_account(id)
);

CREATE INDEX idx_driver_availability_status_current_status
    ON driver.driver_availability_status (current_status, updated_at DESC);

CREATE TABLE driver.driver_availability_slot (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_profile_id uuid NOT NULL REFERENCES driver.driver_profile(id),
    weekday smallint NOT NULL,
    slot_start_local time NOT NULL,
    slot_end_local time NOT NULL,
    is_active boolean NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_driver_availability_slot_profile_weekday
    ON driver.driver_availability_slot (driver_profile_id, weekday, is_active);

CREATE TABLE driver.driver_zone_eligibility (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_profile_id uuid NOT NULL REFERENCES driver.driver_profile(id),
    service_zone_id uuid NOT NULL REFERENCES master.service_zone(id),
    service_type text NOT NULL,
    eligibility_status text NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_driver_zone_eligibility UNIQUE (driver_profile_id, service_zone_id, service_type)
);

CREATE INDEX idx_driver_zone_eligibility_zone_service_status
    ON driver.driver_zone_eligibility (service_zone_id, service_type, eligibility_status);

CREATE TABLE driver.driver_flag (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_profile_id uuid NOT NULL REFERENCES driver.driver_profile(id),
    flag_type text NOT NULL,
    severity text NOT NULL,
    status text NOT NULL,
    reason text NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    resolved_at timestamptz NULL
);

CREATE INDEX idx_driver_flag_profile_status
    ON driver.driver_flag (driver_profile_id, status);
CREATE INDEX idx_driver_flag_type_status
    ON driver.driver_flag (flag_type, status);

CREATE TABLE driver.driver_status_log (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_profile_id uuid NOT NULL REFERENCES driver.driver_profile(id),
    status_type text NOT NULL,
    from_status text NULL,
    to_status text NOT NULL,
    reason_code text NULL,
    reason_note text NULL,
    changed_by_user_id uuid NULL REFERENCES iam.user_account(id),
    changed_at timestamptz NOT NULL
);

CREATE INDEX idx_driver_status_log_profile_changed_at
    ON driver.driver_status_log (driver_profile_id, changed_at DESC);

CREATE TABLE commercial.pricing_plan (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    city_id uuid NOT NULL REFERENCES master.city(id),
    plan_code text NOT NULL,
    version_no integer NOT NULL,
    status text NOT NULL,
    effective_from timestamptz NOT NULL,
    effective_to timestamptz NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    created_by_user_id uuid NULL REFERENCES iam.user_account(id),
    CONSTRAINT uq_pricing_plan UNIQUE (city_id, plan_code, version_no)
);

CREATE INDEX idx_pricing_plan_city_status_effective
    ON commercial.pricing_plan (city_id, status, effective_from DESC);

CREATE TABLE commercial.pricing_rule (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    pricing_plan_id uuid NOT NULL REFERENCES commercial.pricing_plan(id),
    service_type text NOT NULL,
    service_zone_id uuid NULL REFERENCES master.service_zone(id),
    airport_zone_band_id uuid NULL REFERENCES master.airport_zone_band(id),
    one_way_band_id uuid NULL REFERENCES master.one_way_band(id),
    lead_time_bucket text NULL,
    rule_type text NOT NULL,
    amount_paise bigint NOT NULL,
    is_active boolean NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_pricing_rule_plan_service_rule
    ON commercial.pricing_rule (pricing_plan_id, service_type, rule_type, is_active);
CREATE INDEX idx_pricing_rule_zone_service_rule
    ON commercial.pricing_rule (service_zone_id, service_type, rule_type)
    WHERE service_zone_id IS NOT NULL;
CREATE INDEX idx_pricing_rule_airport_rule
    ON commercial.pricing_rule (airport_zone_band_id, rule_type)
    WHERE airport_zone_band_id IS NOT NULL;
CREATE INDEX idx_pricing_rule_one_way_rule
    ON commercial.pricing_rule (one_way_band_id, rule_type)
    WHERE one_way_band_id IS NOT NULL;

CREATE TABLE commercial.tax_profile (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    city_id uuid NULL REFERENCES master.city(id),
    profile_code text NOT NULL,
    version_no integer NOT NULL,
    tax_percent numeric(5,2) NOT NULL,
    status text NOT NULL,
    effective_from timestamptz NOT NULL,
    effective_to timestamptz NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_tax_profile UNIQUE (profile_code, version_no)
);

CREATE TABLE commercial.quote (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_profile_id uuid NOT NULL REFERENCES customer.customer_profile(id),
    city_id uuid NOT NULL REFERENCES master.city(id),
    service_type text NOT NULL,
    pickup_zone_id uuid NULL REFERENCES master.service_zone(id),
    drop_zone_id uuid NULL REFERENCES master.service_zone(id),
    airport_zone_band_id uuid NULL REFERENCES master.airport_zone_band(id),
    one_way_band_id uuid NULL REFERENCES master.one_way_band(id),
    pricing_plan_id uuid NOT NULL REFERENCES commercial.pricing_plan(id),
    tax_profile_id uuid NOT NULL REFERENCES commercial.tax_profile(id),
    scheduled_pickup_at timestamptz NOT NULL,
    quoted_duration_minutes integer NULL,
    pickup_payload jsonb NOT NULL DEFAULT '{}'::jsonb,
    drop_payload jsonb NULL,
    currency_code text NOT NULL,
    subtotal_paise bigint NOT NULL,
    tax_paise bigint NOT NULL,
    total_paise bigint NOT NULL,
    quote_status text NOT NULL,
    expires_at timestamptz NOT NULL,
    requested_at timestamptz NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    metadata jsonb NOT NULL DEFAULT '{}'::jsonb
);

CREATE INDEX idx_quote_customer_created_at
    ON commercial.quote (customer_profile_id, created_at DESC);
CREATE INDEX idx_quote_status_expires
    ON commercial.quote (quote_status, expires_at);
CREATE INDEX idx_quote_scheduled_pickup_at
    ON commercial.quote (scheduled_pickup_at);

CREATE TABLE commercial.quote_component (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    quote_id uuid NOT NULL REFERENCES commercial.quote(id),
    component_type text NOT NULL,
    display_label text NOT NULL,
    amount_paise bigint NOT NULL,
    sort_order integer NOT NULL,
    is_tax boolean NOT NULL DEFAULT false,
    component_payload jsonb NOT NULL DEFAULT '{}'::jsonb,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_quote_component_quote_sort
    ON commercial.quote_component (quote_id, sort_order);

CREATE TABLE commercial.cancellation_policy (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    city_id uuid NOT NULL REFERENCES master.city(id),
    policy_code text NOT NULL,
    version_no integer NOT NULL,
    status text NOT NULL,
    policy_payload jsonb NOT NULL,
    effective_from timestamptz NOT NULL,
    effective_to timestamptz NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_cancellation_policy UNIQUE (city_id, policy_code, version_no)
);

CREATE TABLE commercial.refund_policy (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    city_id uuid NOT NULL REFERENCES master.city(id),
    policy_code text NOT NULL,
    version_no integer NOT NULL,
    status text NOT NULL,
    policy_payload jsonb NOT NULL,
    effective_from timestamptz NOT NULL,
    effective_to timestamptz NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_refund_policy UNIQUE (city_id, policy_code, version_no)
);

CREATE TABLE booking.booking (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_code text NOT NULL UNIQUE,
    customer_profile_id uuid NOT NULL REFERENCES customer.customer_profile(id),
    city_id uuid NOT NULL REFERENCES master.city(id),
    service_type text NOT NULL,
    pickup_zone_id uuid NULL REFERENCES master.service_zone(id),
    drop_zone_id uuid NULL REFERENCES master.service_zone(id),
    pickup_address_text text NOT NULL,
    drop_address_text text NULL,
    pickup_latitude numeric(9,6) NOT NULL,
    pickup_longitude numeric(9,6) NOT NULL,
    drop_latitude numeric(9,6) NULL,
    drop_longitude numeric(9,6) NULL,
    pickup_geo geography(Point, 4326) NOT NULL,
    drop_geo geography(Point, 4326) NULL,
    scheduled_pickup_at timestamptz NOT NULL,
    quoted_duration_minutes integer NULL,
    status text NOT NULL,
    current_assignment_id uuid NULL,
    current_trip_id uuid NULL,
    cancellation_policy_id uuid NOT NULL REFERENCES commercial.cancellation_policy(id),
    refund_policy_id uuid NOT NULL REFERENCES commercial.refund_policy(id),
    fare_snapshot_id uuid NULL,
    current_total_paise bigint NOT NULL,
    payment_state text NOT NULL DEFAULT 'PENDING',
    is_active boolean NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    row_version bigint NOT NULL DEFAULT 0,
    metadata jsonb NOT NULL DEFAULT '{}'::jsonb
);

CREATE INDEX idx_booking_customer_pickup_desc
    ON booking.booking (customer_profile_id, scheduled_pickup_at DESC);
CREATE INDEX idx_booking_status_pickup_at
    ON booking.booking (status, scheduled_pickup_at);
CREATE INDEX idx_booking_city_service_status_pickup
    ON booking.booking (city_id, service_type, status, scheduled_pickup_at);
CREATE INDEX idx_booking_pickup_geo
    ON booking.booking USING gist (pickup_geo);
CREATE INDEX idx_booking_drop_geo
    ON booking.booking USING gist (drop_geo)
    WHERE drop_geo IS NOT NULL;
CREATE INDEX idx_booking_active_schedule
    ON booking.booking (scheduled_pickup_at)
    WHERE status IN ('CONFIRMED', 'PENDING_ASSIGNMENT', 'ASSIGNED', 'DRIVER_ARRIVING', 'ARRIVED', 'TRIP_START_PENDING');

CREATE TABLE commercial.booking_fare_snapshot (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id uuid NOT NULL UNIQUE REFERENCES booking.booking(id),
    quote_id uuid NULL REFERENCES commercial.quote(id),
    pricing_plan_id uuid NOT NULL REFERENCES commercial.pricing_plan(id),
    tax_profile_id uuid NOT NULL REFERENCES commercial.tax_profile(id),
    service_type text NOT NULL,
    scheduled_pickup_at timestamptz NOT NULL,
    quoted_duration_minutes integer NULL,
    subtotal_paise bigint NOT NULL,
    tax_paise bigint NOT NULL,
    total_paise bigint NOT NULL,
    snapshot_payload jsonb NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_booking_fare_snapshot_quote
    ON commercial.booking_fare_snapshot (quote_id)
    WHERE quote_id IS NOT NULL;

CREATE TABLE commercial.fare_adjustment (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id uuid NOT NULL REFERENCES booking.booking(id),
    trip_id uuid NULL,
    adjustment_type text NOT NULL,
    direction text NOT NULL,
    amount_paise bigint NOT NULL,
    reason_code text NOT NULL,
    reason_note text NULL,
    is_customer_visible boolean NOT NULL,
    approved_by_user_id uuid NULL REFERENCES iam.user_account(id),
    created_by_user_id uuid NOT NULL REFERENCES iam.user_account(id),
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_fare_adjustment_booking_created
    ON commercial.fare_adjustment (booking_id, created_at DESC);
CREATE INDEX idx_fare_adjustment_trip_created
    ON commercial.fare_adjustment (trip_id, created_at DESC)
    WHERE trip_id IS NOT NULL;

CREATE TABLE commercial.payout_plan (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    city_id uuid NOT NULL REFERENCES master.city(id),
    plan_code text NOT NULL,
    version_no integer NOT NULL,
    status text NOT NULL,
    effective_from timestamptz NOT NULL,
    effective_to timestamptz NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_payout_plan UNIQUE (city_id, plan_code, version_no)
);

CREATE TABLE commercial.payout_rule (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    payout_plan_id uuid NOT NULL REFERENCES commercial.payout_plan(id),
    service_type text NOT NULL,
    service_zone_id uuid NULL REFERENCES master.service_zone(id),
    airport_zone_band_id uuid NULL REFERENCES master.airport_zone_band(id),
    lead_time_bucket text NULL,
    rule_type text NOT NULL,
    amount_paise bigint NOT NULL,
    is_active boolean NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_payout_rule_plan_service_rule
    ON commercial.payout_rule (payout_plan_id, service_type, rule_type, is_active);

CREATE TABLE booking.booking_passenger_context (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id uuid NOT NULL UNIQUE REFERENCES booking.booking(id),
    passenger_name text NOT NULL,
    passenger_mobile_e164 text NULL,
    is_self_booking boolean NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE booking.booking_instruction (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id uuid NOT NULL REFERENCES booking.booking(id),
    instruction_type text NOT NULL,
    instruction_text text NOT NULL,
    created_by_user_id uuid NOT NULL REFERENCES iam.user_account(id),
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_booking_instruction_booking_created
    ON booking.booking_instruction (booking_id, created_at ASC);

CREATE TABLE booking.booking_state_log (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id uuid NOT NULL REFERENCES booking.booking(id),
    from_status text NULL,
    to_status text NOT NULL,
    reason_code text NULL,
    reason_note text NULL,
    changed_by_user_id uuid NULL REFERENCES iam.user_account(id),
    changed_at timestamptz NOT NULL
);

CREATE INDEX idx_booking_state_log_booking_changed
    ON booking.booking_state_log (booking_id, changed_at ASC);
CREATE INDEX idx_booking_state_log_to_status_changed
    ON booking.booking_state_log (to_status, changed_at DESC);

CREATE TABLE booking.assignment (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id uuid NOT NULL REFERENCES booking.booking(id),
    driver_profile_id uuid NULL REFERENCES driver.driver_profile(id),
    assignment_sequence_no integer NOT NULL,
    status text NOT NULL,
    is_current boolean NOT NULL,
    assigned_at timestamptz NULL,
    driver_eta_seconds integer NULL,
    risk_status text NOT NULL,
    rescue_required boolean NOT NULL DEFAULT false,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    row_version bigint NOT NULL DEFAULT 0,
    CONSTRAINT uq_assignment_booking_sequence UNIQUE (booking_id, assignment_sequence_no)
);

CREATE UNIQUE INDEX uq_assignment_booking_current
    ON booking.assignment (booking_id)
    WHERE is_current = true;
CREATE INDEX idx_assignment_booking_sequence_desc
    ON booking.assignment (booking_id, assignment_sequence_no DESC);
CREATE INDEX idx_assignment_driver_status
    ON booking.assignment (driver_profile_id, status)
    WHERE driver_profile_id IS NOT NULL;
CREATE INDEX idx_assignment_risk_rescue_updated
    ON booking.assignment (risk_status, rescue_required, updated_at DESC);

CREATE TABLE commercial.driver_payout_preview (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    assignment_id uuid NOT NULL UNIQUE REFERENCES booking.assignment(id),
    driver_profile_id uuid NOT NULL REFERENCES driver.driver_profile(id),
    payout_plan_id uuid NOT NULL REFERENCES commercial.payout_plan(id),
    service_type text NOT NULL,
    preview_total_paise bigint NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    snapshot_payload jsonb NOT NULL
);

CREATE INDEX idx_driver_payout_preview_driver_created
    ON commercial.driver_payout_preview (driver_profile_id, created_at DESC);

CREATE TABLE commercial.driver_payout_component (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_payout_preview_id uuid NOT NULL REFERENCES commercial.driver_payout_preview(id),
    component_type text NOT NULL,
    display_label text NOT NULL,
    amount_paise bigint NOT NULL,
    sort_order integer NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_driver_payout_component_preview_sort
    ON commercial.driver_payout_component (driver_payout_preview_id, sort_order);

CREATE TABLE booking.assignment_attempt (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    assignment_id uuid NOT NULL REFERENCES booking.assignment(id),
    driver_profile_id uuid NOT NULL REFERENCES driver.driver_profile(id),
    attempt_status text NOT NULL,
    offer_sequence_no integer NOT NULL,
    offered_at timestamptz NOT NULL,
    responded_at timestamptz NULL,
    response_reason_code text NULL,
    candidate_score numeric(8,4) NULL,
    candidate_snapshot jsonb NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_assignment_attempt UNIQUE (assignment_id, driver_profile_id, offer_sequence_no)
);

CREATE INDEX idx_assignment_attempt_assignment_offered
    ON booking.assignment_attempt (assignment_id, offered_at ASC);
CREATE INDEX idx_assignment_attempt_driver_offered
    ON booking.assignment_attempt (driver_profile_id, offered_at DESC);
CREATE INDEX idx_assignment_attempt_status_offered
    ON booking.assignment_attempt (attempt_status, offered_at DESC);

CREATE TABLE booking.driver_candidate_snapshot (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    assignment_id uuid NOT NULL REFERENCES booking.assignment(id),
    driver_profile_id uuid NOT NULL REFERENCES driver.driver_profile(id),
    snapshot_rank integer NOT NULL,
    score numeric(8,4) NULL,
    snapshot_payload jsonb NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_driver_candidate_snapshot_assignment_rank
    ON booking.driver_candidate_snapshot (assignment_id, snapshot_rank);

CREATE TABLE trip.trip (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id uuid NOT NULL UNIQUE REFERENCES booking.booking(id),
    assignment_id uuid NOT NULL REFERENCES booking.assignment(id),
    driver_profile_id uuid NOT NULL REFERENCES driver.driver_profile(id),
    customer_profile_id uuid NOT NULL REFERENCES customer.customer_profile(id),
    status text NOT NULL,
    arrival_marked_at timestamptz NULL,
    customer_start_confirmed_at timestamptz NULL,
    started_at timestamptz NULL,
    completed_at timestamptz NULL,
    abandoned_at timestamptz NULL,
    final_duration_minutes integer NULL,
    final_total_paise bigint NULL,
    under_review_reason_code text NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    row_version bigint NOT NULL DEFAULT 0
);

CREATE INDEX idx_trip_driver_status
    ON trip.trip (driver_profile_id, status);
CREATE INDEX idx_trip_customer_status
    ON trip.trip (customer_profile_id, status);
CREATE INDEX idx_trip_status_created
    ON trip.trip (status, created_at DESC);

CREATE TABLE trip.trip_event (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    trip_id uuid NOT NULL REFERENCES trip.trip(id),
    event_type text NOT NULL,
    event_at timestamptz NOT NULL,
    actor_type text NULL,
    actor_user_id uuid NULL REFERENCES iam.user_account(id),
    event_payload jsonb NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_trip_event_trip_at
    ON trip.trip_event (trip_id, event_at ASC);
CREATE INDEX idx_trip_event_type_at
    ON trip.trip_event (event_type, event_at DESC);

CREATE TABLE trip.handover_checklist (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    trip_id uuid NOT NULL UNIQUE REFERENCES trip.trip(id),
    confirmed_driver_match boolean NOT NULL DEFAULT false,
    fuel_note text NULL,
    instruction_note text NULL,
    visible_concern_note text NULL,
    confirmed_by_customer_id uuid NOT NULL REFERENCES customer.customer_profile(id),
    confirmed_at timestamptz NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE trip.tracking_session (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    trip_id uuid NOT NULL UNIQUE REFERENCES trip.trip(id),
    status text NOT NULL,
    started_at timestamptz NOT NULL,
    ended_at timestamptz NULL,
    last_ping_at timestamptz NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_tracking_session_status_last_ping
    ON trip.tracking_session (status, last_ping_at DESC);

CREATE TABLE trip.location_ping (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    tracking_session_id uuid NOT NULL REFERENCES trip.tracking_session(id),
    driver_profile_id uuid NOT NULL REFERENCES driver.driver_profile(id),
    ping_at timestamptz NOT NULL,
    latitude numeric(9,6) NOT NULL,
    longitude numeric(9,6) NOT NULL,
    location_geo geography(Point, 4326) NOT NULL,
    heading_degrees numeric(5,2) NULL,
    speed_kph numeric(6,2) NULL,
    accuracy_meters numeric(6,2) NULL,
    source_type text NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_location_ping_tracking_ping_at
    ON trip.location_ping (tracking_session_id, ping_at DESC);
CREATE INDEX idx_location_ping_driver_ping_at
    ON trip.location_ping (driver_profile_id, ping_at DESC);
CREATE INDEX idx_location_ping_geo
    ON trip.location_ping USING gist (location_geo);

CREATE TABLE trip.trip_share_link (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    trip_id uuid NOT NULL REFERENCES trip.trip(id),
    customer_profile_id uuid NOT NULL REFERENCES customer.customer_profile(id),
    share_token_hash text NOT NULL UNIQUE,
    status text NOT NULL,
    expires_at timestamptz NOT NULL,
    revoked_at timestamptz NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_trip_share_link_trip_status
    ON trip.trip_share_link (trip_id, status);
CREATE INDEX idx_trip_share_link_expires
    ON trip.trip_share_link (expires_at);

CREATE TABLE finance.invoice (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id uuid NOT NULL UNIQUE REFERENCES booking.booking(id),
    trip_id uuid NULL REFERENCES trip.trip(id),
    invoice_number text NOT NULL UNIQUE,
    currency_code text NOT NULL,
    subtotal_paise bigint NOT NULL,
    tax_paise bigint NOT NULL,
    total_paise bigint NOT NULL,
    invoice_status text NOT NULL,
    issued_at timestamptz NOT NULL,
    storage_key text NULL,
    snapshot_payload jsonb NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_invoice_issued_at
    ON finance.invoice (issued_at DESC);

CREATE TABLE finance.payment_order (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id uuid NOT NULL REFERENCES booking.booking(id),
    trip_id uuid NULL REFERENCES trip.trip(id),
    invoice_id uuid NULL REFERENCES finance.invoice(id),
    provider_name text NOT NULL,
    provider_order_id text NULL,
    currency_code text NOT NULL,
    amount_paise bigint NOT NULL,
    status text NOT NULL,
    payment_method_type text NULL,
    expires_at timestamptz NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    row_version bigint NOT NULL DEFAULT 0,
    metadata jsonb NOT NULL DEFAULT '{}'::jsonb
);

CREATE UNIQUE INDEX uq_payment_order_provider_order
    ON finance.payment_order (provider_name, provider_order_id)
    WHERE provider_order_id IS NOT NULL;
CREATE INDEX idx_payment_order_booking_created
    ON finance.payment_order (booking_id, created_at DESC);
CREATE INDEX idx_payment_order_status_created
    ON finance.payment_order (status, created_at DESC);

CREATE TABLE finance.payment_transaction (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_order_id uuid NOT NULL REFERENCES finance.payment_order(id),
    provider_transaction_id text NULL,
    transaction_type text NOT NULL,
    status text NOT NULL,
    amount_paise bigint NOT NULL,
    provider_event_at timestamptz NULL,
    provider_payload jsonb NOT NULL,
    recorded_at timestamptz NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_payment_transaction_provider
    ON finance.payment_transaction (provider_transaction_id)
    WHERE provider_transaction_id IS NOT NULL;
CREATE INDEX idx_payment_transaction_order_recorded
    ON finance.payment_transaction (payment_order_id, recorded_at ASC);
CREATE INDEX idx_payment_transaction_status_recorded
    ON finance.payment_transaction (status, recorded_at DESC);

CREATE TABLE support.support_ticket (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_code text NOT NULL UNIQUE,
    ticket_type text NOT NULL,
    booking_id uuid NULL REFERENCES booking.booking(id),
    trip_id uuid NULL REFERENCES trip.trip(id),
    customer_profile_id uuid NULL REFERENCES customer.customer_profile(id),
    driver_profile_id uuid NULL REFERENCES driver.driver_profile(id),
    category_code text NOT NULL,
    sub_category_code text NULL,
    severity text NOT NULL,
    status text NOT NULL,
    current_owner_user_id uuid NULL REFERENCES iam.user_account(id),
    subject text NULL,
    description text NULL,
    opened_at timestamptz NOT NULL,
    resolved_at timestamptz NULL,
    closed_at timestamptz NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    row_version bigint NOT NULL DEFAULT 0
);

CREATE INDEX idx_support_ticket_status_severity_opened
    ON support.support_ticket (status, severity, opened_at DESC);
CREATE INDEX idx_support_ticket_booking
    ON support.support_ticket (booking_id)
    WHERE booking_id IS NOT NULL;
CREATE INDEX idx_support_ticket_trip
    ON support.support_ticket (trip_id)
    WHERE trip_id IS NOT NULL;
CREATE INDEX idx_support_ticket_owner_status
    ON support.support_ticket (current_owner_user_id, status)
    WHERE current_owner_user_id IS NOT NULL;

CREATE TABLE finance.refund_request (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_order_id uuid NOT NULL REFERENCES finance.payment_order(id),
    booking_id uuid NOT NULL REFERENCES booking.booking(id),
    trip_id uuid NULL REFERENCES trip.trip(id),
    support_ticket_id uuid NULL REFERENCES support.support_ticket(id),
    requested_by_user_id uuid NOT NULL REFERENCES iam.user_account(id),
    reason_code text NOT NULL,
    reason_note text NULL,
    requested_amount_paise bigint NOT NULL,
    status text NOT NULL,
    requested_at timestamptz NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_refund_request_booking_requested
    ON finance.refund_request (booking_id, requested_at DESC);
CREATE INDEX idx_refund_request_status_requested
    ON finance.refund_request (status, requested_at DESC);
CREATE INDEX idx_refund_request_ticket
    ON finance.refund_request (support_ticket_id)
    WHERE support_ticket_id IS NOT NULL;

CREATE TABLE finance.refund_decision (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    refund_request_id uuid NOT NULL UNIQUE REFERENCES finance.refund_request(id),
    decision_status text NOT NULL,
    approved_amount_paise bigint NULL,
    decided_by_user_id uuid NOT NULL REFERENCES iam.user_account(id),
    decision_reason_code text NOT NULL,
    decision_note text NULL,
    decided_at timestamptz NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_refund_decision_decider_at
    ON finance.refund_decision (decided_by_user_id, decided_at DESC);

CREATE TABLE finance.refund_transaction (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    refund_request_id uuid NOT NULL REFERENCES finance.refund_request(id),
    provider_refund_id text NULL,
    amount_paise bigint NOT NULL,
    status text NOT NULL,
    provider_payload jsonb NOT NULL,
    processed_at timestamptz NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_refund_transaction_provider
    ON finance.refund_transaction (provider_refund_id)
    WHERE provider_refund_id IS NOT NULL;
CREATE INDEX idx_refund_transaction_request_created
    ON finance.refund_transaction (refund_request_id, created_at ASC);
CREATE INDEX idx_refund_transaction_status_created
    ON finance.refund_transaction (status, created_at DESC);

CREATE TABLE finance.driver_earning_ledger (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_profile_id uuid NOT NULL REFERENCES driver.driver_profile(id),
    trip_id uuid NOT NULL UNIQUE REFERENCES trip.trip(id),
    assignment_id uuid NOT NULL REFERENCES booking.assignment(id),
    payout_preview_id uuid NULL REFERENCES commercial.driver_payout_preview(id),
    ledger_status text NOT NULL,
    gross_payout_paise bigint NOT NULL,
    adjustment_paise bigint NOT NULL,
    net_payout_paise bigint NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    locked_at timestamptz NULL,
    settled_at timestamptz NULL,
    snapshot_payload jsonb NOT NULL
);

CREATE INDEX idx_driver_earning_ledger_driver_created
    ON finance.driver_earning_ledger (driver_profile_id, created_at DESC);
CREATE INDEX idx_driver_earning_ledger_status_created
    ON finance.driver_earning_ledger (ledger_status, created_at DESC);
CREATE INDEX idx_driver_earning_ledger_settled
    ON finance.driver_earning_ledger (settled_at)
    WHERE settled_at IS NOT NULL;

CREATE TABLE finance.driver_payout_batch (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    batch_code text NOT NULL UNIQUE,
    city_id uuid NULL REFERENCES master.city(id),
    status text NOT NULL,
    scheduled_settlement_at timestamptz NULL,
    processed_at timestamptz NULL,
    created_by_user_id uuid NOT NULL REFERENCES iam.user_account(id),
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_driver_payout_batch_status_created
    ON finance.driver_payout_batch (status, created_at DESC);

CREATE TABLE finance.driver_payout_batch_item (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_payout_batch_id uuid NOT NULL REFERENCES finance.driver_payout_batch(id),
    driver_earning_ledger_id uuid NOT NULL UNIQUE REFERENCES finance.driver_earning_ledger(id),
    driver_profile_id uuid NOT NULL REFERENCES driver.driver_profile(id),
    amount_paise bigint NOT NULL,
    item_status text NOT NULL,
    provider_transfer_reference text NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_driver_payout_batch_item_batch_status
    ON finance.driver_payout_batch_item (driver_payout_batch_id, item_status);
CREATE INDEX idx_driver_payout_batch_item_driver_created
    ON finance.driver_payout_batch_item (driver_profile_id, created_at DESC);

CREATE TABLE support.support_ticket_note (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    support_ticket_id uuid NOT NULL REFERENCES support.support_ticket(id),
    note_type text NOT NULL,
    is_internal boolean NOT NULL DEFAULT true,
    author_user_id uuid NULL REFERENCES iam.user_account(id),
    author_role text NULL,
    note_text text NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_support_ticket_note_ticket_created
    ON support.support_ticket_note (support_ticket_id, created_at ASC);

CREATE TABLE support.incident_case (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    support_ticket_id uuid NOT NULL UNIQUE REFERENCES support.support_ticket(id),
    booking_id uuid NULL REFERENCES booking.booking(id),
    trip_id uuid NULL REFERENCES trip.trip(id),
    incident_type text NOT NULL,
    severity text NOT NULL,
    status text NOT NULL,
    owner_user_id uuid NULL REFERENCES iam.user_account(id),
    opened_at timestamptz NOT NULL,
    resolved_at timestamptz NULL,
    closed_at timestamptz NULL,
    summary text NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    row_version bigint NOT NULL DEFAULT 0
);

CREATE INDEX idx_incident_case_status_severity_opened
    ON support.incident_case (status, severity, opened_at DESC);
CREATE INDEX idx_incident_case_owner_status
    ON support.incident_case (owner_user_id, status)
    WHERE owner_user_id IS NOT NULL;

CREATE TABLE support.case_evidence (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    incident_case_id uuid NOT NULL REFERENCES support.incident_case(id),
    evidence_type text NOT NULL,
    storage_key text NULL,
    external_reference text NULL,
    uploaded_by_user_id uuid NULL REFERENCES iam.user_account(id),
    uploaded_at timestamptz NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_case_evidence_incident_uploaded
    ON support.case_evidence (incident_case_id, uploaded_at ASC);

CREATE TABLE support.resolution_action (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    support_ticket_id uuid NULL REFERENCES support.support_ticket(id),
    incident_case_id uuid NULL REFERENCES support.incident_case(id),
    action_type text NOT NULL,
    action_payload jsonb NOT NULL,
    performed_by_user_id uuid NOT NULL REFERENCES iam.user_account(id),
    performed_at timestamptz NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_resolution_action_ticket_performed
    ON support.resolution_action (support_ticket_id, performed_at ASC)
    WHERE support_ticket_id IS NOT NULL;
CREATE INDEX idx_resolution_action_incident_performed
    ON support.resolution_action (incident_case_id, performed_at ASC)
    WHERE incident_case_id IS NOT NULL;

CREATE TABLE commercial.override_audit (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type text NOT NULL,
    entity_id uuid NOT NULL,
    override_type text NOT NULL,
    previous_value jsonb NOT NULL,
    new_value jsonb NOT NULL,
    reason_code text NOT NULL,
    reason_note text NULL,
    ticket_id uuid NULL REFERENCES support.support_ticket(id),
    actor_user_id uuid NOT NULL REFERENCES iam.user_account(id),
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_override_audit_entity_created
    ON commercial.override_audit (entity_type, entity_id, created_at DESC);
CREATE INDEX idx_override_audit_actor_created
    ON commercial.override_audit (actor_user_id, created_at DESC);

CREATE TABLE comms.notification_template (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    template_code text NOT NULL,
    channel text NOT NULL,
    language_code text NOT NULL,
    subject_template text NULL,
    body_template text NOT NULL,
    is_active boolean NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_notification_template UNIQUE (template_code, channel, language_code)
);

CREATE TABLE comms.notification_event (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type text NOT NULL,
    user_account_id uuid NULL REFERENCES iam.user_account(id),
    booking_id uuid NULL REFERENCES booking.booking(id),
    trip_id uuid NULL REFERENCES trip.trip(id),
    payload jsonb NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_notification_event_type_created
    ON comms.notification_event (event_type, created_at DESC);
CREATE INDEX idx_notification_event_user_created
    ON comms.notification_event (user_account_id, created_at DESC)
    WHERE user_account_id IS NOT NULL;

CREATE TABLE comms.notification_delivery (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    notification_event_id uuid NOT NULL REFERENCES comms.notification_event(id),
    channel text NOT NULL,
    target_address text NOT NULL,
    template_id uuid NULL REFERENCES comms.notification_template(id),
    status text NOT NULL,
    provider_message_id text NULL,
    error_code text NULL,
    queued_at timestamptz NOT NULL,
    sent_at timestamptz NULL,
    delivered_at timestamptz NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_notification_delivery_event_channel
    ON comms.notification_delivery (notification_event_id, channel);
CREATE INDEX idx_notification_delivery_status_queued
    ON comms.notification_delivery (status, queued_at DESC);
CREATE INDEX idx_notification_delivery_provider_message
    ON comms.notification_delivery (provider_message_id)
    WHERE provider_message_id IS NOT NULL;

CREATE TABLE audit.audit_log (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_user_id uuid NULL REFERENCES iam.user_account(id),
    actor_role_code text NULL,
    action_type text NOT NULL,
    entity_type text NOT NULL,
    entity_id uuid NOT NULL,
    old_value jsonb NULL,
    new_value jsonb NULL,
    reason_code text NULL,
    note text NULL,
    request_id text NULL,
    source_system text NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_log_entity_created
    ON audit.audit_log (entity_type, entity_id, created_at DESC);
CREATE INDEX idx_audit_log_actor_created
    ON audit.audit_log (actor_user_id, created_at DESC)
    WHERE actor_user_id IS NOT NULL;
CREATE INDEX idx_audit_log_action_created
    ON audit.audit_log (action_type, created_at DESC);

CREATE TABLE audit.domain_event_log (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type text NOT NULL,
    aggregate_id uuid NOT NULL,
    event_type text NOT NULL,
    event_version integer NOT NULL,
    event_payload jsonb NOT NULL,
    occurred_at timestamptz NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_domain_event_log_aggregate
    ON audit.domain_event_log (aggregate_type, aggregate_id, occurred_at ASC);
CREATE INDEX idx_domain_event_log_event_type
    ON audit.domain_event_log (event_type, occurred_at DESC);

CREATE TABLE audit.outbox_event (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type text NOT NULL,
    aggregate_id uuid NOT NULL,
    event_type text NOT NULL,
    payload jsonb NOT NULL,
    outbox_status text NOT NULL,
    available_at timestamptz NOT NULL,
    processed_at timestamptz NULL,
    retry_count integer NOT NULL DEFAULT 0,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_outbox_event_status_available
    ON audit.outbox_event (outbox_status, available_at ASC);
CREATE INDEX idx_outbox_event_event_type_created
    ON audit.outbox_event (event_type, created_at DESC);

CREATE TABLE audit.idempotency_key (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key text NOT NULL,
    request_scope text NOT NULL,
    request_hash text NOT NULL,
    response_reference_type text NULL,
    response_reference_id uuid NULL,
    status text NOT NULL,
    expires_at timestamptz NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uq_idempotency_key_scope_key UNIQUE (request_scope, idempotency_key)
);

CREATE INDEX idx_idempotency_key_expires
    ON audit.idempotency_key (expires_at)
    WHERE expires_at IS NOT NULL;

CREATE TABLE analytics.fact_booking_daily (
    fact_date date NOT NULL,
    city_id uuid NOT NULL REFERENCES master.city(id),
    service_type text NOT NULL,
    zone_id uuid NULL REFERENCES master.service_zone(id),
    booking_count bigint NOT NULL,
    fulfilled_count bigint NOT NULL,
    cancelled_count bigint NOT NULL,
    failed_fulfillment_count bigint NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (fact_date, city_id, service_type, zone_id)
);

CREATE TABLE analytics.fact_trip_daily (
    fact_date date NOT NULL,
    city_id uuid NOT NULL REFERENCES master.city(id),
    service_type text NOT NULL,
    trip_count bigint NOT NULL,
    gross_revenue_paise bigint NOT NULL,
    refund_paise bigint NOT NULL,
    driver_payout_paise bigint NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (fact_date, city_id, service_type)
);

CREATE TABLE analytics.fact_support_daily (
    fact_date date NOT NULL,
    city_id uuid NOT NULL REFERENCES master.city(id),
    open_ticket_count bigint NOT NULL DEFAULT 0,
    resolved_ticket_count bigint NOT NULL DEFAULT 0,
    incident_count bigint NOT NULL DEFAULT 0,
    refund_request_count bigint NOT NULL DEFAULT 0,
    created_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (fact_date, city_id)
);

ALTER TABLE booking.booking
    ADD CONSTRAINT fk_booking_current_assignment
        FOREIGN KEY (current_assignment_id) REFERENCES booking.assignment(id);
ALTER TABLE booking.booking
    ADD CONSTRAINT fk_booking_current_trip
        FOREIGN KEY (current_trip_id) REFERENCES trip.trip(id);
ALTER TABLE booking.booking
    ADD CONSTRAINT fk_booking_fare_snapshot
        FOREIGN KEY (fare_snapshot_id) REFERENCES commercial.booking_fare_snapshot(id);

ALTER TABLE commercial.fare_adjustment
    ADD CONSTRAINT fk_fare_adjustment_trip
        FOREIGN KEY (trip_id) REFERENCES trip.trip(id);

ALTER TABLE driver.driver_availability_status
    ADD CONSTRAINT fk_driver_availability_booking
        FOREIGN KEY (current_booking_id) REFERENCES booking.booking(id);
ALTER TABLE driver.driver_availability_status
    ADD CONSTRAINT fk_driver_availability_assignment
        FOREIGN KEY (current_assignment_id) REFERENCES booking.assignment(id);
ALTER TABLE driver.driver_availability_status
    ADD CONSTRAINT fk_driver_availability_trip
        FOREIGN KEY (current_trip_id) REFERENCES trip.trip(id);

CREATE TRIGGER trg_device_registration_updated_at
    BEFORE UPDATE ON iam.device_registration
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_city_updated_at
    BEFORE UPDATE ON master.city
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_service_zone_updated_at
    BEFORE UPDATE ON master.service_zone
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_serviceability_rule_updated_at
    BEFORE UPDATE ON master.serviceability_rule
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_customer_profile_updated_at
    BEFORE UPDATE ON customer.customer_profile
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_saved_location_updated_at
    BEFORE UPDATE ON customer.saved_location
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_driver_profile_updated_at
    BEFORE UPDATE ON driver.driver_profile
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_driver_document_updated_at
    BEFORE UPDATE ON driver.driver_document
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_driver_bank_account_updated_at
    BEFORE UPDATE ON driver.driver_bank_account
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_driver_zone_eligibility_updated_at
    BEFORE UPDATE ON driver.driver_zone_eligibility
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_booking_updated_at
    BEFORE UPDATE ON booking.booking
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_assignment_updated_at
    BEFORE UPDATE ON booking.assignment
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_trip_updated_at
    BEFORE UPDATE ON trip.trip
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_payment_order_updated_at
    BEFORE UPDATE ON finance.payment_order
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_support_ticket_updated_at
    BEFORE UPDATE ON support.support_ticket
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
CREATE TRIGGER trg_incident_case_updated_at
    BEFORE UPDATE ON support.incident_case
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_saved_location_sync_geo
    BEFORE INSERT OR UPDATE ON customer.saved_location
    FOR EACH ROW EXECUTE FUNCTION public.saved_location_sync_geo();
CREATE TRIGGER trg_booking_sync_pickup_drop_geo
    BEFORE INSERT OR UPDATE ON booking.booking
    FOR EACH ROW EXECUTE FUNCTION public.booking_sync_pickup_drop_geo();
CREATE TRIGGER trg_location_ping_sync_geo
    BEFORE INSERT OR UPDATE ON trip.location_ping
    FOR EACH ROW EXECUTE FUNCTION public.location_ping_sync_geo();

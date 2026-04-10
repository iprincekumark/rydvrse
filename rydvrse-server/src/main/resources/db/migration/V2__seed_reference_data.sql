INSERT INTO iam.role (id, role_code, role_name, is_system_role)
VALUES
    ('10000000-0000-0000-0000-000000000001', 'OPS_EXECUTIVE', 'Operations Executive', true),
    ('10000000-0000-0000-0000-000000000002', 'SUPPORT_SPECIALIST', 'Support Specialist', true),
    ('10000000-0000-0000-0000-000000000003', 'DRIVER_REVIEWER', 'Driver Reviewer', true),
    ('10000000-0000-0000-0000-000000000004', 'FINANCE_ADMIN', 'Finance Administrator', true),
    ('10000000-0000-0000-0000-000000000005', 'CITY_MANAGER', 'City Manager', true),
    ('10000000-0000-0000-0000-000000000006', 'SUPER_ADMIN', 'Super Administrator', true)
ON CONFLICT (role_code) DO NOTHING;

INSERT INTO iam.permission (id, permission_code, permission_name, resource_type)
VALUES
    ('11000000-0000-0000-0000-000000000001', 'dashboard.read', 'Read dashboard', 'DASHBOARD'),
    ('11000000-0000-0000-0000-000000000002', 'booking.read', 'Read bookings', 'BOOKING'),
    ('11000000-0000-0000-0000-000000000003', 'booking.reassign', 'Manual reassignment', 'BOOKING'),
    ('11000000-0000-0000-0000-000000000004', 'booking.cancel', 'Admin cancel booking', 'BOOKING'),
    ('11000000-0000-0000-0000-000000000005', 'support.manage', 'Manage support', 'SUPPORT'),
    ('11000000-0000-0000-0000-000000000006', 'incident.manage', 'Manage incidents', 'INCIDENT'),
    ('11000000-0000-0000-0000-000000000007', 'driver.review', 'Review drivers', 'DRIVER'),
    ('11000000-0000-0000-0000-000000000008', 'finance.refund', 'Refund management', 'FINANCE'),
    ('11000000-0000-0000-0000-000000000009', 'pricing.manage', 'Manage pricing', 'PRICING'),
    ('11000000-0000-0000-0000-000000000010', 'payout.manage', 'Manage payout', 'PAYOUT'),
    ('11000000-0000-0000-0000-000000000011', 'serviceability.manage', 'Manage serviceability', 'SERVICEABILITY'),
    ('11000000-0000-0000-0000-000000000012', 'audit.read', 'Read audit logs', 'AUDIT'),
    ('11000000-0000-0000-0000-000000000013', 'report.read', 'Read reports', 'REPORT')
ON CONFLICT (permission_code) DO NOTHING;

INSERT INTO iam.role_permission (role_id, permission_id)
SELECT role_id, permission_id
FROM (
    VALUES
        ('10000000-0000-0000-0000-000000000001'::uuid, '11000000-0000-0000-0000-000000000001'::uuid),
        ('10000000-0000-0000-0000-000000000001'::uuid, '11000000-0000-0000-0000-000000000002'::uuid),
        ('10000000-0000-0000-0000-000000000001'::uuid, '11000000-0000-0000-0000-000000000003'::uuid),
        ('10000000-0000-0000-0000-000000000001'::uuid, '11000000-0000-0000-0000-000000000004'::uuid),
        ('10000000-0000-0000-0000-000000000001'::uuid, '11000000-0000-0000-0000-000000000005'::uuid),
        ('10000000-0000-0000-0000-000000000002'::uuid, '11000000-0000-0000-0000-000000000005'::uuid),
        ('10000000-0000-0000-0000-000000000002'::uuid, '11000000-0000-0000-0000-000000000006'::uuid),
        ('10000000-0000-0000-0000-000000000003'::uuid, '11000000-0000-0000-0000-000000000007'::uuid),
        ('10000000-0000-0000-0000-000000000004'::uuid, '11000000-0000-0000-0000-000000000008'::uuid),
        ('10000000-0000-0000-0000-000000000005'::uuid, '11000000-0000-0000-0000-000000000009'::uuid),
        ('10000000-0000-0000-0000-000000000005'::uuid, '11000000-0000-0000-0000-000000000010'::uuid),
        ('10000000-0000-0000-0000-000000000005'::uuid, '11000000-0000-0000-0000-000000000011'::uuid),
        ('10000000-0000-0000-0000-000000000005'::uuid, '11000000-0000-0000-0000-000000000013'::uuid),
        ('10000000-0000-0000-0000-000000000006'::uuid, '11000000-0000-0000-0000-000000000001'::uuid),
        ('10000000-0000-0000-0000-000000000006'::uuid, '11000000-0000-0000-0000-000000000002'::uuid),
        ('10000000-0000-0000-0000-000000000006'::uuid, '11000000-0000-0000-0000-000000000003'::uuid),
        ('10000000-0000-0000-0000-000000000006'::uuid, '11000000-0000-0000-0000-000000000004'::uuid),
        ('10000000-0000-0000-0000-000000000006'::uuid, '11000000-0000-0000-0000-000000000005'::uuid),
        ('10000000-0000-0000-0000-000000000006'::uuid, '11000000-0000-0000-0000-000000000006'::uuid),
        ('10000000-0000-0000-0000-000000000006'::uuid, '11000000-0000-0000-0000-000000000007'::uuid),
        ('10000000-0000-0000-0000-000000000006'::uuid, '11000000-0000-0000-0000-000000000008'::uuid),
        ('10000000-0000-0000-0000-000000000006'::uuid, '11000000-0000-0000-0000-000000000009'::uuid),
        ('10000000-0000-0000-0000-000000000006'::uuid, '11000000-0000-0000-0000-000000000010'::uuid),
        ('10000000-0000-0000-0000-000000000006'::uuid, '11000000-0000-0000-0000-000000000011'::uuid),
        ('10000000-0000-0000-0000-000000000006'::uuid, '11000000-0000-0000-0000-000000000012'::uuid),
        ('10000000-0000-0000-0000-000000000006'::uuid, '11000000-0000-0000-0000-000000000013'::uuid)
) bindings(role_id, permission_id)
ON CONFLICT DO NOTHING;

INSERT INTO master.city (
    id,
    city_code,
    city_name,
    country_code,
    timezone,
    launch_status,
    currency_code
)
VALUES (
    '20000000-0000-0000-0000-000000000001',
    'BLR',
    'Bengaluru',
    'IN',
    'Asia/Kolkata',
    'ACTIVE',
    'INR'
)
ON CONFLICT (city_code) DO NOTHING;

INSERT INTO master.service_zone (
    id,
    city_id,
    zone_code,
    zone_name,
    zone_type,
    boundary_geom,
    centroid_geo,
    launch_status,
    sort_order,
    service_types_enabled
)
VALUES
    (
        '20000000-0000-0000-0000-000000000101',
        '20000000-0000-0000-0000-000000000001',
        'BLR_CORE',
        'Bengaluru Core',
        'CORE',
        ST_GeomFromText('MULTIPOLYGON(((77.56 12.90, 77.70 12.90, 77.70 13.02, 77.56 13.02, 77.56 12.90)))', 4326),
        ST_SetSRID(ST_MakePoint(77.63, 12.96), 4326)::geography,
        'ACTIVE',
        1,
        ARRAY['SCHEDULED_LOCAL', 'SCHEDULED_ONE_WAY', 'SCHEDULED_ROUND_TRIP', 'AIRPORT', 'LATE_NIGHT']
    ),
    (
        '20000000-0000-0000-0000-000000000102',
        '20000000-0000-0000-0000-000000000001',
        'BLR_EXTENDED',
        'Bengaluru Extended',
        'EXTENDED',
        ST_GeomFromText('MULTIPOLYGON(((77.48 12.84, 77.78 12.84, 77.78 13.08, 77.48 13.08, 77.48 12.84)))', 4326),
        ST_SetSRID(ST_MakePoint(77.63, 12.96), 4326)::geography,
        'ACTIVE',
        2,
        ARRAY['SCHEDULED_LOCAL', 'SCHEDULED_ONE_WAY', 'SCHEDULED_ROUND_TRIP', 'AIRPORT']
    ),
    (
        '20000000-0000-0000-0000-000000000103',
        '20000000-0000-0000-0000-000000000001',
        'BLR_AIRPORT',
        'Bengaluru Airport',
        'AIRPORT',
        ST_GeomFromText('MULTIPOLYGON(((77.64 13.16, 77.69 13.16, 77.69 13.21, 77.64 13.21, 77.64 13.16)))', 4326),
        ST_SetSRID(ST_MakePoint(77.67, 13.18), 4326)::geography,
        'ACTIVE',
        3,
        ARRAY['AIRPORT']
    )
ON CONFLICT (city_id, zone_code) DO NOTHING;

INSERT INTO master.airport_zone_band (id, city_id, band_code, band_name, service_zone_id, airport_code)
VALUES
    ('20000000-0000-0000-0000-000000000201', '20000000-0000-0000-0000-000000000001', 'A', 'Core to Airport', '20000000-0000-0000-0000-000000000101', 'BLR_T1'),
    ('20000000-0000-0000-0000-000000000202', '20000000-0000-0000-0000-000000000001', 'B', 'Extended to Airport', '20000000-0000-0000-0000-000000000102', 'BLR_T1')
ON CONFLICT (city_id, airport_code, service_zone_id) DO NOTHING;

INSERT INTO master.one_way_band (id, city_id, band_code, band_name, source_zone_id, destination_zone_id)
VALUES
    ('20000000-0000-0000-0000-000000000301', '20000000-0000-0000-0000-000000000001', 'A', 'Core to Core', '20000000-0000-0000-0000-000000000101', '20000000-0000-0000-0000-000000000101'),
    ('20000000-0000-0000-0000-000000000302', '20000000-0000-0000-0000-000000000001', 'B', 'Core to Extended', '20000000-0000-0000-0000-000000000101', '20000000-0000-0000-0000-000000000102'),
    ('20000000-0000-0000-0000-000000000303', '20000000-0000-0000-0000-000000000001', 'B', 'Extended to Core', '20000000-0000-0000-0000-000000000102', '20000000-0000-0000-0000-000000000101')
ON CONFLICT (city_id, source_zone_id, destination_zone_id) DO NOTHING;

INSERT INTO master.serviceability_rule (
    id,
    city_id,
    service_zone_id,
    service_type,
    is_enabled,
    min_lead_minutes,
    operating_start_local,
    operating_end_local
)
VALUES
    ('20000000-0000-0000-0000-000000000401', '20000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000101', 'SCHEDULED_LOCAL', true, 180, '05:00', '23:30'),
    ('20000000-0000-0000-0000-000000000402', '20000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000101', 'SCHEDULED_ONE_WAY', true, 180, '05:00', '23:30'),
    ('20000000-0000-0000-0000-000000000403', '20000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000101', 'SCHEDULED_ROUND_TRIP', true, 180, '05:00', '23:30'),
    ('20000000-0000-0000-0000-000000000404', '20000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000101', 'AIRPORT', true, 180, '00:00', '23:59'),
    ('20000000-0000-0000-0000-000000000405', '20000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000101', 'LATE_NIGHT', true, 180, '20:00', '23:59'),
    ('20000000-0000-0000-0000-000000000406', '20000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000102', 'SCHEDULED_LOCAL', true, 180, '05:00', '22:30'),
    ('20000000-0000-0000-0000-000000000407', '20000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000102', 'SCHEDULED_ONE_WAY', true, 180, '05:00', '22:30'),
    ('20000000-0000-0000-0000-000000000408', '20000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000102', 'SCHEDULED_ROUND_TRIP', true, 180, '05:00', '22:30'),
    ('20000000-0000-0000-0000-000000000409', '20000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000102', 'AIRPORT', true, 180, '00:00', '23:59')
ON CONFLICT (city_id, service_zone_id, service_type) DO NOTHING;

INSERT INTO master.feature_flag (id, flag_key, scope_type, scope_id, is_enabled, payload)
VALUES
    ('20000000-0000-0000-0000-000000000501', 'trip_share_links', 'GLOBAL', NULL, true, '{}'::jsonb),
    ('20000000-0000-0000-0000-000000000502', 'express_booking', 'GLOBAL', NULL, false, '{}'::jsonb),
    ('20000000-0000-0000-0000-000000000503', 'ops_override_trip_start', 'GLOBAL', NULL, false, '{}'::jsonb)
ON CONFLICT (flag_key, scope_type, scope_id) DO NOTHING;

INSERT INTO master.business_config (id, config_key, scope_type, scope_id, config_value, version_no, is_active)
VALUES
    ('20000000-0000-0000-0000-000000000601', 'support_contact', 'GLOBAL', NULL, '{"phone":"+919999999999","email":"support@rydvrse.local"}'::jsonb, 1, true),
    ('20000000-0000-0000-0000-000000000602', 'otp_rate_limit', 'GLOBAL', NULL, '{"per_hour":5,"cooldown_seconds":60}'::jsonb, 1, true),
    ('20000000-0000-0000-0000-000000000603', 'booking_defaults', 'CITY', '20000000-0000-0000-0000-000000000001', '{"default_duration_minutes":90,"minimum_duration_minutes":90,"quote_expiry_minutes":15}'::jsonb, 1, true)
ON CONFLICT (config_key, scope_type, scope_id, version_no) DO NOTHING;

INSERT INTO commercial.pricing_plan (
    id,
    city_id,
    plan_code,
    version_no,
    status,
    effective_from
)
VALUES (
    '30000000-0000-0000-0000-000000000001',
    '20000000-0000-0000-0000-000000000001',
    'BLR_STANDARD',
    1,
    'PUBLISHED',
    now() - interval '1 day'
)
ON CONFLICT (city_id, plan_code, version_no) DO NOTHING;

INSERT INTO commercial.pricing_rule (
    id,
    pricing_plan_id,
    service_type,
    service_zone_id,
    airport_zone_band_id,
    one_way_band_id,
    lead_time_bucket,
    rule_type,
    amount_paise,
    is_active
)
VALUES
    ('30000000-0000-0000-0000-000000000101', '30000000-0000-0000-0000-000000000001', 'SCHEDULED_LOCAL', '20000000-0000-0000-0000-000000000101', NULL, NULL, 'FLEX', 'BASE_90', 32900, true),
    ('30000000-0000-0000-0000-000000000102', '30000000-0000-0000-0000-000000000001', 'SCHEDULED_LOCAL', '20000000-0000-0000-0000-000000000102', NULL, NULL, 'FLEX', 'BASE_90', 37900, true),
    ('30000000-0000-0000-0000-000000000103', '30000000-0000-0000-0000-000000000001', 'SCHEDULED_LOCAL', NULL, NULL, NULL, NULL, 'EXTENSION_30', 9900, true),
    ('30000000-0000-0000-0000-000000000104', '30000000-0000-0000-0000-000000000001', 'SCHEDULED_LOCAL', NULL, NULL, NULL, 'PRIORITY', 'SURCHARGE', 4900, true),
    ('30000000-0000-0000-0000-000000000105', '30000000-0000-0000-0000-000000000001', 'SCHEDULED_LOCAL', NULL, NULL, NULL, NULL, 'NIGHT_SURCHARGE', 7900, true),
    ('30000000-0000-0000-0000-000000000106', '30000000-0000-0000-0000-000000000001', 'SCHEDULED_ONE_WAY', '20000000-0000-0000-0000-000000000101', NULL, NULL, 'FLEX', 'BASE_90', 34900, true),
    ('30000000-0000-0000-0000-000000000107', '30000000-0000-0000-0000-000000000001', 'SCHEDULED_ONE_WAY', NULL, NULL, '20000000-0000-0000-0000-000000000301', NULL, 'ONE_WAY_ALLOWANCE', 5000, true),
    ('30000000-0000-0000-0000-000000000108', '30000000-0000-0000-0000-000000000001', 'SCHEDULED_ONE_WAY', NULL, NULL, '20000000-0000-0000-0000-000000000302', NULL, 'ONE_WAY_ALLOWANCE', 9000, true),
    ('30000000-0000-0000-0000-000000000109', '30000000-0000-0000-0000-000000000001', 'AIRPORT', NULL, '20000000-0000-0000-0000-000000000201', NULL, NULL, 'AIRPORT_FIXED', 44900, true),
    ('30000000-0000-0000-0000-000000000110', '30000000-0000-0000-0000-000000000001', 'AIRPORT', NULL, '20000000-0000-0000-0000-000000000202', NULL, NULL, 'AIRPORT_FIXED', 54900, true),
    ('30000000-0000-0000-0000-000000000111', '30000000-0000-0000-0000-000000000001', 'LATE_NIGHT', '20000000-0000-0000-0000-000000000101', NULL, NULL, 'FLEX', 'BASE_90', 39900, true),
    ('30000000-0000-0000-0000-000000000112', '30000000-0000-0000-0000-000000000001', 'LATE_NIGHT', NULL, NULL, NULL, NULL, 'NIGHT_SURCHARGE', 9900, true)
ON CONFLICT DO NOTHING;

INSERT INTO commercial.tax_profile (
    id,
    city_id,
    profile_code,
    version_no,
    tax_percent,
    status,
    effective_from
)
VALUES (
    '30000000-0000-0000-0000-000000000201',
    '20000000-0000-0000-0000-000000000001',
    'BLR_GST',
    1,
    18.00,
    'PUBLISHED',
    now() - interval '1 day'
)
ON CONFLICT (profile_code, version_no) DO NOTHING;

INSERT INTO commercial.cancellation_policy (
    id,
    city_id,
    policy_code,
    version_no,
    status,
    policy_payload,
    effective_from
)
VALUES (
    '30000000-0000-0000-0000-000000000301',
    '20000000-0000-0000-0000-000000000001',
    'BLR_CANCEL',
    1,
    'PUBLISHED',
    '{"free_until_minutes_before_pickup":60,"late_cancel_fee_paise":7900,"driver_compensation_paise":3000}'::jsonb,
    now() - interval '1 day'
)
ON CONFLICT (city_id, policy_code, version_no) DO NOTHING;

INSERT INTO commercial.refund_policy (
    id,
    city_id,
    policy_code,
    version_no,
    status,
    policy_payload,
    effective_from
)
VALUES (
    '30000000-0000-0000-0000-000000000302',
    '20000000-0000-0000-0000-000000000001',
    'BLR_REFUND',
    1,
    'PUBLISHED',
    '{"customer_no_show_percent":0,"driver_no_show_percent":100,"service_failure_percent":100,"billing_dispute_default_percent":50}'::jsonb,
    now() - interval '1 day'
)
ON CONFLICT (city_id, policy_code, version_no) DO NOTHING;

INSERT INTO commercial.payout_plan (
    id,
    city_id,
    plan_code,
    version_no,
    status,
    effective_from
)
VALUES (
    '30000000-0000-0000-0000-000000000401',
    '20000000-0000-0000-0000-000000000001',
    'BLR_DRIVER_STANDARD',
    1,
    'PUBLISHED',
    now() - interval '1 day'
)
ON CONFLICT (city_id, plan_code, version_no) DO NOTHING;

INSERT INTO commercial.payout_rule (
    id,
    payout_plan_id,
    service_type,
    service_zone_id,
    airport_zone_band_id,
    lead_time_bucket,
    rule_type,
    amount_paise,
    is_active
)
VALUES
    ('30000000-0000-0000-0000-000000000501', '30000000-0000-0000-0000-000000000401', 'SCHEDULED_LOCAL', NULL, NULL, NULL, 'ARRIVAL_FEE', 5000, true),
    ('30000000-0000-0000-0000-000000000502', '30000000-0000-0000-0000-000000000401', 'SCHEDULED_LOCAL', NULL, NULL, NULL, 'BASE_90', 22000, true),
    ('30000000-0000-0000-0000-000000000503', '30000000-0000-0000-0000-000000000401', 'SCHEDULED_LOCAL', NULL, NULL, NULL, 'EXTENSION_30', 7000, true),
    ('30000000-0000-0000-0000-000000000504', '30000000-0000-0000-0000-000000000401', 'SCHEDULED_ONE_WAY', NULL, NULL, NULL, 'BASE_90', 23000, true),
    ('30000000-0000-0000-0000-000000000505', '30000000-0000-0000-0000-000000000401', 'SCHEDULED_ONE_WAY', NULL, NULL, NULL, 'ONE_WAY_ALLOWANCE', 7000, true),
    ('30000000-0000-0000-0000-000000000506', '30000000-0000-0000-0000-000000000401', 'AIRPORT', NULL, '20000000-0000-0000-0000-000000000201', NULL, 'AIRPORT_FIXED', 30000, true),
    ('30000000-0000-0000-0000-000000000507', '30000000-0000-0000-0000-000000000401', 'AIRPORT', NULL, '20000000-0000-0000-0000-000000000202', NULL, 'AIRPORT_FIXED', 36000, true),
    ('30000000-0000-0000-0000-000000000508', '30000000-0000-0000-0000-000000000401', 'LATE_NIGHT', NULL, NULL, NULL, 'BASE_90', 26000, true),
    ('30000000-0000-0000-0000-000000000509', '30000000-0000-0000-0000-000000000401', 'LATE_NIGHT', NULL, NULL, NULL, 'NIGHT_BONUS', 8000, true)
ON CONFLICT DO NOTHING;

INSERT INTO comms.notification_template (
    id,
    template_code,
    channel,
    language_code,
    subject_template,
    body_template,
    is_active
)
VALUES
    ('40000000-0000-0000-0000-000000000001', 'OTP_REQUESTED', 'SMS', 'en', NULL, 'Your Rydvrse OTP is {{otp_code}}.', true),
    ('40000000-0000-0000-0000-000000000002', 'BOOKING_CONFIRMED', 'PUSH', 'en', NULL, 'Your driver booking is confirmed for {{scheduled_pickup_at}}.', true),
    ('40000000-0000-0000-0000-000000000003', 'ASSIGNMENT_LOCKED', 'PUSH', 'en', NULL, '{{driver_name}} has been assigned to your booking.', true),
    ('40000000-0000-0000-0000-000000000004', 'TRIP_STARTED', 'PUSH', 'en', NULL, 'Your trip has started.', true),
    ('40000000-0000-0000-0000-000000000005', 'TRIP_COMPLETED', 'PUSH', 'en', NULL, 'Your trip is complete. Payment is ready.', true),
    ('40000000-0000-0000-0000-000000000006', 'SUPPORT_TICKET_CREATED', 'SMS', 'en', NULL, 'Support ticket {{ticket_code}} has been created.', true)
ON CONFLICT (template_code, channel, language_code) DO NOTHING;

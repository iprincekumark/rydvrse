package com.rydvrse.reporting.infrastructure;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Repository
public class ReportingJdbcRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ReportingJdbcRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void refreshDailyFacts(LocalDate factDate) {
        MapSqlParameterSource params = new MapSqlParameterSource("factDate", factDate);

        jdbcTemplate.update("""
                INSERT INTO analytics.fact_booking_daily (
                    fact_date, city_id, service_type, zone_id, booking_count, fulfilled_count, cancelled_count, failed_fulfillment_count
                )
                SELECT
                    :factDate,
                    b.city_id,
                    b.service_type,
                    b.pickup_zone_id,
                    COUNT(*),
                    SUM(CASE WHEN b.status IN ('ASSIGNED', 'IN_PROGRESS', 'COMPLETED', 'COMPLETED_PAYMENT_PENDING') THEN 1 ELSE 0 END),
                    SUM(CASE WHEN b.status = 'CANCELLED' THEN 1 ELSE 0 END),
                    SUM(CASE WHEN b.status IN ('FAILED', 'PENDING_ASSIGNMENT') THEN 1 ELSE 0 END)
                FROM booking.booking b
                WHERE DATE(b.created_at) = :factDate
                GROUP BY b.city_id, b.service_type, b.pickup_zone_id
                ON CONFLICT (fact_date, city_id, service_type, zone_id)
                DO UPDATE SET
                    booking_count = EXCLUDED.booking_count,
                    fulfilled_count = EXCLUDED.fulfilled_count,
                    cancelled_count = EXCLUDED.cancelled_count,
                    failed_fulfillment_count = EXCLUDED.failed_fulfillment_count,
                    created_at = now()
                """, params);

        jdbcTemplate.update("""
                INSERT INTO analytics.fact_trip_daily (
                    fact_date, city_id, service_type, trip_count, gross_revenue_paise, refund_paise, driver_payout_paise
                )
                SELECT
                    :factDate,
                    b.city_id,
                    b.service_type,
                    COUNT(*),
                    COALESCE(SUM(t.final_total_paise), 0),
                    0,
                    COALESCE(SUM(CAST(t.final_total_paise * 0.70 AS bigint)), 0)
                FROM trip.trip t
                JOIN booking.booking b ON b.id = t.booking_id
                WHERE DATE(t.created_at) = :factDate
                GROUP BY b.city_id, b.service_type
                ON CONFLICT (fact_date, city_id, service_type)
                DO UPDATE SET
                    trip_count = EXCLUDED.trip_count,
                    gross_revenue_paise = EXCLUDED.gross_revenue_paise,
                    refund_paise = EXCLUDED.refund_paise,
                    driver_payout_paise = EXCLUDED.driver_payout_paise,
                    created_at = now()
                """, params);

        jdbcTemplate.update("""
                INSERT INTO analytics.fact_support_daily (
                    fact_date, city_id, open_ticket_count, resolved_ticket_count, incident_count, refund_request_count
                )
                SELECT
                    :factDate,
                    c.id,
                    COALESCE(open_counts.open_ticket_count, 0),
                    COALESCE(resolved_counts.resolved_ticket_count, 0),
                    COALESCE(incident_counts.incident_count, 0),
                    COALESCE(refund_counts.refund_request_count, 0)
                FROM master.city c
                LEFT JOIN (
                    SELECT b.city_id, COUNT(*) AS open_ticket_count
                    FROM support.support_ticket st
                    LEFT JOIN booking.booking b ON b.id = st.booking_id
                    WHERE DATE(st.created_at) = :factDate
                      AND st.status IN ('OPEN', 'IN_PROGRESS', 'REOPENED', 'PENDING_INTERNAL')
                    GROUP BY b.city_id
                ) open_counts ON open_counts.city_id = c.id
                LEFT JOIN (
                    SELECT b.city_id, COUNT(*) AS resolved_ticket_count
                    FROM support.support_ticket st
                    LEFT JOIN booking.booking b ON b.id = st.booking_id
                    WHERE DATE(st.created_at) = :factDate
                      AND st.status IN ('RESOLVED', 'CLOSED')
                    GROUP BY b.city_id
                ) resolved_counts ON resolved_counts.city_id = c.id
                LEFT JOIN (
                    SELECT b.city_id, COUNT(*) AS incident_count
                    FROM support.incident_case ic
                    LEFT JOIN booking.booking b ON b.id = ic.booking_id
                    WHERE DATE(ic.created_at) = :factDate
                    GROUP BY b.city_id
                ) incident_counts ON incident_counts.city_id = c.id
                LEFT JOIN (
                    SELECT b.city_id, COUNT(*) AS refund_request_count
                    FROM finance.refund_request rr
                    LEFT JOIN booking.booking b ON b.id = rr.booking_id
                    WHERE DATE(rr.created_at) = :factDate
                    GROUP BY b.city_id
                ) refund_counts ON refund_counts.city_id = c.id
                ON CONFLICT (fact_date, city_id)
                DO UPDATE SET
                    open_ticket_count = EXCLUDED.open_ticket_count,
                    resolved_ticket_count = EXCLUDED.resolved_ticket_count,
                    incident_count = EXCLUDED.incident_count,
                    refund_request_count = EXCLUDED.refund_request_count,
                    created_at = now()
                """, params);
    }

    public Map<String, Object> operationsSummary(LocalDate fromDate, LocalDate toDate, UUID cityId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("fromDate", fromDate)
                .addValue("toDate", toDate)
                .addValue("cityId", cityId);

        Long bookingCount = jdbcTemplate.queryForObject("""
                SELECT COALESCE(SUM(booking_count), 0)
                FROM analytics.fact_booking_daily
                WHERE fact_date BETWEEN :fromDate AND :toDate
                  AND (:cityId IS NULL OR city_id = :cityId)
                """, params, Long.class);

        Long fulfilledCount = jdbcTemplate.queryForObject("""
                SELECT COALESCE(SUM(fulfilled_count), 0)
                FROM analytics.fact_booking_daily
                WHERE fact_date BETWEEN :fromDate AND :toDate
                  AND (:cityId IS NULL OR city_id = :cityId)
                """, params, Long.class);

        Long grossRevenue = jdbcTemplate.queryForObject("""
                SELECT COALESCE(SUM(gross_revenue_paise), 0)
                FROM analytics.fact_trip_daily
                WHERE fact_date BETWEEN :fromDate AND :toDate
                  AND (:cityId IS NULL OR city_id = :cityId)
                """, params, Long.class);

        Long openTickets = jdbcTemplate.queryForObject("""
                SELECT COALESCE(SUM(open_ticket_count), 0)
                FROM analytics.fact_support_daily
                WHERE fact_date BETWEEN :fromDate AND :toDate
                  AND (:cityId IS NULL OR city_id = :cityId)
                """, params, Long.class);

        return Map.of(
                "from_date", fromDate,
                "to_date", toDate,
                "city_id", cityId,
                "booking_count", bookingCount == null ? 0L : bookingCount,
                "fulfilled_count", fulfilledCount == null ? 0L : fulfilledCount,
                "gross_revenue_paise", grossRevenue == null ? 0L : grossRevenue,
                "open_ticket_count", openTickets == null ? 0L : openTickets
        );
    }
}

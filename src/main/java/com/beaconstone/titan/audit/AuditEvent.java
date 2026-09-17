package com.beaconstone.titan.audit;

import java.util.List;
import java.util.Objects;

/**
 * A customer-change event awaiting an audit entry.
 *
 * <p>{@code changedFields} names the fields that changed and never carries
 * their values. An audit partition records that an email address was replaced,
 * not what it was replaced with, so the ledger cannot become a second copy of
 * customer data.
 */
public record AuditEvent(
        String eventId,
        String tenantId,
        String customerId,
        Kind kind,
        List<String> changedFields,
        String occurredAt) {

    /** How the change reached Titan. */
    public enum Kind {
        PROFILE_EDIT,
        CHECKOUT_BILLING,
        PARTNER_IMPORT
    }

    public AuditEvent {
        Objects.requireNonNull(eventId, "eventId");
        Objects.requireNonNull(tenantId, "tenantId");
        Objects.requireNonNull(customerId, "customerId");
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(occurredAt, "occurredAt");
        changedFields = List.copyOf(changedFields);
    }
}

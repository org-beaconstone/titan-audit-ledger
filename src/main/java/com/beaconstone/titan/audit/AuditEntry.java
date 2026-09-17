package com.beaconstone.titan.audit;

import java.util.List;
import java.util.Objects;

/**
 * An appended, immutable audit record.
 *
 * <p>{@code sequence} is assigned by the partition that accepted it and only
 * ever increases, so entries can be replayed in the order they were recorded.
 */
public record AuditEntry(
        long sequence,
        HomeRegion region,
        String eventId,
        String tenantId,
        String customerId,
        AuditEvent.Kind kind,
        List<String> changedFields) {

    public AuditEntry {
        Objects.requireNonNull(region, "region");
        Objects.requireNonNull(eventId, "eventId");
        Objects.requireNonNull(tenantId, "tenantId");
        Objects.requireNonNull(customerId, "customerId");
        Objects.requireNonNull(kind, "kind");
        changedFields = List.copyOf(changedFields);
    }

    static AuditEntry of(long sequence, HomeRegion region, AuditEvent event) {
        return new AuditEntry(
                sequence,
                region,
                event.eventId(),
                event.tenantId(),
                event.customerId(),
                event.kind(),
                event.changedFields());
    }
}

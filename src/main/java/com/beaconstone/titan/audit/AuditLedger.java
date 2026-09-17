package com.beaconstone.titan.audit;

import java.util.List;

/**
 * An append-only audit partition for one region.
 *
 * <p>There is deliberately no operation to amend or remove an entry. A
 * correction is recorded as a further event.
 */
public interface AuditLedger {

    /** The region this partition serves. */
    HomeRegion region();

    /**
     * Appends the event and returns the entry that was recorded.
     *
     * @throws IllegalStateException if the event id has already been appended
     */
    AuditEntry append(AuditEvent event);

    /** Every entry in this partition, in the order it was appended. */
    List<AuditEntry> entries();
}

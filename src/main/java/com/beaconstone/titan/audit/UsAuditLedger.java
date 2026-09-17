package com.beaconstone.titan.audit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * United States audit partition.
 *
 * <p>A single ordered journal across all tenants in the region, which is what
 * the US reporting pipeline consumes. The sequence is partition-wide.
 */
public final class UsAuditLedger implements AuditLedger {

    private final List<AuditEntry> journal = new ArrayList<>();
    private final Set<String> appendedEventIds = new HashSet<>();

    @Override
    public HomeRegion region() {
        return HomeRegion.US;
    }

    @Override
    public AuditEntry append(AuditEvent event) {
        if (!appendedEventIds.add(event.eventId())) {
            throw new IllegalStateException("Event " + event.eventId() + " is already recorded");
        }
        AuditEntry entry = AuditEntry.of(journal.size() + 1L, region(), event);
        journal.add(entry);
        return entry;
    }

    @Override
    public List<AuditEntry> entries() {
        return List.copyOf(journal);
    }
}

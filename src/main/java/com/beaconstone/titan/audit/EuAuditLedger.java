package com.beaconstone.titan.audit;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * European audit partition.
 *
 * <p>Entries are segregated per tenant, because each Beaconstone tenant is a
 * separate controller in the EU and its trail is produced independently. The
 * sequence is therefore per tenant rather than per partition.
 */
public final class EuAuditLedger implements AuditLedger {

    private final Map<String, List<AuditEntry>> entriesByTenant = new LinkedHashMap<>();
    private final Map<String, Long> appendedEventIds = new LinkedHashMap<>();

    @Override
    public HomeRegion region() {
        return HomeRegion.EU;
    }

    @Override
    public AuditEntry append(AuditEvent event) {
        if (appendedEventIds.containsKey(event.eventId())) {
            throw new IllegalStateException("Event " + event.eventId() + " is already recorded");
        }
        List<AuditEntry> tenantEntries =
                entriesByTenant.computeIfAbsent(event.tenantId(), tenant -> new ArrayList<>());
        AuditEntry entry = AuditEntry.of(tenantEntries.size() + 1L, region(), event);
        tenantEntries.add(entry);
        appendedEventIds.put(event.eventId(), entry.sequence());
        return entry;
    }

    @Override
    public List<AuditEntry> entries() {
        List<AuditEntry> all = new ArrayList<>();
        entriesByTenant.values().forEach(all::addAll);
        return List.copyOf(all);
    }

    /** Entries recorded for one tenant, in order. */
    public List<AuditEntry> entriesForTenant(String tenantId) {
        return List.copyOf(entriesByTenant.getOrDefault(tenantId, List.of()));
    }
}

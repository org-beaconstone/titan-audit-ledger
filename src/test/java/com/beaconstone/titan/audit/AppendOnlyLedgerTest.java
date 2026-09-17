package com.beaconstone.titan.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class AppendOnlyLedgerTest {

    private static AuditEvent event(String eventId, String tenantId) {
        return new AuditEvent(
                eventId,
                tenantId,
                "cust-4417",
                AuditEvent.Kind.PARTNER_IMPORT,
                List.of("email"),
                "2026-04-01T10:30:00Z");
    }

    @Test
    void europeanPartitionSequencesPerTenant() {
        EuAuditLedger ledger = new EuAuditLedger();

        assertEquals(1L, ledger.append(event("evt-1", "tenant-eu-01")).sequence());
        assertEquals(2L, ledger.append(event("evt-2", "tenant-eu-01")).sequence());
        assertEquals(1L, ledger.append(event("evt-3", "tenant-eu-02")).sequence());

        assertEquals(2, ledger.entriesForTenant("tenant-eu-01").size());
        assertEquals(1, ledger.entriesForTenant("tenant-eu-02").size());
        assertEquals(3, ledger.entries().size());
    }

    @Test
    void unitedStatesPartitionSequencesAcrossTheRegion() {
        UsAuditLedger ledger = new UsAuditLedger();

        assertEquals(1L, ledger.append(event("evt-1", "tenant-us-01")).sequence());
        assertEquals(2L, ledger.append(event("evt-2", "tenant-us-02")).sequence());
        assertEquals(3L, ledger.append(event("evt-3", "tenant-us-01")).sequence());
    }

    @Test
    void aRedeliveredEventIsNotAppendedTwice() {
        UsAuditLedger us = new UsAuditLedger();
        us.append(event("evt-1", "tenant-us-01"));

        assertThrows(IllegalStateException.class, () -> us.append(event("evt-1", "tenant-us-01")));
        assertEquals(1, us.entries().size());

        EuAuditLedger eu = new EuAuditLedger();
        eu.append(event("evt-2", "tenant-eu-01"));
        assertThrows(IllegalStateException.class, () -> eu.append(event("evt-2", "tenant-eu-01")));
        assertEquals(1, eu.entries().size());
    }

    @Test
    void exposedEntriesCannotBeMutated() {
        UsAuditLedger ledger = new UsAuditLedger();
        AuditEntry entry = ledger.append(event("evt-1", "tenant-us-01"));

        List<AuditEntry> entries = ledger.entries();
        assertThrows(UnsupportedOperationException.class, () -> entries.add(entry));
        assertThrows(
                UnsupportedOperationException.class, () -> entry.changedFields().add("injected"));
    }
}

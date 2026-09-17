package com.beaconstone.titan.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RecordResidenceTest {

    private static final Map<String, String> ASSIGNMENTS =
            Map.of(
                    "tenant-eu-01", "EU",
                    "tenant-us-01", "US",
                    "tenant-apac-01", "APAC");

    private final RecordResidenceRules rules = new RecordResidenceRules();
    private final ResidenceInventory inventory =
            new ResidenceInventory(new TenantRegionDirectory(ASSIGNMENTS), rules);

    @Test
    void permitsOnlyThePartitionServingTheLocality() {
        assertEquals("eu-journal", rules.permittedPartition(HomeRegion.EU));
        assertEquals("us-journal", rules.permittedPartition(HomeRegion.US));
        assertTrue(rules.permits(HomeRegion.EU, "eu-journal"));
        assertFalse(rules.permits(HomeRegion.EU, "us-journal"));
    }

    @Test
    void refusesAPartitionOutsideTheLocality() {
        assertFalse(rules.permits(HomeRegion.EU, "global-journal"));
        assertFalse(rules.permits(HomeRegion.US, "global-journal"));
    }

    @Test
    void enumeratesTheFootprint() {
        assertEquals(List.of("eu-journal", "us-journal"), rules.footprint());
    }

    @Test
    void reportsWhereEachTenantTrailResides() {
        List<ResidenceInventory.ResidenceEntry> entries =
                inventory.residences(List.of("tenant-eu-01", "tenant-us-01"));

        assertEquals("eu-journal", entries.get(0).partition());
        assertEquals("us-journal", entries.get(1).partition());
        assertNull(entries.get(0).problem());
    }

    @Test
    void reportsTenantsThatCannotBePlaced() {
        List<ResidenceInventory.ResidenceEntry> unsettled =
                inventory.unsettled(List.of("tenant-eu-01", "tenant-apac-01", "tenant-absent-99"));

        assertEquals(2, unsettled.size());
        assertEquals("tenant-apac-01", unsettled.get(0).tenantId());
        assertEquals("tenant-absent-99", unsettled.get(1).tenantId());
        assertTrue(unsettled.stream().allMatch(entry -> entry.problem() != null));
    }
}

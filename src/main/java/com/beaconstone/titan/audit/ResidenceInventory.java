package com.beaconstone.titan.audit;

import java.util.List;
import java.util.Objects;

/**
 * Reports which machines hold each tenant's change trail while at rest.
 *
 * <p>A reviewer asks where the record of alterations to a given tenant's
 * account holders is kept. This inventory resolves the locality the tenant was
 * onboarded into and names the partition holding its journal.
 *
 * <p>It reads the same locality assignments the append path reads, so an entry
 * describes where a journal entry would genuinely land rather than what
 * configuration claims. Placement only is reported: no field names, identifiers
 * or personal details are read or returned.
 */
public final class ResidenceInventory {

    /** Where one tenant's trail resides, or why that is unknown. */
    public record ResidenceEntry(
            String tenantId, HomeRegion locality, String partition, String problem) {}

    private final TenantRegionDirectory directory;
    private final RecordResidenceRules rules;

    public ResidenceInventory(TenantRegionDirectory directory, RecordResidenceRules rules) {
        this.directory = Objects.requireNonNull(directory, "directory");
        this.rules = Objects.requireNonNull(rules, "rules");
    }

    /** Residence for one tenant. An unresolvable tenant is reported, not hidden. */
    public ResidenceEntry residenceFor(String tenantId) {
        try {
            HomeRegion locality = directory.homeRegionForTenant(tenantId);
            return new ResidenceEntry(tenantId, locality, rules.permittedPartition(locality), null);
        } catch (AuditRoutingException failure) {
            return new ResidenceEntry(tenantId, null, null, failure.getMessage());
        }
    }

    /** Residence for several tenants, in the order supplied. */
    public List<ResidenceEntry> residences(List<String> tenantIds) {
        return tenantIds.stream().map(this::residenceFor).toList();
    }

    /** Tenants whose trail cannot currently be placed in any locality. */
    public List<ResidenceEntry> unsettled(List<String> tenantIds) {
        return residences(tenantIds).stream().filter(entry -> entry.partition() == null).toList();
    }
}

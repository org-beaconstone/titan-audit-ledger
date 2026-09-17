package com.beaconstone.titan.audit;

import java.util.Map;

/**
 * Authority for which region holds a tenant's audit trail.
 *
 * <p>Assignments arrive as raw configuration strings, so this is the validation
 * boundary. Resolution fails closed: a missing or unrecognised assignment stops
 * the event from being recorded rather than defaulting to a partition.
 */
public final class TenantRegionDirectory {

    private final Map<String, String> assignedRegions;

    public TenantRegionDirectory(Map<String, String> assignedRegions) {
        this.assignedRegions = Map.copyOf(assignedRegions);
    }

    public HomeRegion homeRegionForTenant(String tenantId) {
        String assigned = assignedRegions.get(tenantId);
        if (assigned == null) {
            throw new AuditRoutingException.UnknownTenant(tenantId);
        }
        return HomeRegion.parse(assigned)
                .orElseThrow(() -> new AuditRoutingException.UnsupportedRegion(tenantId, assigned));
    }
}

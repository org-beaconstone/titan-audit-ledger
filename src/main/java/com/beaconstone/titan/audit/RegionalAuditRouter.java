package com.beaconstone.titan.audit;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Selects the audit partition that serves a tenant's region.
 *
 * <p>Every handler routes through here, so there is one place that decides
 * where a customer-change event is recorded. Partitions are supplied at
 * construction and there is no partition-wide default.
 */
public final class RegionalAuditRouter {

    private final TenantRegionDirectory directory;
    private final Map<HomeRegion, AuditLedger> ledgers;

    public RegionalAuditRouter(TenantRegionDirectory directory, AuditLedger... ledgers) {
        this.directory = Objects.requireNonNull(directory, "directory");
        this.ledgers = new EnumMap<>(HomeRegion.class);
        for (AuditLedger ledger : ledgers) {
            this.ledgers.put(ledger.region(), ledger);
        }
    }

    public AuditLedger ledgerForTenant(String tenantId) {
        HomeRegion region = directory.homeRegionForTenant(tenantId);
        AuditLedger ledger = ledgers.get(region);
        if (ledger == null) {
            throw new AuditRoutingException.UnsupportedRegion(tenantId, region.name());
        }
        return ledger;
    }
}

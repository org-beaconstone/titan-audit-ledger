package com.beaconstone.titan.audit;

import java.util.Objects;

/**
 * Records customer changes delivered by the partner import feed.
 *
 * <p>Feed events arrive in batches and can be redelivered. An event id already
 * present in the region's partition is reported as a duplicate rather than
 * appended twice, which keeps a replayed batch from inflating the trail.
 */
public final class ImportAuditHandler {

    private final RegionalAuditRouter router;

    public ImportAuditHandler(RegionalAuditRouter router) {
        this.router = Objects.requireNonNull(router, "router");
    }

    public AuditEntry handle(AuditEvent event) {
        if (event.kind() != AuditEvent.Kind.PARTNER_IMPORT) {
            throw new IllegalArgumentException("Expected an import change, got " + event.kind());
        }
        AuditLedger ledger = router.ledgerForTenant(event.tenantId());
        return ledger.append(event);
    }
}

package com.beaconstone.titan.audit;

import java.util.Objects;

/**
 * Records profile edits a customer made for themselves in Titan.
 *
 * <p>An edit that changed nothing is rejected rather than recorded, so the
 * trail does not accumulate entries that assert no change.
 */
public final class ProfileAuditHandler {

    private final RegionalAuditRouter router;

    public ProfileAuditHandler(RegionalAuditRouter router) {
        this.router = Objects.requireNonNull(router, "router");
    }

    public AuditEntry handle(AuditEvent event) {
        if (event.kind() != AuditEvent.Kind.PROFILE_EDIT) {
            throw new IllegalArgumentException("Expected a profile edit, got " + event.kind());
        }
        if (event.changedFields().isEmpty()) {
            throw new IllegalArgumentException("A profile edit must name at least one field");
        }
        return router.ledgerForTenant(event.tenantId()).append(event);
    }
}

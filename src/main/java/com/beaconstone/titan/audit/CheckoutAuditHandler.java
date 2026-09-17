package com.beaconstone.titan.audit;

import java.util.Objects;

/**
 * Records billing contact changes captured during checkout.
 *
 * <p>Checkout changes are financially significant, so the handler insists the
 * event names a billing field. A checkout event that only touched, say, a
 * display name belongs on the profile trail instead.
 */
public final class CheckoutAuditHandler {

    private static final String BILLING_FIELD_PREFIX = "billing";

    private final RegionalAuditRouter router;

    public CheckoutAuditHandler(RegionalAuditRouter router) {
        this.router = Objects.requireNonNull(router, "router");
    }

    public AuditEntry handle(AuditEvent event) {
        if (event.kind() != AuditEvent.Kind.CHECKOUT_BILLING) {
            throw new IllegalArgumentException("Expected a checkout change, got " + event.kind());
        }
        boolean touchesBilling =
                event.changedFields().stream().anyMatch(f -> f.startsWith(BILLING_FIELD_PREFIX));
        if (!touchesBilling) {
            throw new IllegalArgumentException("A checkout change must name a billing field");
        }
        return router.ledgerForTenant(event.tenantId()).append(event);
    }
}

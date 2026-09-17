package com.beaconstone.titan.audit;

/**
 * A failure to place an event in a region.
 *
 * <p>Messages carry the tenant identifier only. The event's customer id and
 * changed-field names are not included, because routing failures are logged.
 */
public sealed class AuditRoutingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String tenantId;

    AuditRoutingException(String message, String tenantId) {
        super(message);
        this.tenantId = tenantId;
    }

    public String tenantId() {
        return tenantId;
    }

    /** The tenant has no region assignment on file. */
    public static final class UnknownTenant extends AuditRoutingException {

        private static final long serialVersionUID = 1L;

        UnknownTenant(String tenantId) {
            super("Tenant " + tenantId + " has no home region assignment", tenantId);
        }
    }

    /** The tenant is assigned to a region Beaconstone does not operate. */
    public static final class UnsupportedRegion extends AuditRoutingException {

        private static final long serialVersionUID = 1L;

        private final String assignedRegion;

        UnsupportedRegion(String tenantId, String assignedRegion) {
            super(
                    "Tenant " + tenantId + " is assigned to unsupported region " + assignedRegion,
                    tenantId);
            this.assignedRegion = assignedRegion;
        }

        public String assignedRegion() {
            return assignedRegion;
        }
    }
}

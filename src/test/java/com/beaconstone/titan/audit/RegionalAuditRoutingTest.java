package com.beaconstone.titan.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RegionalAuditRoutingTest {

    private static final Map<String, String> ASSIGNMENTS =
            Map.of(
                    "tenant-eu-01", "EU",
                    "tenant-eu-02", "EU",
                    "tenant-us-01", "US",
                    "tenant-apac-01", "APAC");

    private EuAuditLedger eu;
    private UsAuditLedger us;
    private RegionalAuditRouter router;

    @BeforeEach
    void setUp() {
        eu = new EuAuditLedger();
        us = new UsAuditLedger();
        router = new RegionalAuditRouter(new TenantRegionDirectory(ASSIGNMENTS), eu, us);
    }

    private static AuditEvent event(
            String eventId, String tenantId, AuditEvent.Kind kind, String... changedFields) {
        return new AuditEvent(
                eventId,
                tenantId,
                "cust-4417",
                kind,
                List.of(changedFields),
                "2026-04-01T10:30:00Z");
    }

    @Test
    void routesEuropeanTenantToTheEuropeanPartition() {
        assertSame(eu, router.ledgerForTenant("tenant-eu-01"));
        assertEquals(HomeRegion.EU, router.ledgerForTenant("tenant-eu-01").region());
    }

    @Test
    void routesUnitedStatesTenantToTheUsPartition() {
        assertSame(us, router.ledgerForTenant("tenant-us-01"));
        assertEquals(HomeRegion.US, router.ledgerForTenant("tenant-us-01").region());
    }

    @Test
    void failsClosedForATenantWithNoAssignment() {
        AuditRoutingException.UnknownTenant failure =
                assertThrows(
                        AuditRoutingException.UnknownTenant.class,
                        () -> router.ledgerForTenant("tenant-absent-99"));
        assertEquals("tenant-absent-99", failure.tenantId());
    }

    @Test
    void failsClosedForARegionBeaconstoneDoesNotOperate() {
        AuditRoutingException.UnsupportedRegion failure =
                assertThrows(
                        AuditRoutingException.UnsupportedRegion.class,
                        () -> router.ledgerForTenant("tenant-apac-01"));
        assertEquals("APAC", failure.assignedRegion());
    }

    @Test
    void routingFailuresDoNotDiscloseCustomerIdentifiers() {
        AuditRoutingException failure =
                assertThrows(
                        AuditRoutingException.class,
                        () -> router.ledgerForTenant("tenant-apac-01"));
        assertTrue(failure.getMessage().contains("tenant-apac-01"));
        assertTrue(!failure.getMessage().contains("cust-4417"));
    }

    @Test
    void allThreeHandlersRecordThroughTheSameRouter() {
        AuditEntry profile =
                new ProfileAuditHandler(router)
                        .handle(event("evt-1", "tenant-eu-01", AuditEvent.Kind.PROFILE_EDIT, "email"));
        AuditEntry checkout =
                new CheckoutAuditHandler(router)
                        .handle(
                                event(
                                        "evt-2",
                                        "tenant-eu-01",
                                        AuditEvent.Kind.CHECKOUT_BILLING,
                                        "billingEmail"));
        AuditEntry imported =
                new ImportAuditHandler(router)
                        .handle(
                                event(
                                        "evt-3",
                                        "tenant-us-01",
                                        AuditEvent.Kind.PARTNER_IMPORT,
                                        "displayName"));

        assertEquals(HomeRegion.EU, profile.region());
        assertEquals(HomeRegion.EU, checkout.region());
        assertEquals(HomeRegion.US, imported.region());
        assertEquals(2, eu.entries().size());
        assertEquals(1, us.entries().size());
    }

    @Test
    void handlersRejectAnEventOfTheWrongKind() {
        AuditEvent profileEdit = event("evt-4", "tenant-eu-01", AuditEvent.Kind.PROFILE_EDIT, "email");

        assertThrows(
                IllegalArgumentException.class, () -> new CheckoutAuditHandler(router).handle(profileEdit));
        assertThrows(
                IllegalArgumentException.class, () -> new ImportAuditHandler(router).handle(profileEdit));
    }

    @Test
    void profileEditMustNameAChangedField() {
        AuditEvent empty = event("evt-5", "tenant-eu-01", AuditEvent.Kind.PROFILE_EDIT);

        assertThrows(
                IllegalArgumentException.class, () -> new ProfileAuditHandler(router).handle(empty));
        assertEquals(0, eu.entries().size());
    }

    @Test
    void checkoutChangeMustNameABillingField() {
        AuditEvent notBilling =
                event("evt-6", "tenant-eu-01", AuditEvent.Kind.CHECKOUT_BILLING, "displayName");

        assertThrows(
                IllegalArgumentException.class,
                () -> new CheckoutAuditHandler(router).handle(notBilling));
    }

    @Test
    void entriesRecordFieldNamesOnly() {
        AuditEntry entry =
                new ProfileAuditHandler(router)
                        .handle(
                                event(
                                        "evt-7",
                                        "tenant-eu-01",
                                        AuditEvent.Kind.PROFILE_EDIT,
                                        "email",
                                        "displayName"));

        assertEquals(List.of("email", "displayName"), entry.changedFields());
        assertTrue(!entry.changedFields().contains("ada.lovelace@example.test"));
    }
}

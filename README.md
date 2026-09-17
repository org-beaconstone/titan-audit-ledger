# titan-audit-ledger

Append-only audit trail for Beaconstone Financial customer-change events in the
Titan product suite.

Every customer change is recorded in the audit partition that serves the
tenant's region. The tenant's region is resolved before a partition is chosen,
so an event is never written outside the region that holds the customer.

> Fictional service used for demonstration purposes. It contains no real
> customer records, credentials, or service endpoints.

## Routing

```
ProfileAuditHandler.handle   ─┐
CheckoutAuditHandler.handle  ─┼─> RegionalAuditRouter.ledgerForTenant
ImportAuditHandler.handle    ─┘      -> TenantRegionDirectory.homeRegionForTenant
                                     -> EuAuditLedger | UsAuditLedger
                                        -> AuditLedger.append
```

Three handlers, one router, two partitions. Every handler routes through
`RegionalAuditRouter`, so there is a single place that decides where an event
is recorded.

## Behaviour

- **Append-only.** `AuditLedger` exposes no operation to amend or remove an
  entry; a correction is recorded as a further event. Returned entry lists and
  changed-field lists are immutable.
- **Two partitions, genuinely different.** `EuAuditLedger` segregates entries
  per tenant and sequences per tenant, because each EU tenant is a separate
  controller. `UsAuditLedger` keeps one ordered journal per region, which is
  what the US reporting pipeline consumes.
- **Fails closed.** A tenant with no assignment raises
  `AuditRoutingException.UnknownTenant`; one assigned to a region Beaconstone
  does not operate raises `AuditRoutingException.UnsupportedRegion`. There is no
  default partition and no global writer.
- **Idempotent on redelivery.** An event id already present in a partition is
  rejected rather than appended twice, so a replayed partner batch does not
  inflate the trail.
- **Field names, never values.** `AuditEvent.changedFields` records that an
  email address changed, not what it changed to. Routing failure messages carry
  the tenant identifier only.

## Commands

```bash
gradle build   # compiles and runs the tests
gradle test
```

Requires a JDK 17 toolchain. Compilation runs with `-Xlint:all -Werror`.

package com.beaconstone.titan.audit;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The rules governing where an account holder's personal details, and the
 * trail describing changes to them, are permitted to be kept.
 *
 * <p>Beaconstone Financial holds each party's personal information inside the
 * footprint their tenant was onboarded into, and the audit trail follows the
 * record it describes. A change to a European tenant's account holder is
 * journalled on European hardware; it is not mirrored into the United States
 * footprint, because the trail names the fields that were altered and is
 * therefore itself subject to the same placement rules.
 *
 * <p>These rules are consulted by the router before a partition is chosen, and
 * by reviewers enumerating the footprint.
 */
public final class RecordResidenceRules {

    private static final Map<HomeRegion, String> PARTITION_BY_LOCALITY =
            new EnumMap<>(
                    Map.of(
                            HomeRegion.EU, "eu-journal",
                            HomeRegion.US, "us-journal"));

    /** The only partition in which a trail for this locality may reside. */
    public String permittedPartition(HomeRegion locality) {
        return PARTITION_BY_LOCALITY.get(locality);
    }

    /**
     * Whether a trail belonging to {@code locality} may be kept in
     * {@code partition}.
     *
     * <p>False for every partition outside the locality, so a caller cannot
     * satisfy the rules by widening where it journals.
     */
    public boolean permits(HomeRegion locality, String partition) {
        return permittedPartition(locality).equals(partition);
    }

    /** Every locality a trail is kept in, with the partition holding it. */
    public List<String> footprint() {
        return PARTITION_BY_LOCALITY.values().stream().sorted().toList();
    }
}

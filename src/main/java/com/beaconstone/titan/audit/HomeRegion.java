package com.beaconstone.titan.audit;

import java.util.Optional;

/**
 * A region Beaconstone Financial operates an audit partition in.
 */
public enum HomeRegion {
    EU,
    US;

    /**
     * Interprets a region assignment published by tenant onboarding.
     *
     * @param assignment the raw assignment value
     * @return the region, or empty when the value is not one Beaconstone operates
     */
    public static Optional<HomeRegion> parse(String assignment) {
        for (HomeRegion region : values()) {
            if (region.name().equals(assignment)) {
                return Optional.of(region);
            }
        }
        return Optional.empty();
    }
}

package com.cnmci.stats.beans;

public record StaticticsForRecovery(
        String agent,
        String commune,
        String quartier,
        long totalArtisan,
        String typeEnrolement,
        long montantARecouvrer,
        long montantDejaRecouvrer,
        double tauxRecouvrement
) {
}

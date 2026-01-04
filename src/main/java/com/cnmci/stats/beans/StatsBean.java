package com.cnmci.stats.beans;

public record StatsBean(
        String libelle,
        int population,
        long attendu,
        long paye,
        double pourcentage
    ) {
}

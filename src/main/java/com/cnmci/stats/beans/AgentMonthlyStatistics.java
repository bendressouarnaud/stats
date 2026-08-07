package com.cnmci.stats.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AgentMonthlyStatistics(
        long id,
        String nom,
        String prenom,
        String contact,
        String profil,
        String crm,
        @JsonProperty("artisan_identifie") long artisanIdentifie,
        @JsonProperty("entreprise_identifie") long entrepriseIdentifie,
        @JsonProperty("compagnon_identifie") long compagnonIdentifie,
        @JsonProperty("apprenti_identifie") long apprentiIdentifie,
        @JsonProperty("artisan_renouvellement") long artisanRenouvellement,
        @JsonProperty("artisan_15000") long artisan15000,
        @JsonProperty("artisan_10000") long artisan10000,
        @JsonProperty("artisan_5000") long artisan5000,
        @JsonProperty("artisan_3000") long artisan3000,
        @JsonProperty("entreprise_25000") long entreprise25000,
        @JsonProperty("compagnon_5000") long compagnon5000,
        @JsonProperty("apprenti_5000") long apprenti5000
) {
}

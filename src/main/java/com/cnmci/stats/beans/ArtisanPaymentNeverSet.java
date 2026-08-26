package com.cnmci.stats.beans;

public record ArtisanPaymentNeverSet(
        String nom,
        String contact1,
        String crm,
        String dateEnrolement,
        int montantAPayer,
        String commentaire
) {
}

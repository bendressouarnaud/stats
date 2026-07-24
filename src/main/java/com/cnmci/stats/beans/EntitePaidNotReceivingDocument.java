package com.cnmci.stats.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EntitePaidNotReceivingDocument(
String nom,
String contact,
String note,
@JsonProperty("date_prise_dossier") String datePriseDossier,
String metier,
@JsonProperty("commune_activite") String commune
) {
}

package com.cnmci.stats.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class ArtisanFromMairieResponse {
    private long id;
    private String nom;
    @JsonProperty("date_naissance")
    private String date;
    private String contact1;
    private String contact2;
    private String quartier;
    private String metier;
    private double longitude;
    private double latitude;
}

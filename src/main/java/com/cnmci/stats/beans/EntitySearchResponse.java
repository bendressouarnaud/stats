package com.cnmci.stats.beans;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class EntitySearchResponse {
    private long id;
    private String nom;
    private String contact;
    private String datenaissance;
    private String metier;
    private int paiement;
    private String commune;
    private String type;
    private String image;
    private String datenrolement;
    private String quartier;
    private int amende;
    private double latitude;
    private double longitude;
}

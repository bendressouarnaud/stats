package com.cnmci.stats.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AmendeRequest {
    private long id;
    private int montant;
    private String commentaire;
    @JsonProperty("entity_id")
    private long entityId;
    @JsonProperty("entity_type")
    private String entityType;
}

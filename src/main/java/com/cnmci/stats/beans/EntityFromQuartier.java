package com.cnmci.stats.beans;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class EntityFromQuartier {
    private String quartier;
    private String date;
    private long total;
}

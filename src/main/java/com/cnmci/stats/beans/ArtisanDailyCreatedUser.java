package com.cnmci.stats.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ArtisanDailyCreatedUser(long id, String agent,
                                      @JsonProperty("total_artisan") long artisan,
                                      @JsonProperty("somme_encaisse") long somme) {
}

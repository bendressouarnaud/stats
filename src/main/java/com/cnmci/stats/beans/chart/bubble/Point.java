package com.cnmci.stats.beans.chart.bubble;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Point(int x, long y, @JsonProperty("r") long valeur) {
}

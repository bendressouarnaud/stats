package com.cnmci.stats.beans.chart.polar;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Builder
public class DataSetPolar {
    private String label;
    private List<Long> data;
    @JsonProperty("background_color")
    private List<String> backgroundColor;
}

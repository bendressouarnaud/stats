package com.cnmci.stats.beans.chart.polar;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Builder
public class PolarChartData {
    private List<String> labels;
    @JsonProperty("datasets")
    private List<DataSetPolar> dataSetPolars;
}

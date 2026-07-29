package com.cnmci.stats.beans.chart.bubble;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Builder
public class BubbleChartData {
    @JsonProperty("datasets")
    List<DataSet> dataSet;
}

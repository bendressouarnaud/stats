package com.cnmci.stats.beans.chart.bubble;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Builder
public class DataSet {
    private String label;
    private String backgroundColor;
    private List<Point> data;
}

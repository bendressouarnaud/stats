package com.cnmci.stats.beans;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DailyPaymentLineChart {
    private String day;
    private double total;
}

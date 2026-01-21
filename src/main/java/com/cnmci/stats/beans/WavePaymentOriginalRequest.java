package com.cnmci.stats.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WavePaymentOriginalRequest {
    private Integer amount;
    private String currency;
    @JsonProperty("error_url")
    private String errorUrl;
    @JsonProperty("success_url")
    private String successUrl;
}

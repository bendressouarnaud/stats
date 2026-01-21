package com.cnmci.stats.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WavePaymentResponse {
    private String id;
    private Integer amount;
    @JsonProperty("checkout_status")
    private String checkoutStatus;
    @JsonProperty("client_reference")
    private String clientReference;
    private String currency;
    @JsonProperty("error_url")
    private String errorUrl;
    @JsonProperty("last_payment_error")
    private String lastPaymentError;
    @JsonProperty("business_name")
    private String businessName;
    @JsonProperty("payment_status")
    private String paymentStatus;
    @JsonProperty("success_url")
    private String successUrl;
    @JsonProperty("wave_launch_url")
    private String waveLaunchUrl;
    @JsonProperty("when_completed")
    private String whenCompleted;
    @JsonProperty("when_created")
    private String whenCreated;
    @JsonProperty("when_expires")
    private String whenExpires;
}

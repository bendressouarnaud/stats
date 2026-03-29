package com.cnmci.stats.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SmsResponseToken(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("session_state") String sessionState,
        String scope,
        @JsonProperty("expires_in") int expiresIn,
        @JsonProperty("refresh_expires_in") int refreshExpiresIn,
        @JsonProperty("not-before-policy") int notBeforePolicy
) {
}

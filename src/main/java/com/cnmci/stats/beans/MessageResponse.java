package com.cnmci.stats.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
public class MessageResponse {
    private long id;
    @JsonProperty("local_dateTime")
    private LocalDateTime localDateTime;
    private String message;
    @JsonProperty("http_status")
    private int httpStatus;
}

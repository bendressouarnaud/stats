package com.cnmci.stats.beans;

public record SmsRequest(String channel, String recipient, String body) {
}

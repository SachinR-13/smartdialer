package com.smartdialer.provider;
public interface TelecomProvider {
 String name();
 boolean healthy();
 ProviderStartResult initiate(String idempotencyKey, String phoneNumber);
}

package com.smartdialer.provider;
public record ProviderEvent(String eventId, String providerCallId, ProviderEventType type, String detail) {}

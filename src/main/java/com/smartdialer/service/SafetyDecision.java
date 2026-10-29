package com.smartdialer.service;
public record SafetyDecision(int requested, int approved, String reason, boolean fallbackToProgressive) {}

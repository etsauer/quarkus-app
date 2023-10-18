package io.prometheus.api;

public record LeadTimeData (
    String commit,
    String image,
    Double timestamp,
    Double lead_time
) {}

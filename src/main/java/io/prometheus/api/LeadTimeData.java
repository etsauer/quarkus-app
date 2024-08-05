package io.prometheus.api;

public record LeadTimeData (
    String commit,
    String commit_link,
    String image,
    Double timestamp,
    Double lead_time
) {}

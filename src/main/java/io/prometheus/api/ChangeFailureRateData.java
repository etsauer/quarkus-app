package io.prometheus.api;

public record ChangeFailureRateData(
    String issue_id,
    Double timestamp
) {}

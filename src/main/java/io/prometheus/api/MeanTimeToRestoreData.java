package io.prometheus.api;

public record MeanTimeToRestoreData(
    String issue_id,
    Double time_to_resolve,
    Double timestamp
) {}

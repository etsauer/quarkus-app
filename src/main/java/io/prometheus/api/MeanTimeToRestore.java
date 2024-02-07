package io.prometheus.api;

public record MeanTimeToRestore(
    Double mttr,
    Double last
) {}

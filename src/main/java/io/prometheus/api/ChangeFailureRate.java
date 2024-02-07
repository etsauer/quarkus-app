package io.prometheus.api;

public record ChangeFailureRate(
    Double cfr,
    Double last
) {}

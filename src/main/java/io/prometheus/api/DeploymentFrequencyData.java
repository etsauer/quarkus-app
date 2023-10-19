package io.prometheus.api;

public record DeploymentFrequencyData(
    String image,
    Double timestamp
) {}

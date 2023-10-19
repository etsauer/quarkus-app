package io.prometheus.api;

import java.util.List;

public record DeploymentFrequencyData(
    String image,
    List<Value> values
) {}

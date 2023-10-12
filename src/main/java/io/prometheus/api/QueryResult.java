package io.prometheus.api;

import java.util.List;

public record QueryResult (
    Metric metric,
    List<Value> values
) {}

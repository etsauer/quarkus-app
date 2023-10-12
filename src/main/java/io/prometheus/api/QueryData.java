package io.prometheus.api;

import java.util.List;

public record QueryData (
    String resultType,
    List<QueryResult> result
) {}

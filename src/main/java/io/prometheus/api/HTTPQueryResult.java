package io.prometheus.api;

public record HTTPQueryResult (

    String status,
    QueryData data

) {}
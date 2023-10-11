package io.prometheus.api;

import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

public class HTTPQueryResult {

    public Metric metric;
    public List<Entry<Date,String>> values;
}
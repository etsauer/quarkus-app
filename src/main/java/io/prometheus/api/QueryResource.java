package io.prometheus.api;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.util.Set;

@Path("/query")
public class QueryResource {

    @RestClient
    QueryService queryService;

    @GET
    public Set<HTTPQueryResult> query(String query) {
        return queryService.runQuery(query);
    }
    
}

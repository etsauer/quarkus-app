package io.prometheus.api;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@Path("/query")
@RegisterRestClient(configKey = "prometheus-api")
@ClientHeaderParam(name = "Authorization", value = "Bearer ${bearer.token}")
public interface QueryService {
    
    @GET
    HTTPQueryResult runQuery(@QueryParam("query") String query);

    @GET
    JsonNode runJsonQuery(@QueryParam("query") String query);
}

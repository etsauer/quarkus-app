package io.prometheus.api;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import java.util.Set;

@Path("/query")
@RegisterRestClient(configKey = "prometheus-api")
@ClientHeaderParam(name = "Authorization", value = "Bearer ${bearer.token}")
public interface QueryService {
    
    @GET
    Set<HTTPQueryResult> runQuery(@QueryParam("query") String query);
}

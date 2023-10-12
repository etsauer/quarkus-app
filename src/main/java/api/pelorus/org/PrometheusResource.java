package api.pelorus.org;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.util.Set;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.fasterxml.jackson.databind.JsonNode;

import io.prometheus.api.HTTPQueryResult;
import io.prometheus.api.QueryService;

@Path("/query")
public class PrometheusResource {

    @RestClient
    QueryService queryService;
    
    @GET
    @Path("/sdp_application")
    @Produces(MediaType.APPLICATION_JSON)
    public HTTPQueryResult querySDPApplications() {
        HTTPQueryResult results = queryService.runQuery("sdp:applications[2d]");
        return results;
    }

    @GET
    @Path("/deployment_frequency")
    @Produces(MediaType.APPLICATION_JSON)
    public HTTPQueryResult querySDPApplications(@QueryParam("range") String range) {
        HTTPQueryResult results = queryService.runQuery(String.format("count (count_over_time (deploy_timestamp [%s]))", range));
        return results;
    }

    @GET
    @Path("/json")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonNode queryJson(@QueryParam("query") String query) {
        JsonNode results = queryService.runJsonQuery(query);
        return results;
    }

}

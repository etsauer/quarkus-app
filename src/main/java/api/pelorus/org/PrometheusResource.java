package api.pelorus.org;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.Set;

import io.prometheus.api.HTTPQueryResult;
import io.prometheus.api.QueryResource;

@Path("/query")
public class PrometheusResource {

    QueryResource queryResource;
    
    @GET
    @Path("/sdp_application")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<HTTPQueryResult> querySDPApplications() {
        Set<HTTPQueryResult> results = queryResource.query("sdp:applications[2d]");
        return results;
    }
}

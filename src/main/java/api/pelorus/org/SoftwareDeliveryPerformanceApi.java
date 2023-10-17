package api.pelorus.org;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.fasterxml.jackson.databind.JsonNode;

import io.prometheus.api.App;
import io.prometheus.api.ChangeFailureRate;
import io.prometheus.api.DeploymentFrequency;
import io.prometheus.api.HTTPQueryResult;
import io.prometheus.api.LeadTime;
import io.prometheus.api.MeanTimeToRestore;
import io.prometheus.api.QueryResult;
import io.prometheus.api.QueryService;

@Path("/sdp")
public class SoftwareDeliveryPerformanceApi {

    private final String APPS_LIST = """
        group(
            count_over_time(commit_timestamp [%1$s]) or 
            count_over_time(deploy_timestamp [%1$s]) or 
            count_over_time(failure_creation_timestamp [%1$s]) or 
            count_over_time(failure_resolution_timestamp[%1$s])
        ) by (app)
        """;
    private final String LEAD_TIME_FOR_CHANGE = "avg_over_time(sdp:lead_time:global [%s])";
    private final String LEAD_TIME_FOR_CHANGE_BY_APP = "avg_over_time(sdp:lead_time:by_app{app=~'.*%s.*'}[%s])";
    private final String DEPLOYMENT_FREQUENCY = "count (count_over_time (deploy_timestamp [%s]))";
    private final String DEPLOYMENT_FREQUENCY_BY_APP = "count (count_over_time (deploy_timestamp{app=~'.*%s.*'}[%s]))";
    private final String MEAN_TIME_TO_RESTORE_BY_APP = "avg(avg_over_time(sdp:time_to_restore:by_app{app=~\".*%s.*\"}[%s]))";
    private final String CHANGE_FAILURE_RATE = "(count by (app) (count_over_time(failure_creation_timestamp{app!=\"unknown\"}[%1$s]) or sdp:lead_time:by_app * 0) / count_over_time(sdp:lead_time:by_app [%1$s]))";
    private final String CHANGE_FAILURE_RATE_BY_APP = "(count(count_over_time(failure_creation_timestamp{app=~\".*%1$s.*\"}[%2$s])) or sdp:lead_time:by_app * 0) / sum(count_over_time(sdp:lead_time:by_app{app=~\".*%1$s.*\"} [%2$s]))";

    @RestClient
    QueryService queryService;
    
    @GET
    @Path("/apps")
    @Produces(MediaType.APPLICATION_JSON)
    public List<App> getApps(@QueryParam("range") String range) {
        HTTPQueryResult results = queryService.runQuery(String.format(APPS_LIST, range));
        List<App> list = new ArrayList<App>();
        for (QueryResult qr: results.data().result()) {
            App app = new App(qr.metric().app);
            list.add(app);
        }
        return list;
    }

    @GET
    @Path("/lead_time_for_change")
    @Produces(MediaType.APPLICATION_JSON)
    public HTTPQueryResult queryLeadTimeforChange(@QueryParam("range") String range) {
        HTTPQueryResult results = queryService.runQuery(String.format(LEAD_TIME_FOR_CHANGE, range));
        return results;
    }

    @GET
    @Path("/lead_time_for_change/{app}")
    @Produces(MediaType.APPLICATION_JSON)
    public LeadTime queryLeadTimeforChangeByApp(String app, @QueryParam("range") String range) {
        HTTPQueryResult results = queryService.runQuery(String.format(LEAD_TIME_FOR_CHANGE_BY_APP, app, range));
        return new LeadTime(results.data().result().get(0).value().value());
    }

    @GET
    @Path("/deployment_frequency")
    @Produces(MediaType.APPLICATION_JSON)
    public HTTPQueryResult queryDeploymentFrequency(@QueryParam("range") String range) {
        HTTPQueryResult results = queryService.runQuery(String.format(DEPLOYMENT_FREQUENCY, range));
        return results;
    }

    @GET
    @Path("/deployment_frequency/{app}")
    @Produces(MediaType.APPLICATION_JSON)
    public DeploymentFrequency queryDeploymentFrequencyByApp(String app, @QueryParam("range") String range) {
        HTTPQueryResult results = queryService.runQuery(String.format(DEPLOYMENT_FREQUENCY_BY_APP, app, range));
        return new DeploymentFrequency(results.data().result().get(0).value().value());
    }

    @GET
    @Path("/mean_time_to_restore/{app}")
    @Produces(MediaType.APPLICATION_JSON)
    public MeanTimeToRestore queryMeanTimeToRestoreByApp(String app, @QueryParam("range") String range) {
        HTTPQueryResult results = queryService.runQuery(String.format(MEAN_TIME_TO_RESTORE_BY_APP, app, range));
        return new MeanTimeToRestore(results.data().result().get(0).value().value());
    }

    @GET
    @Path("/change_failure_rate/{app}")
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeFailureRate queryChangeFailureRateByApp(String app, @QueryParam("range") String range) {
        HTTPQueryResult results = queryService.runQuery(String.format(CHANGE_FAILURE_RATE_BY_APP, app, range));
        return new ChangeFailureRate(results.data().result().get(0).value().value());
    }

    @GET
    @Path("/json")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonNode queryJson(@QueryParam("query") String query) {
        JsonNode results = queryService.runJsonQuery(query);
        return results;
    }

}

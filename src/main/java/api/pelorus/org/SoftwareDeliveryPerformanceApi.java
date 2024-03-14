package api.pelorus.org;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jboss.logging.Logger;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.fasterxml.jackson.databind.JsonNode;

import io.prometheus.api.App;
import io.prometheus.api.ChangeFailureRate;
import io.prometheus.api.DeploymentFrequency;
import io.prometheus.api.DeploymentFrequencyData;
import io.prometheus.api.HTTPQueryResult;
import io.prometheus.api.LeadTime;
import io.prometheus.api.LeadTimeData;
import io.prometheus.api.MeanTimeToRestore;
import io.prometheus.api.MeanTimeToRestoreData;
import io.prometheus.api.QueryResult;
import io.prometheus.api.QueryService;
import io.prometheus.api.Value;

import lombok.Builder;

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

    /**
     * Range and At arguments for templates so the arguments don't get confused.
     */
    @Builder
    static record RangeAt(String range, String at) {
    }

    /**
     * App, Range, and At arguments for templates so the arguments don't get
     * confused.
     */
    @Builder
    static record AppRangeAt(String app, String range, String at) {
    }

    private static class LeadTimeForChangeQuery {
        static final String LEAD_TIME_FOR_CHANGE = "avg_over_time(sdp:lead_time:global [%s] @ %s)";
        static final String BY_APP = "avg_over_time(sdp:lead_time:by_app{app=~'.*%s.*'}[%s] @ %s)";
        static final String BY_APP_OFFSET = "avg_over_time(sdp:lead_time:by_app{app=~'.*%1$s.*'}[%2$s] @ %3$s offset %2$s)";
        // It should be this, once https://github.com/dora-metrics/pelorus/issues/1088
        // gets resolved
        static final String BY_APP_DATA = "sdp:lead_time:by_commit{app=~'.*%s.*'}[%s] @ %s";
        // private final String LEAD_TIME_FOR_CHANGE_BY_APP_DATA =
        // "(min_over_time(deploy_timestamp{app=~\".*%1$s.*\"}[%2$s]) -
        // on(app,image_sha) group_left(commit) (max by (app, commit, image_sha)
        // (max_over_time(commit_timestamp{app=~\".*%1$s.*\"}[%2$s]))))";

        static String general(RangeAt args) {
            return LEAD_TIME_FOR_CHANGE.formatted(args.range, args.at);
        }

        static String byApp(AppRangeAt args) {
            return BY_APP.formatted(args.app, args.range, args.at);
        }

        static String byAppOffset(AppRangeAt args) {
            return BY_APP_OFFSET.formatted(args.app, args.range, args.at);
        }

        static String byAppData(AppRangeAt args) {
            return BY_APP_DATA.formatted(args.app, args.range, args.at);
        }
    }

    private static class DeploymentFrequencyQuery {
        // TODO: where does the `@` go?
        static final String DEPLOYMENT_FREQUENCY = "count (count_over_time (deploy_timestamp [%s] @ %s))";
        static final String BY_APP = "count (count_over_time (deploy_timestamp{app=~'.*%s.*'}[%s] @ %s))";
        static final String BY_APP_OFFSET = "count (count_over_time (deploy_timestamp{app=~'.*%1$s.*'}[%2$s] @ %3$s offset %2$s))";
        static final String BY_APP_DATA = "deploy_timestamp{app=~'.*%s.*'}[%s] @ %s";

        static String general(RangeAt args) {
            return DEPLOYMENT_FREQUENCY.formatted(args.range, args.at);
        }

        static String byApp(AppRangeAt args) {
            return BY_APP.formatted(args.app, args.range, args.at);
        }

        static String byAppOffset(AppRangeAt args) {
            return BY_APP_OFFSET.formatted(args.app, args.range, args.at);
        }

        static String byAppData(AppRangeAt args) {
            return BY_APP_DATA.formatted(args.app, args.range, args.at);
        }
    }

    private static class MeanTimeToRestoreQuery {
        static final String BY_APP = "avg(avg_over_time(sdp:time_to_restore:by_app{app=~\".*%s.*\"}[%s] @ %s))";
        static final String BY_APP_OFFSET = "avg(avg_over_time(sdp:time_to_restore:by_app{app=~\".*%1$s.*\"}[%2$s] @ %3$s offset %2$s))";
        static final String BY_APP_DATA = "sdp:time_to_restore:by_issue{app=~\".*%s.*\"}[%s] @ %s";

        static String byApp(AppRangeAt args) {
            return BY_APP.formatted(args.app, args.range, args.at);
        }

        static String byAppOffset(AppRangeAt args) {
            return BY_APP_OFFSET.formatted(args.app, args.range, args.at);
        }

        static String byAppData(AppRangeAt args) {
            return BY_APP_DATA.formatted(args.app, args.range, args.at);
        }
    }

    private static class ChangeFailureRateQuery {
        static final String CHANGE_FAILURE_RATE = "(count by (app) (count_over_time(failure_creation_timestamp{app!=\"unknown\"}[%1$s] @ %2$s) or sdp:lead_time:by_app @ %2$s * 0) / count_over_time(sdp:lead_time:by_app [%1$s] @ %2$s))";
        static final String BY_APP = "(count(count_over_time(failure_creation_timestamp{app=~\".*%1$s.*\"}[%2$s] @ %3$s)) or sdp:lead_time:by_app @ %3$s * 0) / count(count_over_time(sdp:lead_time:by_commit{app=~\".*%1$s.*\"} [%2$s] @ %3$s))";
        static final String BY_APP_OFFSET = "(count(count_over_time(failure_creation_timestamp{app=~\".*%1$s.*\"}[%2$s] @ %3$s offset %2$s)) or sdp:lead_time:by_app @ %3$s * 0) / count(count_over_time(sdp:lead_time:by_commit{app=~\".*%1$s.*\"} [%2$s] @ %3$s offset %2$s))";

        static String general(RangeAt args) {
            return CHANGE_FAILURE_RATE.formatted(args.range, args.at);
        }

        static String byApp(AppRangeAt args) {
            return BY_APP.formatted(args.app, args.range, args.at);
        }

        static String byAppOfsset(AppRangeAt args) {
            return BY_APP_OFFSET.formatted(args.app, args.range, args.at);
        }
    }

    private static final Logger LOG = Logger.getLogger(SoftwareDeliveryPerformanceApi.class);

    @RestClient
    QueryService queryService;

    private HTTPQueryResult logAndQuery(String queryName, String query) {
        LOG.debugf("%s query: %s", queryName, query);
        return queryService.runQuery(query);
    }

    @GET
    @Path("/apps")
    @Produces(MediaType.APPLICATION_JSON)
    public List<App> getApps(@QueryParam("range") String range) {
        HTTPQueryResult results = queryService.runQuery(String.format(APPS_LIST, range));
        List<App> list = new ArrayList<App>();
        for (QueryResult qr : results.data().result()) {
            App app = new App(qr.metric().app.replace("/", ""));
            list.add(app);
        }
        return list;
    }

    @GET
    @Path("/lead_time_for_change")
    @Produces(MediaType.APPLICATION_JSON)
    public HTTPQueryResult queryLeadTimeforChange(@QueryParam("range") String range,
            @QueryParam("start") String start) {
        HTTPQueryResult results = logAndQuery("LEAD_TIME_FOR_CHANGE",
                LeadTimeForChangeQuery.general(RangeAt.builder().range(range).at(start).build()));
        return results;
    }

    @GET
    @Path("/lead_time_for_change/{app}")
    @Produces(MediaType.APPLICATION_JSON)
    public LeadTime queryLeadTimeforChangeByApp(String app, @QueryParam("range") String range,
            @QueryParam("start") String start) {
        final AppRangeAt args = AppRangeAt.builder().app(app).range(range).at(start).build();
        HTTPQueryResult results = logAndQuery("LEAD_TIME_FOR_CHANGE_BY_APP", LeadTimeForChangeQuery.byApp(args));

        HTTPQueryResult offset = logAndQuery("LEAD_TIME_FOR_CHANGE_BY_APP_OFFSET",
                LeadTimeForChangeQuery.byAppOffset(args));
        try {
            return new LeadTime(results.data().result().get(0).value().value(),
                    offset.data().result().get(0).value().value());
        } catch (IndexOutOfBoundsException e) {
            Double current = 0.0;
            Double previous = 0.0;
            if (results.data().result().size() > 0) {
                current = results.data().result().get(0).value().value();
            }
            if (offset.data().result().size() > 0) {
                previous = offset.data().result().get(0).value().value();
            }
            return new LeadTime(current, previous);
        }
    }

    @GET
    @Path("/lead_time_for_change/{app}/data")
    @Produces(MediaType.APPLICATION_JSON)
    public List<LeadTimeData> queryLeadTimeforChangeDataByApp(String app, @QueryParam("range") String range,
            @QueryParam("start") String start) {
        HTTPQueryResult results = logAndQuery("LEAD_TIME_FOR_CHANGE_BY_APP_DATA",
                LeadTimeForChangeQuery.byAppData(AppRangeAt.builder().app(app).range(range).at(start).build()));
        List<LeadTimeData> leadTimeData = new ArrayList<LeadTimeData>();
        for (QueryResult qr : results.data().result()) {
            LeadTimeData data = new LeadTimeData(qr.metric().commit, qr.metric().image_sha,
                    qr.values().get(0).timestamp(), qr.values().get(0).value());
            leadTimeData.add(data);
        }
        return leadTimeData;
    }

    @GET
    @Path("/deployment_frequency")
    @Produces(MediaType.APPLICATION_JSON)
    public HTTPQueryResult queryDeploymentFrequency(@QueryParam("range") String range,
            @QueryParam("start") String start) {
        HTTPQueryResult results = logAndQuery("DEPLOYMENT_FREQUENCY",
                DeploymentFrequencyQuery.general(RangeAt.builder().range(range).at(start).build()));
        return results;
    }

    @GET
    @Path("/deployment_frequency/{app}")
    @Produces(MediaType.APPLICATION_JSON)
    public DeploymentFrequency queryDeploymentFrequencyByApp(String app, @QueryParam("range") String range,
            @QueryParam("start") String start) {
        final AppRangeAt queryArgs = AppRangeAt.builder().app(app).range(range).at(start).build();

        HTTPQueryResult results = logAndQuery("DEPLOYMENT_FREQUENCY_BY_APP", DeploymentFrequencyQuery.byApp(queryArgs));
        HTTPQueryResult offset = logAndQuery("DEPLOYMENT_FREQUENCY_BY_APP_OFFSET",
                DeploymentFrequencyQuery.byAppOffset(queryArgs));
        try {
            return new DeploymentFrequency(results.data().result().get(0).value().value(),
                    offset.data().result().get(0).value().value());
        } catch (IndexOutOfBoundsException e) {
            Double current = 0.0;
            Double previous = 0.0;
            if (results.data().result().size() > 0) {
                current = results.data().result().get(0).value().value();
            }
            if (offset.data().result().size() > 0) {
                previous = offset.data().result().get(0).value().value();
            }
            return new DeploymentFrequency(current, previous);
        }
    }

    @GET
    @Path("/deployment_frequency/{app}/data")
    @Produces(MediaType.APPLICATION_JSON)
    public List<DeploymentFrequencyData> queryDeploymentFrequencyDataByApp(String app,
            @QueryParam("range") String range, @QueryParam("start") String start) {
        HTTPQueryResult results = logAndQuery("DEPLOYMENT_FREQUENCY_BY_APP_DATA",
                DeploymentFrequencyQuery.byAppData(
                        AppRangeAt.builder().app(app).range(range).at(start).build()));
        List<DeploymentFrequencyData> deployFreqData = new ArrayList<DeploymentFrequencyData>();
        for (QueryResult qr : results.data().result()) {
            List<Value> lv = qr.values();
            // Sort the deploy_timestamp metrics so that the earliest one is at the top of
            // the list
            Collections.sort(lv, new Comparator<Value>() {
                @Override
                public int compare(Value v1, Value v2) {
                    return v1.timestamp().compareTo(v2.timestamp());
                }
            });
            // return only the first occurence of a deployment for a given image_sha
            DeploymentFrequencyData data = new DeploymentFrequencyData(qr.metric().image_sha,
                    lv.get(0).timestamp());
            deployFreqData.add(data);
        }
        return deployFreqData;
    }

    @GET
    @Path("/mean_time_to_restore/{app}")
    @Produces(MediaType.APPLICATION_JSON)
    public MeanTimeToRestore queryMeanTimeToRestoreByApp(String app, @QueryParam("range") String range,
            @QueryParam("start") String start) {
        final AppRangeAt queryArgs = AppRangeAt.builder().app(app).range(range).at(start).build();

        HTTPQueryResult results = logAndQuery("MEAN_TIME_TO_RESTORE_BY_APP",
                MeanTimeToRestoreQuery.byApp(queryArgs));
        HTTPQueryResult offset = logAndQuery("MEAN_TIME_TO_RESTORE_BY_APP_OFFSET",
                MeanTimeToRestoreQuery.byAppOffset(queryArgs));
        try {
            return new MeanTimeToRestore(results.data().result().get(0).value().value(),
                    offset.data().result().get(0).value().value());
        } catch (IndexOutOfBoundsException e) {
            Double current = 0.0;
            Double previous = 0.0;
            if (results.data().result().size() > 0) {
                current = results.data().result().get(0).value().value();
            }
            if (offset.data().result().size() > 0) {
                previous = offset.data().result().get(0).value().value();
            }
            return new MeanTimeToRestore(current, previous);
        }
    }

    @GET
    @Path("/mean_time_to_restore/{app}/data")
    @Produces(MediaType.APPLICATION_JSON)
    public List<MeanTimeToRestoreData> queryLeadMeanTimeToRestoreDataByApp(String app,
            @QueryParam("range") String range, @QueryParam("start") String start) {
        HTTPQueryResult results = logAndQuery("MEAN_TIME_TO_RESTORE_BY_APP_DATA",
                MeanTimeToRestoreQuery.byAppData(AppRangeAt.builder().app(app).range(range).at(start).build()));
        List<MeanTimeToRestoreData> mttrData = new ArrayList<MeanTimeToRestoreData>();
        for (QueryResult qr : results.data().result()) {
            MeanTimeToRestoreData data = new MeanTimeToRestoreData(qr.metric().issue_number,
                    qr.values().get(0).value(), qr.values().get(0).timestamp());
            mttrData.add(data);
        }
        return mttrData;
    }

    @GET
    @Path("/change_failure_rate/{app}")
    @Produces(MediaType.APPLICATION_JSON)
    public ChangeFailureRate queryChangeFailureRateByApp(String app, @QueryParam("range") String range,
            @QueryParam("start") String start) {
        final AppRangeAt queryArgs = AppRangeAt.builder().app(app).range(range).at(start).build();

        HTTPQueryResult results = logAndQuery("CHANGE_FAILURE_RATE_BY_APP", ChangeFailureRateQuery.byApp(queryArgs));
        HTTPQueryResult offset = logAndQuery("CHANGE_FAILURE_RATE_BY_APP_OFFSET",
                ChangeFailureRateQuery.byAppOfsset(queryArgs));

        try {
            return new ChangeFailureRate(results.data().result().get(0).value().value(),
                    offset.data().result().get(0).value().value());
        } catch (IndexOutOfBoundsException e) {
            Double current = 0.0;
            Double previous = 0.0;
            if (results.data().result().size() > 0) {
                current = results.data().result().get(0).value().value();
            }
            if (offset.data().result().size() > 0) {
                previous = offset.data().result().get(0).value().value();
            }
            return new ChangeFailureRate(current, previous);
        }
    }

    @GET
    @Path("/json")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonNode queryJson(@QueryParam("query") String query) {
        JsonNode results = queryService.runJsonQuery(query);
        return results;
    }

}

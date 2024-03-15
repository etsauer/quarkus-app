package org.pelorus.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/review")
public class AIReportResource {

    @Inject
    TriageService triage;

    record Review(String review) {
    }

    @POST
    public TriagedReview triage(Review review) {
        return triage.triage(review.review());
    }

}
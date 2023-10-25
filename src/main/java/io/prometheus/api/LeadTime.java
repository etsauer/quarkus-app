package io.prometheus.api;

import javax.validation.constraints.NotEmpty;

public record LeadTime (
    @NotEmpty(message = "{ltfc.lead_time.empty}")
    Double ltfc,
    @NotEmpty(message = "{ltfc.lead_time_last.empty}")
    Double last
) {}

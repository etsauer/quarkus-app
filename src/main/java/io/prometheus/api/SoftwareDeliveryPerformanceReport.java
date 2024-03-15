package io.prometheus.api;

import java.util.List;

public record SoftwareDeliveryPerformanceReport(
    LeadTime lead_time,
    List<LeadTimeData> lead_time_data,
    DeploymentFrequency deployment_frequency,
    List<DeploymentFrequencyData> deployment_frequency_data,
    MeanTimeToRestore mean_time_to_restore,
    List<MeanTimeToRestoreData> mean_time_to_restore_data,
    ChangeFailureRate change_failure_rate,
    List<ChangeFailureRateData> change_failure_rate_data
) {}

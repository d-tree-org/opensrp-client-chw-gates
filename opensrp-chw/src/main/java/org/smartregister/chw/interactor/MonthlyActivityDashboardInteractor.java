package org.smartregister.chw.interactor;

import org.smartregister.reporting.BuildConfig;
import org.smartregister.reporting.contract.ReportContract;
import org.smartregister.reporting.job.RecurringIndicatorGeneratingJob;

import java.util.concurrent.TimeUnit;

public class MonthlyActivityDashboardInteractor implements ReportContract.Interactor {

    public MonthlyActivityDashboardInteractor(){}

    @Override
    public void scheduleDailyTallyJob() {
        RecurringIndicatorGeneratingJob.scheduleJobImmediately(RecurringIndicatorGeneratingJob.TAG);
//        RecurringIndicatorGeneratingJob.scheduleJob(RecurringIndicatorGeneratingJob.TAG,
//                TimeUnit.MINUTES.toMillis(1), TimeUnit.MINUTES.toMillis(1));
    }
}

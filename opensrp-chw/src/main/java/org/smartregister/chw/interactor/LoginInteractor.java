package org.smartregister.chw.interactor;

import org.smartregister.login.interactor.BaseLoginInteractor;
import org.smartregister.view.contract.BaseLoginContract;


public class LoginInteractor extends BaseLoginInteractor implements BaseLoginContract.Interactor {

    private LoginInteractor.Flavor flavor;
    public LoginInteractor(BaseLoginContract.Presenter loginPresenter) {

        super(loginPresenter);
        this.flavor = new LoginInteractorFlv();
    }

    @Override
    protected void scheduleJobsPeriodically() {
        this.flavor.getJobsToSyncPeriodically();
    }

    @Override
    protected void scheduleJobsImmediately() {
        super.scheduleJobsImmediately();
        // Run initial job immediately on log in since the job will run a bit later (~ 15 mins +)
        this.flavor.getJobsToSyncImmediately();
    }

    public interface Flavor {
        void getJobsToSyncPeriodically();
        void getJobsToSyncImmediately();
        long getFlexValue(int value);
    }
}

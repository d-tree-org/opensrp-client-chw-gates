package org.smartregister.chw.interactor;

import android.content.Context;

import androidx.annotation.VisibleForTesting;

import org.smartregister.chw.anc.contract.BaseAncMedicalHistoryContract;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.interactor.BaseAncMedicalHistoryInteractor;
import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.chw.fp.util.AppExecutors;
import org.smartregister.chw.util.Constants;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class AdolescentMedicalHistoryInteractor extends BaseAncMedicalHistoryInteractor {

    protected AppExecutors appExecutors;

    @VisibleForTesting
    AdolescentMedicalHistoryInteractor(AppExecutors appExecutors) { this.appExecutors = appExecutors; }

    public AdolescentMedicalHistoryInteractor() {
        this(new AppExecutors());
    }

    @Override
    public void getMemberHistory(String memberID, Context context, BaseAncMedicalHistoryContract.InteractorCallBack callBack) {
        final Runnable runnable = () -> {

            final List<Visit> visits = new ArrayList<>();
            try {
                visits.addAll(VisitUtils.getVisits(memberID, Constants.ADOLESCENT_HOME_VISIT_DONE));
            } catch (Exception e) {
                Timber.e(e);
            }
            appExecutors.mainThread().execute(() -> callBack.onDataFetched(visits));
        };

        appExecutors.diskIO().execute(runnable);
    }
}

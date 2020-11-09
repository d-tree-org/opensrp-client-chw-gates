package org.smartregister.chw.contract;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import org.smartregister.chw.anc.contract.BaseAncMedicalHistoryContract;
import org.smartregister.chw.anc.domain.Visit;

import java.util.List;

public interface AdolescentMedicalHistoryContract extends BaseAncMedicalHistoryContract {

    interface View extends BaseAncMedicalHistoryContract.View {
        void onDataReceived(List<Visit> visits);

        android.view.View bindViews(Activity activity);

        void processViewData(List<Visit> visits, Context context);



    }

}

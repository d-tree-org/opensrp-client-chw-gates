package org.smartregister.chw.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.smartregister.chw.R;
import org.smartregister.chw.anc.activity.BaseAncMedicalHistoryActivity;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.presenter.BaseAncMedicalHistoryPresenter;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.chw.contract.AdolescentMedicalHistoryContract;
import org.smartregister.chw.core.activity.CoreAncMedicalHistoryActivity;
import org.smartregister.chw.core.domain.MedicalHistory;
import org.smartregister.chw.core.utils.MedicalHistoryViewBuilder;
import org.smartregister.chw.interactor.AdolescentMedicalHistoryInteractor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AdolescentMedicalHistoryActivity extends BaseAncMedicalHistoryActivity implements AdolescentMedicalHistoryContract.View {

    protected LayoutInflater inflater;
    private LinearLayout parentView;
    private Map<String, List<Visit>> visitMap = new LinkedHashMap<>();
    private Context context;
    private List<Visit> visits;

    public static void startMe(Activity activity, MemberObject memberObject) {
        Intent intent = new Intent(activity, AdolescentMedicalHistoryActivity.class);
        intent.putExtra(Constants.ANC_MEMBER_OBJECTS.MEMBER_PROFILE_OBJECT, memberObject);
        activity.startActivity(intent);
    }

    @Override
    public void initializePresenter() {
        presenter = new BaseAncMedicalHistoryPresenter(new AdolescentMedicalHistoryInteractor(), this, memberObject.getBaseEntityId());
    }

    @Override
    public View renderView(List<Visit> visits) {
        super.renderView(visits);
        View view = bindViews(this);
        displayLoadingState(true);
        processViewData(visits, this);
        displayLoadingState(false);
        return view;
    }

    @Override
    public View bindViews(Activity activity) {
        inflater = activity.getLayoutInflater();
        this.context = activity;
        parentView = new LinearLayout(activity);
        parentView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        parentView.setOrientation(LinearLayout.VERTICAL);
        return parentView;
    }

    @Override
    public void processViewData(List<Visit> visits, Context context) {
        this.visits = visits;

        for (Visit v : this.visits) {
            List<Visit> type_visits = visitMap.get(v.getVisitType());
            if (type_visits == null) type_visits = new ArrayList<>();

            type_visits.add(v);
            visitMap.put(v.getVisitType(), type_visits);
        }

        evaluateLastVisitDate();

    }

    private void evaluateLastVisitDate() {
        if (visits.size() > 0) {

            List<MedicalHistory> medicalHistories = new ArrayList<>();
            MedicalHistory history = new MedicalHistory();
            int days = Days.daysBetween(new DateTime(visits.get(0).getDate()), new DateTime()).getDays();
            if (days < 1) {
                history.setText(context.getString(org.smartregister.chw.core.R.string.less_than_twenty_four));
            } else {
                history.setText(context.getString(org.smartregister.chw.core.R.string.last_visit_x_days_ago, Integer.toString(days)));
            }
            medicalHistories.add(history);

            View view = new MedicalHistoryViewBuilder(inflater, context)
                    .withHistory(medicalHistories)
                    .withTitle(context.getString(R.string.last_visit))
                    .build();

            parentView.addView(view);
        }
    }
}

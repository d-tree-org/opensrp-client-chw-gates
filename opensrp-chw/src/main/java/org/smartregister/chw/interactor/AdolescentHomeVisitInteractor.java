package org.smartregister.chw.interactor;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.R;
import org.smartregister.chw.action_helper.MedicationInUseActionHelper;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.actionhelper.HomeVisitActionHelper;
import org.smartregister.chw.anc.contract.BaseAncHomeVisitContract;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.interactor.BaseAncHomeVisitInteractor;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.anc.repository.VisitDetailsRepository;
import org.smartregister.chw.anc.repository.VisitRepository;
import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.chw.util.Constants;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.clientandeventmodel.Obs;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

public class AdolescentHomeVisitInteractor extends BaseAncHomeVisitInteractor {
    protected LinkedHashMap<String, BaseAncHomeVisitAction> actionList;
    protected Context context;
    protected Map<String, List<VisitDetail>> details = null;
    protected MemberObject memberObject;
    protected BaseAncHomeVisitContract.View view;
    protected Date dob;
    protected Boolean editMode = false;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());


    @Override
    public void calculateActions(BaseAncHomeVisitContract.View view, MemberObject memberObject, BaseAncHomeVisitContract.InteractorCallBack callBack) {

        actionList = new LinkedHashMap<>();
        context = view.getContext();
        this.memberObject = memberObject;
        editMode = view.getEditMode();
        try {
            this.dob = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(memberObject.getDob());
        } catch (ParseException e) {
            Timber.e(e);
        }
        this.view = view;
        // get the preloaded data
        if (view.getEditMode()) {
            Visit lastVisit = getVisitRepository().getLatestVisit(memberObject.getBaseEntityId(), Constants.ADOLESCENT_HOME_VISIT_DONE);
            if (lastVisit != null) {
                details = VisitUtils.getVisitGroups(getVisitDetailsRepository().getVisits(lastVisit.getVisitId()));
            }
        }

        Runnable runnable = () -> {

            try {
                evaluateEducationAndCounseling();
                evaluateMedicationInUse();
                evaluateForAdditionalNeeds();
                evaluateRemarks();
            } catch (BaseAncHomeVisitAction.ValidationException e) {
                Timber.e(e);
            } catch (Exception e) {
                e.printStackTrace();
            }

            appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));

        };
        appExecutors.diskIO().execute(runnable);

    }

    private void evaluateEducationAndCounseling() throws Exception {

        HomeVisitActionHelper counselingHelper = new HomeVisitActionHelper() {

            private String adolescent_education_counseling;

            @Override
            public void onPayloadReceived(String jsonPayload) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonPayload);
                    adolescent_education_counseling = org.smartregister.chw.util.JsonFormUtils.getCheckBoxValue(jsonObject, "adolescent_counseling_given");
                } catch (JSONException e) {
                    Timber.e(e);
                }
            }

            @Override
            public String evaluateSubTitle() {
                return MessageFormat.format("{0}: {1}", context.getString(R.string.education_counseling_provided), adolescent_education_counseling);
            }

            @Override
            public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
                if (StringUtils.isNotBlank(adolescent_education_counseling)) {
                    return BaseAncHomeVisitAction.Status.COMPLETED;
                } else {
                    return BaseAncHomeVisitAction.Status.PENDING;
                }
            }
        };

        BaseAncHomeVisitAction education_counseling = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.adolescent_education_counseling))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Utils.getLocalForm("adolescent_hv_education_counseling", CoreConstants.JSON_FORM.locale, CoreConstants.JSON_FORM.assetManager))
                .withHelper(counselingHelper)
                .build();
        actionList.put(context.getString(R.string.adolescent_education_counseling), education_counseling);
    }

    private void evaluateMedicationInUse() throws Exception {

        BaseAncHomeVisitAction danger_signs = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_home_visit_medication_in_use))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Utils.getLocalForm("hv_medication_in_use", CoreConstants.JSON_FORM.locale, CoreConstants.JSON_FORM.assetManager))
                .withHelper(new MedicationInUseActionHelper())
                .build();
        actionList.put(context.getString(R.string.anc_home_visit_medication_in_use), danger_signs);
    }

    private void evaluateForAdditionalNeeds() throws Exception {

        HomeVisitActionHelper additionalNeedsHelper = new HomeVisitActionHelper() {

            private String adolescent_additional_needs;

            @Override
            public void onPayloadReceived(String jsonPayload) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonPayload);
                    adolescent_additional_needs = org.smartregister.chw.util.JsonFormUtils.getCheckBoxValue(jsonObject, "additional_counseling");
                } catch (JSONException e) {
                    Timber.e(e);
                }
            }

            @Override
            public String evaluateSubTitle() {
                return MessageFormat.format("{0}: {1}", context.getString(R.string.adolescent_additional_needs_requested), adolescent_additional_needs);
            }

            @Override
            public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
                if (StringUtils.isNotBlank(adolescent_additional_needs)) {
                    return BaseAncHomeVisitAction.Status.COMPLETED;
                } else {
                    return BaseAncHomeVisitAction.Status.PENDING;
                }
            }
        };

        BaseAncHomeVisitAction additional_needs = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.adolescent_additional_needs))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Utils.getLocalForm("adolescent_hv_additional_counseling", CoreConstants.JSON_FORM.locale, CoreConstants.JSON_FORM.assetManager))
                .withHelper(additionalNeedsHelper)
                .build();
        actionList.put(context.getString(R.string.adolescent_additional_needs), additional_needs);
    }

    private void evaluateRemarks() throws Exception {

        HomeVisitActionHelper remarksHelper = new HomeVisitActionHelper() {

            private String adolescent_reproductive_health_conditions;

            @Override
            public void onPayloadReceived(String jsonPayload) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonPayload);
                    adolescent_reproductive_health_conditions = org.smartregister.chw.util.JsonFormUtils.getValue(jsonObject, "chw_remarks_comments");
                } catch (JSONException e) {
                    Timber.e(e);
                }
            }

            @Override
            public String evaluateSubTitle() {
                return MessageFormat.format("{0}: {1}", context.getString(R.string.chw_remarks_or_comments_given), adolescent_reproductive_health_conditions);
            }

            @Override
            public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
                if (StringUtils.isNotBlank(adolescent_reproductive_health_conditions)) {
                    return BaseAncHomeVisitAction.Status.COMPLETED;
                } else {
                    return BaseAncHomeVisitAction.Status.PENDING;
                }
            }
        };

        BaseAncHomeVisitAction additional_needs = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.chw_remarks_or_comments))
                .withOptional(true)
                .withDetails(details)
                .withFormName(Utils.getLocalForm("adolescent_hv_remarks", CoreConstants.JSON_FORM.locale, CoreConstants.JSON_FORM.assetManager))
                .withHelper(remarksHelper)
                .build();
        actionList.put(context.getString(R.string.chw_remarks_or_comments), additional_needs);
    }

    protected VisitRepository getVisitRepository() {
        return AncLibrary.getInstance().visitRepository();
    }

    protected VisitDetailsRepository getVisitDetailsRepository() {
        return AncLibrary.getInstance().visitDetailsRepository();
    }

    @Override
    protected void prepareEvent(Event baseEvent) {
        if (baseEvent != null) {
            // add adolescent date obs and last
            List<Object> list = new ArrayList<>();
            list.add(new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date()));
            baseEvent.addObs(new Obs("concept", "text", "adolescent_visit_date", "",
                    list, new ArrayList<>(), null, "adolescent_visit_date"));
        }
    }

    @Override
    protected String getEncounterType() {
        return Constants.ADOLESCENT_HOME_VISIT_DONE;
    }

    @Override
    protected String getTableName() {
        return CoreConstants.TABLE_NAME.ADOLESCENT;
    }
}

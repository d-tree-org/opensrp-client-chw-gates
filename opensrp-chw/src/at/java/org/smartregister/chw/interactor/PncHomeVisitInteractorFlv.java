package org.smartregister.chw.interactor;

import android.content.Context;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.R;
import org.smartregister.chw.action_helper.MedicationInUseActionHelper;
import org.smartregister.chw.actionhelper.ImmunizationActionHelper;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.actionhelper.HomeVisitActionHelper;
import org.smartregister.chw.anc.contract.BaseAncHomeVisitContract;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.VaccineDisplay;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.fragment.BaseAncHomeVisitFragment;
import org.smartregister.chw.anc.fragment.BaseHomeVisitImmunizationFragment;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.chw.core.domain.Person;
import org.smartregister.chw.core.rule.PNCHealthFacilityVisitRule;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.chw.core.utils.VaccineScheduleUtil;
import org.smartregister.chw.dao.ChwPNCDao;
import org.smartregister.chw.dao.ChwPNCDaoFlv;
import org.smartregister.chw.dao.PersonDao;
import org.smartregister.chw.domain.PNCHealthFacilityVisitSummary;
import org.smartregister.chw.util.Constants;
import org.smartregister.chw.util.PNCVisitUtil;
import org.smartregister.chw.util.VisitLocationUtils;
import org.smartregister.immunization.domain.VaccineWrapper;
import org.smartregister.util.JsonFormUtils;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;


public class PncHomeVisitInteractorFlv extends DefaultPncHomeVisitInteractorFlv {
    protected List<Person> children;
    protected BaseAncHomeVisitContract.View view;

    @Override
    public LinkedHashMap<String, BaseAncHomeVisitAction> calculateActions(BaseAncHomeVisitContract.View view, MemberObject memberObject, BaseAncHomeVisitContract.InteractorCallBack callBack) throws BaseAncHomeVisitAction.ValidationException {
        actionList = new LinkedHashMap<>();
        context = view.getContext();
        this.memberObject = memberObject;
        editMode = view.getEditMode();
        this.view = view;

        // get the preloaded data
        if (view.getEditMode()) {
            Visit lastVisit = AncLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), Constants.EventType.PNC_HOME_VISIT);
            if (lastVisit != null) {
                details = VisitUtils.getVisitGroups(AncLibrary.getInstance().visitDetailsRepository().getVisits(lastVisit.getVisitId()));
            }
        }

        children = PersonDao.getMothersChildren(memberObject.getBaseEntityId());
        if (children == null) {
            children = new ArrayList<>();
        }

        try {
            evaluateDangerSignsMother();
            evaluateMedicationInUse();
            evaluatePNCHealthFacilityVisit();
            evaluateCounselling();
            evaluateMalariaPrevention();
            evaluateRemarks(actionList, details, context);
        } catch (BaseAncHomeVisitAction.ValidationException e) {
            throw (e);
        } catch (Exception e) {
            Timber.e(e);
        }
        return actionList;
    }

    private void evaluateDangerSignsMother() throws Exception {

        HomeVisitActionHelper pncDangerSignsMotherHelper = new HomeVisitActionHelper() {
            private String danger_signs_present_mama;

            @Override
            public void onPayloadReceived(String s) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    danger_signs_present_mama = org.smartregister.chw.util.JsonFormUtils.getCheckBoxValue(jsonObject, "danger_signs_present_mama");
                } catch (JSONException e) {
                    Timber.e(e);
                }
            }

            @Override
            public String evaluateSubTitle() {
                return MessageFormat.format("{0}: {1}", context.getString(R.string.pnc_danger_signs_mama), danger_signs_present_mama);
            }

            @Override
            public String postProcess(String jsonPayload) {
                return VisitLocationUtils.updateWithCurrentGpsLocation(jsonPayload);
            }

            @Override
            public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
                if (danger_signs_present_mama == null) {
                    return BaseAncHomeVisitAction.Status.PENDING;
                }

                if (StringUtils.isNotBlank(danger_signs_present_mama)) {
                    return BaseAncHomeVisitAction.Status.COMPLETED;
                } else {
                    return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
                }
            }
        };

        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.pnc_danger_signs_mother))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getDangerSignsMother())
                .withHelper(pncDangerSignsMotherHelper)
                .build();
        actionList.put(context.getString(R.string.pnc_danger_signs_mother), action);
    }

    private void evaluateMedicationInUse() throws Exception {

        BaseAncHomeVisitAction danger_signs = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_home_visit_medication_in_use))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Utils.getLocalForm("hv_medication_in_use", CoreConstants.JSON_FORM.locale, CoreConstants.JSON_FORM.assetManager))
                .withHelper(new MedicationInUseActionHelper(MedicationInUseActionHelper.ClientType.PNC))
                .build();
        actionList.put(context.getString(R.string.anc_home_visit_medication_in_use), danger_signs);

    }

    private void evaluateCounselling() throws Exception {
        HomeVisitActionHelper counsellingHelper = new HomeVisitActionHelper() {
            private String couselling_pnc;

            @Override
            public void onPayloadReceived(String jsonPayload) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonPayload);
                    couselling_pnc = org.smartregister.chw.util.JsonFormUtils.getCheckBoxValue(jsonObject, "couselling_pnc");
                } catch (JSONException e) {
                    Timber.e(e);
                }
            }

            @Override
            public String evaluateSubTitle() {
                String counsellingStatus = "None".equals(couselling_pnc) ? context.getString(R.string.subtask_not_done) : context.getString(R.string.subtask_done);
                return MessageFormat.format("{0}: {1}", context.getString(R.string.counselling), counsellingStatus);
            }

            @Override
            public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
                if (StringUtils.isNotBlank(couselling_pnc)) {
                    return BaseAncHomeVisitAction.Status.COMPLETED;
                } else {
                    return BaseAncHomeVisitAction.Status.PENDING;
                }
            }
        };

        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.pnc_counselling))
                .withOptional(true)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getCOUNSELLING())
                .withHelper(counsellingHelper)
                .build();
        actionList.put(context.getString(R.string.pnc_counselling), action);
    }

    private void evaluateMalariaPrevention() throws Exception {
        HomeVisitActionHelper malariaPreventionHelper = new HomeVisitActionHelper() {
            private String fam_llin;
            private String llin_2days;
            private String llin_condition;

            @Override
            public void onPayloadReceived(String jsonPayload) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonPayload);
                    fam_llin = org.smartregister.chw.util.JsonFormUtils.getValue(jsonObject, "fam_llin");
                    llin_2days = org.smartregister.chw.util.JsonFormUtils.getValue(jsonObject, "llin_2days");
                    llin_condition = org.smartregister.chw.util.JsonFormUtils.getValue(jsonObject, "llin_condition");
                } catch (JSONException e) {
                    Timber.e(e);
                }
            }

            @Override
            public String evaluateSubTitle() {
                StringBuilder stringBuilder = new StringBuilder();
                if (fam_llin.equalsIgnoreCase("No")) {
                    stringBuilder.append(MessageFormat.format("{0}: {1}\n", context.getString(R.string.uses_net), StringUtils.capitalize(fam_llin.trim().toLowerCase())));
                } else {
                    stringBuilder.append(MessageFormat.format("{0}: {1} · ", context.getString(R.string.uses_net), StringUtils.capitalize(fam_llin.trim().toLowerCase())));
                    stringBuilder.append(MessageFormat.format("{0}: {1} · ", context.getString(R.string.slept_under_net), StringUtils.capitalize(llin_2days.trim().toLowerCase())));
                    stringBuilder.append(MessageFormat.format("{0}: {1}", context.getString(R.string.net_condition), StringUtils.capitalize(llin_condition.trim().toLowerCase())));
                }
                return stringBuilder.toString();
            }

            @Override
            public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
                if (StringUtils.isBlank(fam_llin)) {
                    return BaseAncHomeVisitAction.Status.PENDING;
                }

                if (fam_llin.equalsIgnoreCase("Yes") && llin_2days.equalsIgnoreCase("Yes") && llin_condition.equalsIgnoreCase("Okay")) {
                    return BaseAncHomeVisitAction.Status.COMPLETED;
                } else {
                    return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
                }
            }
        };

        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.pnc_malaria_prevention))
                .withOptional(true)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getMalariaPrevention())
                .withHelper(malariaPreventionHelper)
                .build();
        actionList.put(context.getString(R.string.pnc_malaria_prevention), action);
    }

    @Override
    protected void evaluatePNCHealthFacilityVisit() throws Exception {

        PNCHealthFacilityVisitSummary summary = ChwPNCDao.getLastHealthFacilityVisitSummary(memberObject.getBaseEntityId());
        if (summary != null) {
            PNCHealthFacilityVisitRule visitRule = PNCVisitUtil.getNextPNCHealthFacilityVisit(summary.getDeliveryDate(), summary.getLastVisitDate());

            if (visitRule != null && visitRule.getVisitName() != null) {
                String title;
                int visit_num;
                switch (visitRule.getVisitName()) {
                    case "3":
                        title = context.getString(R.string.pnc_health_facility_visit_days_three_to_seven);
                        visit_num = 2;
                        break;
                    case "8":
                        title = context.getString(R.string.pnc_health_facility_visit_days_eight_to_twenty_eight);
                        visit_num = 3;
                        break;
                    case "29":
                        title = context.getString(R.string.pnc_health_facility_visit_days_twenty_nine_to_forty_two);
                        visit_num = 4;
                        break;
                    default:
                        title = context.getString(R.string.pnc_health_facility_visit_within_fourty_eight_hours);
                        visit_num = 1;
                        break;
                }

                BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, title)
                        .withOptional(true)
                        .withDetails(details)
                        .withFormName(visit_num == 1 ? Constants.JSON_FORM.PNC_HOME_VISIT.getHealthFacilityVisit() : Constants.JSON_FORM.PNC_HOME_VISIT.getHealthFacilityVisitTwo())
                        .withHelper(new PNCHealthFacilityVisitHelper(visitRule, visit_num))
                        .build();
                actionList.put(title, action);
            }
        }
    }

    @Override
    protected void evaluateImmunization(Person baby) throws Exception {
        if (getAgeInDays(baby.getDob()) <= 28) {
            List<VaccineWrapper> wrappers = VaccineScheduleUtil.getChildDueVaccines(baby.getBaseEntityID(), baby.getDob(), 0);
            if (wrappers.size() > 0) {
                List<VaccineDisplay> displays = new ArrayList<>();
                for (VaccineWrapper vaccineWrapper : wrappers) {
                    VaccineDisplay display = new VaccineDisplay();
                    display.setVaccineWrapper(vaccineWrapper);
                    display.setStartDate(baby.getDob());
                    display.setEndDate(new Date());
                    displays.add(display);
                }

                Map<String, List<VisitDetail>> details = null;
                if (editMode) {
                    Visit lastVisit = AncLibrary.getInstance().visitRepository().getLatestVisit(baby.getBaseEntityID(), Constants.EventType.IMMUNIZATION_VISIT);
                    if (lastVisit != null) {
                        details = VisitUtils.getVisitGroups(AncLibrary.getInstance().visitDetailsRepository().getVisits(lastVisit.getVisitId()));
                    }
                }

                BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, MessageFormat.format(context.getString(R.string.pnc_immunization_at_birth), baby.getFullName()))
                        .withOptional(true)
                        .withDetails(details)
                        .withBaseEntityID(baby.getBaseEntityID())
                        .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.SEPARATE)
                        .withDestinationFragment(BaseHomeVisitImmunizationFragment.getInstance(view, baby.getBaseEntityID(), details, displays))
                        .withHelper(new ImmunizationActionHelper(context, wrappers))
                        .build();
                actionList.put(MessageFormat.format(context.getString(R.string.pnc_immunization_at_birth), baby.getFullName()), action);

            }
        }
    }

    private void evaluateRemarks(LinkedHashMap<String, BaseAncHomeVisitAction> actionList,
                                 Map<String, List<VisitDetail>> details,
                                 final Context context) throws BaseAncHomeVisitAction.ValidationException {
        BaseAncHomeVisitAction remark_ba = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_home_visit_remarks_and_comments))
                .withOptional(true)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.ANC_HOME_VISIT.getRemarksAndComments())
                .withHelper(new PncHomeVisitInteractorFlv.RemarksAction())
                .build();
        actionList.put(context.getString(R.string.anc_home_visit_remarks_and_comments), remark_ba);
    }

    private class PNCHealthFacilityVisitHelper implements BaseAncHomeVisitAction.AncHomeVisitActionHelper {
        private Context context;
        private String jsonPayload;

        private PNCHealthFacilityVisitRule visitRule;
        private int visit_num;

        private String pnc_visit;
        private String pnc_hf_visit_date;
        private String vit_a_mother;
        private String ifa_mother;

        private BaseAncHomeVisitAction.ScheduleStatus scheduleStatus;
        private String subTitle;
        private Date date;

        public PNCHealthFacilityVisitHelper(PNCHealthFacilityVisitRule visitRule, int visit_num) {
            this.visitRule = visitRule;
            this.visit_num = visit_num;
        }

        @Override
        public void onJsonFormLoaded(String jsonPayload, Context context, Map<String, List<VisitDetail>> map) {
            this.context = context;
            this.jsonPayload = jsonPayload;
        }

        @Override
        public String getPreProcessed() {
            try {
                scheduleStatus = (visitRule.getOverDueDate().toLocalDate().isBefore(LocalDate.now())) ? BaseAncHomeVisitAction.ScheduleStatus.OVERDUE : BaseAncHomeVisitAction.ScheduleStatus.DUE;
                String due = (visitRule.getOverDueDate().toLocalDate().isBefore(LocalDate.now())) ? context.getString(R.string.overdue) : context.getString(R.string.due);

                subTitle = MessageFormat.format("{0} {1}", due, DateTimeFormat.forPattern("dd MMM yyyy").print(visitRule.getOverDueDate().toLocalDate()));
                JSONObject jsonObject = new JSONObject(jsonPayload);
                JSONArray fields = JsonFormUtils.fields(jsonObject);


                String title = jsonObject.getJSONObject(JsonFormConstants.STEP1).getString(JsonFormConstants.STEP_TITLE);
                jsonObject.getJSONObject(JsonFormConstants.STEP1).put("title", MessageFormat.format(title, visitRule.getVisitName()));

                JSONObject pnc_visit = JsonFormUtils.getFieldJSONObject(fields, "pnc_visit_{0}");
                pnc_visit.put(JsonFormConstants.KEY, MessageFormat.format("pnc_visit_{0}", visit_num));
                pnc_visit.put("hint",
                        MessageFormat.format(pnc_visit.getString(JsonFormConstants.HINT),
                                visit_num,
                                DateTimeFormat.forPattern("dd MMM yyyy").print(visitRule.getDueDate()
                                )
                        )
                );

                JSONObject pnc_visit_date = JsonFormUtils.getFieldJSONObject(fields, "pnc_hf_visit{0}_date");
                pnc_visit_date.put(JsonFormConstants.KEY, MessageFormat.format("pnc_hf_visit{0}_date", visit_num));
                pnc_visit_date.put("hint",
                        MessageFormat.format(pnc_visit_date.getString(JsonFormConstants.HINT), visitRule.getVisitName())
                );
                updateObjectRelevance(pnc_visit_date);

                if (visit_num == 1) {
                    updateObjectRelevance(JsonFormUtils.getFieldJSONObject(fields, "vit_a_mother"));
                    updateObjectRelevance(JsonFormUtils.getFieldJSONObject(fields, "ifa_mother"));
                }

                return jsonObject.toString();
            } catch (Exception e) {
                Timber.e(e);
            }
            return null;
        }

        private void updateObjectRelevance(JSONObject jsonObject) throws JSONException {
            JSONObject relevance = jsonObject.getJSONObject(JsonFormConstants.RELEVANCE);
            JSONObject step = relevance.getJSONObject("step1:pnc_visit_{0}");
            relevance.put(MessageFormat.format("step1:pnc_visit_{0}", visit_num), step);
            relevance.remove("step1:pnc_visit_{0}");
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                pnc_visit = org.smartregister.chw.util.JsonFormUtils.getValue(jsonObject, MessageFormat.format("pnc_visit_{0}", visit_num));
                pnc_hf_visit_date = org.smartregister.chw.util.JsonFormUtils.getValue(jsonObject, MessageFormat.format("pnc_hf_visit{0}_date", visit_num));

                if (visit_num == 1) {
                    vit_a_mother = org.smartregister.chw.util.JsonFormUtils.getValue(jsonObject, "vit_a_mother");
                    ifa_mother = org.smartregister.chw.util.JsonFormUtils.getValue(jsonObject, "ifa_mother");
                }

                if (StringUtils.isNotBlank(pnc_hf_visit_date)) {
                    date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(pnc_hf_visit_date);
                }
            } catch (JSONException e) {
                Timber.e(e);
            } catch (ParseException e) {
                Timber.e(e);
            }
        }

        @Override
        public BaseAncHomeVisitAction.ScheduleStatus getPreProcessedStatus() {
            return scheduleStatus;
        }

        @Override
        public String getPreProcessedSubTitle() {
            return subTitle;
        }

        @Override
        public String postProcess(String s) {
            try {
                JSONObject jsonObject = new JSONObject(s);

                JSONArray field = JsonFormUtils.fields(jsonObject);
                JSONObject confirmed_visits = JsonFormUtils.getFieldJSONObject(field, "confirmed_health_facility_visits");
                JSONObject facility_visit_date = JsonFormUtils.getFieldJSONObject(field, "last_health_facility_visit_date");
                pnc_hf_visit_date = org.smartregister.chw.util.JsonFormUtils.getValue(jsonObject, MessageFormat.format("pnc_hf_visit{0}_date", visit_num));

                String count = String.valueOf(visit_num);
                String value = org.smartregister.chw.util.JsonFormUtils.getValue(jsonObject, MessageFormat.format("pnc_visit_{0}", visit_num));
                if (value.equalsIgnoreCase("Yes")) {
                    count = String.valueOf(visit_num + 1);
                    facility_visit_date.put(JsonFormConstants.VALUE, pnc_hf_visit_date);
                } else {
                    facility_visit_date.remove(JsonFormConstants.VALUE);
                }

                if (!confirmed_visits.getString(JsonFormConstants.VALUE).equals(count)) {
                    confirmed_visits.put(JsonFormConstants.VALUE, count);
                    return jsonObject.toString();
                }

            } catch (JSONException e) {
                Timber.e(e);
            }
            return null;
        }

        @Override
        public String evaluateSubTitle() {
            if ("No".equals(pnc_visit)) {
                return context.getString(R.string.visit_not_done).replace("\n", "");
            } else {
                if (date == null) {
                    return null;
                }

                if (visit_num == 1) {
                    return MessageFormat.format(" {0}: {1} \n {2}: {3} \n {4}: {5}",
                            context.getString(R.string.date), new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date), "Vitamin A received", vit_a_mother, "IFA tablets received", ifa_mother);
                } else {
                    return MessageFormat.format("{0}: {1}", context.getString(R.string.date), new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date));
                }
            }
        }


        @Override
        public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isBlank(pnc_visit)) {
                return BaseAncHomeVisitAction.Status.PENDING;
            }

            if (pnc_visit.equalsIgnoreCase("Yes")) {
                return BaseAncHomeVisitAction.Status.COMPLETED;
            } else if (pnc_visit.equalsIgnoreCase("No")) {
                return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
            } else {
                return BaseAncHomeVisitAction.Status.PENDING;
            }
        }

        @Override
        public void onPayloadReceived(BaseAncHomeVisitAction baseAncHomeVisitAction) {
            Timber.d("onPayloadReceived");
        }
    }


    private class RemarksAction implements BaseAncHomeVisitAction.AncHomeVisitActionHelper {
        private String chw_comment_anc;
        private Context context;

        @Override
        public void onJsonFormLoaded(String s, Context context, Map<String, List<VisitDetail>> map) {
            this.context = context;
        }

        @Override
        public String getPreProcessed() {
            return null;
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                chw_comment_anc = org.smartregister.chw.util.JsonFormUtils.getValue(jsonObject, "chw_comment_anc");
            } catch (JSONException e) {
                Timber.e(e);
            }
        }

        @Override
        public BaseAncHomeVisitAction.ScheduleStatus getPreProcessedStatus() {
            return null;
        }

        @Override
        public String getPreProcessedSubTitle() {
            return null;
        }

        @Override
        public String postProcess(String s) {
            return null;
        }

        @Override
        public String evaluateSubTitle() {
            return MessageFormat.format("{0}: {1}",
                    context.getString(R.string.remarks_and__comments), StringUtils.capitalize(chw_comment_anc));
        }

        @Override
        public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isBlank(chw_comment_anc)) {
                return BaseAncHomeVisitAction.Status.PENDING;
            }

            return BaseAncHomeVisitAction.Status.COMPLETED;
        }

        @Override
        public void onPayloadReceived(BaseAncHomeVisitAction baseAncHomeVisitAction) {
            Timber.v("onPayloadReceived");
        }
    }

}
package org.smartregister.chw.interactor;

import android.text.TextUtils;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.R;
import org.smartregister.chw.anc.actionhelper.HomeVisitActionHelper;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.chw.util.Constants;
import org.smartregister.chw.util.JsonFormUtils;
import org.smartregister.chw.util.VisitLocationUtils;
import org.smartregister.immunization.domain.ServiceWrapper;
import org.smartregister.util.LangUtils;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

public class ChildHomeVisitInteractorFlv extends DefaultChildHomeVisitInteractorFlv {

    @Override
    protected void bindEvents(Map<String, ServiceWrapper> serviceWrapperMap) throws BaseAncHomeVisitAction.ValidationException {
        try {

            String formName = "child_danger_signs";
            int daysInteger = 0;

            String ageString = Utils.getDuration(memberObject.getDob());
            if (ageString.contains("w") && !ageString.contains("m") && !ageString.contains("y")) {
                //Has weeks
                String weeks = ageString.contains("w") ? ageString.substring(0, ageString.indexOf("w")) : "";
                int weeksInteger = Integer.parseInt(weeks);

                if (ageString.contains("d")) {
                    String days = ageString.contains("d") ? ageString.substring(ageString.indexOf("w") + 2, ageString.indexOf("d")) : "";
                    daysInteger = Integer.parseInt(days);
                }

                int totalDays = (weeksInteger * 7) + daysInteger;

                if (totalDays <= 30) {
                    formName = "new_born_danger_signs";
                }
            } else if (ageString.contains("d")) {
                String days = ageString.contains("d") ? ageString.substring(0, ageString.indexOf("d")) : "";
                daysInteger = Integer.parseInt(days);

                if (daysInteger <= 30) {
                    formName = "new_born_danger_signs";
                }
            }

            evaluateDangerSigns(formName);
            //evaluateExclusiveBreastFeeding(serviceWrapperMap);
            evaluateMalariaPrevention();
            evaluateCounselling();
            evaluateRemarks();
        } catch (BaseAncHomeVisitAction.ValidationException e) {
            throw (e);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private void evaluateDangerSigns(String formName) throws Exception {

        HomeVisitActionHelper dangerSignHelper = new HomeVisitActionHelper() {

            private String child_danger_signs;

            @Override
            public void onPayloadReceived(String jsonPayload) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonPayload);
                    child_danger_signs = org.smartregister.chw.util.JsonFormUtils.getCheckBoxValue(jsonObject, "danger_signs_present_child");
                } catch (JSONException e) {
                    Timber.e(e);
                }
            }

            @Override
            public String evaluateSubTitle() {
                return MessageFormat.format("{0}: {1}", "Danger Signs", child_danger_signs);
            }

            @Override
            public String postProcess(String jsonPayload) {
                return VisitLocationUtils.updateWithCurrentGpsLocation(jsonPayload);
            }

            @Override
            public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
                if (StringUtils.isNotBlank(child_danger_signs)) {
                    return BaseAncHomeVisitAction.Status.COMPLETED;
                } else {
                    return BaseAncHomeVisitAction.Status.PENDING;
                }
            }
        };

        BaseAncHomeVisitAction danger_signs = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_home_visit_danger_signs))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Utils.getLocalForm(formName, getLocale(), CoreConstants.JSON_FORM.assetManager))
                .withHelper(dangerSignHelper)
                .build();
        actionList.put(context.getString(R.string.anc_home_visit_danger_signs), danger_signs);
    }

    private void evaluateCounselling() throws Exception {
        HomeVisitActionHelper counsellingHelper = new HomeVisitActionHelper() {
            private String couselling_child;

            @Override
            public void onPayloadReceived(String jsonPayload) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonPayload);
                    couselling_child = org.smartregister.chw.util.JsonFormUtils.getCheckBoxValue(jsonObject, "pnc_counselling");
                } catch (JSONException e) {
                    Timber.e(e);
                }
            }

            @Override
            public String evaluateSubTitle() {
                return MessageFormat.format("{0}: {1}", "Counselling", couselling_child);
            }

            @Override
            public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
                if (StringUtils.isNotBlank(couselling_child)) {
                    return BaseAncHomeVisitAction.Status.COMPLETED;
                } else {
                    return BaseAncHomeVisitAction.Status.PENDING;
                }
            }
        };

        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.pnc_counselling))
                .withOptional(true)
                .withDetails(details)
                .withFormName(Utils.getLocalForm("child_hv_counselling", CoreConstants.JSON_FORM.locale, CoreConstants.JSON_FORM.assetManager))
                .withHelper(counsellingHelper)
                .build();
        actionList.put(context.getString(R.string.pnc_counselling), action);
    }

    @Override
    protected int immunizationCeiling() {
        return 60;
    }

    private void evaluateNutritionStatus() throws BaseAncHomeVisitAction.ValidationException {
        HomeVisitActionHelper nutritionStatusHelper = new HomeVisitActionHelper() {
            private String nutritionStatus;

            @Override
            public void onPayloadReceived(String jsonPayload) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonPayload);
                    nutritionStatus = JsonFormUtils.getValue(jsonObject, "nutrition_status_1m5yr").toLowerCase();
                } catch (JSONException e) {
                    Timber.e(e);
                }
            }

            @Override
            public String evaluateSubTitle() {
                return MessageFormat.format("{0}: {1}", context.getString(R.string.nutrition_status), nutritionStatus);
            }

            @Override
            public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
                if (StringUtils.isBlank(nutritionStatus))
                    return BaseAncHomeVisitAction.Status.PENDING;

                return BaseAncHomeVisitAction.Status.COMPLETED;
            }
        };

        BaseAncHomeVisitAction observation = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_home_visit_nutrition_status))
                .withOptional(true)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.CHILD_HOME_VISIT.getNutritionStatus())
                .withHelper(nutritionStatusHelper)
                .build();
        actionList.put(context.getString(R.string.anc_home_visit_nutrition_status), observation);
    }

    @Override
    protected void evaluateObsAndIllness() throws BaseAncHomeVisitAction.ValidationException {
        class ObsIllnessBabyHelper extends HomeVisitActionHelper {
            private String date_of_illness;
            private String illness_description;
            private String action_taken;
            private LocalDate illnessDate;

            @Override
            public void onPayloadReceived(String jsonPayload) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonPayload);
                    date_of_illness = JsonFormUtils.getValue(jsonObject, "date_of_illness");
                    illness_description = JsonFormUtils.getValue(jsonObject, "illness_description");
                    action_taken = JsonFormUtils.getValue(jsonObject, "action_taken_1m5yr");
                    illnessDate = DateTimeFormat.forPattern("dd-MM-yyyy").parseLocalDate(date_of_illness);
                } catch (Exception e) {
                    Timber.e(e);
                }
            }

            @Override
            public String evaluateSubTitle() {
                if (illnessDate == null)
                    return "";

                return MessageFormat.format("{0}: {1}\n {2}: {3}",
                        DateTimeFormat.forPattern("dd MMM yyyy").print(illnessDate),
                        illness_description, context.getString(R.string.action_taken), action_taken
                );
            }

            @Override
            public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
                if (StringUtils.isBlank(date_of_illness))
                    return BaseAncHomeVisitAction.Status.PENDING;

                return BaseAncHomeVisitAction.Status.COMPLETED;
            }
        }

        BaseAncHomeVisitAction observation = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.anc_home_visit_observations_n_illnes))
                .withOptional(true)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.getObsIllness())
                .withHelper(new ObsIllnessBabyHelper())
                .build();
        actionList.put(context.getString(R.string.anc_home_visit_observations_n_illnes), observation);
    }

    private void evaluateMalariaPrevention() throws Exception {
        HomeVisitActionHelper malariaPreventionHelper = new HomeVisitActionHelper() {
            private String famllin1m5yr;
            private String llin2days1m5yr;
            private String llinCondition1m5yr;

            @Override
            public void onPayloadReceived(String jsonPayload) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonPayload);
                    famllin1m5yr = JsonFormUtils.getValue(jsonObject, "fam_llin_1m5yr");
                    llin2days1m5yr = JsonFormUtils.getValue(jsonObject, "llin_2days_1m5yr");
                    llinCondition1m5yr = JsonFormUtils.getValue(jsonObject, "llin_condition_1m5yr");
                } catch (JSONException e) {
                    Timber.e(e);
                }
            }

            @Override
            public String evaluateSubTitle() {

                // Handle translation of drop down values
                if (!TextUtils.isEmpty(famllin1m5yr) && !TextUtils.isEmpty(llin2days1m5yr)) {
                    famllin1m5yr = getYesNoTranslation(famllin1m5yr);
                    llin2days1m5yr = getYesNoTranslation(llin2days1m5yr);
                }

                if (!TextUtils.isEmpty(llinCondition1m5yr)) {
                    if ("Okay".equals(llinCondition1m5yr)) {
                        llinCondition1m5yr = context.getString(R.string.okay);
                    } else if ("Bad".equals(llinCondition1m5yr)) {
                        llinCondition1m5yr = context.getString(R.string.bad);
                    }
                }

                StringBuilder stringBuilder = new StringBuilder();
                if (famllin1m5yr.equalsIgnoreCase(context.getString(R.string.no))) {
                    stringBuilder.append(MessageFormat.format("{0}: {1}\n", context.getString(R.string.uses_net), StringUtils.capitalize(famllin1m5yr.trim().toLowerCase())));
                } else {
                    stringBuilder.append(MessageFormat.format("{0}: {1} · ", context.getString(R.string.uses_net), StringUtils.capitalize(famllin1m5yr.trim().toLowerCase())));
                    stringBuilder.append(MessageFormat.format("{0}: {1} · ", context.getString(R.string.slept_under_net), StringUtils.capitalize(llin2days1m5yr.trim().toLowerCase())));
                    stringBuilder.append(MessageFormat.format("{0}: {1}", context.getString(R.string.net_condition), StringUtils.capitalize(llinCondition1m5yr.trim().toLowerCase())));
                }
                return stringBuilder.toString();
            }

            public String getYesNoTranslation(String subtitleText) {
                if ("Yes".equals(subtitleText)) {
                    return context.getString(R.string.yes);
                } else if ("No".equals(subtitleText)) {
                    return context.getString(R.string.no);
                } else {
                    return subtitleText;
                }
            }

            @Override
            public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
                if (StringUtils.isBlank(famllin1m5yr)) {
                    return BaseAncHomeVisitAction.Status.PENDING;
                }

                if (famllin1m5yr.equalsIgnoreCase("Yes") && llin2days1m5yr.equalsIgnoreCase("Yes") && llinCondition1m5yr.equalsIgnoreCase("Okay")) {
                    return BaseAncHomeVisitAction.Status.COMPLETED;
                } else {
                    return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
                }
            }
        };

        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.pnc_malaria_prevention))
                .withOptional(true)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.CHILD_HOME_VISIT.getMalariaPrevention())
                .withHelper(malariaPreventionHelper)
                .build();
        actionList.put(context.getString(R.string.pnc_malaria_prevention), action);
    }

    private void evaluateRemarks() throws Exception {
        HomeVisitActionHelper remarksHelper = new HomeVisitActionHelper() {
            private String chw_comment_child;

            @Override
            public void onPayloadReceived(String jsonPayload) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonPayload);
                    chw_comment_child = org.smartregister.chw.util.JsonFormUtils.getValue(jsonObject, "chw_comment_child");
                } catch (JSONException e) {
                    Timber.e(e);
                }
            }

            @Override
            public String evaluateSubTitle() {
                return MessageFormat.format("{0}: {1}", context.getString(R.string.remarks_and__comments), chw_comment_child);
            }

            @Override
            public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
                if (StringUtils.isNotBlank(chw_comment_child)) {
                    return BaseAncHomeVisitAction.Status.COMPLETED;
                } else {
                    return BaseAncHomeVisitAction.Status.PENDING;
                }
            }
        };

        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.remarks_and__comments))
                .withOptional(true)
                .withDetails(details)
                .withFormName(Utils.getLocalForm("child_hv_remarks", CoreConstants.JSON_FORM.locale, CoreConstants.JSON_FORM.assetManager))
                .withHelper(remarksHelper)
                .build();
        actionList.put(context.getString(R.string.remarks_and__comments), action);
    }

    private Locale getLocale() {
        String language = LangUtils.getLanguage(CoreChwApplication.getInstance().getApplicationContext());
        return new Locale(language);
    }
}

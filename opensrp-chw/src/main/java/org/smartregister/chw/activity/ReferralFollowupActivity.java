package org.smartregister.chw.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreReferralUtils;
import org.smartregister.chw.referral.util.Constants;
import org.smartregister.chw.schedulers.ChwScheduleTaskExecutor;
import org.smartregister.domain.Task;
import org.smartregister.repository.TaskRepository;
import org.smartregister.util.LangUtils;

import timber.log.Timber;

import static org.smartregister.chw.core.utils.CoreConstants.JSON_FORM.getReferralFollowupForm;
import static org.smartregister.chw.malaria.util.JsonFormUtils.validateParameters;
import static org.smartregister.chw.util.JsonFormUtils.ENCOUNTER_TYPE;
import static org.smartregister.util.JsonFormUtils.VALUE;
import static org.smartregister.util.JsonFormUtils.getFieldJSONObject;


public class ReferralFollowupActivity extends BaseReferralFollowupActivity {
    private static final String CLIENT = "client";
    private boolean isComingFromReferralDetails = false;

    private String taskId = "";
    private static final String TASK_IDENTIFIER = "taskIdentifier";

    public static void startReferralFollowupActivity(Activity activity, String taskIdentifier, String baseEntityId) {
        Intent intent = new Intent(activity, ReferralFollowupActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityId);

        int referralType = getReferralType(taskIdentifier);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.REFERRAL_FOLLOWUP_FORM_NAME, getReferralFollowupForm(referralType));
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.JSON_FORM, getReferralFollowupForm(referralType));
        intent.putExtra(TASK_IDENTIFIER, taskIdentifier);
        if (activity.getClass() == AtReferralDetailsViewActivity.class)
            intent.putExtra("IS_COMING_FROM_REFERRAL_DETAILS", true);

        intent.putExtra(Constants.ACTIVITY_PAYLOAD.ACTION, Constants.ACTIVITY_PAYLOAD_TYPE.FOLLOW_UP_VISIT);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getExtras() != null) {
            taskId = getIntent().getExtras().getString(TASK_IDENTIFIER);
            isComingFromReferralDetails = getIntent().getBooleanExtra("IS_COMING_FROM_REFERRAL_DETAILS", false);
        }
    }


    public String getTaskIdentifier() {
        return taskId;
    }

    public static int getReferralType(String identifier) {
        TaskRepository taskRepository = ChwApplication.getInstance().getTaskRepository();
        Task task = taskRepository.getTaskByIdentifier(identifier);
        return task.getPriority();
    }

    @Override
    protected void attachBaseContext(Context base) {
        // get language from prefs
        String lang = LangUtils.getLanguage(base.getApplicationContext());
        super.attachBaseContext(LangUtils.setAppLocale(base, lang));
    }

    @Override
    protected void completeReferralTask(String jsonString) {
        try {
            JSONObject form = new JSONObject(jsonString);
            Triple<Boolean, JSONObject, JSONArray> registrationFormParams = validateParameters(form.toString());
            JSONObject jsonForm = registrationFormParams.getMiddle();
            String encounter_type = jsonForm.optString(ENCOUNTER_TYPE);

            if (org.smartregister.chw.util.Constants.EncounterType.REFERRAL_FOLLOW_UP_VISIT.equals(encounter_type) || org.smartregister.chw.util.Constants.EncounterType.LINKAGE_FOLLOW_UP_VISIT.equals(encounter_type)) {
                JSONArray fields = registrationFormParams.getRight();

                JSONObject wantToComplete = getFieldJSONObject(fields, "complete_referral");
                JSONObject visit_hf_object = getFieldJSONObject(fields, "visit_hf");
                if (visit_hf_object != null && "Yes".equalsIgnoreCase(visit_hf_object.optString(VALUE)) ||
                        wantToComplete != null && "No".equalsIgnoreCase(wantToComplete.optString(VALUE))) {
                    // update task
                    TaskRepository taskRepository = ChwApplication.getInstance().getTaskRepository();
                    Task task = taskRepository.getTaskByIdentifier(getTaskIdentifier());
                    task.setStatus(Task.TaskStatus.COMPLETED);
                    taskRepository.addOrUpdate(task);
                }
            }

            // update schedule
            String baseEntityId = jsonForm.optString(CoreConstants.ENTITY_ID);
            updateReferralFollowUpVisitSchedule(baseEntityId);
            if (isComingFromReferralDetails) {
                Intent intent = new Intent(this, ReferralRegisterActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }

            finish();

        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    private void updateReferralFollowUpVisitSchedule(String baseEntityId) {
        ChwApplication.getInstance().getScheduleRepository().deleteScheduleByName(CoreConstants.SCHEDULE_TYPES.REFERRAL_FOLLOWUP_VISIT, baseEntityId);

        // check if there is any task ready/pending
        Task oldestTask = CoreReferralUtils.getTaskForEntity(baseEntityId, false);
        if (oldestTask != null) {
            ChwScheduleTaskExecutor.getInstance().execute(baseEntityId, org.smartregister.chw.util.Constants.EncounterType.REFERRAL_FOLLOW_UP_VISIT, oldestTask.getAuthoredOn().toDate());
        }
    }
}

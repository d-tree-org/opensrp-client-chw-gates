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
import org.smartregister.chw.referral.util.Constants;
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

    private String taskId = "";
    private static final String TASK_IDENTIFIER = "taskIdentifier";

    public static void startReferralFollowupActivity(Activity activity, String taskIdentifier, String baseEntityId) {
        Intent intent = new Intent(activity, ReferralFollowupActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityId);

        int referralType = getReferralType(taskIdentifier);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.REFERRAL_FOLLOWUP_FORM_NAME, getReferralFollowupForm(referralType));
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.JSON_FORM, getReferralFollowupForm(referralType));
        intent.putExtra(TASK_IDENTIFIER, taskIdentifier);

        intent.putExtra(Constants.ACTIVITY_PAYLOAD.ACTION, Constants.ACTIVITY_PAYLOAD_TYPE.FOLLOW_UP_VISIT);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getExtras() != null)
            taskId = getIntent().getExtras().getString(TASK_IDENTIFIER);
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
                JSONObject visit_hf_object = getFieldJSONObject(fields, "visit_hf");
                JSONObject services_hf_object = getFieldJSONObject(fields, "services_hf");
                if (visit_hf_object != null && "Yes".equalsIgnoreCase(visit_hf_object.optString(VALUE)) &&
                        services_hf_object != null && "Yes".equalsIgnoreCase(services_hf_object.optString(VALUE)) ) {
                    // update task
                    TaskRepository taskRepository = ChwApplication.getInstance().getTaskRepository();
                    Task task = taskRepository.getTaskByIdentifier(getTaskIdentifier());
                    task.setStatus(Task.TaskStatus.COMPLETED);
                    taskRepository.addOrUpdate(task);
                }
            }

            finish();

        } catch (JSONException e) {
            Timber.e(e);
        }
    }
}

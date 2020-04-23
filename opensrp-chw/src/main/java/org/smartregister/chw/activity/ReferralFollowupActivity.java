package org.smartregister.chw.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.referral.util.Constants;
import org.smartregister.domain.Task;
import org.smartregister.repository.TaskRepository;
import org.smartregister.util.LangUtils;

import static org.smartregister.chw.core.utils.CoreConstants.JSON_FORM.getReferralFollowupForm;


public class ReferralFollowupActivity extends BaseReferralFollowupActivity {
    private static final String CLIENT = "client";

    public static void startReferralFollowupActivity(Activity activity, String taskIdentifier) {
        Intent intent = new Intent(activity, ReferralFollowupActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, taskIdentifier);

        int referralType = getReferralType(taskIdentifier);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.REFERRAL_FOLLOWUP_FORM_NAME, getReferralFollowupForm(referralType));
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.JSON_FORM, getReferralFollowupForm(referralType));

        intent.putExtra(Constants.ACTIVITY_PAYLOAD.ACTION, Constants.ACTIVITY_PAYLOAD_TYPE.FOLLOW_UP_VISIT);
        activity.startActivity(intent);
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
}

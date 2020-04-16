package org.smartregister.chw.activity;

import android.app.Activity;
import android.content.Intent;

import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.referral.domain.MemberObject;
import org.smartregister.chw.referral.util.Constants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Task;
import org.smartregister.repository.TaskRepository;

import static org.smartregister.chw.core.utils.CoreConstants.JSON_FORM.getReferralFollowupForm;


public class ReferralFollowupActivity extends BaseReferralFollowupActivity {
    private static final String CLIENT = "client";
    private static Activity referralActivity;

    public static void startReferralFollowupActivity(Activity activity, MemberObject memberObject, CommonPersonObjectClient client) {
        Intent intent = new Intent(activity, ReferralFollowupActivity.class);
        intent.putExtra(org.smartregister.chw.malaria.util.Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, client.getCaseId());
        intent.putExtra(Constants.REFERRAL_MEMBER_OBJECT.MEMBER_OBJECT, memberObject);

        int referralType = getReferralType(client.getCaseId());
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.REFERRAL_FOLLOWUP_FORM_NAME, getReferralFollowupForm(referralType));
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.JSON_FORM, getReferralFollowupForm(referralType));

        intent.putExtra(org.smartregister.chw.malaria.util.Constants.ACTIVITY_PAYLOAD.ACTION, org.smartregister.chw.malaria.util.Constants.ACTIVITY_PAYLOAD_TYPE.FOLLOW_UP_VISIT);
        intent.putExtra(CLIENT, client);
        referralActivity = activity;
        activity.startActivity(intent);
    }

    @Override
    protected Activity getMalariaRegisterActivity() {
        return referralActivity;
    }

    public static int getReferralType(String identifier) {
        TaskRepository taskRepository = ChwApplication.getInstance().getTaskRepository();
        Task task = taskRepository.getTaskByIdentifier(identifier);
        return task.getPriority();
    }
}

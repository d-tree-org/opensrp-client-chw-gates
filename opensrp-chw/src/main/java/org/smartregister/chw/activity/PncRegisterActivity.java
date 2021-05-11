package org.smartregister.chw.activity;


import android.app.Activity;
import android.content.Intent;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.chw.anc.util.JsonFormUtils;
import org.smartregister.chw.core.activity.CoreFamilyRegisterActivity;
import org.smartregister.chw.core.activity.CorePncRegisterActivity;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.fragment.PncRegisterFragment;
import org.smartregister.chw.job.BasePncCloseJob;
import org.smartregister.job.SyncServiceJob;
import org.smartregister.view.fragment.BaseRegisterFragment;

import timber.log.Timber;

public class PncRegisterActivity extends CorePncRegisterActivity {

    public static void startPncRegistrationActivity(Activity activity, String memberBaseEntityID, String phoneNumber, String formName,
                                                    String uniqueId, String familyBaseID, String family_name) {
        Intent intent = new Intent(activity, PncRegisterActivity.class);
        intent.putExtra(org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, memberBaseEntityID);
        phone_number = phoneNumber;
        familyBaseEntityId = familyBaseID;
        form_name = formName;
        familyName = family_name;
        unique_id = uniqueId;
        intent.putExtra(org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD.ACTION, org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD_TYPE.REGISTRATION);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.TABLE_NAME, getFormTable());
        activity.startActivity(intent);
    }

    @Override
    public void onRegistrationSaved(String encounterType, boolean isEdit, boolean hasChildren) {
        // do nothing
    }

    @Override
    protected void registerBottomNavigation() {
        super.registerBottomNavigation();
        FamilyRegisterActivity.registerBottomNavigation(bottomNavigationHelper, bottomNavigationView, this);
    }

    @Override
    protected Class<? extends CoreFamilyRegisterActivity> getFamilyRegisterActivity() {
        return FamilyRegisterActivity.class;
    }

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new PncRegisterFragment();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResultExtended(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            try {
                JSONObject form = new JSONObject(data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON));
                String encounter_type = form.optString(Constants.JSON_FORM_EXTRA.ENCOUNTER_TYPE);

                if (CoreConstants.EventType.PREGNANCY_OUTCOME.equals(encounter_type)) {
                    JSONArray fields = org.smartregister.util.JsonFormUtils.fields(form);
                    JSONObject deliveryDateJson = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, DBConstants.KEY.DELIVERY_DATE);
                    if (deliveryDateJson != null) {
                        String deliveryDateString = deliveryDateJson.getString(org.smartregister.util.JsonFormUtils.VALUE);
                        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd-MM-yyyy");
                        if (StringUtils.isNotBlank(deliveryDateString)) {
                            int pncDays = Days.daysBetween(new DateTime(formatter.parseDateTime(deliveryDateString)), new DateTime()).getDays();
                            if (pncDays > 43) {
                                BasePncCloseJob.scheduleJobImmediately(BasePncCloseJob.TAG);
                            }
                        }
                    }
                    String pregnancyOutcome = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, org.smartregister.chw.util.Constants.pregnancyOutcome).optString(JsonFormUtils.VALUE);
                    if (EnumUtils.isValidEnum(org.smartregister.chw.util.Constants.FamilyRegisterOptionsUtil.class, pregnancyOutcome)) {
                        startRegisterActivity(FamilyRegisterActivity.class);
                        this.finish();
                        return;
                    }
                }
                SyncServiceJob.scheduleJobImmediately(SyncServiceJob.TAG);

            } catch (Exception e) {
                Timber.e(e);
            }
        }

        this.finish();
    }
}

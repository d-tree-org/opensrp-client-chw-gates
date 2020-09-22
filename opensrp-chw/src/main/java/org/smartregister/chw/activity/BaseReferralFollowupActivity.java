package org.smartregister.chw.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.CoreReferralFollowupActivity;
import org.smartregister.chw.R;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.util.Constants;
import org.smartregister.domain.Task;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.repository.TaskRepository;

import java.util.Collections;
import java.util.List;

import timber.log.Timber;

import static com.vijay.jsonwizard.constants.JsonFormConstants.JSON_FORM_KEY.JSON;
import static org.smartregister.chw.core.utils.CoreConstants.ENTITY_ID;
import static org.smartregister.chw.core.utils.CoreConstants.JSON_FORM.isMultiPartForm;
import static org.smartregister.chw.malaria.util.JsonFormUtils.validateParameters;
import static org.smartregister.chw.util.JsonFormUtils.ENCOUNTER_TYPE;
import static org.smartregister.chw.util.JsonFormUtils.REQUEST_CODE_GET_JSON;
import static org.smartregister.util.JsonFormUtils.VALUE;
import static org.smartregister.util.JsonFormUtils.getFieldJSONObject;

public abstract class BaseReferralFollowupActivity extends CoreReferralFollowupActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavigationMenu.getInstance(this, null, null);
    }

    @Override
    public void startFormActivity(JSONObject jsonForm) {

        Intent intent = new Intent(this, Utils.metadata().familyMemberFormActivity);
        intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

        Form form = new Form();
        form.setActionBarBackground(org.smartregister.chw.core.R.color.family_actionbar);
        form.setWizard(false);
        form.setHomeAsUpIndicator(org.smartregister.chw.core.R.mipmap.ic_cross_white);
        form.setSaveLabel(getString(org.smartregister.chw.core.R.string.submit));

        String title = getContext().getString(R.string.referral_followup);
        if(jsonForm.optString("encounter_type", "").equalsIgnoreCase("Linkage Follow-up Visit")){
            title = getContext().getString(R.string.linkage_followup);
        }

        if (isMultiPartForm(jsonForm)) {
            form.setWizard(true);
            form.setNavigationBackground(org.smartregister.chw.core.R.color.family_navigation);
            form.setName(title);
            form.setNextLabel(this.getResources().getString(org.smartregister.chw.core.R.string.next));
            form.setPreviousLabel(this.getResources().getString(org.smartregister.chw.core.R.string.back));
        }
        intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
        startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);
    }

    @Override
    public List<String> getViewIdentifiers() {
        return Collections.singletonList(CoreConstants.CONFIGURATION.MALARIA_REGISTER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_GET_JSON) {
            String jsonString = data.getStringExtra(JSON);
            try {
                JSONObject form = new JSONObject(jsonString);
                Triple<Boolean, JSONObject, JSONArray> registrationFormParams = validateParameters(form.toString());
                JSONObject jsonForm = registrationFormParams.getMiddle();
                String encounter_type = jsonForm.optString(ENCOUNTER_TYPE);

                if (Constants.EncounterType.REFERRAL_FOLLOW_UP_VISIT.equals(encounter_type) || Constants.EncounterType.LINKAGE_FOLLOW_UP_VISIT.equals(encounter_type)) {
                    JSONArray fields = registrationFormParams.getRight();
                    JSONObject visit_hf_object = getFieldJSONObject(fields, "visit_hf");
                    JSONObject wantToComplete = getFieldJSONObject(fields, "complete_referral");
                    if (visit_hf_object != null && "Yes".equalsIgnoreCase(visit_hf_object.optString(VALUE)) ||
                            wantToComplete != null && "No".equalsIgnoreCase(wantToComplete.optString(VALUE)) ) {
                        // update task
                        TaskRepository taskRepository = ChwApplication.getInstance().getTaskRepository();
                        Task task = taskRepository.getTaskByIdentifier(jsonForm.optString(ENTITY_ID));
                        task.setStatus(Task.TaskStatus.COMPLETED);
                        taskRepository.addOrUpdate(task);
                    }
                }

                finish();

            } catch (JSONException e) {
                Timber.e(e);
            }

        } else {
            finish();
        }

    }

    @Override
    protected void onResumption() {
        super.onResumption();
        NavigationMenu menu = NavigationMenu.getInstance(this, null, null);
        if (menu != null) {
            menu.getNavigationAdapter().setSelectedView(CoreConstants.DrawerMenu.REFERRALS);
        }
    }
}

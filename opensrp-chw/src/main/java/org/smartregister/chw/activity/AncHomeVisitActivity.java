package org.smartregister.chw.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.chw.anc.activity.BaseAncHomeVisitActivity;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.anc.presenter.BaseAncHomeVisitPresenter;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.core.R;
import org.smartregister.chw.core.task.RunnableTask;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreReferralUtils;
import org.smartregister.chw.interactor.AncHomeVisitInteractor;
import org.smartregister.chw.schedulers.ChwScheduleTaskExecutor;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.util.LangUtils;

import java.util.Date;

import timber.log.Timber;

public class AncHomeVisitActivity extends BaseAncHomeVisitActivity {

    private boolean originatesFromAncRegister;

    public static void startMe(Activity activity, String baseEntityID, Boolean isEditMode) {
        startMe(activity, baseEntityID, isEditMode, false);
    }

    public static void startMe(Activity activity, String baseEntityID, Boolean isEditMode, Boolean isFromRegister) {
        Intent intent = new Intent(activity, AncHomeVisitActivity.class);
        intent.putExtra(org.smartregister.chw.anc.util.Constants.ANC_MEMBER_OBJECTS.BASE_ENTITY_ID, baseEntityID);
        intent.putExtra(org.smartregister.chw.anc.util.Constants.ANC_MEMBER_OBJECTS.EDIT_MODE, isEditMode);
        intent.putExtra(org.smartregister.chw.util.Constants.ANC_INTENT_KEY.ORIGINATES_FROM_ANC_REGISTER, isFromRegister);
        activity.startActivityForResult(intent, org.smartregister.chw.anc.util.Constants.REQUEST_CODE_HOME_VISIT);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        originatesFromAncRegister = getIntent().getBooleanExtra(org.smartregister.chw.util.Constants.ANC_INTENT_KEY.ORIGINATES_FROM_ANC_REGISTER, false);
    }

    @Override
    protected void registerPresenter() {
        presenter = new BaseAncHomeVisitPresenter(memberObject, this, new AncHomeVisitInteractor());
    }

    @Override
    public void submittedAndClose() {
        // recompute schedule
        Runnable runnable = () -> ChwScheduleTaskExecutor.getInstance().execute(memberObject.getBaseEntityId(), CoreConstants.EventType.ANC_HOME_VISIT, new Date());
        org.smartregister.chw.util.Utils.startAsyncTask(new RunnableTask(runnable), null);
        if (originatesFromAncRegister) {
           startAncRegisterActivity();
        } else {
            super.submittedAndClose();
        }
    }

    private void startAncRegisterActivity() {
        Intent intent = new Intent(this, AncRegisterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void startFormActivity(JSONObject jsonForm) {

        Form form = new Form();
        form.setActionBarBackground(R.color.family_actionbar);
        form.setWizard(false);

        Intent intent = new Intent(this, ReferralWizardFormActivity.class);
        intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());
        intent.putExtra(Constants.WizardFormActivity.EnableOnCloseDialog, false);
        intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
        intent.putExtra(Constants.INTENT_KEY.BASE_ENTITY_ID, baseEntityID);
        startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);
    }

    @Override
    protected void attachBaseContext(Context base) {
        // get language from prefs
        String lang = LangUtils.getLanguage(base.getApplicationContext());
        super.attachBaseContext(LangUtils.setAppLocale(base, lang));
    }

    @Override
    public void onDestroy() {
        try {
            super.onDestroy();
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == org.smartregister.chw.anc.util.Constants.REQUEST_CODE_GET_JSON) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    String jsonString = data.getStringExtra(org.smartregister.chw.anc.util.Constants.JSON_FORM_EXTRA.JSON);

                    //check if client is being referred
                    JSONObject form = new JSONObject(jsonString);
                    JSONArray a = form.getJSONObject("step1").getJSONArray("fields");
                    String buttonAction = "";

                    for (int i = 0; i < a.length(); i++) {
                        org.json.JSONObject jo = a.getJSONObject(i);
                        if (jo.getString("key").equalsIgnoreCase("save_n_link") || jo.getString("key").equalsIgnoreCase("save_n_refer")) {
                            if (jo.optString("value") != null && jo.optString("value").compareToIgnoreCase("true") == 0) {
                                buttonAction = jo.getJSONObject("action").getString("behaviour");
                            }
                        }
                    }
                    if (!buttonAction.isEmpty()) {
                        // check if other referral exists
                        String businessStatus = buttonAction.equalsIgnoreCase("refer") ? CoreConstants.BUSINESS_STATUS.REFERRED : CoreConstants.BUSINESS_STATUS.LINKED;
                        if (!CoreReferralUtils.hasReferralTask(baseEntityID, businessStatus)) {
                            //refer
                            CoreReferralUtils.createReferralEvent(ChwApplication.getInstance().getContext().allSharedPreferences(),
                                    jsonString, CoreConstants.TABLE_NAME.ANC_REFERRAL, baseEntityID);

                            //add referral schedule
                            ChwScheduleTaskExecutor.getInstance().execute(memberObject.getBaseEntityId(), CoreConstants.EventType.ANC_REFERRAL, new Date());

                            Toast.makeText(getContext(), getResources().getString(org.smartregister.chw.R.string.referral_submitted), Toast.LENGTH_LONG).show();
                        }
                    }
                    //end of check referral

                    BaseAncHomeVisitAction ancHomeVisitAction = actionList.get(current_action);
                    if (ancHomeVisitAction != null) {
                        ancHomeVisitAction.setJsonPayload(jsonString);
                    }
                } catch (Exception e) {
                    Timber.e(e);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {

                BaseAncHomeVisitAction ancHomeVisitAction = actionList.get(current_action);
                if (ancHomeVisitAction != null)
                    ancHomeVisitAction.evaluateStatus();
            }

        }

        // update the adapter after every payload
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
            redrawVisitUI();
        }
    }
}

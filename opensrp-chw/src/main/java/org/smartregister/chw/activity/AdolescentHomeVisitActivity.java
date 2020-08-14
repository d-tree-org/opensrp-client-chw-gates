package org.smartregister.chw.activity;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.anc.presenter.BaseAncHomeVisitPresenter;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreReferralUtils;
import org.smartregister.chw.interactor.AdolescentHomeVisitInteractor;
import org.smartregister.chw.schedulers.ChwScheduleTaskExecutor;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;

import java.util.Date;

import timber.log.Timber;

public class AdolescentHomeVisitActivity extends ChildHomeVisitActivity {

    @Override
    protected void registerPresenter() {
        presenter = new BaseAncHomeVisitPresenter(memberObject, this, new AdolescentHomeVisitInteractor());
    }

    @Override
    public void submittedAndClose() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);
        CommonPersonObject commonPersonObject = CoreLibrary.getInstance().context().commonrepository(CoreConstants.TABLE_NAME.ADOLESCENT).findByBaseEntityId(memberObject.getBaseEntityId());
        CommonPersonObjectClient pClient = new CommonPersonObjectClient(commonPersonObject.getCaseId(), commonPersonObject.getDetails(), "");
        pClient.setColumnmaps(commonPersonObject.getColumnmaps());
        AdolescentProfileActivity.startMe(this, false, commonPersonObject.getCaseId(), pClient, AdolescentProfileActivity.class);
        close();
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

                    for (int i=0; i<a.length(); i++) {
                        org.json.JSONObject jo = a.getJSONObject(i);
                        if (jo.getString("key").compareToIgnoreCase("save_n_link") == 0 || jo.getString("key").compareToIgnoreCase("save_n_refer") == 0) {
                            if(jo.optString("value") != null && jo.optString("value").compareToIgnoreCase("true") == 0){
                                buttonAction = jo.getJSONObject("action").getString("behaviour");
                            }
                        }
                    }
                    if(!buttonAction.isEmpty()) {
                        // check if other referral exists
                        String entityID = baseEntityID != null ? baseEntityID : memberObject.getBaseEntityId();
                        String businessStatus = buttonAction.equalsIgnoreCase("refer") ? CoreConstants.BUSINESS_STATUS.REFERRED : CoreConstants.BUSINESS_STATUS.LINKED;
                        if (!CoreReferralUtils.hasReferralTask(entityID, businessStatus)) {
                            //refer
                            CoreReferralUtils.createReferralEvent(ChwApplication.getInstance().getContext().allSharedPreferences(),
                                    jsonString, CoreConstants.TABLE_NAME.ADOLESCENT, entityID);

                            //add referral schedule
                            ChwScheduleTaskExecutor.getInstance().execute(memberObject.getBaseEntityId(), CoreConstants.EventType.ADOLESCENT_REFERRAL, new Date());

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

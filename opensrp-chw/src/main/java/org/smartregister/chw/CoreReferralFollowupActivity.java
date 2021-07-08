package org.smartregister.chw;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.MenuRes;
import androidx.fragment.app.Fragment;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.Context;
import org.smartregister.CoreLibrary;
import org.smartregister.chw.malaria.activity.BaseMalariaRegisterActivity;
import org.smartregister.chw.malaria.contract.MalariaRegisterContract;
import org.smartregister.chw.malaria.fragment.BaseMalariaRegisterFragment;
import org.smartregister.chw.malaria.interactor.BaseMalariaRegisterInteractor;
import org.smartregister.chw.malaria.model.BaseMalariaRegisterModel;
import org.smartregister.chw.malaria.presenter.BaseMalariaRegisterPresenter;
import org.smartregister.chw.malaria.util.Constants;
import org.smartregister.chw.presenter.CoreReferralFollowupPresenter;
import org.smartregister.util.Utils;
import org.smartregister.view.activity.BaseRegisterActivity;
import org.smartregister.view.fragment.BaseRegisterFragment;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

public class CoreReferralFollowupActivity extends BaseRegisterActivity implements MalariaRegisterContract.View {
    protected String BASE_ENTITY_ID;
    protected String ACTION;
    protected String FORM_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BASE_ENTITY_ID = getIntent().getStringExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID);
        ACTION = getIntent().getStringExtra(Constants.ACTIVITY_PAYLOAD.ACTION);
        FORM_NAME = getIntent().getStringExtra(org.smartregister.chw.referral.util.Constants.ACTIVITY_PAYLOAD.REFERRAL_FOLLOWUP_FORM_NAME);
        onStartActivityWithAction();
    }

    /**
     * Process a payload when an activity is started with an action
     */
    protected void onStartActivityWithAction() {
        if (FORM_NAME != null && ACTION != null) {
            startFormActivity(FORM_NAME, BASE_ENTITY_ID, null);
        }
    }

    @Override
    public void startRegistration() {
        startFormActivity(FORM_NAME, null, null);
    }

    @Override
    public void startFormActivity(String formName, String entityId, String metaData) {
        try {
            if (mBaseFragment instanceof BaseMalariaRegisterFragment) {
                String providerId = CoreLibrary.getInstance().context().allSharedPreferences().fetchRegisteredANM();
                String locationId = Context.getInstance().allSharedPreferences().fetchUserLocalityId(providerId);
                presenter().startForm(formName, entityId, metaData, locationId);
            }
        } catch (Exception e) {
            Timber.e(e);
            displayToast(getString(org.smartregister.malaria.R.string.error_unable_to_start_form));
        }
    }

    @Override
    public void startFormActivity(JSONObject jsonForm) {
        Intent intent = new Intent(this, BaseMalariaRegisterActivity.class);
        intent.putExtra(Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

        if (getFormConfig() != null) {
            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, getFormConfig());
        }
        startActivityForResult(intent, Constants.REQUEST_CODE_GET_JSON);
    }

    @Override
    public Form getFormConfig() {
        return null;
    }

    @Override
    protected void onActivityResultExtended(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {
            presenter().saveForm(data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON));
            finish();
        }
    }

    @Override
    public List<String> getViewIdentifiers() {
        return Arrays.asList(Constants.CONFIGURATION.MALARIA_CONFIRMATION);
    }

    /**
     * Override this to subscribe to bottom navigation
     */
    @Override
    protected void registerBottomNavigation() {
        // not needed
    }

    @MenuRes
    public int getMenuResource() {
        return org.smartregister.malaria.R.menu.bottom_nav_family_menu;
    }

    @Override
    protected void initializePresenter() {
        presenter = new CoreReferralFollowupPresenter(this, new BaseMalariaRegisterModel(), new BaseMalariaRegisterInteractor());
    }

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new BaseMalariaRegisterFragment();
    }

    @Override
    protected Fragment[] getOtherFragments() {
        return new Fragment[0];
    }

    @Override
    public MalariaRegisterContract.Presenter presenter() {
        return (MalariaRegisterContract.Presenter) presenter;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.REQUEST_CODE_GET_JSON) {

            try {
                String jsonString = data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON);
                JSONObject form = new JSONObject(jsonString);
                presenter().saveForm(form.toString());
            } catch (JSONException e) {
                Timber.e(e);
                displayToast(getString(org.smartregister.malaria.R.string.error_unable_to_save_form));
            }
            startClientProcessing();
        }
    }

    private void updateFormField(JSONArray formFieldArrays, String formFieldKey, String updateValue) {
        if (updateValue != null) {
            JSONObject formObject = org.smartregister.util.JsonFormUtils.getFieldJSONObject(formFieldArrays, formFieldKey);
            if (formObject != null) {
                try {
                    formObject.remove(org.smartregister.util.JsonFormUtils.VALUE);
                    formObject.put(org.smartregister.util.JsonFormUtils.VALUE, updateValue);
                } catch (JSONException e) {
                    Timber.e(e);
                }
            }
        }
    }

    public void startClientProcessing() {
        try {
            long lastSyncTimeStamp = Utils.getAllSharedPreferences().fetchLastUpdatedAtDate(0);
            Date lastSyncDate = new Date(lastSyncTimeStamp);
            //ReferralUtil.getClientProcessorForJava().processClient(ReferralUtil.getSyncHelper().getEvents(lastSyncDate, BaseRepository.TYPE_Unprocessed));
            Utils.getAllSharedPreferences().saveLastUpdatedAtDate(lastSyncDate.getTime());
        } catch (Exception e) {
            Timber.d(e);
        }

    }
}

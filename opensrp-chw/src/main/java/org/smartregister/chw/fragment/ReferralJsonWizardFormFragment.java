package org.smartregister.chw.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.fragments.JsonWizardFormFragment;
import com.vijay.jsonwizard.interactors.JsonFormInteractor;

import org.smartregister.chw.R;
import org.smartregister.chw.presenter.ReferralJsonWizardFormFragmentPresenter;

public class ReferralJsonWizardFormFragment extends JsonWizardFormFragment {

    public static ReferralJsonWizardFormFragment getFormFragment(String stepName) {
        ReferralJsonWizardFormFragment jsonFormFragment = new ReferralJsonWizardFormFragment();
        Bundle bundle = new Bundle();
        bundle.putString(JsonFormConstants.JSON_FORM_KEY.STEPNAME, stepName);
        jsonFormFragment.setArguments(bundle);
        return jsonFormFragment;
    }

    @Override
    public void customClick(Context context, String behaviour){
        //save
        save();
    }

    @Override
    protected ReferralJsonWizardFormFragmentPresenter createPresenter() {
        return new ReferralJsonWizardFormFragmentPresenter(this, JsonFormInteractor.getInstance());
    }
}

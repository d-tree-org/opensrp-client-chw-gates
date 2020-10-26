package org.smartregister.chw.activity;

import android.content.Intent;

import androidx.fragment.app.Fragment;

import org.json.JSONObject;
import org.smartregister.chw.fragment.MonthlyActivitiesRegisterFragment;
import org.smartregister.chw.presenter.MonthlyActivitiesRegisterPresenter;
import org.smartregister.view.activity.BaseRegisterActivity;
import org.smartregister.view.fragment.BaseRegisterFragment;

import java.util.List;

public class MonthlyActivitiesRegisterActivity extends BaseRegisterActivity {


    @Override
    protected void initializePresenter() {
        presenter = new MonthlyActivitiesRegisterPresenter();
    }

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new MonthlyActivitiesRegisterFragment();
    }

    @Override
    protected Fragment[] getOtherFragments() {
        return new Fragment[0];
    }

    @Override
    public void startFormActivity(String s, String s1, String s2) {

    }

    @Override
    public void startFormActivity(JSONObject jsonObject) {

    }

    @Override
    protected void onActivityResultExtended(int i, int i1, Intent intent) {

    }

    @Override
    public List<String> getViewIdentifiers() {
        return null;
    }

    @Override
    public void startRegistration() {

    }
}

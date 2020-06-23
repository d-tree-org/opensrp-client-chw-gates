package org.smartregister.chw.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.MenuRes;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.LabelVisibilityMode;

import org.json.JSONObject;
import org.smartregister.chw.contract.AdolescentRegisterContract;
import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.fragment.AdolescentRegisterFragment;
import org.smartregister.chw.interactor.AdolescentRegisterInteractor;
import org.smartregister.chw.listener.ChwBottomNavigationListener;
import org.smartregister.chw.malaria.listener.MalariaBottomNavigationListener;
import org.smartregister.chw.model.AdolescentRegisterModel;
import org.smartregister.chw.presenter.AdolescentRegisterPresenter;
import org.smartregister.chw.util.Utils;
import org.smartregister.family.domain.FamilyMetadata;
import org.smartregister.helper.BottomNavigationHelper;
import org.smartregister.listener.BottomNavigationListener;
import org.smartregister.view.activity.BaseRegisterActivity;
import org.smartregister.view.fragment.BaseRegisterFragment;

import java.util.Arrays;
import java.util.List;

public class AdolescentRegisterActivity extends BaseRegisterActivity implements AdolescentRegisterContract.View {

    @Override
    public AdolescentRegisterContract.Presenter presenter() {
        return (AdolescentRegisterContract.Presenter) presenter;
    }

    @Override
    protected void initializePresenter() {
        presenter = new AdolescentRegisterPresenter(this, new AdolescentRegisterModel(), new AdolescentRegisterInteractor());
    }

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new AdolescentRegisterFragment();
    }

    @Override
    protected Fragment[] getOtherFragments() {
        return new Fragment[0];
    }

    @Override
    protected void registerBottomNavigation() {
        bottomNavigationHelper = new BottomNavigationHelper();
        bottomNavigationView = findViewById(org.smartregister.R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
            bottomNavigationView.getMenu().removeItem(org.smartregister.R.id.action_clients);
            bottomNavigationView.getMenu().removeItem(org.smartregister.malaria.R.id.action_register);
            bottomNavigationView.getMenu().removeItem(org.smartregister.R.id.action_search);
            bottomNavigationView.getMenu().removeItem(org.smartregister.R.id.action_library);
            bottomNavigationView.inflateMenu(getMenuResource());
            bottomNavigationHelper.disableShiftMode(bottomNavigationView);
            FamilyRegisterActivity.registerBottomNavigation(bottomNavigationHelper, bottomNavigationView, this);
        }
    }

    @MenuRes
    public int getMenuResource() {
        return org.smartregister.malaria.R.menu.bottom_nav_family_menu;
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
        return Arrays.asList(Utils.metadata().familyRegister.config);
    }

    @Override
    public void startRegistration() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavigationMenu.getInstance(this, null, null);
    }
}

package org.smartregister.chw.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.MenuRes;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.LabelVisibilityMode;

import org.json.JSONObject;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.fragment.MonthlyActivitiesRegisterFragment;
import org.smartregister.chw.presenter.MonthlyActivitiesRegisterPresenter;
import org.smartregister.chw.util.ChartUtil;
import org.smartregister.chw.util.Constants;
import org.smartregister.chw.util.Utils;
import org.smartregister.helper.BottomNavigationHelper;
import org.smartregister.reporting.ReportingLibrary;
import org.smartregister.reporting.domain.CompositeIndicatorTally;
import org.smartregister.reporting.repository.DailyIndicatorCountRepository;
import org.smartregister.view.activity.BaseRegisterActivity;
import org.smartregister.view.fragment.BaseRegisterFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class MonthlyActivitiesRegisterActivity extends BaseRegisterActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavigationMenu.getInstance(this, null, null);

        ReportingLibrary reportingLibrary = ReportingLibrary.getInstance();
        String indicatorsConfigFile = "config/indicator-definitions.yml";
        reportingLibrary.initIndicatorData(indicatorsConfigFile, ChwApplication.getInstance().getRepository().getReadableDatabase(ChwApplication.getInstance().getPassword()));

        //addSampleIndicatorDailyTally();

    }

    public void addSampleIndicatorDailyTally() {
        DailyIndicatorCountRepository dailyIndicatorCountRepository = ReportingLibrary.getInstance().dailyIndicatorCountRepository();
        String eventDateFormat = "E MMM dd hh:mm:ss z yyyy";
        Date dateCreated = null;
        try {
            dateCreated = new SimpleDateFormat(eventDateFormat, Locale.getDefault()).parse(new Date().toString());
        } catch (ParseException pe) {
            Timber.e(pe.toString());
        }
        dailyIndicatorCountRepository.add(new CompositeIndicatorTally(null, 80, ChartUtil.numericIndicatorKey, dateCreated));
        dailyIndicatorCountRepository.add(new CompositeIndicatorTally(null, 60, ChartUtil.pieChartYesIndicatorKey, dateCreated));
        dailyIndicatorCountRepository.add(new CompositeIndicatorTally(null, 20, ChartUtil.pieChartNoIndicatorKey, dateCreated));


    }


    @Override
    protected void setupViews() {
        super.setupViews();
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
        bottomNavigationView.setVisibility(View.GONE);
    }

    @MenuRes
    public int getMenuResource() {
        return org.smartregister.malaria.R.menu.bottom_nav_family_menu;
    }

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
        Fragment fg  = new MonthlyActivitiesRegisterFragment();
        return new Fragment[]{fg};
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
    protected void onResumption() {
        super.onResumption();
        NavigationMenu menu = NavigationMenu.getInstance(this, null, null);
        if (menu != null) {
            menu.getNavigationAdapter().setSelectedView(Constants.DrawerMenu.MONTHLY_ACTIVITY);
        }
    }

}

package org.smartregister.chw.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;

import org.smartregister.CoreLibrary;
import org.smartregister.chw.BuildConfig;
import org.smartregister.chw.R;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.core.activity.CoreFamilyRegisterActivity;
import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.fragment.FamilyRegisterFragment;
import org.smartregister.chw.fragment.VisitLocationFragment;
import org.smartregister.chw.listener.ChwBottomNavigationListener;
import org.smartregister.chw.listener.FamilyBottomNavigationListener;
import org.smartregister.chw.referral.ReferralLibrary;
import org.smartregister.chw.util.Constants;
import org.smartregister.helper.BottomNavigationHelper;
import org.smartregister.simprint.SimPrintsIdentifyActivity;
import org.smartregister.view.fragment.BaseRegisterFragment;

public class FamilyRegisterActivity extends CoreFamilyRegisterActivity {

    private static final int IDENTIFY_RESULT_CODE = 4061;

    public static void startFamilyRegisterForm(Activity activity) {
        Intent intent = new Intent(activity, FamilyRegisterActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.ACTION, Constants.ACTION.START_REGISTRATION);
        activity.startActivity(intent);
    }

    public static void startFingerprintScan(Activity activity){
        String moduleId = CoreLibrary.getInstance().context().allSharedPreferences().fetchUserLocalityName("");
        if (moduleId == null || moduleId.isEmpty()){
            moduleId = "global_module";
        }
        SimPrintsIdentifyActivity.startSimprintsIdentifyActivity(activity,
                moduleId, IDENTIFY_RESULT_CODE);
    }

    public static void registerBottomNavigation(
            BottomNavigationHelper bottomNavigationHelper,
            BottomNavigationView bottomNavigationView,
            Activity activity
    ) {

        if (bottomNavigationView != null) {
            bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);

            bottomNavigationView.getMenu().clear();
            bottomNavigationView.inflateMenu(R.menu.bottom_nav_family_menu);
            bottomNavigationHelper.disableShiftMode(bottomNavigationView);
            bottomNavigationView.setOnNavigationItemSelectedListener(new ChwBottomNavigationListener(activity));
        }

        if (!BuildConfig.SUPPORT_QR)
            bottomNavigationView.getMenu().removeItem(org.smartregister.family.R.id.action_scan_qr);

        if (!BuildConfig.SUPPORT_REPORT)
            bottomNavigationView.getMenu().removeItem(org.smartregister.family.R.id.action_job_aids);

        if (activity instanceof PncRegisterActivity ||
                activity instanceof ChildRegisterActivity ||
                activity instanceof AdolescentRegisterActivity ||
                activity instanceof AncRegisterActivity)
            bottomNavigationView.getMenu().removeItem(R.id.action_scan_fingerprint);

    }

    @Override
    protected void registerBottomNavigation() {
        super.registerBottomNavigation();

        if (!BuildConfig.SUPPORT_QR)
            bottomNavigationView.getMenu().removeItem(org.smartregister.family.R.id.action_scan_qr);

        if (!BuildConfig.SUPPORT_REPORT)
            bottomNavigationView.getMenu().removeItem(org.smartregister.family.R.id.action_job_aids);

        bottomNavigationView.setOnNavigationItemSelectedListener(new FamilyBottomNavigationListener(this, bottomNavigationView));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavigationMenu.getInstance(this, null, null);
        ChwApplication.getInstance().notifyAppContextChange(); // initialize the language (bug in translation)
        ReferralLibrary.getInstance().seedSampleReferralServicesAndIndicators(); // Used to seed referral module services and problems, temporally and will be removed once they are sync from the server in future versions.
        action = getIntent().getStringExtra(Constants.ACTIVITY_PAYLOAD.ACTION);
        if (action != null && action.equals(Constants.ACTION.START_REGISTRATION)) {
            startRegistration();
        }
        //Fragment with background task to capture current CHW location
        addVisitLocationFragment();
    }

    private void addVisitLocationFragment(){
        VisitLocationFragment visitLocationFragment = new VisitLocationFragment();

        // Using FragmentManager to add the fragment to the activity
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(visitLocationFragment, "visitLocationBackgroundTask");
        fragmentTransaction.commit();
    }

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new FamilyRegisterFragment();
    }
}
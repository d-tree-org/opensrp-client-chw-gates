package org.smartregister.chw.listener;

import android.app.Activity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.view.MenuItem;

import org.smartregister.chw.R;
import org.smartregister.chw.activity.FamilyRegisterActivity;
import org.smartregister.view.activity.BaseRegisterActivity;

/**
 * Author : Isaya Mollel on 2019-11-28.
 */
public class BottomNavigationListenerFlv extends DefaultBottomNavigationListenerFlv {

    BottomNavigationListenerFlv(Activity context){
        super(context);
    }

    @Override
    public boolean navigationItemSelected(MenuItem item, Activity context, BottomNavigationView view) {

        BaseRegisterActivity baseRegisterActivity = (BaseRegisterActivity) context;

        if (item.getItemId() == R.id.action_scan_fingerprint){
            if (context instanceof FamilyRegisterActivity){
                FamilyRegisterActivity activity = (FamilyRegisterActivity) context;
                activity.startFingerprintScan(activity);
            }
        }
        if (item.getItemId() == R.id.action_register){
            if (context instanceof FamilyRegisterActivity){
                baseRegisterActivity.startRegistration();
            }else {
                FamilyRegisterActivity.startFamilyRegisterForm(context);
            }
        }

        return true;
    }
}

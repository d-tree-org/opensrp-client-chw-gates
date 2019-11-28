package org.smartregister.chw.listener;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;

import org.smartregister.chw.R;
import org.smartregister.chw.activity.FamilyRegisterActivity;
import org.smartregister.chw.activity.JobAidsActivity;
import org.smartregister.view.activity.BaseRegisterActivity;

/**
 * Author : Isaya Mollel on 2019-11-28.
 */
public class BottomNavigationListenerFlv extends DefaultBottomNavigationListenerFlv {

    @Override
    public boolean navigationItemSelected(MenuItem item, Activity context) {
        BaseRegisterActivity baseRegisterActivity = (BaseRegisterActivity) context;

        if (item.getItemId() == org.smartregister.family.R.id.action_family) {
            if (context instanceof FamilyRegisterActivity) {
                baseRegisterActivity.switchToBaseFragment();
            } else {
                Intent intent = new Intent(context, FamilyRegisterActivity.class);
                context.startActivity(intent);
                context.finish();
            }
        }
        else if (item.getItemId() == R.id.action_scan_fingerprint) {

            if (context instanceof FamilyRegisterActivity){
                FamilyRegisterActivity activity = (FamilyRegisterActivity)context;
                activity.startFingerprintScan(activity);
                return true;
            }
        }
        else if (item.getItemId() == org.smartregister.family.R.id.action_register) {

            if (context instanceof FamilyRegisterActivity) {
                baseRegisterActivity.startRegistration();
            } else {
                FamilyRegisterActivity.startFamilyRegisterForm(context);
            }

            return false;
        }
        else if (item.getItemId() == org.smartregister.family.R.id.action_job_aids) {
            //view.setSelectedItemId(R.id.action_family);
            Intent intent = new Intent(context, JobAidsActivity.class);
            context.startActivity(intent);
            return false;
        }

        return true;
    }
}

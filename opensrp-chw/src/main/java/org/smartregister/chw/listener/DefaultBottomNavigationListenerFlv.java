package org.smartregister.chw.listener;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;

import org.smartregister.chw.activity.FamilyRegisterActivity;
import org.smartregister.chw.activity.JobAidsActivity;
import org.smartregister.view.activity.BaseRegisterActivity;

/**
 * Author : Isaya Mollel on 2019-11-27.
 */
public abstract class DefaultChwBottomNavigationListenerFlv implements ChwBottomNavigationListener.Flavor{

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
        } else if (item.getItemId() == org.smartregister.family.R.id.action_scan_qr) {
            baseRegisterActivity.startQrCodeScanner();
            return false;
        } else if (item.getItemId() == org.smartregister.family.R.id.action_register) {

            if (context instanceof FamilyRegisterActivity) {
                baseRegisterActivity.startRegistration();
            } else {
                FamilyRegisterActivity.startFamilyRegisterForm(context);
            }

            return false;
        } else if (item.getItemId() == org.smartregister.family.R.id.action_job_aids) {
            //view.setSelectedItemId(R.id.action_family);
            Intent intent = new Intent(context, JobAidsActivity.class);
            context.startActivity(intent);
            return false;
        }

        return true;
    }
}

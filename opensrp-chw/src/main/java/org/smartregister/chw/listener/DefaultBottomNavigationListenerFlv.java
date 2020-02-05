package org.smartregister.chw.listener;

import android.app.Activity;
import android.content.Intent;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.view.MenuItem;

import org.smartregister.chw.activity.JobAidsActivity;
import org.smartregister.view.activity.BaseRegisterActivity;

public abstract class DefaultBottomNavigationListenerFlv extends
        org.smartregister.family.listener.FamilyBottomNavigationListener implements BottomNavigationFlavor {

    DefaultBottomNavigationListenerFlv(Activity context){
        super(context);
    }

    @Override
    public boolean navigationItemSelected(MenuItem item, Activity context, BottomNavigationView view) {

        BaseRegisterActivity baseRegisterActivity = (BaseRegisterActivity) context;

        if (item.getItemId() == org.smartregister.family.R.id.action_register) {
            view.setSelectedItemId(org.smartregister.family.R.id.action_family);
            baseRegisterActivity.startRegistration();
            return false;
        } else if (item.getItemId() == org.smartregister.family.R.id.action_job_aids) {
            view.setSelectedItemId(org.smartregister.family.R.id.action_family);
            Intent intent = new Intent(context, JobAidsActivity.class);
            context.startActivity(intent);
            return false;
        } else {
            super.onNavigationItemSelected(item);
        }

        return true;
    }
}

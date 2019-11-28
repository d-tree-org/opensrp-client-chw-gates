package org.smartregister.chw.listener;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;

import org.smartregister.chw.R;
import org.smartregister.chw.activity.FamilyRegisterActivity;
import org.smartregister.chw.activity.JobAidsActivity;
import org.smartregister.view.activity.BaseRegisterActivity;

/**
 * Author : Isaya Mollel on 2019-11-27.
 */
public abstract class DefaultBottomNavigationListenerFlv implements BottomNavigationFavor {

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
        }

        return true;
    }
}

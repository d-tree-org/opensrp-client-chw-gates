package org.smartregister.chw.listener;

import android.app.Activity;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;

import org.smartregister.chw.R;
import org.smartregister.chw.activity.FamilyRegisterActivity;

/**
 * Author : Isaya Mollel on 2019-11-28.
 */
public class FamilyBottomNavigationListenerFlv extends DefaultBottomNavigationListenerFlv {

    FamilyBottomNavigationListenerFlv(Activity context){
        super(context);
    }

    @Override
    public boolean navigationItemSelected(MenuItem item, Activity context, BottomNavigationView view) {

        if (item.getItemId() == R.id.action_scan_fingerprint){
            if (context instanceof FamilyRegisterActivity){
                FamilyRegisterActivity activity = (FamilyRegisterActivity) context;
                activity.startFingerprintScan(activity);
            }
        }

        return true;
    }
}

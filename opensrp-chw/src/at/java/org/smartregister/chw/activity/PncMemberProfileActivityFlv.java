package org.smartregister.chw.activity;

import android.view.Menu;

import org.smartregister.chw.R;

/**
 * Author : Isaya Mollel on 2019-11-18.
 */
public class PncMemberProfileActivityFlv implements PncMemberProfileActivity.Flavor {

    @Override
    public Boolean onCreateOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_malaria_confirmation).setVisible(true);
        return true;
    }

}

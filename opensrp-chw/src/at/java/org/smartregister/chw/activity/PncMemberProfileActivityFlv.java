package org.smartregister.chw.activity;

import android.view.Menu;

import org.smartregister.chw.R;
import org.smartregister.chw.util.UtilsFlv;

public class PncMemberProfileActivityFlv implements PncMemberProfileActivity.Flavor {

    @Override
    public Boolean onCreateOptionsMenu(Menu menu, String baseEntityId) {
        UtilsFlv.updateMalariaMenuItems(baseEntityId, menu);
        menu.findItem(R.id.action_fp_initiation).setVisible(true);
        return true;
    }

}

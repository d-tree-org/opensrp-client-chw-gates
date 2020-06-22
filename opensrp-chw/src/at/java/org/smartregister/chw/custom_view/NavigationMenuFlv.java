package org.smartregister.chw.custom_view;

import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.job.AfyatekTaskServiceJob;

import java.util.HashMap;

public class NavigationMenuFlv implements NavigationMenu.Flavour {

    @Override
    public String[] getSupportedLanguages() {
        return new String[] { "English", "Kiswahili" };
    }

    @Override
    public HashMap<String, String> getTableMapValues(){
        HashMap<String, String> mp = new HashMap<>();
        mp.put(CoreConstants.DrawerMenu.REFERRALS, CoreConstants.TABLE_NAME.TASK);
        mp.put(CoreConstants.DrawerMenu.ADOLESCENT, "ec_adolescent");
        return mp;
    }

    @Override
    public void executeSync(){
        AfyatekTaskServiceJob.scheduleJobImmediately(AfyatekTaskServiceJob.TAG);
    }

}

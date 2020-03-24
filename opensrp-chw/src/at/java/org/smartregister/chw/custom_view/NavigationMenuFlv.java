package org.smartregister.chw.custom_view;

import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.core.utils.CoreConstants;

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
        return mp;
    }

}

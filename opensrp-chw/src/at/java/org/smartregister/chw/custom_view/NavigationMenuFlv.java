package org.smartregister.chw.custom_view;

import org.smartregister.chw.core.custom_views.NavigationMenu;

/**
 * Author : Isaya Mollel on 2019-11-12.
 */
public class NavigationMenuFlv implements NavigationMenu.Flavour {

    @Override
    public String[] getSupportedLanguages() {
        return new String[] { "English, Kiswahili" };
    }
}

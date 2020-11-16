package org.smartregister.chw.model;

import org.smartregister.chw.R;
import org.smartregister.chw.core.model.NavigationModel;
import org.smartregister.chw.core.model.NavigationOption;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.util.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NavigationModelFlv implements NavigationModel.Flavor {

    private static List<NavigationOption> navigationOptions = new ArrayList<>();

    @Override
    public List<NavigationOption> getNavigationItems() {
        if (navigationOptions.size() == 0) {
            NavigationOption op1 = new NavigationOption(R.mipmap.sidemenu_families, R.mipmap.sidemenu_families_active, R.string.menu_all_families, Constants.DrawerMenu.ALL_FAMILIES, 0);
            NavigationOption op2 = new NavigationOption(R.mipmap.sidemenu_children, R.mipmap.sidemenu_children_active, R.string.menu_child_clients, Constants.DrawerMenu.CHILD_CLIENTS, 0);
            NavigationOption op3 = new NavigationOption(R.mipmap.sidemenu_anc, R.mipmap.sidemenu_anc_active, R.string.menu_anc, Constants.DrawerMenu.ANC, 0);
            NavigationOption op4 = new NavigationOption(R.mipmap.sidemenu_pnc, R.mipmap.sidemenu_pnc_active, R.string.menu_pnc, Constants.DrawerMenu.PNC, 0);
            NavigationOption op5 = new NavigationOption(R.mipmap.side_menu_adolescent, R.mipmap.side_menu_adolescent_active, R.string.menu_adolescent, Constants.DrawerMenu.ADOLESCENT, 0);
            NavigationOption op6 = new NavigationOption(R.mipmap.sidemenu_referrals, R.mipmap.sidemenu_referrals_active, R.string.menu_referrals, Constants.DrawerMenu.REFERRALS, 0);
            NavigationOption op7 = new NavigationOption(R.drawable.ic_chart, R.mipmap.ic_chart_active, R.string.monthly_activity, Constants.DrawerMenu.MONTHLY_ACTIVITY, 0);
            navigationOptions.addAll(Arrays.asList(op1, op3, op2, op4, op5, op6, op7));
        }

        return navigationOptions;
    }
}

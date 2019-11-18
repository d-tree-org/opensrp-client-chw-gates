package org.smartregister.chw.custom_view;

import android.support.design.widget.FloatingActionButton;

import org.smartregister.chw.R;

import static org.smartregister.chw.core.utils.Utils.redrawWithOption;

/**
 * Author : Isaya Mollel on 2019-11-12.
 */
public class FamilyMemberFloatingMenuFlv implements FamilyMemberFloatingMenu.Flavor {

    @Override
    public void reDraw(FamilyMemberFloatingMenu menu, boolean has_phone) {
        redrawWithOption(menu, has_phone);
    }

    @Override
    public void prepareFab(FamilyMemberFloatingMenu menu, FloatingActionButton fab) {
        fab.setOnClickListener(v -> menu.animateFAB());
        fab.setImageResource(R.drawable.ic_edit_white);
    }

    @Override
    public void fabInteraction(FamilyMemberFloatingMenu menu) {
        menu.animateFAB();
    }
}

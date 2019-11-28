package org.smartregister.chw.listener;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;

public class FamilyBottomNavigationListener extends org.smartregister.family.listener.FamilyBottomNavigationListener {

    private Activity context;
    private BottomNavigationView view;
    private BottomNavigationFlavor flavor;

    public FamilyBottomNavigationListener(Activity context, BottomNavigationView view){
        super(context);
        this.context = context;
        this.view = view;
        this.flavor = new FamilyBottomNavigationListenerFlv(context);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return flavor.navigationItemSelected(item, context, view);
    }
}

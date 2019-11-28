package org.smartregister.chw.listener;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;

import org.smartregister.chw.R;
import org.smartregister.chw.activity.FamilyRegisterActivity;
import org.smartregister.chw.activity.JobAidsActivity;
import org.smartregister.chw.model.FamilyProfileModel;
import org.smartregister.view.activity.BaseRegisterActivity;

public class FamilyBottomNavigationListener extends org.smartregister.family.listener.FamilyBottomNavigationListener {

    private Activity context;
    private BottomNavigationView view;
    private BottomNavigationFavor flavor = new FamilyBottomNavigationListenerFlv();

    public FamilyBottomNavigationListener(Activity context, BottomNavigationView view){
        super(context);
        this.context = context;
        this.view = view;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return flavor.navigationItemSelected(item, context, view);
    }
}

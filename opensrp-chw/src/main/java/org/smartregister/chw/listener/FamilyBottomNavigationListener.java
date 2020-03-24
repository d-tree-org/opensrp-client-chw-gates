package org.smartregister.chw.listener;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.smartregister.chw.activity.JobAidsActivity;
import org.smartregister.view.activity.BaseRegisterActivity;

public class FamilyBottomNavigationListener extends org.smartregister.family.listener.FamilyBottomNavigationListener {

    private Activity context;
    private BottomNavigationView view;
    private BottomNavigationFlavor flavor;

    public FamilyBottomNavigationListener(Activity context, BottomNavigationView view){
        super(context);
        this.context = context;
        this.view = view;
        this.flavor = new BottomNavigationListenerFlv(context);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return flavor.navigationItemSelected(item, context, view);
    }
}

package org.smartregister.chw.listener;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.MenuItem;

import org.smartregister.chw.activity.FamilyRegisterActivity;
import org.smartregister.chw.activity.JobAidsActivity;
import org.smartregister.chw.core.listener.CoreBottomNavigationListener;
import org.smartregister.family.R;
import org.smartregister.view.activity.BaseRegisterActivity;

public class ChwBottomNavigationListener extends CoreBottomNavigationListener {

    private Activity context;
    private Flavor flavor = new ChwBottomNavigationListenerFlv();

    public ChwBottomNavigationListener(Activity context) {
        super(context);
        this.context = context;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//        super.onNavigationItemSelected(item);
        return flavor.navigationItemSelected(item, context);
    }

    public interface Flavor {
        boolean navigationItemSelected(MenuItem item, Activity context);
    }

}

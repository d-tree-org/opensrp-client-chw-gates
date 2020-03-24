package org.smartregister.chw.listener;

import android.app.Activity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.view.MenuItem;

/**
 * Author : Isaya Mollel on 2019-11-28.
 */
public interface BottomNavigationFlavor {
    boolean navigationItemSelected(MenuItem item, Activity context, BottomNavigationView view);
}

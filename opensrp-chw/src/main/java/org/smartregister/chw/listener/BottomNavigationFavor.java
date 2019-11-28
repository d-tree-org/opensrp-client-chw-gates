package org.smartregister.chw.listener;

import android.app.Activity;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;

/**
 * Author : Isaya Mollel on 2019-11-28.
 */
public interface BottomNavigationFavor {
    boolean navigationItemSelected(MenuItem item, Activity context, BottomNavigationView view);
}

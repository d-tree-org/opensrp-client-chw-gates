package org.smartregister.chw.adapter;

import android.app.Activity;

import org.smartregister.chw.core.model.NavigationOption;

import java.util.List;
import java.util.Map;

/**
 * Author : Isaya Mollel on 2019-11-25.
 */
public class NavigationAdapter extends org.smartregister.chw.core.adapter.NavigationAdapter {

    public NavigationAdapter(List<NavigationOption> navigationOptions, Activity context, Map<String, Class> registeredActivities){
        super(navigationOptions, context, registeredActivities);
    }
}

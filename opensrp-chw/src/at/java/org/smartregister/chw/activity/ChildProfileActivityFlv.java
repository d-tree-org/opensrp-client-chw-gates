package org.smartregister.chw.activity;

import android.app.Activity;
import android.widget.Toast;

import org.smartregister.chw.R;
import org.smartregister.chw.core.fragment.FamilyCallDialogFragment;
import org.smartregister.chw.core.listener.OnClickFloatingMenu;
import org.smartregister.chw.presenter.ChildProfilePresenter;

/**
 * Author : Isaya Mollel on 2019-11-12.
 */
public class ChildProfileActivityFlv implements ChildProfileActivity.Flavor{

    @Override
    public OnClickFloatingMenu getOnClickFloatingMenu(Activity activity, ChildProfilePresenter presenter) {
        return viewId -> {
            switch (viewId){
                case R.id.call_layout:
                    FamilyCallDialogFragment.launchDialog(activity, presenter.getFamilyId());
                    break;
                case R.id.refer_to_facility_fab:
                    Toast.makeText(activity, "Refer to facility", Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        };
    }

    @Override
    public boolean showMalariaConfirmationMenu() {
        return false;
    }

    @Override
    public boolean showFollowUpVisit() {
        return true;
    }
}

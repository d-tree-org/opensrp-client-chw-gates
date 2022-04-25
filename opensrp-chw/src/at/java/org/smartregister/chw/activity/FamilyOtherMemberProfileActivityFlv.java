package org.smartregister.chw.activity;

import android.app.Activity;
import android.view.Menu;
import android.widget.Toast;

import org.smartregister.chw.R;
import org.smartregister.chw.core.fragment.FamilyCallDialogFragment;
import org.smartregister.chw.core.listener.OnClickFloatingMenu;
import org.smartregister.chw.util.UtilsFlv;
import org.smartregister.commonregistry.CommonPersonObjectClient;

import static org.smartregister.chw.core.utils.Utils.isWomanOfReproductiveAge;

public class FamilyOtherMemberProfileActivityFlv implements FamilyOtherMemberProfileActivity.Flavor{

    @Override
    public OnClickFloatingMenu getOnClickFloatingMenu(final Activity activity, final String familyBaseEntityId) {
        return viewId -> {
            switch (viewId) {
                case R.id.call_layout:
                    FamilyCallDialogFragment.launchDialog(activity, familyBaseEntityId);
                    break;
                case R.id.refer_to_facility_fab:
                    Toast.makeText(activity, "Refer to facility", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        };
    }

    @Override
    public void updateMalariaMenuItems(String baseEntityId, Menu menu) {

        //Disable Malaria menus : at
        menu.findItem(R.id.action_malaria_followup_visit).setVisible(false);
        menu.findItem(R.id.action_malaria_diagnosis).setEnabled(false);

        UtilsFlv.updateMalariaMenuItems(baseEntityId, menu);
    }

    @Override
    public void updateFpMenuItems(String baseEntityId, Menu menu) {
        menu.findItem(R.id.action_fp_change).setVisible(false);
        menu.findItem(R.id.action_fp_initiation).setVisible(false);
        menu.findItem(R.id.action_family_planning_initiation).setVisible(false);
        // No FP
        //UtilsFlv.updateFpMenuItems(baseEntityId, menu);
    }

    @Override
    public boolean isWra(CommonPersonObjectClient commonPersonObject) {
        return isWomanOfReproductiveAge(commonPersonObject, 10, 49);
    }

    @Override
    public boolean hasANC() {
        return true;
    }
}

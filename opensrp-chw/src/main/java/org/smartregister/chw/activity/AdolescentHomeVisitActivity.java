package org.smartregister.chw.activity;

import android.app.Activity;
import android.content.Intent;

import org.smartregister.CoreLibrary;
import org.smartregister.chw.anc.presenter.BaseAncHomeVisitPresenter;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.interactor.AdolescentHomeVisitInteractor;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;

public class AdolescentHomeVisitActivity extends ChildHomeVisitActivity {

    @Override
    protected void registerPresenter() {
        presenter = new BaseAncHomeVisitPresenter(memberObject, this, new AdolescentHomeVisitInteractor());
    }

    @Override
    public void submittedAndClose() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);
        CommonPersonObject commonPersonObject = CoreLibrary.getInstance().context().commonrepository(CoreConstants.TABLE_NAME.ADOLESCENT).findByBaseEntityId(memberObject.getBaseEntityId());
        CommonPersonObjectClient pClient = new CommonPersonObjectClient(commonPersonObject.getCaseId(), commonPersonObject.getDetails(), "");
        pClient.setColumnmaps(commonPersonObject.getColumnmaps());
        AdolescentProfileActivity.startMe(this, false, commonPersonObject.getCaseId(), pClient, AdolescentProfileActivity.class);
        close();
    }
}

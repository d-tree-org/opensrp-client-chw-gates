package org.smartregister.chw.presenter;

import org.smartregister.chw.malaria.contract.MalariaRegisterContract;
import org.smartregister.chw.malaria.presenter.BaseMalariaRegisterPresenter;

public class CoreReferralFollowupPresenter extends BaseMalariaRegisterPresenter {

    public CoreReferralFollowupPresenter(MalariaRegisterContract.View view, MalariaRegisterContract.Model model, MalariaRegisterContract.Interactor interactor) {
        super(view, model, interactor);
    }

    @Override
    public void onRegistrationSaved() {

    }

    private MalariaRegisterContract.View getView() {
        return this.viewReference != null ? (MalariaRegisterContract.View)this.viewReference.get() : null;
    }

}

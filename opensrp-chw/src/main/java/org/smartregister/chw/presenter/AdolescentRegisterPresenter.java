package org.smartregister.chw.presenter;

import org.smartregister.chw.contract.AdolescentRegisterContract;

import java.lang.ref.WeakReference;
import java.util.List;

public class AdolescentRegisterPresenter implements AdolescentRegisterContract.Presenter, AdolescentRegisterContract.InteractorCallBack {

    public static final String TAG = AdolescentRegisterPresenter.class.getName();

    protected WeakReference<AdolescentRegisterContract.View> viewReference;
    private AdolescentRegisterContract.Interactor interactor;
    protected AdolescentRegisterContract.Model model;

    public AdolescentRegisterPresenter(AdolescentRegisterContract.View viewReference, AdolescentRegisterContract.Model model, AdolescentRegisterContract.Interactor interactor) {
        this.viewReference = new WeakReference<>(viewReference);
        this.interactor = interactor;
        this.model = model;
    }

    @Override
    public void startForm(String formName, String entityId, String metadata, String currentLocationId) throws Exception {

    }

    @Override
    public void saveForm(String jsonString) {

    }

    @Override
    public void registerViewConfigurations(List<String> list) {

    }

    @Override
    public void unregisterViewConfiguration(List<String> list) {

    }

    @Override
    public void onDestroy(boolean b) {

    }

    @Override
    public void updateInitials() {

    }
}

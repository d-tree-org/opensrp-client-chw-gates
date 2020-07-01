package org.smartregister.chw.presenter;

import org.smartregister.chw.contract.AdolescentProfileContract;
import org.smartregister.chw.interactor.AdolescentProfileInteractor;
import org.smartregister.commonregistry.CommonPersonObjectClient;

import java.lang.ref.WeakReference;

public class AdolescentProfilePresenter implements AdolescentProfileContract.Presenter, AdolescentProfileContract.InteractorCallBack {

    public String adolescentBaseEntityId;
    public String familyID;
    private WeakReference<AdolescentProfileContract.View> view;
    private AdolescentProfileContract.Interactor interactor;
    private String dob;
    private String familyName;

    public AdolescentProfilePresenter(AdolescentProfileContract.View adolescentView, String adolescentBaseEntityId) {
        this.adolescentBaseEntityId = adolescentBaseEntityId;
        this.view = new WeakReference<>(adolescentView);
        this.interactor = new AdolescentProfileInteractor();
    }

    @Override
    public void fetchProfileData() {
        interactor.refreshProfileInfo(adolescentBaseEntityId, this);
    }

    @Override
    public void refreshProfileView(CommonPersonObjectClient pClient) {
        if (pClient == null || pClient.getColumnmaps() == null) {
            return;
        }

    }

    @Override
    public void fetchVisitStatus(String baseEntityId) {

    }

    @Override
    public void fetchUpcomingServiceAndFamilyDue(String baseEntityId) {

    }

    @Override
    public AdolescentProfileContract.View getView() {
        return null;
    }

    @Override
    public void onDestroy(boolean b) {

    }

    @Override
    public void hideProgress() {
        getView().showProgressBar(false);
    }
}

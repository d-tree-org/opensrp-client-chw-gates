package org.smartregister.chw.presenter;

import org.smartregister.chw.contract.AdolescentProfileContract;
import org.smartregister.chw.interactor.AdolescentProfileInteractor;
import org.smartregister.chw.util.ChildDBConstants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;

import java.lang.ref.WeakReference;

public class AdolescentProfilePresenter implements AdolescentProfileContract.Presenter, AdolescentProfileContract.InteractorCallBack {

    public String adolescentBaseEntityId;
    public String familyID;
    private WeakReference<AdolescentProfileContract.View> view;
    private AdolescentProfileContract.Interactor interactor;
    private String dob;
    private String familyName;
    private String gender;
    private String phoneNumber;
    private CommonPersonObjectClient commonPersonObjectClient;

    public AdolescentProfilePresenter(AdolescentProfileContract.View adolescentView, String adolescentBaseEntityId) {
        this.adolescentBaseEntityId = adolescentBaseEntityId;
        this.view = new WeakReference<>(adolescentView);
        this.interactor = new AdolescentProfileInteractor();
    }

    @Override
    public void fetchProfileData() {
        interactor.refreshProfileInfo(adolescentBaseEntityId, this);
        commonPersonObjectClient = interactor.getcommonPersonObjectClient();
    }

    @Override
    public void setFamilyID(String familyID) {
        this.familyID = familyID;
    }

    @Override
    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }


    @Override
    public void setAdolescentGender(String gender) {
        this.gender = gender;
    }

    @Override
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public void refreshProfileView(CommonPersonObjectClient pClient) {
        if (pClient == null || pClient.getColumnmaps() == null) {
            return;
        }
        String firstName = Utils.getValue(pClient.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true);
        String lastName = Utils.getValue(pClient.getColumnmaps(), DBConstants.KEY.LAST_NAME, true);
        String middleName = Utils.getValue(pClient.getColumnmaps(), DBConstants.KEY.MIDDLE_NAME, true);
        String adolescentName = org.smartregister.util.Utils.getName(firstName, middleName + " " + lastName);
        String dobString = org.smartregister.util.Utils.getValue(pClient.getColumnmaps(), org.smartregister.chw.referral.util.DBConstants.KEY.DOB, false);
        String age = org.smartregister.family.util.Utils.getTranslatedDate(org.smartregister.family.util.Utils.getDuration(dobString), getView().getContext());
        getView().setAdolescentNameAndAge(String.format("%s, %s", adolescentName, age));

        getView().setGender(gender);

        String villageTown = Utils.getValue(pClient.getColumnmaps(), ChildDBConstants.KEY.FAMILY_HOME_ADDRESS, true);
        getView().setVillageLocation(villageTown);

        String id = Utils.getValue(pClient.getColumnmaps(), DBConstants.KEY.UNIQUE_ID, true);
        getView().setUniqueId(id);

    }

    public String getAdolescentGender() {
        return gender;
    }

    @Override
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getFamilyID() {
        return familyID;
    }

    @Override
    public String getFamilyName() {
        return familyName;
    }

    @Override
    public void fetchVisitStatus(String baseEntityId) {

    }

    @Override
    public void fetchUpcomingServiceAndFamilyDue(String baseEntityId) {

    }

    @Override
    public AdolescentProfileContract.View getView() {
        if (view != null)
            return view.get();
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

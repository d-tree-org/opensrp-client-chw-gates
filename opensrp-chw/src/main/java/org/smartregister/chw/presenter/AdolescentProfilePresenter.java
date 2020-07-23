package org.smartregister.chw.presenter;

import android.util.Pair;

import org.apache.commons.lang3.tuple.Triple;
import org.smartregister.chw.R;
import org.smartregister.chw.contract.AdolescentProfileContract;
import org.smartregister.chw.interactor.AdolescentProfileInteractor;
import org.smartregister.chw.model.AdolescentRegisterModel;
import org.smartregister.chw.model.ChildRegisterModel;
import org.smartregister.chw.util.ChildDBConstants;
import org.smartregister.chw.util.JsonFormUtils;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.FetchStatus;
import org.smartregister.family.contract.FamilyProfileContract;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;

import java.lang.ref.WeakReference;

public class AdolescentProfilePresenter implements AdolescentProfileContract.Presenter, AdolescentProfileContract.InteractorCallBack{

    public String adolescentBaseEntityId;
    public String familyID;
    private WeakReference<AdolescentProfileContract.View> view;
    private AdolescentProfileContract.Interactor interactor;
    private String dob;
    private String familyName;
    private String gender;
    private String phoneNumber;
    private String familyHead;
    private String familyCareGiver;
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
    public void setFamilyHead(String familyHead) {
        this.familyHead = familyHead;
    }

    @Override
    public void setFamilyCareGiver(String familyCareGiver) {
        this.familyCareGiver = familyCareGiver;
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

    @Override
    public void onRegistrationSaved(boolean isEditMode) {
        if (getView() != null) {
            getView().hideProgressDialog();
            getView().refreshProfile(FetchStatus.fetched);
        }
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
    public String getFamilyHead() {
        return familyHead;
    }

    @Override
    public String getPrimaryCareGiver() {
        return familyCareGiver;
    }

    @Override
    public void updateAdolescentProfile(String jsonString) {
        getView().showProgressDialog(R.string.updating);
        Pair<Client, Event> pair = new AdolescentRegisterModel().processRegistration(jsonString);
        if (pair == null) {
            return;
        }

        interactor.saveRegistration(pair, jsonString, true, this);

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

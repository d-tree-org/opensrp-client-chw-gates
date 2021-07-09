package org.smartregister.chw.presenter;

import android.util.Pair;

import org.apache.commons.lang3.tuple.Triple;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.smartregister.chw.R;
import org.smartregister.chw.contract.AdolescentProfileContract;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.interactor.AdolescentProfileInteractor;
import org.smartregister.chw.model.AdolescentRegisterModel;
import org.smartregister.chw.model.AdolescentVisit;
import org.smartregister.chw.util.ChildDBConstants;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.FetchStatus;
import org.smartregister.domain.Task;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;

import java.lang.ref.WeakReference;
import java.util.Set;

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
    public void updateAdolescentVisit(AdolescentVisit adolescentVisit) {
        if(adolescentVisit != null) {

            if(adolescentVisit.getVisitStatus().equalsIgnoreCase(CoreConstants.VisitType.DUE.name())) {
                getView().setVisitButtonDueStatus();
            }
            if(adolescentVisit.getVisitStatus().equalsIgnoreCase(CoreConstants.VisitType.OVERDUE.name())) {
                getView().setVisitButtonOverdueStatus();
            }
            if (adolescentVisit.getVisitStatus().equalsIgnoreCase(CoreConstants.VisitType.LESS_TWENTY_FOUR.name())) {
                getView().setVisitLessTwentyFourView(adolescentVisit.getLastVisitMonthName());
            }
            if (adolescentVisit.getVisitStatus().equalsIgnoreCase(CoreConstants.VisitType.VISIT_THIS_MONTH.name())) {
                getView().setVisitAboveTwentyFourView();
            }
            if (adolescentVisit.getVisitStatus().equalsIgnoreCase(CoreConstants.VisitType.NOT_VISIT_THIS_MONTH.name())) {
                getView().setVisitNotDoneThisMonth();
            }
            if (adolescentVisit.getLastVisitTime() != 0) {
                getView().setLastVisitRowView(adolescentVisit.getLastVisitDays());
            }
            if (!adolescentVisit.getVisitStatus().equalsIgnoreCase(CoreConstants.VisitType.NOT_VISIT_THIS_MONTH.name()) && adolescentVisit.getLastVisitTime() != 0) {
                getView().enableEdit(new Period(new DateTime(adolescentVisit.getLastVisitTime()), DateTime.now()).getHours() <= 24);
            }

        }
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
        interactor.refreshAdolescentVisitBar(getView().getContext(), baseEntityId, this);
    }

    @Override
    public void fetchUpcomingServiceAndFamilyDue(String baseEntityId) {
        interactor.refreshUpcomingServicesAndFamilyDue(getView().getContext(), getFamilyID(), baseEntityId, this);
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

    @Override
    public void updateFamilyMemberServiceDue(String serviceDueStatus) {
        if (getView() != null) {

            if(serviceDueStatus.equalsIgnoreCase(CoreConstants.FamilyServiceType.DUE.name())) {
                getView().setFamilyHasServiceDue();
            } else if (serviceDueStatus.equalsIgnoreCase(CoreConstants.FamilyServiceType.OVERDUE.name())) {
                getView().setFamilyHasServiceOverdue();
            } else if (serviceDueStatus.equalsIgnoreCase(CoreConstants.FamilyServiceType.NOTHING.name())) {
                getView().setFamilyHasNothingDue();
            }

        }
    }

    @Override
    public void updateVisitNotDone(long value) {
        interactor.updateVisitNotDone(value, this);
    }

    @Override
    public void verifyFingerprint() {
        interactor.getFingerprintForVerification(adolescentBaseEntityId, this);
    }

    @Override
    public void updateVisitNotDone() {
        hideProgress();
        getView().openVisitMonthView();
    }

    @Override
    public void undoVisitNotDone() {
        hideProgress();
        getView().showUndoVisitNotDoneView();
    }

    @Override
    public void onFingerprintFetched(String fingerprint, boolean hasFingerprint, org.smartregister.domain.db.Client client) {
        if (hasFingerprint){
            getView().callFingerprintVerification(fingerprint);
        }else{
            getView().callFingerprintRegistration(client);
        }
    }
  
    @Override
    public void fetchTasks() {
        interactor.getClientTasks(CoreConstants.REFERRAL_PLAN_ID, adolescentBaseEntityId, this);
    }

    @Override
    public void setClientTasks(Set<Task> taskList) {
        if (getView() != null) {
            getView().setClientTasks(taskList);
        }
    }
}

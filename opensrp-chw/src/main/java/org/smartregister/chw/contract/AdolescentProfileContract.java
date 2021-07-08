package org.smartregister.chw.contract;

import android.content.Context;
import android.util.Pair;

import org.smartregister.chw.model.AdolescentVisit;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.FetchStatus;
import org.smartregister.domain.Task;
import org.smartregister.view.contract.BaseProfileContract;

import java.util.Set;

public interface AdolescentProfileContract extends BaseProfileContract {

    interface View extends BaseProfileContract.View{

        Context getContext();

        AdolescentProfileContract.Presenter presenter();

        void setAdolescentNameAndAge(String nameAndAge);

        void setGender(String gender);

        void setVillageLocation(String location);

        void setUniqueId(String uniqueId);

        void refreshProfile(final FetchStatus fetchStatus);

        void setOverDueColor();

        void enableEdit(boolean enable);

        void setVisitButtonDueStatus();

        void setVisitButtonOverdueStatus();


        void setVisitLessTwentyFourView(String monthName);

        void setVisitAboveTwentyFourView();

        void setVisitNotDoneThisMonth();

        void setLastVisitRowView(String days);

        void setFamilyHasNothingDue();

        void setFamilyHasServiceDue();

        void setFamilyHasServiceOverdue();

        void openVisitMonthView();

        void openMedicalHistory();

        void openUpcomingService();

        void openFamilyDueServices();

        void showUndoVisitNotDoneView();

        void showProgressBar(boolean status);

        void hideView();

        void setClientTasks(Set<Task> taskList);

    }

    interface Presenter extends BaseProfileContract.Presenter {

        AdolescentProfileContract.View getView();

        void fetchProfileData();

        String getAdolescentGender();

        String getPhoneNumber();

        String getFamilyID();

        String getFamilyName();

        String getFamilyHead();

        String getPrimaryCareGiver();

        void updateAdolescentProfile(String jsonString);

        void fetchVisitStatus(String baseEntityId);

        void fetchUpcomingServiceAndFamilyDue(String baseEntityId);

        void updateVisitNotDone(long value);

        void fetchTasks();

    }
    interface Model{}
    interface Interactor{

        void updateVisitNotDone(long value, AdolescentProfileContract.InteractorCallBack callBack);

        void refreshProfileInfo(String baseEntityId, AdolescentProfileContract.InteractorCallBack callback);

        void refreshAdolescentVisitBar(Context context, String baseEntityId, AdolescentProfileContract.InteractorCallBack interactorCallBack);

        void refreshUpcomingServicesAndFamilyDue(Context context, String familyId, String baseEntityId, AdolescentProfileContract.InteractorCallBack callBack);

        CommonPersonObjectClient getcommonPersonObjectClient();

        void saveRegistration(final Pair<Client, Event> pair, String jsonString, final boolean isEditMode, final AdolescentProfileContract.InteractorCallBack callBack);

        void getClientTasks(String planId, String baseEntityId, AdolescentProfileContract.InteractorCallBack callback);
    }
    interface InteractorCallBack extends BaseProfileContract{

        void updateAdolescentVisit(AdolescentVisit adolescentVisit);

        void updateFamilyMemberServiceDue(String serviceDueStatus);

        void setFamilyID(String familyID);

        void setFamilyName(String familyName);

        void setAdolescentGender(String gender);

        void setPhoneNumber(String phoneNumber);

        void setFamilyHead(String familyHead);

        void setFamilyCareGiver(String familyCareGiver);

        void refreshProfileView(CommonPersonObjectClient pClient);

        void onRegistrationSaved(boolean isEditMode);

        void hideProgress();

        void updateVisitNotDone();

        void undoVisitNotDone();

        void setClientTasks(Set<Task> taskList);
    }

}

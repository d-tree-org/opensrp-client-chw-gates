package org.smartregister.chw.contract;

import android.content.Context;
import android.util.Pair;

import org.smartregister.chw.malaria.contract.MalariaProfileContract;
import org.smartregister.chw.malaria.domain.MemberObject;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.FetchStatus;
import org.smartregister.view.contract.BaseProfileContract;

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

        void openMedicalHistory();

        void openUpcomingService();

        void openFamilyDueServices();

        void showProgressBar(boolean status);

        void hideView();

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
    }
    interface Model{}
    interface Interactor{

        void refreshProfileInfo(String baseEntityId, AdolescentProfileContract.InteractorCallBack callback);

        CommonPersonObjectClient getcommonPersonObjectClient();

        void saveRegistration(final Pair<Client, Event> pair, String jsonString, final boolean isEditMode, final AdolescentProfileContract.InteractorCallBack callBack);

    }
    interface InteractorCallBack extends BaseProfileContract{

        void setFamilyID(String familyID);

        void setFamilyName(String familyName);

        void setAdolescentGender(String gender);

        void setPhoneNumber(String phoneNumber);

        void setFamilyHead(String familyHead);

        void setFamilyCareGiver(String familyCareGiver);

        void refreshProfileView(CommonPersonObjectClient pClient);

        void onRegistrationSaved(boolean isEditMode);

        void hideProgress();
    }

}

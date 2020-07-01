package org.smartregister.chw.contract;

import org.smartregister.chw.malaria.contract.MalariaProfileContract;
import org.smartregister.chw.malaria.domain.MemberObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.view.contract.BaseProfileContract;

public interface AdolescentProfileContract {

    interface View extends BaseProfileContract.View{

        AdolescentProfileContract.Presenter presenter();

        void setAdolescentNameAndAge(String nameAndAge);

        void setGender(String gender);

        void setVillageLocation(String location);

        void setUniqueId(String uniqueId);

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


        void fetchVisitStatus(String baseEntityId);

        void fetchUpcomingServiceAndFamilyDue(String baseEntityId);
    }
    interface Model{}
    interface Interactor{

        void refreshProfileInfo(String baseEntityId, AdolescentProfileContract.InteractorCallBack callback);

        void saveRegistration(String jsonString, final AdolescentProfileContract.InteractorCallBack callBack);

    }
    interface InteractorCallBack{

        void refreshProfileView(CommonPersonObjectClient pClient);

        void hideProgress();
    }

}

package org.smartregister.chw.contract;

public interface AdolescentProfileContract {

    interface View extends InteractorCallBack{

        void setProfileViewWithData();

        void setOverDueColor();

        void openMedicalHistory();

        void openUpcomingService();

        void openFamilyDueServices();

        void showProgressBar(boolean status);

        void hideView();

    }

    interface Presenter{}
    interface Model{}
    interface Interactor{}
    interface InteractorCallBack{}

}

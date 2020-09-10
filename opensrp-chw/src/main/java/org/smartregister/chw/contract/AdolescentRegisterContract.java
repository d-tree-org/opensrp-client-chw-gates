package org.smartregister.chw.contract;

import android.util.Pair;

import org.json.JSONObject;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.view.contract.BaseRegisterContract;

public interface AdolescentRegisterContract {

    interface View extends BaseRegisterContract.View {
        Presenter presenter();
    }

    interface Presenter extends BaseRegisterContract.Presenter {

        void startForm(String formName, String entityId, String metadata, String currentLocationId) throws Exception;

        void saveForm(String jsonString);

    }

    interface Model {
        JSONObject getFormAsJson(String formName, String entityId, String currentLocationId) throws Exception;

        Pair<Client, Event> processRegistration(String jsonString);
    }

    interface Interactor {

    }

    interface InteractorCallBack {

    }

}

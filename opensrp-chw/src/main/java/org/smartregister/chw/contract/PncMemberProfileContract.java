package org.smartregister.chw.contract;

import org.json.JSONObject;
import org.smartregister.chw.anc.contract.BaseAncMemberProfileContract;
import org.smartregister.chw.model.ReferralTypeModel;
import org.smartregister.chw.pnc.contract.BasePncMemberProfileContract;
import org.smartregister.domain.Task;
import org.smartregister.domain.db.Client;
import org.smartregister.repository.AllSharedPreferences;

import java.util.List;
import java.util.Set;

public interface PncMemberProfileContract {

    interface View extends BasePncMemberProfileContract.View {
        void startFormActivity(JSONObject formJson);

        List<ReferralTypeModel> getReferralTypeModels();

        void onPregnancyOutcomeUpdated(boolean successful);

        void setClientTasks(Set<Task> taskList);

        void callFingerprintVerification(String fingerprintId);

        void callFingerprintRegistration(Client client);

    }

    interface Presenter extends BasePncMemberProfileContract.Presenter {
        void startPncReferralForm();

        void verifyFingerprint();

        void referToFacility();

        void saveForm(String json, String table);

        void createReferralEvent(AllSharedPreferences allSharedPreferences, String jsonString) throws Exception;

        void fetchTasks();
    }

    interface Interactor extends BasePncMemberProfileContract.Interactor, BaseAncMemberProfileContract.Interactor {
        void createReferralEvent(AllSharedPreferences allSharedPreferences, String jsonString, String entityID) throws Exception;

        void updatePregnancyOutcome(final String jsonString, final PncMemberProfileContract.InteractorCallBack callBack, final String table);

        void getClientTasks(String planId, String baseEntityId, PncMemberProfileContract.InteractorCallBack callback);

        void getFingerprintForVerification(String baseEntityId, InteractorCallBack callBack);

    }

    interface InteractorCallBack {
        void onSaved(boolean result);

        void setClientTasks(Set<Task> taskList);

        void onFingerprintFetched(String fingerprint, boolean hasFingerprint, Client client);
    }

}

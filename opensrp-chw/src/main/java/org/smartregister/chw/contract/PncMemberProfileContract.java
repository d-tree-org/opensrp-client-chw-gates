package org.smartregister.chw.contract;

import org.json.JSONObject;
import org.smartregister.chw.anc.contract.BaseAncMemberProfileContract;
import org.smartregister.chw.model.ReferralTypeModel;
import org.smartregister.chw.pnc.contract.BasePncMemberProfileContract;
import org.smartregister.domain.Task;
import org.smartregister.repository.AllSharedPreferences;

import java.util.List;
import java.util.Set;

public interface PncMemberProfileContract {

    interface View extends BasePncMemberProfileContract.View {
        void startFormActivity(JSONObject formJson);

        List<ReferralTypeModel> getReferralTypeModels();

        void onPregnancyOutcomeUpdated(boolean successful);

        void setClientTasks(Set<Task> taskList);
    }

    interface Presenter extends BasePncMemberProfileContract.Presenter {
        void startPncReferralForm();

        void referToFacility();

        void saveForm(String json, String table);

        void createReferralEvent(AllSharedPreferences allSharedPreferences, String jsonString) throws Exception;

        void fetchTasks();
    }

    interface Interactor extends BasePncMemberProfileContract.Interactor, BaseAncMemberProfileContract.Interactor {
        void createReferralEvent(AllSharedPreferences allSharedPreferences, String jsonString, String entityID) throws Exception;

        void updatePregnancyOutcome(final String jsonString, final PncMemberProfileContract.InteractorCallBack callBack, final String table);

        void getClientTasks(String planId, String baseEntityId, PncMemberProfileContract.InteractorCallBack callback);
    }

    interface InteractorCallBack {
        void onSaved(boolean result);

        void setClientTasks(Set<Task> taskList);
    }

}

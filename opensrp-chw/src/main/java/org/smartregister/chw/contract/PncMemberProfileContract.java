package org.smartregister.chw.contract;

import org.json.JSONObject;
import org.smartregister.chw.anc.contract.BaseAncMemberProfileContract;
import org.smartregister.chw.anc.contract.BaseAncRegisterContract;
import org.smartregister.chw.model.ReferralTypeModel;
import org.smartregister.chw.pnc.contract.BasePncMemberProfileContract;
import org.smartregister.repository.AllSharedPreferences;

import java.util.List;

public interface PncMemberProfileContract {

    interface View extends BasePncMemberProfileContract.View {
        void startFormActivity(JSONObject formJson);

        List<ReferralTypeModel> getReferralTypeModels();

        void onPregnancyOutcomeUpdated(boolean successful);

    }

    interface Presenter extends BasePncMemberProfileContract.Presenter {
        void startPncReferralForm();

        void referToFacility();

        void saveForm(String json, String table);

        void createReferralEvent(AllSharedPreferences allSharedPreferences, String jsonString) throws Exception;
    }

    interface Interactor extends BasePncMemberProfileContract.Interactor, BaseAncMemberProfileContract.Interactor {
        void createReferralEvent(AllSharedPreferences allSharedPreferences, String jsonString, String entityID) throws Exception;

        void updatePregnancyOutcome(final String jsonString, final PncMemberProfileContract.InteractorCallBack callBack, final String table);

    }

    interface InteractorCallBack {
        void onSaved(boolean result);
    }

}

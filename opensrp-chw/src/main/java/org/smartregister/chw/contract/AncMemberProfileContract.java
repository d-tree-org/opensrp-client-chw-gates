package org.smartregister.chw.contract;

import org.smartregister.chw.model.ReferralTypeModel;
import org.smartregister.domain.db.Client;

import java.util.List;

public interface AncMemberProfileContract extends org.smartregister.chw.core.contract.AncMemberProfileContract {

    interface Interactor  {
        void getFingerprintForVerification(String baseEntityId, InteractorCallback callBack);
    }

    interface InteractorCallback {
        void onFingerprintFetched(String fingerprint, boolean hasFingerprint, Client client);
    }

    interface Presenter{
        void referToFacility();

        void verifyFingerprint();
    }

    interface View{
        List<ReferralTypeModel> getReferralTypeModels();

        void callFingerprintVerification(String fingerprintId);

        void callFingerprintRegistration(Client client);

    }
}

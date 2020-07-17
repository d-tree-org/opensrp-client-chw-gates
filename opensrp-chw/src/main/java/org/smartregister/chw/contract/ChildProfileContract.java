package org.smartregister.chw.contract;

import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.db.Client;

public interface ChildProfileContract extends org.smartregister.chw.core.contract.CoreChildProfileContract {

    public interface Presenter {
        void verifyChildFingerprint();
    }

    public interface Interactor {
        void getChildFingerprintForVerification(String baseEntityId, InteractorCallback callback);
    }

    public interface InteractorCallback {
        void callFingerprintVerification(String clientFingerprint);
        void callFingerprintEnrollment(Client client);
    }

    public interface View {
        void callFingerprintRegister(Client client);

        void callFingerprintVerification(String fingerprintId);
    }

}

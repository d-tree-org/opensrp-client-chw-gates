package org.smartregister.chw.contract;

import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.db.Client;

public interface ChildProfileContract extends org.smartregister.chw.core.contract.CoreChildProfileContract {

    public interface Presenter {
        void verifyChildFingerprint();
        void getChildVerifyFingerprintPermission(String childBaseEntityId);
    }

    public interface Interactor {
        void getChildFingerprintForVerification(String baseEntityId, InteractorCallback callback);
        void verifyChildFingerprintPermission(String baseEntityId, InteractorCallback callback);
    }

    public interface InteractorCallback {
        void callFingerprintVerification(String clientFingerprint);
        void callFingerprintEnrollment(Client client);
        void setVerifyFingerprintPermission(boolean permission);
    }

    public interface View {
        void callFingerprintRegister(Client client);

        void callFingerprintVerification(String fingerprintId);

        void updateChildFingerprintVerificationPermission(boolean permission);
    }

}

package org.smartregister.chw.interactor;

import androidx.annotation.VisibleForTesting;

import org.smartregister.chw.contract.AdolescentRegisterContract;
import org.smartregister.family.util.AppExecutors;


public class AdolescentRegisterInteractor implements AdolescentRegisterContract.Interactor {

    private AppExecutors appExecutors;

    @VisibleForTesting
    public AdolescentRegisterInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
    }

    public AdolescentRegisterInteractor() { this(new AppExecutors()); }


}

package org.smartregister.chw.interactor;

import android.database.Cursor;

import org.smartregister.chw.contract.AdolescentProfileContract;
import org.smartregister.chw.core.utils.CoreChildUtils;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.chw.malaria.domain.MemberObject;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.family.util.AppExecutors;

import timber.log.Timber;

public class AdolescentProfileInteractor implements AdolescentProfileContract.Interactor {

    private CommonPersonObjectClient pClient;

    protected AppExecutors appExecutors;


    public AdolescentProfileInteractor() {
        this(new AppExecutors());
    }

    AdolescentProfileInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
    }

    public CommonRepository getCommonRepository(String tableName) {
        return Utils.context().commonrepository(tableName);
    }

    @Override
    public void refreshProfileInfo(String baseEntityId, AdolescentProfileContract.InteractorCallBack callback) {

        Runnable runnable = () -> {
            String query = CoreChildUtils.mainSelect("ec_adolescent", CoreConstants.TABLE_NAME.FAMILY, CoreConstants.TABLE_NAME.FAMILY_MEMBER, baseEntityId);

            Cursor cursor = null;

            try {
                cursor = getCommonRepository("ec_adolescent").rawCustomQueryForAdapter(query);
                if (cursor != null && cursor.moveToFirst()) {
                    CommonPersonObject personObject = getCommonRepository("ec_adolescent").readAllcommonforCursorAdapter(cursor);
                    pClient = new CommonPersonObjectClient(personObject.getCaseId(),
                            personObject.getDetails(), "");
                    pClient.setColumnmaps(personObject.getColumnmaps());

                    appExecutors.mainThread().execute(() -> {
                        callback.refreshProfileView(pClient);
                    });
                }

            } catch (Exception e) {
                Timber.e(e, "AdolescentProfileInteractor --> refreshProfileInfo");
            }

        };

    }

    @Override
    public void saveRegistration(String jsonString, AdolescentProfileContract.InteractorCallBack callBack) {

    }
}

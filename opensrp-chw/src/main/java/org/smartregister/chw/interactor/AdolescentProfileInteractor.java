package org.smartregister.chw.interactor;

import android.database.Cursor;

import org.smartregister.chw.contract.AdolescentProfileContract;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.core.utils.CoreChildUtils;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.chw.malaria.domain.MemberObject;
import org.smartregister.chw.util.AdolescentUtils;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.family.util.DBConstants;

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
    public void refreshProfileInfo(String baseEntityId, final AdolescentProfileContract.InteractorCallBack callback) {

        Runnable runnable = () -> {
            String query = AdolescentUtils.mainSelect(CoreConstants.TABLE_NAME.ADOLESCENT, CoreConstants.TABLE_NAME.FAMILY,
                    CoreConstants.TABLE_NAME.FAMILY_MEMBER, baseEntityId);

            Cursor cursor = null;

            try {
                cursor = getCommonRepository(CoreConstants.TABLE_NAME.ADOLESCENT).rawCustomQueryForAdapter(query);
                if (cursor != null && cursor.moveToFirst()) {
                    CommonPersonObject personObject = getCommonRepository(CoreConstants.TABLE_NAME.ADOLESCENT).readAllcommonforCursorAdapter(cursor);
                    pClient = new CommonPersonObjectClient(personObject.getCaseId(),
                            personObject.getDetails(), "");
                    pClient.setColumnmaps(personObject.getColumnmaps());

                    final String familyID = Utils.getValue(pClient.getColumnmaps(), ChildDBConstants.KEY.RELATIONAL_ID, false);
                    final String gender = org.smartregister.family.util.Utils.getValue(pClient.getColumnmaps(), DBConstants.KEY.GENDER, true);

                    final CommonPersonObject familyPersonObject = getCommonRepository(Utils.metadata().familyRegister.tableName).findByBaseEntityId(familyID);
                    final CommonPersonObjectClient client = new CommonPersonObjectClient(familyPersonObject.getCaseId(), familyPersonObject.getDetails(), "");
                    client.setColumnmaps(familyPersonObject.getColumnmaps());

                    final String familyName = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.FIRST_NAME, false);
                    final String phoneNumber = org.smartregister.family.util.Utils.getValue(pClient.getColumnmaps(), org.smartregister.chw.util.ChildDBConstants.KEY.FAMILY_MEMBER_PHONENUMBER, true);

                    appExecutors.mainThread().execute(() -> {
                        callback.setAdolescentGender(gender);
                        callback.setFamilyID(familyID);
                        callback.setFamilyName(familyName);
                        callback.setPhoneNumber(phoneNumber);
                        callback.refreshProfileView(pClient);
                        callback.hideProgress();
                    });
                }

            } catch (Exception e) {
                Timber.e(e, "AdolescentProfileInteractor --> refreshProfileInfo");
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

        };

        appExecutors.diskIO().execute(runnable);

    }

    @Override
    public void saveRegistration(String jsonString, AdolescentProfileContract.InteractorCallBack callBack) {

    }

    public CommonPersonObjectClient getcommonPersonObjectClient() {
        return pClient;
    }
}

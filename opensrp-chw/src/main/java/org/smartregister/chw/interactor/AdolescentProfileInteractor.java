package org.smartregister.chw.interactor;

import android.database.Cursor;
import android.util.Pair;

import org.json.JSONObject;
import org.smartregister.chw.contract.AdolescentProfileContract;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.core.utils.CoreChildUtils;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.chw.malaria.domain.MemberObject;
import org.smartregister.chw.util.AdolescentUtils;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.UniqueIdRepository;
import org.smartregister.sync.ClientProcessorForJava;
import org.smartregister.sync.helper.ECSyncHelper;

import java.util.Date;

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
                    final String familyHead = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.FAMILY_HEAD, false);
                    final String familyCareGiver = Utils.getValue(client.getColumnmaps(), DBConstants.KEY.PRIMARY_CAREGIVER, false);
                    appExecutors.mainThread().execute(() -> {
                        callback.setAdolescentGender(gender);
                        callback.setFamilyID(familyID);
                        callback.setFamilyName(familyName);
                        callback.setPhoneNumber(phoneNumber);
                        callback.setFamilyHead(familyHead);
                        callback.setFamilyCareGiver(familyCareGiver);
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
    public void saveRegistration(final Pair<Client, Event> pair, String jsonString, final boolean isEditMode, AdolescentProfileContract.InteractorCallBack callBack) {

        Runnable runnable = () -> {
            saveRegistration(pair, jsonString, isEditMode);
            appExecutors.mainThread().execute(() -> {
                if (callBack != null) {
                    callBack.onRegistrationSaved(isEditMode);
                }
            });
        };

        appExecutors.diskIO().execute(runnable);
    }

    private void saveRegistration(Pair<Client, Event> pair, String jsonString, boolean isEditMode) {
        try {

            Client baseClient = pair.first;
            Event baseEvent = pair.second;

            if (baseClient != null) {
                JSONObject clientJson = new JSONObject(JsonFormUtils.gson.toJson(baseClient));
                if (isEditMode) {
                    JsonFormUtils.mergeAndSaveClient(getSyncHelper(), baseClient);
                } else {
                    getSyncHelper().addClient(baseClient.getBaseEntityId(), clientJson);
                }
            }

            if (baseEvent != null) {
                JSONObject eventJson = new JSONObject(JsonFormUtils.gson.toJson(baseEvent));
                getSyncHelper().addEvent(baseEvent.getBaseEntityId(), eventJson);
            }

            if (!isEditMode && baseClient != null) {
                String opensrpId = baseClient.getIdentifier(Utils.metadata().uniqueIdentifierKey);
                //mark OPENSRP ID as used
                getUniqueIdRepository().close(opensrpId);
            }

            if (baseClient != null || baseEvent != null) {
                String imageLocation = JsonFormUtils.getFieldValue(jsonString, org.smartregister.family.util.Constants.KEY.PHOTO);
                JsonFormUtils.saveImage(baseEvent.getProviderId(), baseClient.getBaseEntityId(), imageLocation);
            }

            long lastSyncTimeStamp = getAllSharedPreferences().fetchLastUpdatedAtDate(0);
            Date lastSyncDate = new Date(lastSyncTimeStamp);
            getClientProcessorForJava().processClient(getSyncHelper().getEvents(lastSyncDate, BaseRepository.TYPE_Unprocessed));
            getAllSharedPreferences().saveLastUpdatedAtDate(lastSyncDate.getTime());
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    public ECSyncHelper getSyncHelper() {
        return FamilyLibrary.getInstance().getEcSyncHelper();
    }

    public UniqueIdRepository getUniqueIdRepository() {
        return FamilyLibrary.getInstance().getUniqueIdRepository();
    }

    public AllSharedPreferences getAllSharedPreferences() {
        return Utils.context().allSharedPreferences();
    }

    public ClientProcessorForJava getClientProcessorForJava() {
        return FamilyLibrary.getInstance().getClientProcessorForJava();
    }

    public CommonPersonObjectClient getcommonPersonObjectClient() {
        return pClient;
    }
}

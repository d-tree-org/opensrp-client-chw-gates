package org.smartregister.chw.interactor;

import android.content.Context;
import android.database.Cursor;
import android.util.Pair;

import org.json.JSONObject;
import org.smartregister.chw.contract.AdolescentProfileContract;
import org.smartregister.chw.model.AdolescentVisit;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.chw.schedulers.ChwScheduleTaskExecutor;
import org.smartregister.chw.util.AdolescentHomeVisit;
import org.smartregister.chw.util.AdolescentUtils;
import org.smartregister.chw.util.Constants;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
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
    public void refreshAdolescentVisitBar(Context context, String baseEntityId, AdolescentProfileContract.InteractorCallBack interactorCallBack) {
        if (pClient == null) {
            return;
        }
        AdolescentHomeVisit adolescentHomeVisit = AdolescentUtils.getAdolescentLastHomeVisit(baseEntityId);

        String dobString = org.smartregister.chw.util.Utils.getValue(pClient.getColumnmaps(), DBConstants.KEY.DOB, false);

        // Simulate the last visit date to check if all conditions work
        /*SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.getDefault());
        String dateString = "13-08-2020 01:10:32";
        Date simulatedDate = new Date();

        try {
            simulatedDate = sdf.parse(dateString);
        } catch (ParseException e) {
            Timber.e(e);
        }*/

        final AdolescentVisit adolescentVisit = AdolescentUtils.getAdolescentVisitStatus(context, dobString, adolescentHomeVisit.getLastHomeVisitDate(),
                adolescentHomeVisit.getVisitNotDoneDate(), adolescentHomeVisit.getDateCreated());
        Runnable runnable = () -> appExecutors.mainThread().execute(() -> interactorCallBack.updateAdolescentVisit(adolescentVisit));
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void refreshUpcomingServicesAndFamilyDue(Context context, String familyId, String baseEntityId, AdolescentProfileContract.InteractorCallBack callBack) {
        if (pClient == null) {
            return;
        }
        updateFamilyDueStatus(context, familyId, baseEntityId, callBack);

    }

    private void updateFamilyDueStatus(Context context, String familyId, String baseEntityId, final AdolescentProfileContract.InteractorCallBack callback) {
        FamilyInteractor familyInteractor = new FamilyInteractor();
        familyInteractor.updateFamilyDueStatus(context, baseEntityId, familyId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {
                        callback.updateFamilyMemberServiceDue(s);
                    }

                    @Override
                    public void onError(Throwable e) {
                        callback.hideProgress();
                    }

                    @Override
                    public void onComplete() {
                        callback.hideProgress();
                    }
                });
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

    @Override
    public void updateVisitNotDone(long value, final AdolescentProfileContract.InteractorCallBack callBack) {
        updateHomeVisitAsEvent(value)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(Object o) {
                        if (value == 0) {
                            callBack.undoVisitNotDone();
                        } else {
                            callBack.updateVisitNotDone();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        callBack.hideProgress();

                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private Observable<Object> updateHomeVisitAsEvent(final long value) {
        return Observable.create(objectObservableEmitter -> {
            if (value == 0) {
                AdolescentUtils.undoVisitNotDone(getcommonPersonObjectClient().entityId());
            } else {
                AdolescentUtils.visitNotDone(getcommonPersonObjectClient().entityId());
            }
            ChwScheduleTaskExecutor.getInstance().execute(getcommonPersonObjectClient().entityId(), Constants.ADOLESCENT_HOME_VISIT_NOT_DONE, new Date());
            objectObservableEmitter.onNext("");
        });
    }
}

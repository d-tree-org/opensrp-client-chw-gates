package org.smartregister.chw.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.location.Location;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.chw.R;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.contract.AncMemberProfileContract;
import org.smartregister.chw.core.activity.CoreAncMemberProfileActivity;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.dao.MalariaDao;
import org.smartregister.chw.dataloader.AncMemberDataLoader;
import org.smartregister.chw.dataloader.FamilyMemberDataLoader;
import org.smartregister.chw.form_data.NativeFormsDataBinder;
import org.smartregister.chw.interactor.AncMemberProfileInteractor;
import org.smartregister.chw.model.FamilyProfileModel;
import org.smartregister.chw.model.ReferralTypeModel;
import org.smartregister.chw.presenter.AncMemberProfilePresenter;
import org.smartregister.chw.schedulers.ChwScheduleTaskExecutor;
import org.smartregister.chw.util.VisitLocationUtils;
import org.smartregister.chw.viewmodel.VisitLocationViewModel;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.AllCommonsRepository;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.domain.Task;
import org.smartregister.domain.db.Client;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.family.interactor.FamilyProfileInteractor;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.simprint.SimPrintsConstantHelper;
import org.smartregister.simprint.SimPrintsRegisterActivity;
import org.smartregister.simprint.SimPrintsRegistration;
import org.smartregister.simprint.SimPrintsVerification;
import org.smartregister.simprint.SimPrintsVerifyActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class AncMemberProfileActivity extends CoreAncMemberProfileActivity implements AncMemberProfileContract.View {

    private static final int VERIFY_RESULT_CODE = 8379;
    private static final int REGISTER_RESULT_CODE = 5361;

    org.smartregister.domain.db.Client currentClient;

    private List<ReferralTypeModel> referralTypeModels = new ArrayList<>();

    //Visit location variables
    private VisitLocationViewModel visitLocationViewModel;
    private Location visitLocation;

    public static void startMe(Activity activity, String baseEntityID) {
        Intent intent = new Intent(activity, AncMemberProfileActivity.class);
        intent.putExtra(Constants.ANC_MEMBER_OBJECTS.BASE_ENTITY_ID, baseEntityID);
        activity.startActivity(intent);
    }

    @Override
    public void setupViews() {
        super.setupViews();
        TextView verifyFingerprint = findViewById(R.id.textview_verify_fingerprint);
        verifyFingerprint.setVisibility(View.GONE);
        //verifyFingerprint.setOnClickListener(this);
        TextView verifyFingerprintAlt = findViewById(R.id.textview_verify_fingerprint_alt);
        verifyFingerprintAlt.setVisibility(View.GONE);
        //verifyFingerprintAlt.setOnClickListener(this);
    }

    @Override
    protected void onCreation() {
        super.onCreation();
        if (((ChwApplication) ChwApplication.getInstance()).hasReferrals()) {
            addAncReferralTypes();
        }

        captureCurrentLocation();

    }

    private void captureCurrentLocation(){
        visitLocationViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(VisitLocationViewModel.class);
        visitLocationViewModel.getLocationLiveData().observe(this, new androidx.lifecycle.Observer<Location>() {
            @Override
            public void onChanged(Location location) {
                if (location != null){
                    VisitLocationUtils.updateLocationInPreference(location);
                }
            }
        });
    }

    @Override
    public void initializeFloatingMenu() {
        // menu not needed
    }

    @Override
    protected void onResumption() {
        super.onResumption();
        ancMemberProfilePresenter().fetchTasks();
    }

    private void addAncReferralTypes() {
        referralTypeModels.add(new ReferralTypeModel(getString(R.string.anc_danger_signs),
                org.smartregister.chw.util.Constants.JSON_FORM.getAncReferralForm()));
        if (MalariaDao.isRegisteredForMalaria(baseEntityID)) {
            referralTypeModels.add(new ReferralTypeModel(getString(R.string.client_malaria_follow_up), null));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_remove_member) {
            CommonRepository commonRepository = Utils.context().commonrepository(Utils.metadata().familyMemberRegister.tableName);

            final CommonPersonObject commonPersonObject = commonRepository.findByBaseEntityId(memberObject.getBaseEntityId());
            final CommonPersonObjectClient client =
                    new CommonPersonObjectClient(commonPersonObject.getCaseId(), commonPersonObject.getDetails(), "");
            client.setColumnmaps(commonPersonObject.getColumnmaps());

            IndividualProfileRemoveActivity.startIndividualProfileActivity(AncMemberProfileActivity.this, client, memberObject.getFamilyBaseEntityId(), memberObject.getFamilyHead(), memberObject.getPrimaryCareGiver(), AncRegisterActivity.class.getCanonicalName());
            return true;
        } else if (itemId == R.id.action_pregnancy_out_come) {
            PncRegisterActivity.startPncRegistrationActivity(AncMemberProfileActivity.this, memberObject.getBaseEntityId(), null, CoreConstants.JSON_FORM.getPregnancyOutcome(), AncLibrary.getInstance().getUniqueIdRepository().getNextUniqueId().getOpenmrsId(), memberObject.getFamilyBaseEntityId(), memberObject.getFamilyName());
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.anc_danger_signs_outcome).setVisible(false);
        menu.findItem(R.id.action_malaria_diagnosis).setVisible(false);
        return true;
    }

    @Override // to chw
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        assert data != null;
        String mJsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
        String json = "";
        if (visitLocation != null){
            json = VisitLocationUtils.updateWithCurrentGpsLocation(mJsonString);
        }else{
            json = mJsonString;
        }
        data.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, json);

        //Call super method with updated data
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) return;

        if (requestCode == JsonFormUtils.REQUEST_CODE_GET_JSON) {
            try {
                String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
                JSONObject form = new JSONObject(jsonString);

                //Update Family Member
                if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Utils.metadata().familyMemberRegister.updateEventType)) {
                    FamilyEventClient familyEventClient =
                            new FamilyProfileModel(memberObject.getFamilyName()).processUpdateMemberRegistration(jsonString, memberObject.getBaseEntityId());
                    new FamilyProfileInteractor().saveRegistration(familyEventClient, jsonString, true, ancMemberProfilePresenter());
                }
                //Update ANC Registration Information
                else if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(CoreConstants.EventType.UPDATE_ANC_REGISTRATION)) {
                    AllSharedPreferences allSharedPreferences = org.smartregister.util.Utils.getAllSharedPreferences();
                    Event baseEvent = org.smartregister.chw.anc.util.JsonFormUtils.processJsonForm(allSharedPreferences, jsonString, Constants.TABLES.ANC_MEMBERS);
                    NCUtils.processEvent(baseEvent.getBaseEntityId(), new JSONObject(org.smartregister.chw.anc.util.JsonFormUtils.gson.toJson(baseEvent)));
                    AllCommonsRepository commonsRepository = CoreChwApplication.getInstance().getAllCommonsRepository(CoreConstants.TABLE_NAME.ANC_MEMBER);

                    JSONArray field = org.smartregister.util.JsonFormUtils.fields(form);
                    JSONObject phoneNumberObject = org.smartregister.util.JsonFormUtils.getFieldJSONObject(field, DBConstants.KEY.PHONE_NUMBER);
                    String phoneNumber = phoneNumberObject.getString(CoreJsonFormUtils.VALUE);
                    String baseEntityId = baseEvent.getBaseEntityId();
                    if (commonsRepository != null) {
                        ContentValues values = new ContentValues();
                        values.put(DBConstants.KEY.PHONE_NUMBER, phoneNumber);
                        CoreChwApplication.getInstance().getRepository().getWritableDatabase().update(CoreConstants.TABLE_NAME.ANC_MEMBER, values, DBConstants.KEY.BASE_ENTITY_ID + " = ?  ", new String[]{baseEntityId});
                    }

                }
                // ANC Referral
                else if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(CoreConstants.EventType.ANC_REFERRAL)) {
                    ancMemberProfilePresenter().createReferralEvent(Utils.getAllSharedPreferences(), jsonString);
                    showToast(this.getString(R.string.referral_submitted));
                }

            } catch (Exception e) {
                Timber.e(e, "AncMemberProfileActivity -- > onActivityResult");
            }
        } else if (requestCode == Constants.REQUEST_CODE_HOME_VISIT) {
            refreshViewOnHomeVisitResult();
        } else if (requestCode == CoreConstants.ProfileActivityResults.CHANGE_COMPLETED) {
            ChwScheduleTaskExecutor.getInstance().execute(memberObject.getBaseEntityId(), CoreConstants.EventType.ANC_HOME_VISIT, new Date());
            finish();
        } else if (requestCode == VERIFY_RESULT_CODE){
            if (data.getExtras() != null){
                SimPrintsVerification simprintsVerification = (SimPrintsVerification) data.getSerializableExtra(SimPrintsConstantHelper.INTENT_DATA);
                assert simprintsVerification != null;
                if (simprintsVerification.getCheckStatus()){
                    switch (simprintsVerification.getMaskedTier()){
                        case TIER_3:
                        case TIER_2:
                        case TIER_1:
                            showSnackBar(this.getResources().getString(R.string.fingerprint_matched));
                            break;
                        default:
                            showSnackBar(this.getResources().getString(R.string.fingerprint_did_not_match));
                    }
                }else{
                    showSnackBar(this.getResources().getString(R.string.fingerprint_verification_terminated));
                }
            }
        }else if (requestCode == REGISTER_RESULT_CODE){
            if (data.getExtras() != null){

                SimPrintsRegistration simPrintsRegistration = (SimPrintsRegistration) data.getSerializableExtra(SimPrintsConstantHelper.INTENT_DATA);
                assert simPrintsRegistration != null;
                if (simPrintsRegistration.getCheckStatus()){
                    String guid = simPrintsRegistration.getGuid();

                    if (!guid.isEmpty()){

                        Map<String, String> identifier = new HashMap<>();
                        identifier.put("simprints_guid", guid);

                        currentClient.setIdentifiers(identifier);

                        JSONObject object = CoreLibrary.getInstance().context().getEventClientRepository().convertToJson(currentClient);

                        CoreLibrary.getInstance().context().getEventClientRepository().addorUpdateClient(currentClient.getBaseEntityId(), object);

                        showSnackBar(this.getResources().getString(R.string.fingerprint_enrolled));
                    }
                }
            }
        }
    }

    private void showSnackBar(String message){
        View parentLayout = findViewById(android.R.id.content);
        Snackbar.make(parentLayout, message, Snackbar.LENGTH_INDEFINITE)
                .setAction("CLOSE", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                })
                .setActionTextColor(getResources().getColor(android.R.color.holo_red_light ))
                .show();
    }

    @Override
    public void startFormForEdit(Integer title_resource, String formName) {
        try {
            JSONObject form = null;
            boolean isPrimaryCareGiver = memberObject.getPrimaryCareGiver().equals(memberObject.getBaseEntityId());
            String titleString = title_resource != null ? getResources().getString(title_resource) : null;

            if (formName.equals(CoreConstants.JSON_FORM.getAncRegistration())) {

                NativeFormsDataBinder binder = new NativeFormsDataBinder(this, memberObject.getBaseEntityId());
                binder.setDataLoader(new AncMemberDataLoader(titleString));
                form = binder.getPrePopulatedForm(formName);

            } else if (formName.equals(CoreConstants.JSON_FORM.getFamilyMemberRegister())) {

                String eventName = org.smartregister.chw.util.Utils.metadata().familyMemberRegister.updateEventType;

                NativeFormsDataBinder binder = new NativeFormsDataBinder(this, memberObject.getBaseEntityId());
                binder.setDataLoader(new FamilyMemberDataLoader(memberObject.getFamilyName(), isPrimaryCareGiver, titleString, eventName, memberObject.getChwMemberId()));

                form = binder.getPrePopulatedForm(CoreConstants.JSON_FORM.getFamilyMemberEdit());
            }

            startActivityForResult(org.smartregister.chw.util.JsonFormUtils.getAncPncStartFormIntent(form, this), JsonFormUtils.REQUEST_CODE_GET_JSON);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    protected void registerPresenter() {
        presenter = new AncMemberProfilePresenter(this, new AncMemberProfileInteractor(this), memberObject);
    }

    @Override
    public void openMedicalHistory() {
        AncMedicalHistoryActivity.startMe(this, memberObject);
    }

    @Override
    public void openUpcomingService() {
        AncUpcomingServicesActivity.startMe(this, memberObject);
    }

    @Override
    public void openFamilyDueServices() {
        Intent intent = new Intent(this, FamilyProfileActivity.class);

        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID, memberObject.getFamilyBaseEntityId());
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.FAMILY_HEAD, memberObject.getFamilyHead());
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.PRIMARY_CAREGIVER, memberObject.getPrimaryCareGiver());
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.FAMILY_NAME, memberObject.getFamilyName());

        intent.putExtra(CoreConstants.INTENT_KEY.SERVICE_DUE, hasDueServices);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        int id = view.getId();
        if (id == R.id.textview_record_visit || id == R.id.textview_record_reccuring_visit) {
            AncHomeVisitActivity.startMe(this, memberObject.getBaseEntityId(), false);
        } else if (id == R.id.textview_edit) {
            AncHomeVisitActivity.startMe(this, memberObject.getBaseEntityId(), true);
        } else if (id == R.id.referral_row) {
            Task task = (Task) view.getTag();
            ReferralFollowupActivity.startReferralFollowupActivity(this, task.getIdentifier(), memberObject.getBaseEntityId());
        }else if (id == R.id.textview_verify_fingerprint || id == R.id.textview_verify_fingerprint_alt){
            //callVerifyFingerprint();
        }
    }

    private void callVerifyFingerprint(){
        getPresenter().verifyFingerprint();
    }

    private AncMemberProfilePresenter getPresenter(){
        return (AncMemberProfilePresenter) presenter();
    }

    @Override
    public void setClientTasks(Set<Task> taskList) {
        boolean isReferralFollowDue = false;

        for(Task task : taskList) {
            int days = Math.abs(Days.daysBetween(task.getAuthoredOn(), DateTime.now()).getDays());
            if( (days>=1 && task.getPriority() == 1) || days >= 3 ) {
                isReferralFollowDue = true;
                break;
            }
        }

        RelativeLayout layoutReferralRow = findViewById(R.id.referral_row);
        if (isReferralFollowDue) {
            layoutReferralRow.setOnClickListener(this);
            layoutReferralRow.setVisibility(View.VISIBLE);
            findViewById(R.id.view_referral_row).setVisibility(View.VISIBLE);
            layoutReferralRow.setTag(taskList.iterator().next());
        } else {
            layoutReferralRow.setVisibility(View.GONE);
            findViewById(R.id.view_referral_row).setVisibility(View.GONE);
        }
    }

    @Override
    public void onMemberDetailsReloaded(MemberObject memberObject) {
        this.memberObject = memberObject;
        super.onMemberDetailsReloaded(memberObject);
    }

    private void refreshViewOnHomeVisitResult() {
        Observable<Visit> observable = Observable.create(visitObservableEmitter -> {
            Visit lastVisit = getVisit(CoreConstants.EventType.ANC_HOME_VISIT);
            visitObservableEmitter.onNext(lastVisit);
            visitObservableEmitter.onComplete();
        });

        final Disposable[] disposable = new Disposable[1];
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Visit>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable[0] = d;
                    }

                    @Override
                    public void onNext(Visit visit) {
                        displayView();
                        setLastVisit(visit.getDate());
                        onMemberDetailsReloaded(memberObject);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                    }

                    @Override
                    public void onComplete() {
                        disposable[0].dispose();
                        disposable[0] = null;
                    }
                });
    }

    @Override
    public void startFormActivity(JSONObject formJson) {
        startActivityForResult(CoreJsonFormUtils.getJsonIntent(this, formJson, Utils.metadata().familyMemberFormActivity),
                JsonFormUtils.REQUEST_CODE_GET_JSON);
    }

    @Override
    public List<ReferralTypeModel> getReferralTypeModels() {
        return referralTypeModels;
    }

    @Override
    public void callFingerprintVerification(String fingerprintId) {
        String moduleId = CoreLibrary.getInstance().context().allSharedPreferences().fetchUserLocalityName("");
        if (moduleId == null || moduleId.isEmpty()){
            moduleId = "global_module";
        }
        SimPrintsVerifyActivity.startSimprintsVerifyActivity(this,
                moduleId, fingerprintId, VERIFY_RESULT_CODE);
    }

    @Override
    public void callFingerprintRegistration(Client client) {

        currentClient = client;

        try {
            DateTime birthdate = client.getBirthdate();
            SimpleDateFormat dateFormatForRiddler = new SimpleDateFormat("dd-MM-yyyy");
            String formatedDate = "";
            formatedDate = dateFormatForRiddler.format(birthdate.toDate());

            JSONObject metadata = new JSONObject();
            metadata.put("DOB", formatedDate);

            String moduleId = CoreLibrary.getInstance().context().allSharedPreferences().fetchUserLocalityName("");
            if (moduleId == null || moduleId.isEmpty()){
                moduleId = "global_module";
            }

            SimPrintsRegisterActivity.startSimprintsRegisterActivity(this,
                    moduleId, REGISTER_RESULT_CODE, metadata);

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}

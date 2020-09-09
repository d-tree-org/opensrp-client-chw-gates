package org.smartregister.chw.activity;

import android.app.Activity;
import android.content.Intent;
import android.util.Pair;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.json.JSONObject;
import org.smartregister.chw.R;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.contract.PncMemberProfileContract;
import org.smartregister.chw.core.activity.CoreFamilyProfileActivity;
import org.smartregister.chw.core.activity.CorePncMemberProfileActivity;
import org.smartregister.chw.core.activity.CorePncRegisterActivity;
import org.smartregister.chw.core.interactor.CorePncMemberProfileInteractor;
import org.smartregister.chw.core.listener.OnClickFloatingMenu;
import org.smartregister.chw.core.rule.PncVisitAlertRule;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.custom_view.AncFloatingMenu;
import org.smartregister.chw.dao.MalariaDao;
import org.smartregister.chw.dataloader.AncMemberDataLoader;
import org.smartregister.chw.dataloader.FamilyMemberDataLoader;
import org.smartregister.chw.dataloader.PncMemberDataLoader;
import org.smartregister.chw.form_data.NativeFormsDataBinder;
import org.smartregister.chw.fp.util.FamilyPlanningConstants;
import org.smartregister.chw.interactor.ChildProfileInteractor;
import org.smartregister.chw.interactor.FamilyProfileInteractor;
import org.smartregister.chw.interactor.PncMemberProfileInteractor;
import org.smartregister.chw.model.ChildRegisterModel;
import org.smartregister.chw.model.FamilyProfileModel;
import org.smartregister.chw.model.ReferralTypeModel;
import org.smartregister.chw.presenter.PncMemberProfilePresenter;
import org.smartregister.chw.schedulers.ChwScheduleTaskExecutor;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.family.contract.FamilyProfileContract;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.Nullable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static org.smartregister.family.util.JsonFormUtils.REQUEST_CODE_GET_JSON;

public class PncMemberProfileActivity extends CorePncMemberProfileActivity implements PncMemberProfileContract.View {

    private Flavor flavor = new PncMemberProfileActivityFlv();
    private List<ReferralTypeModel> referralTypeModels = new ArrayList<>();

    public static void startMe(Activity activity, String baseEntityID) {
        Intent intent = new Intent(activity, PncMemberProfileActivity.class);
        intent.putExtra(Constants.ANC_MEMBER_OBJECTS.BASE_ENTITY_ID, baseEntityID);
        activity.startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_pnc_member_registration) {
            startEditMemberJsonForm();
            return true;
        }

        if (item.getTitle().equals(getString(R.string.edit_pregnancy_outcome))){
            startEditingPregnancyOutcome();
        }

        return super.onOptionsItemSelected(item);
    }

    private void startEditingPregnancyOutcome(){
        try{
            JSONObject form = null;
            String titleString = getResources().getString(R.string.edit_pregnancy_outcome);

            String formName = CoreConstants.JSON_FORM.getPregnancyOutcome();

            NativeFormsDataBinder binder = new NativeFormsDataBinder(this, memberObject.getBaseEntityId());
            binder.setDataLoader(new PncMemberDataLoader(titleString));
            form = binder.getPrePopulatedForm(formName);

            startActivityForResult(org.smartregister.chw.util.JsonFormUtils.getAncPncStartFormIntent(form, this), JsonFormUtils.REQUEST_CODE_GET_JSON);

        }catch (Exception e){
            Timber.e(e);
        }
    }

    private void startEditMemberJsonForm() {
        try {
            JSONObject form = null;
            boolean isPrimaryCareGiver = memberObject.getPrimaryCareGiver().equals(memberObject.getBaseEntityId());
            String titleString = getResources().getString(R.string.edit_member_form_title);

            String eventName = org.smartregister.chw.util.Utils.metadata().familyMemberRegister.updateEventType;

            String uniqueID = memberObject.getBaseEntityId();

            NativeFormsDataBinder binder = new NativeFormsDataBinder(this, memberObject.getBaseEntityId());
            binder.setDataLoader(new FamilyMemberDataLoader(memberObject.getFamilyName(), isPrimaryCareGiver, titleString, eventName, uniqueID));
            form = binder.getPrePopulatedForm(CoreConstants.JSON_FORM.getFamilyMemberEdit());

            if (form != null){
                startFormActivity(form);
            }

        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;

        switch (requestCode) {
            case org.smartregister.chw.util.Constants.ProfileActivityResults.CHANGE_COMPLETED:
                Intent intent = new Intent(PncMemberProfileActivity.this, PncRegisterActivity.class);
                intent.putExtras(getIntent().getExtras());
                startActivity(intent);
                finish();
                break;
            case REQUEST_CODE_GET_JSON:
                try {
                    String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
                    JSONObject form = new JSONObject(jsonString);
                    if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Utils.metadata().familyMemberRegister.updateEventType)) {

                        FamilyEventClient familyEventClient =
                                new FamilyProfileModel(memberObject.getFamilyName()).processUpdateMemberRegistration(jsonString, memberObject.getBaseEntityId());
                        new FamilyProfileInteractor().saveRegistration(familyEventClient, jsonString, true, (FamilyProfileContract.InteractorCallBack) pncMemberProfilePresenter());
                    }

                    if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals("Pregnancy Outcome")){
                        ((PncMemberProfileContract.Presenter)presenter).saveForm(jsonString, "ec_pregnancy_outcome");
                    }

                    if (org.smartregister.chw.util.Constants.EventType.UPDATE_CHILD_REGISTRATION.equals(form.getString(JsonFormUtils.ENCOUNTER_TYPE))) {
                        Pair<Client, Event> pair = new ChildRegisterModel().processRegistration(jsonString);

                        if (pair != null) {
                            ((PncMemberProfileInteractor) pncMemberProfileInteractor).updateChild(pair, jsonString, null);
                        }

                    } else if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(CoreConstants.EventType.PNC_REFERRAL)) {
                        pncMemberProfilePresenter().createReferralEvent(Utils.getAllSharedPreferences(), jsonString);
                        showToast(this.getString(R.string.referral_submitted));
                    }

                } catch (Exception e) {
                    Timber.e(e);
                }
                break;
            case Constants.REQUEST_CODE_HOME_VISIT:
                this.setupViews();
                ChwScheduleTaskExecutor.getInstance().execute(memberObject.getBaseEntityId(), CoreConstants.EventType.PNC_HOME_VISIT, new Date());
                refreshOnHomeVisitResult();
                break;
            default:
                break;
        }
    }

    @Override
    public void setupViews() {
        super.setupViews();
        PncVisitAlertRule summaryVisit = getVisitDetails();
        String statusVisit = summaryVisit.getButtonStatus();
        if (statusVisit.equals("OVERDUE")) {
            textview_record_visit.setVisibility(View.VISIBLE);
            textview_record_visit.setBackgroundResource(R.drawable.rounded_red_btn);
        } else if (statusVisit.equals("DUE")) {
            textview_record_visit.setVisibility(View.VISIBLE);
            textview_record_visit.setBackgroundResource(R.drawable.rounded_blue_btn);
        } else if (ChildProfileInteractor.VisitType.VISIT_DONE.name().equals(statusVisit)) {
            Visit lastVisit = getVisit(Constants.EVENT_TYPE.PNC_HOME_VISIT);
            if (lastVisit != null) {
                if ((Days.daysBetween(new DateTime(lastVisit.getCreatedAt()), new DateTime()).getDays() < 1) &&
                        (Days.daysBetween(new DateTime(lastVisit.getDate()), new DateTime()).getDays() <= 1)) {
                    setEditViews(true, true, lastVisit.getDate().getTime());
                } else {
                    textview_record_visit.setVisibility(View.GONE);
                    layoutRecordView.setVisibility(View.GONE);
                }

            } else {
                textview_record_visit.setVisibility(View.VISIBLE);
                textview_record_visit.setBackgroundResource(R.drawable.rounded_blue_btn);
            }
        } else {
            textview_record_visit.setVisibility(View.GONE);
            layoutRecordView.setVisibility(View.GONE);
        }
    }

    private void refreshOnHomeVisitResult() {
        Observable<Visit> observable = Observable.create(e -> {
            Visit lastVisit = getVisit(CoreConstants.EventType.PNC_HOME_VISIT);
            e.onNext(lastVisit);
            e.onComplete();
        });

        final Disposable[] disposable = new Disposable[1];
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Visit>() {
            @Override
            public void onSubscribe(Disposable d) {
                disposable[0] = d;
            }

            @Override
            public void onNext(Visit visit) {
                displayView();
                setLastVisit(visit.getDate());
                setupViews();
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
    protected Class<? extends CoreFamilyProfileActivity> getFamilyProfileActivityClass() {
        return FamilyProfileActivity.class;
    }

    @Override
    protected CorePncMemberProfileInteractor getPncMemberProfileInteractor() {
        return new PncMemberProfileInteractor(this);
    }

    @Override
    protected void removePncMember() {
        IndividualProfileRemoveActivity.startIndividualProfileActivity(PncMemberProfileActivity.this, clientObject(), memberObject.getFamilyBaseEntityId(), memberObject.getFamilyHead(), memberObject.getPrimaryCareGiver(), PncRegisterActivity.class.getCanonicalName());
    }

    @Override
    protected Class<? extends CorePncRegisterActivity> getPncRegisterActivityClass() {
        return PncRegisterActivity.class;
    }

    @Override
    protected void onCreation() {
        super.onCreation();
        if (((ChwApplication) ChwApplication.getInstance()).hasReferrals()) {
            addPncReferralTypes();
        }
    }

    @Override
    public void onPregnancyOutcomeUpdated(boolean successful) {
        if (successful){
            Toast.makeText(this, getString(R.string.updated_successfully), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, getString(R.string.updated_unsuccessfully), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void registerPresenter() {
        presenter = new PncMemberProfilePresenter(this, new PncMemberProfileInteractor(this), memberObject);
    }

    @Override
    public void initializeFloatingMenu() {
        // menu not needed
    }

    @Override
    public void openVisitMonthView() {
        return;
    }

    @Override
    public void openUpcomingService() {
        PncUpcomingServicesActivity.startMe(this, memberObject);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        flavor.onCreateOptionsMenu(menu, memberObject.getBaseEntityId());
        return true;
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);

        switch (view.getId()) {
            case R.id.textview_record_visit:
            case R.id.textview_record_reccuring_visit:
                PncHomeVisitActivity.startMe(this, memberObject, false);
                break;
            case R.id.textview_edit:
                PncHomeVisitActivity.startMe(this, memberObject, true);
                break;
            default:
                break;
        }
    }

    public PncMemberProfileContract.Presenter pncMemberProfilePresenter() {
        return new PncMemberProfilePresenter(this, new PncMemberProfileInteractor(this), memberObject);
    }

    private CommonPersonObjectClient clientObject() {
        CommonRepository commonRepository = org.smartregister.chw.util.Utils.context().commonrepository(org.smartregister.chw.util.Utils.metadata().familyMemberRegister.tableName);
        final CommonPersonObject commonPersonObject = commonRepository.findByBaseEntityId(memberObject.getBaseEntityId());
        final CommonPersonObjectClient client =
                new CommonPersonObjectClient(commonPersonObject.getCaseId(), commonPersonObject.getDetails(), "");
        client.setColumnmaps(commonPersonObject.getColumnmaps());
        return client;
    }

    private PncVisitAlertRule getVisitDetails() {
        return ((PncMemberProfileInteractor) pncMemberProfileInteractor).getVisitSummary(memberObject.getBaseEntityId());
    }

    private void setEditViews(boolean enable, boolean within24Hours, Long longDate) {
        if (enable) {
            if (within24Hours) {
                Calendar cal = Calendar.getInstance();
                int offset = cal.getTimeZone().getOffset(cal.getTimeInMillis());
                new Date(longDate - (long) offset);
                String pncDay = pncMemberProfileInteractor.getPncDay(memberObject.getBaseEntityId());
                layoutNotRecordView.setVisibility(View.VISIBLE);
                tvEdit.setVisibility(View.VISIBLE);
                textViewUndo.setVisibility(View.GONE);
                textViewNotVisitMonth.setVisibility(View.VISIBLE);
                textViewNotVisitMonth.setText(MessageFormat.format(getContext().getString(R.string.pnc_visit_done), pncDay));
                imageViewCross.setImageResource(R.drawable.activityrow_visited);
                textview_record_visit.setVisibility(View.GONE);
            } else {
                layoutNotRecordView.setVisibility(View.GONE);

            }
        } else {
            layoutNotRecordView.setVisibility(View.GONE);
        }
    }

    @Override
    public void openMedicalHistory() {
        PncMedicalHistoryActivity.startMe(this, memberObject);
    }

    private void redrawFabWithNoPhone() {
        ((AncFloatingMenu) baseAncFloatingMenu).redraw(!StringUtils.isBlank(memberObject.getPhoneNumber())
                || !StringUtils.isBlank(getFamilyHeadPhoneNumber()));
    }

    @Override
    public void startFormActivity(JSONObject formJson) {
        startActivityForResult(CoreJsonFormUtils.getJsonIntent(this, formJson, Utils.metadata().familyMemberFormActivity),
                REQUEST_CODE_GET_JSON);
    }

    @Override
    public List<ReferralTypeModel> getReferralTypeModels() {
        return referralTypeModels;
    }

    private void addPncReferralTypes() {
        referralTypeModels.add(new ReferralTypeModel(getString(R.string.pnc_danger_signs),
                org.smartregister.chw.util.Constants.JSON_FORM.getPncReferralForm()));
        referralTypeModels.add(new ReferralTypeModel(getString(R.string.fp_post_partum), null));
        if (MalariaDao.isRegisteredForMalaria(((PncMemberProfilePresenter) presenter()).getEntityId())) {
            referralTypeModels.add(new ReferralTypeModel(getString(R.string.client_malaria_follow_up), null));
        }
    }

    @Override
    protected void startMalariaRegister() {
        MalariaRegisterActivity.startMalariaRegistrationActivity(this, memberObject.getBaseEntityId());
    }

    @Override
    protected void startFpRegister() {
        FpRegisterActivity.startFpRegistrationActivity(this, memberObject.getBaseEntityId(), memberObject.getDob(), CoreConstants.JSON_FORM.getFpRegistrationForm(), FamilyPlanningConstants.ActivityPayload.REGISTRATION_PAYLOAD_TYPE);
    }

    @Override
    protected void startFpChangeMethod() {
        FpRegisterActivity.startFpRegistrationActivity(this, memberObject.getBaseEntityId(), memberObject.getDob(), CoreConstants.JSON_FORM.getFpChengeMethodForm(), FamilyPlanningConstants.ActivityPayload.CHANGE_METHOD_PAYLOAD_TYPE);
    }

    @Override
    protected void startMalariaFollowUpVisit() {
        MalariaFollowUpVisitActivity.startMalariaFollowUpActivity(this, memberObject.getBaseEntityId());
    }

    @Override
    protected void getRemoveBabyMenuItem(MenuItem item) {
            for (CommonPersonObjectClient child : getChildren(memberObject)) {
                for (Map.Entry<String, String> entry : menuItemRemoveNames.entrySet()) {
                    if (entry.getKey().equalsIgnoreCase(item.getTitle().toString()) && entry.getValue().equalsIgnoreCase(child.entityId())) {
                        IndividualProfileRemoveActivity.startIndividualProfileActivity(PncMemberProfileActivity.this, child,
                                memberObject.getFamilyBaseEntityId()
                                , memberObject.getFamilyHead(), memberObject.getPrimaryCareGiver(), ChildRegisterActivity.class.getCanonicalName());
                    }
                }
        }
    }

    public interface Flavor {
        Boolean onCreateOptionsMenu(@Nullable Menu menu, @Nullable String baseEntityId);
    }
}
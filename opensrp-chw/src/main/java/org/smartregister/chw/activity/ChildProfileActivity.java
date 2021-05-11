package org.smartregister.chw.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.vijay.jsonwizard.utils.AllSharedPreferences;

import org.joda.time.DateTime;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.chw.R;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.contract.ChildProfileContract;
import org.smartregister.chw.core.activity.CoreChildProfileActivity;
import org.smartregister.chw.core.activity.CoreUpcomingServicesActivity;
import org.smartregister.chw.core.listener.OnClickFloatingMenu;
import org.smartregister.chw.core.model.CoreChildProfileModel;
import org.smartregister.chw.core.presenter.CoreChildProfilePresenter;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.custom_view.FamilyMemberFloatingMenu;
import org.smartregister.chw.dao.MalariaDao;
import org.smartregister.chw.model.ReferralTypeModel;
import org.smartregister.chw.presenter.ChildProfilePresenter;
import org.smartregister.chw.schedulers.ChwScheduleTaskExecutor;
import org.smartregister.domain.Task;
import org.smartregister.domain.db.Client;
import org.smartregister.family.util.Constants;
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

import static org.smartregister.chw.anc.util.Constants.ANC_MEMBER_OBJECTS.MEMBER_PROFILE_OBJECT;

public class ChildProfileActivity extends CoreChildProfileActivity implements ChildProfileContract.View {
    public FamilyMemberFloatingMenu familyFloatingMenu;
    private Flavor flavor = new ChildProfileActivityFlv();
    private List<ReferralTypeModel> referralTypeModels = new ArrayList<>();

    private static final int VERIFY_RESULT_CODE = 8379;
    private static final int REGISTER_RESULT_CODE = 5361;
    private static final String SESSION_BASE_ENTITY_ID = "session_base_entity_id"; 

    private TextView textViewNotVisitMonth;
    private TextView textViewUndo;
    private TextView textViewRecord;
    private TextView textViewVisitNot;
    private TextView verifyChildFingerprint;
    private RelativeLayout layoutNotRecordView;
    private RelativeLayout layoutRecordButtonDone;
    private LinearLayout layoutRecordView;

    Client currentClient;

    public List<ReferralTypeModel> getReferralTypeModels() {
        return referralTypeModels;
    }

    private SharedPreferences preferences;

    @Override
    protected void onCreation() {
        super.onCreation();

        preferences = this.getPreferences(MODE_PRIVATE);

        initializePresenter();
        onClickFloatingMenu = flavor.getOnClickFloatingMenu(this, (ChildProfilePresenter) presenter);
        setupViews();
        setUpToolbar();
        registerReceiver(mDateTimeChangedReceiver, sIntentFilter);
        if (((ChwApplication) ChwApplication.getInstance()).hasReferrals()) {
            addChildReferralTypes();
        }
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        int i = view.getId();
        if (i == R.id.last_visit_row) {
            openMedicalHistoryScreen();
        } else if (i == R.id.most_due_overdue_row) {
            openUpcomingServicePage();
        } else if (i == R.id.textview_record_visit || i == R.id.record_visit_done_bar) {
            openVisitHomeScreen(false);
        } else if (i == R.id.family_has_row) {
            openFamilyDueTab();
        } else if (i == R.id.textview_edit) {
            openVisitHomeScreen(true);
        }
        if (i == R.id.textview_visit_not) {
            presenter().updateVisitNotDone(System.currentTimeMillis());
            imageViewCrossChild.setVisibility(View.VISIBLE);
            imageViewCrossChild.setImageResource(R.drawable.activityrow_notvisited);
        } else if (i == R.id.textview_undo) {
            presenter().updateVisitNotDone(0);
        } else if(i == R.id.referral_row) {
            Task task = (Task) view.getTag();
            ReferralFollowupActivity.startReferralFollowupActivity(this, task.getIdentifier(), childBaseEntityId);
        }else if (i == R.id.textview_verify_fingerprint){
            //Call out verification
            org.smartregister.chw.presenter.ChildProfilePresenter presenter = (org.smartregister.chw.presenter.ChildProfilePresenter) presenter();
            presenter.verifyChildFingerprint();
        }
    }

    @Override
    protected void initializePresenter() {
        String familyName = getIntent().getStringExtra(Constants.INTENT_KEY.FAMILY_NAME);
        if (familyName == null) {
            familyName = "";
        }

        presenter = new ChildProfilePresenter(this, new CoreChildProfileModel(familyName), childBaseEntityId);
    }

    @Override
    protected void setupViews() {
        super.setupViews();

        textViewNotVisitMonth = findViewById(R.id.textview_not_visit_this_month);
        textViewUndo = findViewById(R.id.textview_undo);
        textViewVisitNot = findViewById(R.id.textview_visit_not);
        textViewRecord = findViewById(R.id.textview_record_visit);
        layoutRecordButtonDone = findViewById(R.id.record_visit_done_bar);
        layoutNotRecordView = findViewById(R.id.record_visit_status_bar);
        layoutRecordView = findViewById(R.id.record_visit_bar);

        verifyChildFingerprint = findViewById(R.id.textview_verify_fingerprint);
        verifyChildFingerprint.setOnClickListener(this);

        fetchProfileData();

        showFingerprintVerificationPrompt();
    }

    private void showFingerprintVerificationPrompt(){
        
        //Get the previous session stored base entity ID
        SharedPreferences.Editor editor = preferences.edit();
        String lastSessionBaseEntityId = preferences.getString(SESSION_BASE_ENTITY_ID, "_");
        
        if (lastSessionBaseEntityId.equalsIgnoreCase("_") || !lastSessionBaseEntityId.equalsIgnoreCase(childBaseEntityId)){
            //No Previous session or was a different child, show prompt and start a new session
            displayFPVerifyPrompt();
            editor.putString(SESSION_BASE_ENTITY_ID, childBaseEntityId != null ? childBaseEntityId : "_");
            editor.apply();
        }
    }

    void displayFPVerifyPrompt(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Verify Child Fingerprint");
        builder.setMessage("Please scan child fingerprints by pressing 'Verify fingerprints' button");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    @Override
    public void updateHasPhone(boolean hasPhone) {
        if (familyFloatingMenu != null) {
            familyFloatingMenu.reDraw(hasPhone);
        }
    }

    @Override
    public void setVisitNotDoneThisMonth() {
        openVisitMonthView();
        textViewNotVisitMonth.setText(getString(R.string.not_visiting_this_month));
        textViewUndo.setText(getString(R.string.undo));
        textViewUndo.setVisibility(View.VISIBLE);
        imageViewCrossChild.setImageResource(R.drawable.activityrow_notvisited);
    }

    @Override
    public void setVisitAboveTwentyFourView() {
        textViewVisitNot.setVisibility(View.GONE);
        openVisitRecordDoneView();
        textViewRecord.setBackgroundResource(R.drawable.record_btn_selector_above_twentyfr);
        textViewRecord.setTextColor(getResources().getColor(R.color.light_grey_text));
    }

    private void openVisitRecordDoneView() {
        layoutRecordButtonDone.setVisibility(View.VISIBLE);
        layoutNotRecordView.setVisibility(View.GONE);
        layoutRecordView.setVisibility(View.GONE);
    }

    @Override
    public void callFingerprintRegister(Client client) {

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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_malaria_registration:
                MalariaRegisterActivity.startMalariaRegistrationActivity(ChildProfileActivity.this,
                        ((CoreChildProfilePresenter) presenter()).getChildClient().getCaseId());
                return true;
            case R.id.action_remove_member:
                IndividualProfileRemoveActivity.startIndividualProfileActivity(ChildProfileActivity.this, ((ChildProfilePresenter) presenter()).getChildClient(),
                        ((ChildProfilePresenter) presenter()).getFamilyID()
                        , ((ChildProfilePresenter) presenter()).getFamilyHeadID(), ((ChildProfilePresenter) presenter()).getPrimaryCareGiverID(), ChildRegisterActivity.class.getCanonicalName());

                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.action_sick_child_form).setVisible(ChwApplication.getApplicationFlavor().hasChildSickForm());
        menu.findItem(R.id.action_sick_child_follow_up).setVisible(false);
        menu.findItem(R.id.action_malaria_diagnosis).setVisible(false);
        menu.findItem(R.id.action_malaria_followup_visit).setVisible(false);
        return true;
    }

    @Override
    protected void onResumption() {
        super.onResumption();
        fetchProfileData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CoreConstants.ProfileActivityResults.CHANGE_COMPLETED && resultCode == Activity.RESULT_OK) {
            Intent intent = new Intent(ChildProfileActivity.this, ChildProfileActivity.class);
            assert getIntent().getExtras() != null;
            intent.putExtras(getIntent().getExtras());
            startActivity(intent);
            finish();
        }else if (requestCode == VERIFY_RESULT_CODE && resultCode == Activity.RESULT_OK){

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
        }else if(requestCode == REGISTER_RESULT_CODE && resultCode == RESULT_OK){
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
        ChwScheduleTaskExecutor.getInstance().execute(memberObject.getBaseEntityId(), CoreConstants.EventType.CHILD_HOME_VISIT, new Date());
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

    private void openMedicalHistoryScreen() {
        ChildMedicalHistoryActivity.startMe(this, memberObject);
    }

    private void openUpcomingServicePage() {
        MemberObject memberObject = new MemberObject(((ChildProfilePresenter) presenter()).getChildClient());
        CoreUpcomingServicesActivity.startMe(this, memberObject);
    }

    private void openVisitHomeScreen(boolean isEditMode) {
        ChildHomeVisitActivity.startMe(this, memberObject, isEditMode, ChildHomeVisitActivity.class);
    }

    private void openFamilyDueTab() {
        Intent intent = new Intent(this, FamilyProfileActivity.class);

        intent.putExtra(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID, ((ChildProfilePresenter) presenter()).getFamilyId());
        intent.putExtra(Constants.INTENT_KEY.FAMILY_HEAD, ((ChildProfilePresenter) presenter()).getFamilyHeadID());
        intent.putExtra(Constants.INTENT_KEY.PRIMARY_CAREGIVER, ((ChildProfilePresenter) presenter()).getPrimaryCareGiverID());
        intent.putExtra(Constants.INTENT_KEY.FAMILY_NAME, ((ChildProfilePresenter) presenter()).getFamilyName());

        intent.putExtra(org.smartregister.chw.util.Constants.INTENT_KEY.SERVICE_DUE, true);
        startActivity(intent);
    }

    private void addChildReferralTypes() {
        referralTypeModels.add(new ReferralTypeModel(getString(R.string.sick_child),
                org.smartregister.chw.util.Constants.JSON_FORM.getChildReferralForm()));
        if (MalariaDao.isRegisteredForMalaria(childBaseEntityId)) {
            referralTypeModels.add(new ReferralTypeModel(getString(R.string.client_malaria_follow_up), null));
        }
    }

    @Override
    protected View.OnClickListener getSickListener() {
        return v -> {
            Intent intent = new Intent(getApplication(), SickFormMedicalHistory.class);
            intent.putExtra(MEMBER_PROFILE_OBJECT, memberObject);
            startActivity(intent);
        };
    }

    public interface Flavor {
        OnClickFloatingMenu getOnClickFloatingMenu(Activity activity, ChildProfilePresenter presenter);
    }
}

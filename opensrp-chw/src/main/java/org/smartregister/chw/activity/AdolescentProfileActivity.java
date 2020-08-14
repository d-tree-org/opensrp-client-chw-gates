package org.smartregister.chw.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONObject;
import org.opensrp.api.constants.Gender;
import org.smartregister.chw.R;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.contract.AdolescentProfileContract;
import org.smartregister.chw.core.dao.AncDao;
import org.smartregister.chw.core.utils.CoreChildUtils;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.dataloader.FamilyMemberDataLoader;
import org.smartregister.chw.form_data.NativeFormsDataBinder;
import org.smartregister.chw.presenter.AdolescentProfilePresenter;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.domain.FetchStatus;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.helper.ImageRenderHelper;
import org.smartregister.view.activity.BaseProfileActivity;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;


public class AdolescentProfileActivity extends BaseProfileActivity implements AdolescentProfileContract.View {

    private static final String UPDATE_ADOLESCENT_REGISTRATION = "Update Adolescent Registration" ;
    protected MemberObject memberObject;
    private TextView textViewTitle;
    protected CircleImageView imageView;
    protected TextView textViewName;
    protected TextView textViewGender;
    protected TextView textViewLocation;
    protected TextView textViewUniqueID;
    protected TextView tvEdit;
    protected View recordVisitPanel;
    public RelativeLayout layoutFamilyHasRow;
    public RelativeLayout layoutReferralRow;
    private TextView textViewRecord;
    private TextView textViewVisitNot;
    private TextView textViewNotVisitMonth;
    protected TextView textViewLastVisit;
    protected ImageView imageViewCross;
    protected TextView textViewUndo;
    private RelativeLayout layoutNotRecordView;
    private RelativeLayout layoutLastVisitRow;
    private RelativeLayout layoutMostDueOverdue;
    private RelativeLayout layoutRecordButtonDone;
    private LinearLayout layoutRecordView;
    private TextView textViewFamilyHas;
    private View viewMostDueRow;
    private View viewFamilyRow;
    private View viewLastVisitRow;
    protected TextView textViewMedicalHistory;
    protected ImageView imageViewCrossChild;

    public String baseEntityId;
    public String lastVisitDay;
    public boolean isComesFromFamily = false;
    private String PhoneNumber;
    protected String familyBaseEntityId;
    protected String familyHead;
    protected String primaryCaregiver;
    protected String familyName;
    protected CommonPersonObjectClient commonPersonObject;
    public Handler handler = new Handler();

    private ProgressBar progressBar;

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_adolescent_profile);
        Toolbar toolbar = findViewById(R.id.collapsing_toolbar);
        textViewTitle = findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            baseEntityId = getIntent().getStringExtra(org.smartregister.family.util.Constants.INTENT_KEY.BASE_ENTITY_ID);
            isComesFromFamily = getIntent().getBooleanExtra(CoreConstants.INTENT_KEY.IS_COMES_FROM_FAMILY, false);
            commonPersonObject = (CommonPersonObjectClient) getIntent().getSerializableExtra(CoreConstants.INTENT_KEY.CHILD_COMMON_PERSON);
        }

        memberObject = new MemberObject(commonPersonObject);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
            upArrow.setColorFilter(getResources().getColor(R.color.text_blue), PorterDuff.Mode.SRC_ATOP);
            actionBar.setHomeAsUpIndicator(upArrow);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        textViewTitle.setOnClickListener(v -> onBackPressed());
        appBarLayout = this.findViewById(org.smartregister.malaria.R.id.collapsing_toolbar_appbarlayout);
        if (Build.VERSION.SDK_INT >= 21) {
            appBarLayout.setOutlineProvider(null);
        }
        imageRenderHelper = new ImageRenderHelper(this);
        setupViews();
        initializePresenter();

    }

    public static void startMe(Activity activity, boolean isComesFromFamily, String baseEntityId, CommonPersonObjectClient pc, Class<?> cls) {
        Intent intent = new Intent(activity, cls);
        intent.putExtra(CoreConstants.INTENT_KEY.IS_COMES_FROM_FAMILY, isComesFromFamily);
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.BASE_ENTITY_ID, baseEntityId);
        intent.putExtra(CoreConstants.INTENT_KEY.CHILD_COMMON_PERSON, pc);
        activity.startActivity(intent);

    }

    @Override
    protected void setupViews() {
        imageView = findViewById(R.id.imageview_profile);
        textViewName = findViewById(R.id.textview_name);
        textViewGender = findViewById(R.id.textview_gender);
        textViewLocation = findViewById(R.id.textview_address);
        textViewUniqueID = findViewById(R.id.textview_id);
        tvEdit = findViewById(org.smartregister.chw.core.R.id.textview_edit);
        textViewRecord = findViewById(R.id.textview_record_visit);
        textViewVisitNot = findViewById(R.id.textview_visit_not);
        textViewNotVisitMonth = findViewById(R.id.textview_not_visit_this_month);
        recordVisitPanel = findViewById(org.smartregister.chw.core.R.id.record_visit_panel);
        textViewLastVisit = findViewById(org.smartregister.chw.core.R.id.textview_last_vist_day);
        textViewUndo = findViewById(R.id.textview_undo);
        imageViewCrossChild = findViewById(R.id.tick_image);
        imageViewCross = findViewById(org.smartregister.chw.core.R.id.cross_image);
        layoutRecordView = findViewById(R.id.record_visit_bar);
        layoutNotRecordView = findViewById(R.id.record_visit_status_bar);
        layoutLastVisitRow = findViewById(org.smartregister.chw.core.R.id.last_visit_row);
        textViewMedicalHistory = findViewById(org.smartregister.chw.core.R.id.text_view_medical_hstory);
        layoutMostDueOverdue = findViewById(org.smartregister.chw.core.R.id.most_due_overdue_row);
        layoutFamilyHasRow = findViewById(org.smartregister.chw.core.R.id.family_has_row);
        layoutReferralRow = findViewById(org.smartregister.chw.core.R.id.referral_row);
        textViewFamilyHas = findViewById(org.smartregister.chw.core.R.id.textview_family_has);
        layoutRecordButtonDone = findViewById(R.id.record_visit_done_bar);
        viewLastVisitRow = findViewById(org.smartregister.chw.core.R.id.view_last_visit_row);
        viewMostDueRow = findViewById(org.smartregister.chw.core.R.id.view_most_due_overdue_row);
        viewFamilyRow = findViewById(org.smartregister.chw.core.R.id.view_family_row);
        progressBar = findViewById(org.smartregister.chw.core.R.id.progress_bar);
        textViewTitle = findViewById(R.id.toolbar_title);

        // Setup onClick Listener
        textViewRecord.setOnClickListener(this);
        layoutRecordButtonDone.setOnClickListener(this);
        layoutLastVisitRow.setOnClickListener(this);
        layoutFamilyHasRow.setOnClickListener(this);
        textViewVisitNot.setOnClickListener(this);
        textViewUndo.setOnClickListener(this);
        setUpToolbar();
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();

        if (i == R.id.textview_record_visit || i == R.id.record_visit_done_bar) {
            openVisitHomeScreen(false);
        } else if (i == R.id.textview_visit_not) {
            showProgressBar(true);
            presenter().updateVisitNotDone(System.currentTimeMillis());
            tvEdit.setVisibility(View.GONE);
        } else if (i == R.id.textview_undo) {
            showProgressBar(true);
            presenter().updateVisitNotDone(0);
        } else if (i == R.id.textview_edit) {
            openVisitHomeScreen(true);
        } else if (i == R.id.most_due_overdue_row) {
            Toast.makeText(this, "Overdue thingy clicked", Toast.LENGTH_SHORT).show();
        } else if(i == R.id.last_visit_row) {
            openMedicalHistory();
            Toast.makeText(this, "You clicked the last visit thingy", Toast.LENGTH_SHORT).show();
        } else if (i == R.id.family_has_row) {
            openFamilyDueServices();
        }

    }

    @Override
    public AdolescentProfileContract.Presenter presenter() {
        return (AdolescentProfileContract.Presenter) presenter;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.other_member_menu, menu);

        // Check if they are already registered and they are gender is female because they are already in reproductive age
        if (!AncDao.isANCMember(baseEntityId) && "Female".equalsIgnoreCase(presenter().getAdolescentGender())) {
            menu.findItem(R.id.action_anc_registration).setVisible(true);
        } else {
            menu.findItem(R.id.action_anc_registration).setVisible(false);
        }

        menu.findItem(R.id.action_sick_child_follow_up).setVisible(false);
        menu.findItem(R.id.action_malaria_diagnosis).setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_anc_registration) {
            startAncRegister();
            return true;
        } else if (i == R.id.action_registration) {
            startFormForEdit(R.string.edit_member_form_title);
            return true;
        } else if (i == R.id.action_remove_member) {
            CommonRepository commonRepository = Utils.context().commonrepository(Utils.metadata().familyMemberRegister.tableName);

            final CommonPersonObject commonPersonObject = commonRepository.findByBaseEntityId(baseEntityId);
            final CommonPersonObjectClient client =
                    new CommonPersonObjectClient(commonPersonObject.getCaseId(), commonPersonObject.getDetails(), "");
            client.setColumnmaps(commonPersonObject.getColumnmaps());
            IndividualProfileRemoveActivity.startIndividualProfileActivity(AdolescentProfileActivity.this, client, presenter().getFamilyID(), presenter().getFamilyHead(), presenter().getPrimaryCareGiver(), AdolescentProfileActivity.class.getCanonicalName());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void startFormForEdit(Integer title_resource) {

        CommonRepository commonRepository = Utils.context().commonrepository(Utils.metadata().familyMemberRegister.tableName);

        final CommonPersonObject personObject = commonRepository.findByBaseEntityId(baseEntityId);
        final CommonPersonObjectClient client =
                new CommonPersonObjectClient(personObject.getCaseId(), personObject.getDetails(), "");
        client.setColumnmaps(personObject.getColumnmaps());

        startEditMemberJsonForm(title_resource, client);
    }

    protected void startEditMemberJsonForm(Integer title_resource, CommonPersonObjectClient client) {

        String titleString = title_resource != null ? getResources().getString(title_resource) : null;
        boolean isPrimaryCareGiver = commonPersonObject.getCaseId().equalsIgnoreCase(presenter().getPrimaryCareGiver());
        String eventName = UPDATE_ADOLESCENT_REGISTRATION;

        String uniqueID = commonPersonObject.getColumnmaps().get(DBConstants.KEY.UNIQUE_ID);

        NativeFormsDataBinder binder = new NativeFormsDataBinder(this, client.getCaseId());
        binder.setDataLoader(new FamilyMemberDataLoader(presenter().getFamilyName(), isPrimaryCareGiver, titleString, eventName, uniqueID));
        //JSONObject jsonObject = binder.getPrePopulatedForm(CoreConstants.JSON_FORM.getFamilyMemberRegister());
        JSONObject jsonObject = binder.getPrePopulatedForm(org.smartregister.chw.core.utils.Utils.getLocalForm
                ("family_member_edit", CoreConstants.JSON_FORM.locale, CoreConstants.JSON_FORM.assetManager));

        try {
            if (jsonObject != null)
                startFormActivity(jsonObject);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    public void startFormActivity(JSONObject jsonForm) {

        Intent intent = new Intent(this, Utils.metadata().familyMemberFormActivity);
        intent.putExtra(Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());


        Form form = new Form();
        form.setActionBarBackground(R.color.family_actionbar);
        form.setWizard(false);
        intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);

        startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);
    }

    @Override
    public void showUndoVisitNotDoneView() {
        presenter().fetchVisitStatus(baseEntityId);
    }

    @Override
    public void setAdolescentNameAndAge(String nameAndAge) { textViewName.setText(nameAndAge); }

    @Override
    public void setGender(String gender) { textViewGender.setText(getGenderTranslated(gender)); }

    @Override
    public void setVillageLocation(String location) { textViewLocation.setText(location); }

    @Override
    public void setUniqueId(String uniqueId) {
        String id = String.format(getString(R.string.unique_id_text), uniqueId);
        textViewUniqueID.setText(id);
    }

    @Override
    public void refreshProfile(FetchStatus fetchStatus) {
        if(fetchStatus.equals(FetchStatus.fetched)) {
            handler.postDelayed(() -> presenter().fetchProfileData(), 100);
        }
    }

    @Override
    public void setOverDueColor() {

    }

    @Override
    public void setVisitButtonDueStatus() {
        openVisitButtonView();
        textViewRecord.setBackgroundResource(R.drawable.rounded_blue_btn);
        textViewRecord.setTextColor(getResources().getColor(R.color.white));
    }

    @Override
    public void setVisitButtonOverdueStatus() {
        openVisitButtonView();
        textViewRecord.setBackgroundResource(R.drawable.rounded_red_btn);
        textViewRecord.setTextColor(getResources().getColor(R.color.white));
    }

    private void openVisitButtonView() {
        layoutNotRecordView.setVisibility(View.GONE);
        layoutRecordButtonDone.setVisibility(View.GONE);
        layoutRecordView.setVisibility(View.VISIBLE);
    }

    @Override
    public void setVisitLessTwentyFourView(String monthName) {
        textViewNotVisitMonth.setText(getString(R.string.visit_month, monthName));
        textViewUndo.setText(getString(R.string.edit));
        textViewUndo.setVisibility(View.GONE);
        imageViewCrossChild.setImageResource(R.drawable.activityrow_visited);
        openVisitMonthView();
    }

    @Override
    public void openVisitMonthView() {
        layoutNotRecordView.setVisibility(View.VISIBLE);
        layoutRecordButtonDone.setVisibility(View.GONE);
        layoutRecordView.setVisibility(View.GONE);
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
        layoutRecordView.setVisibility(View.VISIBLE);
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
    public void setFamilyHasNothingDue() {
        layoutFamilyHasRow.setVisibility(View.VISIBLE);
        viewFamilyRow.setVisibility(View.VISIBLE);
        textViewFamilyHas.setText(getString(R.string.family_has_nothing_due));
    }

    @Override
    public void setFamilyHasServiceDue() {
        layoutFamilyHasRow.setVisibility(View.VISIBLE);
        viewFamilyRow.setVisibility(View.VISIBLE);
        textViewFamilyHas.setText(getString(R.string.family_has_services_due));
    }

    @Override
    public void setFamilyHasServiceOverdue() {
        layoutFamilyHasRow.setVisibility(View.VISIBLE);
        viewFamilyRow.setVisibility(View.VISIBLE);
        textViewFamilyHas.setText(CoreChildUtils.fromHtml(getString(R.string.family_has_service_overdue)));
    }

    @Override
    public void setLastVisitRowView(String days) {
        lastVisitDay = days;
        if (TextUtils.isEmpty(days)) {
            layoutLastVisitRow.setVisibility(View.GONE);
            viewLastVisitRow.setVisibility(View.GONE);
        } else {
            layoutLastVisitRow.setVisibility(View.VISIBLE);
            textViewLastVisit.setText(getString(R.string.last_visit_40_days_ago, days));
            viewLastVisitRow.setVisibility(View.VISIBLE);
        }
    }

    private void openVisitHomeScreen(boolean isEditMode) {
        AdolescentHomeVisitActivity.startMe(this, memberObject, isEditMode, AdolescentHomeVisitActivity.class);
    }

    @Override
    public void openMedicalHistory() {

    }

    @Override
    public void openUpcomingService() {

    }

    @Override
    public void openFamilyDueServices() {
        Intent intent = new Intent(this, FamilyProfileActivity.class);

        intent.putExtra(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID, ((AdolescentProfilePresenter) presenter()).getFamilyID());
        intent.putExtra(Constants.INTENT_KEY.FAMILY_HEAD, ((AdolescentProfilePresenter) presenter()).getFamilyHead());
        intent.putExtra(Constants.INTENT_KEY.PRIMARY_CAREGIVER, ((AdolescentProfilePresenter) presenter()).getPrimaryCareGiver());
        intent.putExtra(Constants.INTENT_KEY.FAMILY_NAME, ((AdolescentProfilePresenter) presenter()).getFamilyName());

        intent.putExtra(org.smartregister.chw.util.Constants.INTENT_KEY.SERVICE_DUE, true);
        startActivity(intent);
    }

    @Override
    public void showProgressBar(boolean status) {
        progressBar.setVisibility(status ? View.VISIBLE : View.GONE);
    }

    @Override
    public void hideView() {

    }

    @Override
    public void enableEdit(boolean enable) {
        if (enable) {
            tvEdit.setVisibility(View.VISIBLE);
            tvEdit.setOnClickListener(this);
        } else {
            tvEdit.setVisibility(View.GONE);
            tvEdit.setOnClickListener(null);
        }
    }

    @Override
    protected void initializePresenter() {
        if (presenter == null) {
            presenter = new AdolescentProfilePresenter(this, baseEntityId);
        }

        fetchProfileData();
    }

    @Override
    protected ViewPager setupViewPager(ViewPager viewPager) {
        return null;
    }

    @Override
    protected void fetchProfileData() {
        presenter().fetchProfileData();
        updateDateAdolescentDetails();
    }

    private void updateDateAdolescentDetails() {

        handler.postDelayed(() -> {
            layoutMostDueOverdue.setVisibility(View.GONE);
            viewMostDueRow.setVisibility(View.GONE);
            presenter().fetchVisitStatus(baseEntityId);
            presenter().fetchUpcomingServiceAndFamilyDue(baseEntityId);
        }, 100);

    }

    @Override
    public Context getContext() {
        return this;
    }

    private String getGenderTranslated(String gender) {
        if (gender.equalsIgnoreCase(Gender.MALE.toString())) {
            return getResources().getString(org.smartregister.family.R.string.male);
        } else if (gender.equalsIgnoreCase(Gender.FEMALE.toString())) {
            return getResources().getString(org.smartregister.family.R.string.female);
        }
        return "";
    }

    public void setUpToolbar() {
        if (isComesFromFamily) {
            textViewTitle.setText(getString(R.string.return_to_family_members));
        } else {
            textViewTitle.setText(getString(R.string.return_to_previous_page_adolescent));
        }
    }

    protected void startAncRegister() {
        PhoneNumber = presenter().getPhoneNumber();
        familyBaseEntityId = presenter().getFamilyID();
        familyName = presenter().getFamilyName();
        AncRegisterActivity.startAncRegistrationActivity(AdolescentProfileActivity.this, baseEntityId, PhoneNumber,
                org.smartregister.chw.util.Constants.JSON_FORM.getAncRegistration(), null, familyBaseEntityId, familyName);
        this.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) return;

        if (requestCode == JsonFormUtils.REQUEST_CODE_GET_JSON) {

            try {
               String jsonString = data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON);
                presenter().updateAdolescentProfile(jsonString);

            } catch (Exception e) {
                Timber.e(e);
            }

        }

    }
}

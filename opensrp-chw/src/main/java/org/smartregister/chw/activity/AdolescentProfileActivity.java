package org.smartregister.chw.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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

import org.json.JSONArray;
import org.json.JSONObject;
import org.opensrp.api.constants.Gender;
import org.smartregister.chw.R;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.contract.AdolescentProfileContract;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.dao.AncDao;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.dataloader.FamilyMemberDataLoader;
import org.smartregister.chw.form_data.NativeFormsDataBinder;
import org.smartregister.chw.malaria.activity.BaseMalariaProfileActivity;
import org.smartregister.chw.model.FamilyProfileModel;
import org.smartregister.chw.presenter.AdolescentProfilePresenter;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.AllCommonsRepository;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.domain.FetchStatus;
import org.smartregister.family.contract.FamilyProfileContract;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.family.interactor.FamilyProfileInteractor;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.helper.ImageRenderHelper;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.view.activity.BaseProfileActivity;
import org.smartregister.view.contract.BaseProfileContract;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

import static org.smartregister.chw.core.utils.Utils.isWomanOfReproductiveAge;


public class AdolescentProfileActivity extends BaseProfileActivity implements AdolescentProfileContract.View {

    private static final String UPDATE_ADOLESCENT_REGISTRATION = "Update Adolescent Registration" ;
    protected MemberObject memberObject;
    private TextView textViewTitle;
    protected CircleImageView imageView;
    protected TextView textViewName;
    protected TextView textViewGender;
    protected TextView textViewLocation;
    protected TextView textViewUniqueID;
    protected RelativeLayout rlLastVisit;
    protected RelativeLayout rlUpcomingServices;
    protected RelativeLayout rlFamilyServicesDue;
    protected RelativeLayout visitStatus;
    protected ImageView imageViewCross;
    protected TextView textViewUndo;
    private TextView tvUpComingServices;
    private TextView tvFamilyStatus;
    protected TextView textViewVisitDone;
    protected RelativeLayout visitDone;
    protected LinearLayout recordVisits;
    protected TextView textViewVisitDoneEdit;
    protected TextView textViewRecordAncNotDone;
    public String baseEntityId;
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
            memberObject = (MemberObject) getIntent().getSerializableExtra(org.smartregister.chw.anc.util.Constants.ANC_MEMBER_OBJECTS.MEMBER_PROFILE_OBJECT);
            commonPersonObject = (CommonPersonObjectClient) getIntent().getSerializableExtra(CoreConstants.INTENT_KEY.CHILD_COMMON_PERSON);
        }

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
        TextView textViewVerifyFingerPrint = findViewById(R.id.textview_verify_fingerprint);
        textViewVerifyFingerPrint.setVisibility(View.GONE);
        imageRenderHelper = new ImageRenderHelper(this);
        setupViews();
        initializePresenter();

    }

    @Override
    protected void setupViews() {
        imageView = findViewById(R.id.imageview_profile);
        textViewName = findViewById(R.id.textview_name);
        textViewGender = findViewById(R.id.textview_gender);
        textViewLocation = findViewById(R.id.textview_address);
        textViewUniqueID = findViewById(R.id.textview_id);
        progressBar = findViewById(R.id.progress_bar);
        textViewTitle = findViewById(R.id.toolbar_title);
        setUpToolbar();
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
    public void setAdolescentNameAndAge(String nameAndAge) { textViewName.setText(nameAndAge); }

    @Override
    public void setGender(String gender) { textViewGender.setText(getGenderTranslated(gender)); }

    @Override
    public void setVillageLocation(String location) { textViewLocation.setText(location); }

    @Override
    public void setUniqueId(String uniqueId) { textViewUniqueID.setText(uniqueId); }

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
    public void openMedicalHistory() {

    }

    @Override
    public void openUpcomingService() {

    }

    @Override
    public void openFamilyDueServices() {

    }

    @Override
    public void showProgressBar(boolean status) {
        progressBar.setVisibility(status ? View.VISIBLE : View.GONE);
    }

    @Override
    public void hideView() {

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

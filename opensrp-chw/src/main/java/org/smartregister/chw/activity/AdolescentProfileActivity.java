package org.smartregister.chw.activity;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import org.opensrp.api.constants.Gender;
import org.smartregister.chw.R;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.contract.AdolescentProfileContract;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.malaria.activity.BaseMalariaProfileActivity;
import org.smartregister.chw.malaria.util.Constants;
import org.smartregister.chw.presenter.AdolescentProfilePresenter;
import org.smartregister.helper.ImageRenderHelper;
import org.smartregister.view.activity.BaseProfileActivity;
import org.smartregister.view.contract.BaseProfileContract;

import de.hdodenhof.circleimageview.CircleImageView;


public class AdolescentProfileActivity extends BaseProfileActivity implements AdolescentProfileContract.View {

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
    public void setAdolescentNameAndAge(String nameAndAge) { textViewName.setText(nameAndAge); }

    @Override
    public void setGender(String gender) { textViewGender.setText(getGenderTranslated(gender)); }

    @Override
    public void setVillageLocation(String location) { textViewLocation.setText(location); }

    @Override
    public void setUniqueId(String uniqueId) { textViewUniqueID.setText(uniqueId); }

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
}

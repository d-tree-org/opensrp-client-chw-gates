package org.smartregister.chw.activity;

import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.contract.AdolescentProfileContract;
import org.smartregister.view.activity.BaseProfileActivity;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdolescentProfileActivity extends BaseProfileActivity implements AdolescentProfileContract.View {

    protected MemberObject memberObject;
    protected CircleImageView imageView;
    protected TextView textViewName;
    protected TextView textViewGender;
    protected TextView textViewLocation;
    protected TextView textViewUniqueID;

    @Override
    public void setProfileViewWithData() {

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

    }

    @Override
    public void hideView() {

    }

    @Override
    protected void initializePresenter() {

    }

    @Override
    protected ViewPager setupViewPager(ViewPager viewPager) {
        return null;
    }

    @Override
    protected void fetchProfileData() {

    }
}

package org.smartregister.chw.fragment;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import org.smartregister.chw.R;
import org.smartregister.chw.contract.MonthlyActivitiesFragmentContract;
import org.smartregister.chw.contract.MonthlyActivityContract;
import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.model.MonthlyActivityRegisterFragmentModel;
import org.smartregister.chw.presenter.MonthlyActivityRegisterFragmentPresenter;
import org.smartregister.chw.referral.contract.BaseReferralRegisterFragmentContract;
import org.smartregister.chw.referral.provider.ReferralRegisterProvider;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.reporting.domain.IndicatorTally;
import org.smartregister.view.activity.BaseRegisterActivity;
import org.smartregister.view.customcontrols.CustomFontTextView;
import org.smartregister.view.customcontrols.FontVariant;
import org.smartregister.view.fragment.BaseRegisterFragment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MonthlyActivitiesRegisterFragment extends BaseRegisterFragment implements MonthlyActivitiesFragmentContract.View  {

    public MonthlyActivitiesRegisterFragment(){}

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.dashboard_fragment, MonthlyActivityDashboard.newInstance());
        transaction.addToBackStack(null);
        transaction.commit();

    }

    @Override
    protected void initializePresenter() {
        if (this.getActivity() != null){
            this.presenter = new MonthlyActivityRegisterFragmentPresenter(this, new MonthlyActivityRegisterFragmentModel(), (String)null);
        }
    }

    @Override
    public void setupViews(View view) {
        super.setupViews(view);

        Toolbar toolbar = view.findViewById(org.smartregister.R.id.register_toolbar);
        toolbar.setContentInsetsAbsolute(0, 0);
        toolbar.setContentInsetsRelative(0, 0);
        toolbar.setContentInsetStartWithNavigation(0);

        NavigationMenu.getInstance(getActivity(), null, toolbar);

        View navbarContainer = view.findViewById(R.id.register_nav_bar_container);
        navbarContainer.setFocusable(false);

        qrCodeScanImageView = (ImageView)view.findViewById(R.id.scanQrCode);
        if (qrCodeScanImageView != null) {
            qrCodeScanImageView.setVisibility(View.GONE);
        }

        android.view.View searchBarLayout = view.findViewById(R.id.search_bar_layout);
        searchBarLayout.setBackgroundResource(R.color.customAppThemeBlue);
        if (getSearchView() != null) {
            getSearchView().setBackgroundResource(R.color.white);
            getSearchView().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_search, 0, 0, 0);
        }
        searchBarLayout.setVisibility(View.GONE);

        TextView filterView = (TextView)view.findViewById(R.id.filter_text_view);
        if (filterView != null) {
            filterView.setText(this.getString(R.string.sort));
            filterView.setVisibility(View.GONE);
        }

        ImageView logo = (ImageView)view.findViewById(R.id.opensrp_logo_image_view);
        if (logo != null) {
            logo.setVisibility(View.GONE);
        }

        CustomFontTextView titleView = (CustomFontTextView)view.findViewById(org.smartregister.chw.referral.R.id.txt_title_label);
        if (titleView != null) {
            titleView.setVisibility(View.VISIBLE);
            titleView.setText(this.getString(R.string.monthly_activity));
            titleView.setFontVariant(FontVariant.REGULAR);
        }

        if (clientsView != null)
            clientsView.setVisibility(View.GONE);

        headerTextDisplay.setVisibility(View.GONE);
        filterStatus.setVisibility(View.GONE);
        filterRelativeLayout.setVisibility(View.GONE);
        syncButton.setVisibility(View.GONE);

    }

    @Override
    public void setTotalPatients() {

    }

    @Override
    public void initializeAdapter(Set<org.smartregister.configurableviews.model.View> visibleColumns) {
        ReferralRegisterProvider referralRegisterProvider = new ReferralRegisterProvider(getActivity(), paginationViewHandler, registerActionHandler, visibleColumns);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, referralRegisterProvider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }

    @Override
    public MonthlyActivitiesFragmentContract.Presenter presenter() {
        return (MonthlyActivitiesFragmentContract.Presenter)this.presenter;
    }

    @Override
    public void setUniqueID(String s) {
        if (this.getSearchView() != null) {
            this.getSearchView().setText(s);
        }
    }

    public void setAdvancedSearchFormData(HashMap<String, String> hashMap) {
    }

    protected String getMainCondition() {
        return this.presenter().getMainCondition();
    }

    protected String getDefaultSortQuery() {
        return this.presenter().getDefaultSortQuery();
    }

    protected void startRegistration() {
    }

    protected void onViewClicked(android.view.View view) {
        if (this.getActivity() != null) {
            if (view.getTag() instanceof CommonPersonObjectClient && view.getTag(org.smartregister.chw.referral.R.id.VIEW_ID) == "click_view_normal") {
                this.openProfile((CommonPersonObjectClient)view.getTag());
            }

        }
    }

    @Override
    protected int getLayout() {
        return R.layout.monthly_activities_fragment;
    }

    protected void openProfile(CommonPersonObjectClient client) {
    }

    protected void openFollowUpVisit(CommonPersonObjectClient client) {
    }

    public void showNotFoundPopup(String s) {
    }

}

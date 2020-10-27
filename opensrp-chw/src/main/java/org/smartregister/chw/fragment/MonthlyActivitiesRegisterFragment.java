package org.smartregister.chw.fragment;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import org.smartregister.chw.R;
import org.smartregister.chw.contract.MonthlyActivitiesFragmentContract;
import org.smartregister.chw.contract.MonthlyActivityContract;
import org.smartregister.chw.model.MonthlyActivityRegisterFragmentModel;
import org.smartregister.chw.presenter.MonthlyActivityRegisterFragmentPresenter;
import org.smartregister.chw.referral.contract.BaseReferralRegisterFragmentContract;
import org.smartregister.chw.referral.provider.ReferralRegisterProvider;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.reporting.domain.IndicatorTally;
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
    protected void initializePresenter() {
        if (this.getActivity() != null){
            this.presenter = new MonthlyActivityRegisterFragmentPresenter(this, new MonthlyActivityRegisterFragmentModel(), (String)null);
        }
    }

    @Override
    public void setupViews(View view) {
        super.setupViews(view);
        this.qrCodeScanImageView = (ImageView)view.findViewById(R.id.scanQrCode);
        if (this.qrCodeScanImageView != null) {
            this.qrCodeScanImageView.setVisibility(View.GONE);
        }

        android.view.View searchBarLayout = view.findViewById(R.id.search_bar_layout);
        searchBarLayout.setBackgroundResource(R.color.customAppThemeBlue);
        if (this.getSearchView() != null) {
            this.getSearchView().setBackgroundResource(R.color.white);
            this.getSearchView().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_search, 0, 0, 0);
        }

        TextView filterView = (TextView)view.findViewById(R.id.filter_text_view);
        if (filterView != null) {
            filterView.setText(this.getString(R.string.sort));
        }

        ImageView logo = (ImageView)view.findViewById(R.id.opensrp_logo_image_view);
        if (logo != null) {
            logo.setVisibility(View.GONE);
        }

        CustomFontTextView titleView = (CustomFontTextView)view.findViewById(org.smartregister.chw.referral.R.id.txt_title_label);
        if (titleView != null) {
            titleView.setVisibility(View.VISIBLE);
            titleView.setText(this.getString(R.string.issued_referrals));
            titleView.setFontVariant(FontVariant.REGULAR);
        }
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

    protected void openProfile(CommonPersonObjectClient client) {
    }

    protected void openFollowUpVisit(CommonPersonObjectClient client) {
    }

    public void showNotFoundPopup(String s) {
    }

}

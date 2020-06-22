package org.smartregister.chw.fragment;

import android.widget.LinearLayout;

import androidx.appcompat.widget.Toolbar;

import org.smartregister.chw.R;
import org.smartregister.chw.contract.AdolescentRegisterFragmentContract;
import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.chw.model.AdolescentRegisterFragmentModel;
import org.smartregister.chw.presenter.AdolescentRegisterFragmentPresenter;
import org.smartregister.chw.provider.AdolescentRegisterProvider;
import org.smartregister.chw.provider.FamilyRegisterProvider;
import org.smartregister.chw.provider.ReferralRegisterProvider;
import org.smartregister.configurableviews.model.View;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.view.customcontrols.CustomFontTextView;
import org.smartregister.view.fragment.BaseRegisterFragment;

import java.util.HashMap;
import java.util.Set;

import static org.smartregister.chw.R.color.primary_color;
import static org.smartregister.chw.R.color.white;

public class AdolescentRegisterFragment extends BaseRegisterFragment implements AdolescentRegisterFragmentContract.View {

    private android.view.View view;
    @Override
    public void initializeAdapter(Set<View> visibleColumns) {
        AdolescentRegisterProvider adolescentRegisterProvider = new AdolescentRegisterProvider(getActivity(), paginationViewHandler, registerActionHandler, visibleColumns);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, adolescentRegisterProvider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);

    }

    @Override
    public AdolescentRegisterFragmentContract.Presenter presenter() {
        return (AdolescentRegisterFragmentContract.Presenter) presenter;
    }

    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }
        presenter = new AdolescentRegisterFragmentPresenter(this, new AdolescentRegisterFragmentModel(), null);
    }

    @Override
    public void setupViews(android.view.View view) {
        super.setupViews(view);
        this.view = view;

        Toolbar toolbar = view.findViewById(org.smartregister.R.id.register_toolbar);
        toolbar.setContentInsetsAbsolute(0, 0);
        toolbar.setContentInsetsRelative(0, 0);
        toolbar.setContentInsetStartWithNavigation(0);

        NavigationMenu.getInstance(getActivity(), null, null);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        android.view.View searchBarLayout = view.findViewById(org.smartregister.chw.core.R.id.search_bar_layout);
        searchBarLayout.setLayoutParams(params);
        searchBarLayout.setBackgroundResource(org.smartregister.chw.core.R.color.chw_primary);
        searchBarLayout.setPadding(searchBarLayout.getPaddingLeft(), searchBarLayout.getPaddingTop(), searchBarLayout.getPaddingRight(), (int) Utils.convertDpToPixel(10, getActivity()));

        CustomFontTextView titleView = view.findViewById(org.smartregister.chw.core.R.id.txt_title_label);
        if (titleView != null) {
            titleView.setPadding(0, titleView.getTop(), titleView.getPaddingRight(), titleView.getPaddingBottom());
            titleView.setText("Adolescent Clients");
        }
    }

    @Override
    public void setUniqueID(String s) {

    }

    @Override
    public void setAdvancedSearchFormData(HashMap<String, String> hashMap) {

    }

    @Override
    protected String getMainCondition() {
        return presenter().getMainCondition();
    }

    @Override
    protected String getDefaultSortQuery() {
        return null;
    }

    @Override
    protected void startRegistration() {

    }

    @Override
    protected void onViewClicked(android.view.View view) {

    }

    @Override
    public void showNotFoundPopup(String s) {

    }

    @Override
    protected void updateSearchView() {
        super.updateSearchView();
        if (getSearchView() != null) {
            getSearchView().setBackgroundColor(getResources().getColor(white));
        }
    }

    @Override
    protected void onResumption() {
        super.onResumption();
        Toolbar toolbar = view.findViewById(org.smartregister.R.id.register_toolbar);
        toolbar.setContentInsetsAbsolute(0, 0);
        toolbar.setContentInsetsRelative(0, 0);
        toolbar.setContentInsetStartWithNavigation(0);

        NavigationMenu.getInstance(getActivity(), null, toolbar);
    }
}

package org.smartregister.chw.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.R;
import org.smartregister.chw.activity.AdolescentHomeVisitActivity;
import org.smartregister.chw.activity.AdolescentProfileActivity;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.chw.contract.AdolescentRegisterFragmentContract;
import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.QueryBuilder;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.chw.model.AdolescentRegisterFragmentModel;
import org.smartregister.chw.presenter.AdolescentRegisterFragmentPresenter;
import org.smartregister.chw.provider.AdolescentRegisterProvider;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.configurableviews.model.View;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.view.activity.BaseRegisterActivity;
import org.smartregister.view.customcontrols.CustomFontTextView;
import org.smartregister.view.customcontrols.FontVariant;
import org.smartregister.view.fragment.BaseRegisterFragment;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

import static org.smartregister.chw.R.color.white;

public class AdolescentRegisterFragment extends BaseRegisterFragment implements AdolescentRegisterFragmentContract.View {

    public static final String CLICK_VIEW_NORMAL = "click_view_normal";
    public static final String CLICK_VIEW_DOSAGE_STATUS = "click_view_dosage_status";

    private static final String DUE_FILTER_TAG = "PRESSED";
    private android.view.View dueOnlyLayout;
    private android.view.View view;
    private boolean dueFilterActive = false;

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
        String viewConfigurationIdentifier = ((BaseRegisterActivity) getActivity()).getViewIdentifiers().get(0);
        presenter = new AdolescentRegisterFragmentPresenter(this, new AdolescentRegisterFragmentModel(), viewConfigurationIdentifier);
    }

    @Override
    public void setupViews(android.view.View view) {
        super.setupViews(view);
        this.view = view;

        Toolbar toolbar = view.findViewById(org.smartregister.R.id.register_toolbar);
        toolbar.setContentInsetsAbsolute(0, 0);
        toolbar.setContentInsetsRelative(0, 0);
        toolbar.setContentInsetStartWithNavigation(0);

        NavigationMenu.getInstance(getActivity(), null, toolbar);

        android.view.View navbarContainer = view.findViewById(R.id.register_nav_bar_container);
        navbarContainer.setFocusable(false);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        android.view.View searchBarLayout = view.findViewById(org.smartregister.chw.core.R.id.search_bar_layout);
        searchBarLayout.setLayoutParams(params);
        searchBarLayout.setBackgroundResource(org.smartregister.chw.core.R.color.chw_primary);
        searchBarLayout.setPadding(searchBarLayout.getPaddingLeft(), searchBarLayout.getPaddingTop(), searchBarLayout.getPaddingRight(), (int) Utils.convertDpToPixel(10, getActivity()));

        // Update title name
        ImageView logo = view.findViewById(org.smartregister.family.R.id.opensrp_logo_image_view);
        if (logo != null) {
            logo.setVisibility(android.view.View.GONE);
        }

        CustomFontTextView titleView = view.findViewById(org.smartregister.chw.core.R.id.txt_title_label);
        if (titleView != null) {
            titleView.setVisibility(android.view.View.VISIBLE);
            titleView.setText(R.string.adolescent_fragment_title);
            titleView.setFontVariant(FontVariant.REGULAR);
            titleView.setPadding(0, titleView.getTop(), titleView.getPaddingRight(), titleView.getPaddingBottom());
        }

        android.view.View topLeftLayout = view.findViewById(R.id.top_left_layout);
        topLeftLayout.setVisibility(android.view.View.GONE);

        android.view.View topRightLayout = view.findViewById(R.id.top_right_layout);
        topRightLayout.setVisibility(android.view.View.GONE);

        android.view.View sortFilterBarLayout = view.findViewById(R.id.register_sort_filter_bar_layout);
        sortFilterBarLayout.setVisibility(android.view.View.GONE);

        android.view.View filterSortLayout = view.findViewById(R.id.filter_sort_layout);
        filterSortLayout.setVisibility(android.view.View.GONE);

        dueOnlyLayout = view.findViewById(R.id.due_only_layout);
        dueOnlyLayout.setVisibility(android.view.View.VISIBLE);
        dueOnlyLayout.setOnClickListener(registerActionHandler);

        if (getSearchView() != null) {
            getSearchView().setBackgroundResource(org.smartregister.family.R.color.white);
            getSearchView().setCompoundDrawablesWithIntrinsicBounds(org.smartregister.family.R.drawable.ic_action_search, 0, 0, 0);
            getSearchView().setTextColor(getResources().getColor(R.color.text_black));
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
        return presenter().getDefaultSortQuery();
    }

    @Override
    protected void startRegistration() {

    }

    @Override
    protected void onViewClicked(android.view.View view) {

        if (getActivity() == null) {
            return;
        }

        if (view.getTag() instanceof CommonPersonObjectClient && view.getTag(org.smartregister.chw.core.R.id.VIEW_ID) == CLICK_VIEW_NORMAL) {
            goToAdolescentDetailActivity((CommonPersonObjectClient) view.getTag(), false);
        } else if (view.getTag() instanceof CommonPersonObjectClient && view.getTag(org.smartregister.chw.core.R.id.VIEW_ID) == CLICK_VIEW_DOSAGE_STATUS) {
            MemberObject memberObject = new MemberObject((CommonPersonObjectClient) view.getTag());
            AdolescentHomeVisitActivity.startMe(getActivity(), memberObject, false, AdolescentHomeVisitActivity.class);
        }
        else if (view.getId() == org.smartregister.chw.core.R.id.due_only_layout) {
            toggleFilterSelection(view);
        }

    }

    public void goToAdolescentDetailActivity(CommonPersonObjectClient patient,
                                        boolean launchDialog) {
        if (launchDialog) {
            Timber.i(patient.name);
        }
        Intent intent = new Intent(getActivity(), AdolescentProfileActivity.class);
        intent.putExtra(CoreConstants.INTENT_KEY.IS_COMES_FROM_FAMILY, false);
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.BASE_ENTITY_ID, patient.getCaseId());
        intent.putExtra(org.smartregister.chw.anc.util.Constants.ANC_MEMBER_OBJECTS.MEMBER_PROFILE_OBJECT, new MemberObject(patient));
        intent.putExtra(CoreConstants.INTENT_KEY.CHILD_COMMON_PERSON, patient);
        startActivity(intent);
    }

    protected void toggleFilterSelection(android.view.View dueOnlyLayout) {
        if (dueOnlyLayout != null) {
            if (dueOnlyLayout.getTag() == null) {
                dueFilterActive = true;
                dueFilter(dueOnlyLayout);
            } else if (dueOnlyLayout.getTag().toString().equals(DUE_FILTER_TAG)) {
                dueFilterActive = false;
                normalFilter(dueOnlyLayout);
            }
        }
    }

    protected void normalFilter(android.view.View dueOnlyLayout) {
        filter(searchText(), "", presenter().getMainCondition());
        dueOnlyLayout.setTag(null);
        switchViews(dueOnlyLayout, false);
    }

    protected void dueFilter(android.view.View dueOnlyLayout) {
        filter(searchText(), "", presenter().getDueFilterCondition());
        dueOnlyLayout.setTag(DUE_FILTER_TAG);
        switchViews(dueOnlyLayout, true);
    }

    protected void filter(String filterString, String joinTableString, String mainConditionString) {
        filters = filterString;
        joinTable = joinTableString;
        mainCondition = mainConditionString;
        filterandSortExecute(countBundle());
    }

    protected String searchText() {
        return (getSearchView() == null) ? "" : getSearchView().getText().toString();
    }

    private void switchViews(android.view.View dueOnlyLayout, boolean isPress) {
        TextView dueOnlyTextView = dueOnlyLayout.findViewById(org.smartregister.chw.core.R.id.due_only_text_view);
        if (isPress) {
            dueOnlyTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, org.smartregister.chw.core.R.drawable.ic_due_filter_on, 0);
        } else {
            dueOnlyTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, org.smartregister.chw.core.R.drawable.ic_due_filter_off, 0);

        }
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

    private String defaultFilterAndSortQuery() {
        SmartRegisterQueryBuilder sqb = new SmartRegisterQueryBuilder(mainSelect);

        String query = "";
        String customFilter = getFilterString();
        try {
            if (isValidFilterForFts(commonRepository())) {

                String myquery = QueryBuilder.getQuery(joinTables, mainCondition, tablename, customFilter, clientAdapter, Sortqueries);
                List<String> ids = commonRepository().findSearchIds(myquery);
                query = sqb.toStringFts(ids, tablename, CommonRepository.ID_COLUMN,
                        Sortqueries);
                query = sqb.Endquery(query);
            } else {
                sqb.addCondition(customFilter);
                query = sqb.orderbyCondition(Sortqueries);
                query = sqb.Endquery(sqb.addlimitandOffset(query, clientAdapter.getCurrentlimit(), clientAdapter.getCurrentoffset()));

            }
        } catch (Exception e) {
            Timber.e(e);
        }

        return query;
    }

    private String getFilterString() {
        StringBuilder customFilter = new StringBuilder();
        if (StringUtils.isNotBlank(filters)) {
            customFilter.append(MessageFormat.format(" and ( {0}.{1} like ''%{2}%'' ", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.FIRST_NAME, filters));
            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.LAST_NAME, filters));
            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.MIDDLE_NAME, filters));
            customFilter.append(MessageFormat.format(" or {0}.{1} like ''%{2}%'' ) ", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.UNIQUE_ID, filters));
        }

        if (dueFilterActive)
            customFilter.append(getDueCondition());

        return customFilter.toString();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle args) {
        if (id == LOADER_ID) {
            return new CursorLoader(getActivity()) {
                @Override
                public Cursor loadInBackground() {
                    // Count query
                    final String COUNT = "count_execute";
                    if (args != null && args.getBoolean(COUNT)) {
                        countExecute();
                    }
                    String query = defaultFilterAndSortQuery();
                    return commonRepository().rawCustomQueryForAdapter(query);
                }
            };
        }
        return super.onCreateLoader(id, args);
    }

    public String getDueCondition() {
        return " and " + CoreConstants.TABLE_NAME.ANC_MEMBER + ".base_entity_id in (select base_entity_id from schedule_service where strftime('%Y-%m-%d') BETWEEN due_date and expiry_date and schedule_name = '" + CoreConstants.SCHEDULE_TYPES.ANC_VISIT + "' and ifnull(not_done_date,'') = '' and ifnull(completion_date,'') = '' )  ";
    }

    @Override
    public void countExecute() {
        Cursor cursor = null;
        try {

            String query = "select count(*) from " + presenter().getMainTable() + " inner join " + CoreConstants.TABLE_NAME.FAMILY_MEMBER +
                    " on " + presenter().getMainTable() + "." + org.smartregister.chw.anc.util.DBConstants.KEY.BASE_ENTITY_ID + " = " +
                    CoreConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.BASE_ENTITY_ID +
                    " where " + presenter().getMainCondition();

            if (StringUtils.isNotBlank(filters))
                query = query + getFilterString();

            cursor = commonRepository().rawCustomQueryForAdapter(query);
            cursor.moveToFirst();
            clientAdapter.setTotalcount(cursor.getInt(0));
            Timber.v("total count here %d", clientAdapter.getTotalcount());

            clientAdapter.setCurrentlimit(20);
            clientAdapter.setCurrentoffset(0);


        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}

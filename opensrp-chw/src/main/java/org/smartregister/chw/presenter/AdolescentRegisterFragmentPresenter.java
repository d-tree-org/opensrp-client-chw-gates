package org.smartregister.chw.presenter;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.contract.AdolescentRegisterFragmentContract;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.fragment.AdolescentRegisterFragment;
import org.smartregister.chw.malaria.contract.MalariaRegisterFragmentContract;
import org.smartregister.chw.malaria.util.Constants;
import org.smartregister.chw.malaria.util.DBConstants;
import org.smartregister.configurableviews.model.RegisterConfiguration;
import org.smartregister.configurableviews.model.View;
import org.smartregister.configurableviews.model.ViewConfiguration;

import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.TreeSet;

import static org.apache.commons.lang3.StringUtils.trim;

public class AdolescentRegisterFragmentPresenter implements AdolescentRegisterFragmentContract.Presenter {

    protected WeakReference<AdolescentRegisterFragmentContract.View> viewReference;

    protected AdolescentRegisterFragmentContract.Model model;

    protected RegisterConfiguration config;

    protected Set<View> visibleColumns = new TreeSet<>();
    protected String viewConfigurationIdentifier;

    public AdolescentRegisterFragmentPresenter(AdolescentRegisterFragmentContract.View viewReference,
                                               AdolescentRegisterFragmentContract.Model model, String viewConfigurationIdentifier) {
        this.viewReference = new WeakReference<>(viewReference);
        this.model = model;
        this.config = model.defaultRegisterConfiguration();
        this.viewConfigurationIdentifier = viewConfigurationIdentifier;
    }

    @Override
    public String getMainCondition() {
        return " " + CoreConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.DATE_REMOVED + " is null " +
                "AND " + CoreConstants.TABLE_NAME.ADOLESCENT + "." + DBConstants.KEY.IS_CLOSED + " is 0 ";
    }

    @Override
    public String getDefaultSortQuery() {
        return null;
    }

    @Override
    public String getMainTable() {
        return CoreConstants.TABLE_NAME.ADOLESCENT;
    }

    @Override
    public String getDueFilterCondition() {
        return null;
    }

    @Override
    public void processViewConfigurations() {
        if (StringUtils.isBlank(viewConfigurationIdentifier)) {
            return;
        }

        ViewConfiguration viewConfiguration = model.getViewConfiguration(viewConfigurationIdentifier);
        if (viewConfiguration != null) {
            config = (RegisterConfiguration) viewConfiguration.getMetadata();
            this.visibleColumns = model.getRegisterActiveColumns(viewConfigurationIdentifier);
        }

        if (config.getSearchBarText() != null && getView() != null) {
            getView().updateSearchBarHint(config.getSearchBarText());
        }
    }

    @Override
    public void initializeQueries(String mainCondition) {

        String tableName = "ec_adolescent";
        mainCondition = trim(getMainCondition()).equals("") ? mainCondition : getMainCondition();
        String countSelect = model.countSelect(tableName, mainCondition);
        String mainSelect = model.mainSelect(tableName, mainCondition);

        if (getView() != null) {

            getView().initializeQueryParams(tableName, countSelect, mainSelect);
            getView().initializeAdapter(visibleColumns);

            getView().countExecute();
            getView().filterandSortInInitializeQueries();
        }

    }

    @Override
    public void startSync() {

    }

    @Override
    public void searchGlobally(String s) {

    }

    protected AdolescentRegisterFragmentContract.View getView() {
        if (viewReference != null)
            return viewReference.get();
        else
            return null;
    }
}

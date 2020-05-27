package org.smartregister.chw.presenter;

import org.smartregister.chw.R;
import org.smartregister.chw.referral.contract.BaseReferralRegisterFragmentContract;
import org.smartregister.chw.referral.presenter.BaseReferralRegisterFragmentPresenter;
import org.smartregister.chw.referral.util.DBConstants;
import org.smartregister.chw.util.Constants;

import static org.apache.commons.lang3.StringUtils.trim;

public class ReferralFollowupFragmentPresenter extends BaseReferralRegisterFragmentPresenter {

    public ReferralFollowupFragmentPresenter(BaseReferralRegisterFragmentContract.View view, BaseReferralRegisterFragmentContract.Model model, String viewConfigurationIdentifier) {
        super(view, model, viewConfigurationIdentifier);
    }

    @Override
    public String getMainCondition() {
        return " " + Constants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.DATE_REMOVED + " is null ";
    }

    @Override
    public void processViewConfigurations() {
        super.processViewConfigurations();
        if (config.getSearchBarText() != null && getView() != null) {
            getView().updateSearchBarHint(getView().getContext().getString(R.string.search_name_or_id));
        }
    }

    @Override
    public String getMainTable() {
        return "task";
    }

    public String getDefaultSortQuery() {
        return "";
    }

    public String getDueFilterCondition() {
        return "";
    }

    @Override
    public void initializeQueries(String mainCondition) {
        String tableName = "task";
        String condition = trim(getMainCondition()).equals("") ? mainCondition : getMainCondition();
        String countSelect = model.countSelect(tableName, condition);
        String mainSelect = model.mainSelect(tableName, condition);

        if (getView() != null) {

            getView().initializeQueryParams(tableName, countSelect, mainSelect);
            getView().initializeAdapter(visibleColumns);

            getView().countExecute();
            getView().filterandSortInInitializeQueries();
        }
    }
}

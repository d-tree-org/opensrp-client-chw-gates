package org.smartregister.chw.model;

import org.smartregister.chw.contract.AdolescentRegisterContract;
import org.smartregister.chw.contract.AdolescentRegisterFragmentContract;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.malaria.MalariaLibrary;
import org.smartregister.chw.malaria.util.ConfigHelper;
import org.smartregister.configurableviews.ConfigurableViewsLibrary;
import org.smartregister.configurableviews.model.RegisterConfiguration;
import org.smartregister.configurableviews.model.View;
import org.smartregister.configurableviews.model.ViewConfiguration;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.family.util.DBConstants;

import java.util.Set;

public class AdolescentRegisterFragmentModel implements AdolescentRegisterFragmentContract.Model {
    @Override
    public RegisterConfiguration defaultRegisterConfiguration() {
        return ConfigHelper.defaultRegisterConfiguration(MalariaLibrary.getInstance().context().applicationContext());
    }

    @Override
    public ViewConfiguration getViewConfiguration(String viewConfigurationIdentifier) {
        return ConfigurableViewsLibrary.getInstance().getConfigurableViewsHelper().getViewConfiguration(viewConfigurationIdentifier);
    }

    @Override
    public Set<View> getRegisterActiveColumns(String viewConfigurationIdentifier) {
        return ConfigurableViewsLibrary.getInstance().getConfigurableViewsHelper().getRegisterActiveColumns(viewConfigurationIdentifier);
    }

    @Override
    public String countSelect(String tableName, String mainCondition) {
        SmartRegisterQueryBuilder countQueryBuilder = new SmartRegisterQueryBuilder();
        countQueryBuilder.SelectInitiateMainTableCounts(tableName);
        return countQueryBuilder.mainCondition(mainCondition);
    }

    @Override
    public String mainSelect(String tableName, String mainCondition) {
        SmartRegisterQueryBuilder queryBUilder = new SmartRegisterQueryBuilder();
        queryBUilder.selectInitiateMainTable(tableName, mainColumns(tableName), "id");
        return queryBUilder.mainCondition(mainCondition);
    }

    protected String[] mainColumns(String tableName) {
        String[] columns = new String[]{
                tableName + ".relationalid",
                tableName + ".first_name",
                tableName + ".middle_name",
                tableName + ".last_name",
                tableName + "." + DBConstants.KEY.DOB
        };
        return columns;
    }
}

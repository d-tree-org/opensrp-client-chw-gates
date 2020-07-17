package org.smartregister.chw.application;

import org.smartregister.CoreLibrary;
import org.smartregister.SyncFilter;
import org.smartregister.chw.util.Utils;

public class ChwSyncConfigurationFlv extends DefaultChwSyncConfigurationFlv {
    @Override
    public String getSyncFilterValueForSettings() {
        String providerId = Utils.context().allSharedPreferences().fetchRegisteredANM();
        String teamId = Utils.context().allSharedPreferences().fetchDefaultTeamId(providerId);
        return teamId;
    }

    @Override
    public SyncFilter getSettingsSyncFilterParam() {
        return SyncFilter.TEAM_ID;
    }

    @Override
    public SyncFilter getSyncFilterParam() {
        return SyncFilter.PROVIDER;
    }

    @Override
    public String getSyncFilterValue() {
        return CoreLibrary.getInstance().context().allSharedPreferences().fetchRegisteredANM();
    }

    @Override
    public SyncFilter getEncryptionParam() {
        return SyncFilter.LOCATION;
    }
}
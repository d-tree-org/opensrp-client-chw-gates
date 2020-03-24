package org.smartregister.chw.application;

import org.smartregister.CoreLibrary;
import org.smartregister.SyncFilter;

public class ChwSyncConfigurationFlv extends DefaultChwSyncConfigurationFlv {
    @Override
    public String getSyncFilterValueForSettings() {
        return super.getSyncFilterValueForSettings();
    }

    @Override
    public SyncFilter getSettingsSyncFilterParam() {
        return super.getSettingsSyncFilterParam();
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
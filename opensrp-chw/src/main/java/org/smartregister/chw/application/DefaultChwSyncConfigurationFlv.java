package org.smartregister.chw.application;

import org.apache.http.params.SyncBasicHttpParams;
import org.smartregister.CoreLibrary;
import org.smartregister.SyncFilter;
import org.smartregister.chw.util.Utils;

public class DefaultChwSyncConfigurationFlv implements ChwSyncConfiguration.Flavor{
    @Override
    public String getSyncFilterValueForSettings() {
        String providerId = CoreLibrary.getInstance().context().allSharedPreferences().fetchRegisteredANM();
        String locationId = CoreLibrary.getInstance().context().allSharedPreferences().fetchUserLocalityId(providerId);
        return locationId;
    }

    @Override
    public SyncFilter getSettingsSyncFilterParam() {
        return SyncFilter.LOCATION;
    }

    @Override
    public SyncFilter getSyncFilterParam() {
        return SyncFilter.LOCATION;
    }

    @Override
    public String getSyncFilterValue() {
        return Utils.getSyncFilterValue();
    }

    @Override
    public SyncFilter getEncryptionParam() {
        return SyncFilter.LOCATION;
    }
}

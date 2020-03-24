package org.smartregister.chw.application;

import org.smartregister.SyncConfiguration;
import org.smartregister.SyncFilter;
import org.smartregister.chw.BuildConfig;
import org.smartregister.chw.core.utils.Utils;

/**
 * Created by samuelgithengi on 10/19/18.
 */
public class ChwSyncConfiguration extends SyncConfiguration {

    private static ChwSyncConfiguration.Flavor flavor = new ChwSyncConfigurationFlv();

    @Override
    public int getSyncMaxRetries() {
        return BuildConfig.MAX_SYNC_RETRIES;
    }

    @Override
    public SyncFilter getSyncFilterParam() {
        return this.flavor.getSyncFilterParam();
    }

    @Override
    public String getSyncFilterValue() {
        return this.flavor.getSyncFilterValue();
    }

    @Override
    public int getUniqueIdSource() {
        return BuildConfig.OPENMRS_UNIQUE_ID_SOURCE;
    }

    @Override
    public int getUniqueIdBatchSize() {
        return BuildConfig.OPENMRS_UNIQUE_ID_BATCH_SIZE;
    }

    @Override
    public int getUniqueIdInitialBatchSize() {
        return BuildConfig.OPENMRS_UNIQUE_ID_INITIAL_BATCH_SIZE;
    }

    @Override
    public boolean isSyncSettings() {
        return BuildConfig.IS_SYNC_SETTINGS;
    }

    @Override
    public SyncFilter getEncryptionParam() {
        return this.flavor.getEncryptionParam();
    }

    @Override
    public boolean updateClientDetailsTable() {
        return false;
    }

    @Override
    public String getSettingsSyncFilterValue() {
        return this.flavor.getSyncFilterValueForSettings();
    }

    @Override
    public SyncFilter getSettingsSyncFilterParam() {
        return this.flavor.getSettingsSyncFilterParam();
    }

    public boolean isSyncUsingPost() {
        return !BuildConfig.DEBUG;
    }

    public interface Flavor {
        String getSyncFilterValueForSettings();
        SyncFilter getSettingsSyncFilterParam();
        SyncFilter getSyncFilterParam();
        String getSyncFilterValue();
        SyncFilter getEncryptionParam();
    }
}

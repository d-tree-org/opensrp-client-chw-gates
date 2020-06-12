package org.smartregister.chw.sync;


import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.Context;
import org.smartregister.CoreLibrary;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.sync.helper.SyncSettingsServiceHelper;
import org.smartregister.sync.intent.SettingsSyncIntentService;

import timber.log.Timber;

import static org.smartregister.util.Log.logError;


public class AfyatekSettingsSyncIntentService extends SettingsSyncIntentService {

    private static final String TAG = AfyatekSettingsSyncIntentService.class.getCanonicalName();
    protected SyncSettingsServiceHelper syncSettingsServiceHelper;
    private SharedPreferences preferences;
    public static final String IS_SIMPRINTS_RESEARCH_ENABLED = "IS_SIMPRINTS_RESEARCH_ENABLED";

    public AfyatekSettingsSyncIntentService() {}

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("ssssssss", "In Settings Sync Intent Service...");
        if (intent != null) {
            try {
                JSONArray settingsFromServer = syncSettingsServiceHelper.pullSettingsFromServer(CoreLibrary.getInstance().getSyncConfiguration().getSettingsSyncFilterValue());
                if (settingsFromServer.length() > 0) {
                    intent.putExtra(AllConstants.INTENT_KEY.SYNC_TOTAL_RECORDS, settingsFromServer.length());
                    updateSimprintsResearchEnable(settingsFromServer);
                // When the research enabled field is not yet reset, set it and default it to true
                } else if (!preferences.contains(IS_SIMPRINTS_RESEARCH_ENABLED)){
                    preferences.edit().putBoolean(IS_SIMPRINTS_RESEARCH_ENABLED, true).commit();
                }

            } catch (Exception e) {
                logError(TAG + " Error fetching client settings");
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Context context = CoreLibrary.getInstance().context();
        syncSettingsServiceHelper = new SyncSettingsServiceHelper(context.configuration().dristhiBaseURL(), context.getHttpAgent());
        preferences = getApplication().getSharedPreferences("AllSharedPreferences", MODE_PRIVATE);
    }

    private void updateSimprintsResearchEnable(JSONArray settingsFromServer) {
        boolean activate = false;
        try {
            JSONObject latestConfigs = settingsFromServer.getJSONObject(settingsFromServer.length() -1);
            String serverVersion = latestConfigs.getString("serverVersion");
            CoreLibrary.getInstance().context().allSharedPreferences().updateLastSettingsSyncTimeStamp(!TextUtils.isEmpty(serverVersion) ? Long.valueOf(serverVersion) : 0l);
            CoreLibrary.getInstance().context().allSettings().put(AllSharedPreferences.LAST_SETTINGS_SYNC_TIMESTAMP, serverVersion);
            JSONArray settings = latestConfigs.getJSONArray("settings");
            JSONObject setting = settings.getJSONObject(0);
            int value = Integer.parseInt(setting.getString("value"));
            if (value == 1) {
                activate = true;
            }

            preferences.edit().putBoolean(IS_SIMPRINTS_RESEARCH_ENABLED, activate).commit();

        } catch (JSONException e) {
            Timber.e(e);
        }
    }

}

package org.smartregister.chw.model;

import android.util.Pair;

import org.json.JSONObject;
import org.smartregister.chw.contract.AdolescentRegisterContract;
import org.smartregister.chw.util.JsonFormUtils;
import org.smartregister.chw.util.Utils;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;

public class AdolescentRegisterModel implements AdolescentRegisterContract.Model {
    @Override
    public JSONObject getFormAsJson(String formName, String entityId, String currentLocationId) throws Exception {
        return null;
    }

    @Override
    public Pair<Client, Event> processRegistration(String jsonString) {
        return JsonFormUtils.processAdolescentRegistrationForm(Utils.context().allSharedPreferences(), jsonString);
    }
}

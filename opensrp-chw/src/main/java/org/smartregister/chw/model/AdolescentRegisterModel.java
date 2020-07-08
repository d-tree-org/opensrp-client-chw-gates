package org.smartregister.chw.model;

import org.json.JSONObject;
import org.smartregister.chw.contract.AdolescentRegisterContract;

public class AdolescentRegisterModel implements AdolescentRegisterContract.Model {
    @Override
    public JSONObject getFormAsJson(String formName, String entityId, String currentLocationId) throws Exception {
        return null;
    }
}

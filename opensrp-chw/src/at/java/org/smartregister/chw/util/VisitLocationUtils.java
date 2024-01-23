package org.smartregister.chw.util;

import android.content.SharedPreferences;
import android.location.Location;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.repository.AllSharedPreferences;

/**
 * Author issyzac on 21/11/2023
 */
public class VisitLocationUtils {

    private static final String GPS = "gps";
    private static final String CONCEPT = "concept";
    private static final String OPENMRS_DATA_TYPE = "openmrs_data_type";
    private static final String TEXT_DATA_TYPE = "text";

    private static final String VISIT_LOC_LAT = "visit_location_latitude";
    private static final String VISIT_LOC_LONG = "visit_location_longitude";

    private static AllSharedPreferences sharedPreferences = org.smartregister.util.Utils.getAllSharedPreferences();

    public static String updateWithCurrentGpsLocation(String jsonString) {

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONObject stepOne = jsonObject.getJSONObject(JsonFormUtils.STEP1);
            JSONArray jsonArray = stepOne.getJSONArray(JsonFormUtils.FIELDS);

            JSONObject formGpsLocationObject = new JSONObject();
            formGpsLocationObject.put(JsonFormConstants.KEY, GPS);
            formGpsLocationObject.put(JsonFormConstants.OPENMRS_ENTITY_PARENT, "");
            formGpsLocationObject.put(JsonFormConstants.OPENMRS_ENTITY, CONCEPT);
            formGpsLocationObject.put(JsonFormConstants.OPENMRS_ENTITY_ID, "163277AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
            formGpsLocationObject.put(OPENMRS_DATA_TYPE, TEXT_DATA_TYPE);
            formGpsLocationObject.put(JsonFormConstants.TYPE, GPS);
            formGpsLocationObject.put(JsonFormConstants.VALUE, getVisitLocationLatitude() + " " + getVisitLocationLongitude());

            jsonArray.put(formGpsLocationObject);

            return jsonObject.toString();

        } catch (JSONException e) {

            e.printStackTrace();
            return jsonString;
        }
    }

    public static void updateLocationInPreference(Location visitLocation){
        sharedPreferences.savePreference(VISIT_LOC_LAT, String.valueOf(visitLocation.getLatitude()));
        sharedPreferences.savePreference(VISIT_LOC_LONG, String.valueOf(visitLocation.getLongitude()));
    }

    public static String getVisitLocationLatitude(){
        return sharedPreferences.getPreference(VISIT_LOC_LAT);
    }

    public static String getVisitLocationLongitude(){
        return sharedPreferences.getPreference(VISIT_LOC_LONG);
    }

}

package org.smartregister.chw.action_helper;

import android.content.Context;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.util.JsonFormUtils;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * Author issyzac on 23/11/2023
 */
public class MedicationInUseActionHelper extends org.smartregister.chw.actionhelper.DangerSignsAction {

    private Context context;
    private String usingMedication;
    private String jsonString;
    private ClientType clientType;

    String ANC_MEDICATIONS = "medication_currently_using_anc";
    String PNC_MEDICATIONS = "medication_currently_using_pnc";
    String CHILD_MEDICATIONS = "medication_currently_using_child";
    String ADOLESCENT_MEDICATIONS = "medication_currently_using_adolescent";

    public MedicationInUseActionHelper(ClientType clientType){
        this.clientType = clientType;
    }

    @Override
    public void onJsonFormLoaded(String s, Context context, Map<String, List<VisitDetail>> map) {
        this.context = context;
        this.jsonString = s;
    }

    @Override
    public String getPreProcessed() {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray fields = JsonFormUtils.fields(jsonObject);

            switch (clientType){
                case ANC:
                    JsonFormUtils.getFieldJSONObject(fields, PNC_MEDICATIONS).put(JsonFormConstants.TYPE, JsonFormConstants.HIDDEN);
                    JsonFormUtils.getFieldJSONObject(fields, CHILD_MEDICATIONS).put(JsonFormConstants.TYPE, JsonFormConstants.HIDDEN);
                    JsonFormUtils.getFieldJSONObject(fields, ADOLESCENT_MEDICATIONS).put(JsonFormConstants.TYPE, JsonFormConstants.HIDDEN);
                    break;
                case PNC:
                    JsonFormUtils.getFieldJSONObject(fields, ANC_MEDICATIONS).put(JsonFormConstants.TYPE, JsonFormConstants.HIDDEN);
                    JsonFormUtils.getFieldJSONObject(fields, CHILD_MEDICATIONS).put(JsonFormConstants.TYPE, JsonFormConstants.HIDDEN);
                    JsonFormUtils.getFieldJSONObject(fields, ADOLESCENT_MEDICATIONS).put(JsonFormConstants.TYPE, JsonFormConstants.HIDDEN);
                    break;
                case CHILD:
                    JsonFormUtils.getFieldJSONObject(fields, PNC_MEDICATIONS).put(JsonFormConstants.TYPE, JsonFormConstants.HIDDEN);
                    JsonFormUtils.getFieldJSONObject(fields, ANC_MEDICATIONS).put(JsonFormConstants.TYPE, JsonFormConstants.HIDDEN);
                    JsonFormUtils.getFieldJSONObject(fields, ADOLESCENT_MEDICATIONS).put(JsonFormConstants.TYPE, JsonFormConstants.HIDDEN);
                    break;
                case ADOLESCENT:
                    JsonFormUtils.getFieldJSONObject(fields, PNC_MEDICATIONS).put(JsonFormConstants.TYPE, JsonFormConstants.HIDDEN);
                    JsonFormUtils.getFieldJSONObject(fields, CHILD_MEDICATIONS).put(JsonFormConstants.TYPE, JsonFormConstants.HIDDEN);
                    JsonFormUtils.getFieldJSONObject(fields, ANC_MEDICATIONS).put(JsonFormConstants.TYPE, JsonFormConstants.HIDDEN);
                    break;
                default:
                    return jsonString;
            }

            return jsonObject.toString();

        }catch (JSONException e){
            e.printStackTrace();
        }
        return jsonString;
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            usingMedication = JsonFormUtils.getValue(jsonObject, "using_medication");
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    @Override
    public BaseAncHomeVisitAction.ScheduleStatus getPreProcessedStatus() {
        return null;
    }

    @Override
    public String getPreProcessedSubTitle() {
        return null;
    }

    @Override
    public String evaluateSubTitle() {
        return "";
    }

    @Override
    public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
        if (!StringUtils.isBlank(usingMedication)) {
            return BaseAncHomeVisitAction.Status.COMPLETED;
        } else {
            return BaseAncHomeVisitAction.Status.PENDING;
        }
    }

    @Override
    public void onPayloadReceived(BaseAncHomeVisitAction baseAncHomeVisitAction) {
        Timber.v("onPayloadReceived");
    }

    public enum ClientType {
        ANC,
        PNC,
        CHILD,
        ADOLESCENT
    }

}
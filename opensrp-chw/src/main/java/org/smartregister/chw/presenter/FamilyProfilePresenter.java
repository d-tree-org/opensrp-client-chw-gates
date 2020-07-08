package org.smartregister.chw.presenter;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.chw.core.contract.FamilyProfileExtendedContract;
import org.smartregister.chw.core.domain.FamilyMember;
import org.smartregister.chw.core.model.CoreChildRegisterModel;
import org.smartregister.chw.core.presenter.CoreFamilyProfilePresenter;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.interactor.FamilyChangeContractInteractor;
import org.smartregister.chw.interactor.FamilyProfileInteractor;
import org.smartregister.chw.model.ChildRegisterModel;
import org.smartregister.family.contract.FamilyProfileContract;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.view.LocationPickerView;

import timber.log.Timber;

import static org.smartregister.util.JsonFormUtils.getFieldValue;


public class FamilyProfilePresenter extends CoreFamilyProfilePresenter {

    public FamilyProfilePresenter(FamilyProfileExtendedContract.View view, FamilyProfileContract.Model model, String familyBaseEntityId, String familyHead, String primaryCaregiver, String familyName) {
        super(view, model, familyBaseEntityId, familyHead, primaryCaregiver, familyName);
        interactor = new FamilyProfileInteractor();
        verifyHasPhone();
    }

    @Override
    protected CoreChildRegisterModel getChildRegisterModel() {
        return new ChildRegisterModel();
    }


    @Override
    public boolean updatePrimaryCareGiver(Context context, String jsonString, String familyBaseEntityId, String entityID) {

        boolean res = false;
        try {
            FamilyMember member = CoreJsonFormUtils.getFamilyMemberFromRegistrationForm(jsonString, familyBaseEntityId, entityID);
            if (member != null && member.getPrimaryCareGiver()) {
                LocationPickerView lpv = new LocationPickerView(context);
                lpv.init();
                String lastLocationId = LocationHelper.getInstance().getOpenMrsLocationId(lpv.getSelectedItem());

                new FamilyChangeContractInteractor().updateFamilyRelations(context, member, lastLocationId);
                res = true;
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        return res;
    }

    @Override
    public String saveChwFamilyMember(String jsonString) {
        try {
            getView().showProgressDialog(org.smartregister.family.R.string.saving_dialog_title);

            if (isAdolescent(jsonString)) {
                jsonString = updateAdolescentEventType(jsonString);
            }

            FamilyEventClient familyEventClient = model.processMemberRegistration(jsonString, familyBaseEntityId);
            if (familyEventClient == null) {
                return null;
            }

            interactor.saveRegistration(familyEventClient, jsonString, false, this);
            return familyEventClient.getClient().getBaseEntityId();
        } catch (Exception e) {
            Timber.e(e);
        }
        return null;
    }

    private boolean isAdolescent(String jsonString) {

        String age = getFieldValue(jsonString, "age_calculated");
        Float ageInt = Float.parseFloat(age);

        return (ageInt >= 13 && ageInt <= 19);
    }

    private String updateAdolescentEventType(String jsonString) {

        try {
            JSONObject jsonForm = new JSONObject(jsonString);
            jsonForm.putOpt("encounter_type", "Adolescent Registration");
            return jsonForm.toString();
        } catch (JSONException e) {
            Timber.e(e);
        }

        return null;
    }

}

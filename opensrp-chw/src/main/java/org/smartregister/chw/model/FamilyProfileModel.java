package org.smartregister.chw.model;

import org.smartregister.chw.util.JsonFormUtils;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.family.model.BaseFamilyProfileModel;

public class FamilyProfileModel extends BaseFamilyProfileModel {
    public FamilyProfileModel(String familyName) {
        super(familyName);
    }

    @Override
    public void updateWra(FamilyEventClient familyEventClient) {
        new FamilyProfileModelFlv().updateWra(familyEventClient);
    }

    @Override
    public FamilyEventClient processMemberRegistration(String jsonString, String familyBaseEntityId) {
        FamilyEventClient familyEventClient = JsonFormUtils.processFamilyMemberRegistrationForm(FamilyLibrary.getInstance().context().allSharedPreferences(), jsonString, familyBaseEntityId);
        if (familyEventClient == null) {
            return null;
        } else {
            this.updateWra(familyEventClient);
            return familyEventClient;
        }
    }

    public interface Flavor {
        void updateWra(FamilyEventClient familyEventClient);
    }
}

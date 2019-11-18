package org.smartregister.chw.model;

import org.smartregister.chw.core.utils.FormUtils;
import org.smartregister.family.domain.FamilyEventClient;

/**
 * Author : Isaya Mollel on 2019-11-12.
 */
public class FamilyProfileModelFlv implements FamilyProfileModel.Flavor {

    @Override
    public void updateWra(FamilyEventClient familyEventClient) {
        FormUtils.updateWraForBA(familyEventClient);
    }
}

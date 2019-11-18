package org.smartregister.chw.provider;

/**
 * Author : Isaya Mollel on 2019-11-12.
 */
public class FamilyRegisterProviderFlv extends DefaultFamilyRegisterProviderFlv {

    @Override
    public boolean hasMalaria() {
        return true; //TODO: Rodgers, Why is this true all the time?
    }
}

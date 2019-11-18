package org.smartregister.chw.provider;

public class FamilyRegisterProviderFlv extends DefaultFamilyRegisterProviderFlv {

    @Override
    public boolean hasMalaria() {
        return true; //TODO: Rodgers, Why is this true all the time?
    }
}

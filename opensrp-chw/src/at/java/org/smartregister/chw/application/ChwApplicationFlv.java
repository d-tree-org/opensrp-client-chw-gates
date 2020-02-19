package org.smartregister.chw.application;


public class ChwApplicationFlv extends DefaultChwApplicationFlv {
    @Override
    public boolean hasReferrals() {
        return true;
    }

    @Override
    public boolean hasWashCheck() {
        return false;
    }
}
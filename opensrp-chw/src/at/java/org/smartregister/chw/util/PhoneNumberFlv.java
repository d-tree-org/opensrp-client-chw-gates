package org.smartregister.chw.util;

import org.smartregister.chw.core.adapter.MemberAdapter;

/**
 * Author : Isaya Mollel on 2019-11-12.
 */
public class PhoneNumberFlv implements MemberAdapter.Flavor {

    @Override
    public boolean isPhoneNumberLength16Digit() {
        return false;
    }
}

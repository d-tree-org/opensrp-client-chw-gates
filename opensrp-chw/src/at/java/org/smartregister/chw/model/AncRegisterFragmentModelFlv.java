package org.smartregister.chw.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Author : Isaya Mollel on 2019-11-12.
 */
public class AncRegisterFragmentModelFlv implements AncRegisterFragmentModel.Flavor {

    @Override
    public Set<String> mainColumns(String s) {
        return new HashSet<>();
    }
}

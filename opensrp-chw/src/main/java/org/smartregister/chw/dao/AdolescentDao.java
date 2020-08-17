package org.smartregister.chw.dao;

import android.content.ContentValues;

import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.dao.AbstractDao;
import org.smartregister.family.util.DBConstants;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdolescentDao extends AbstractDao {

    public static boolean isAdolescentMember(String baseEntityID) {

        String sql = "select count(*) count from ec_adolescent where base_entity_id = '" + baseEntityID + "' and is_closed = 0";

        DataMap<Integer> dataMap = cursor -> getCursorIntValue(cursor, "count");

        List<Integer> res = readData(sql, dataMap);

        if (res == null || res.size() != 1) {
            return false;
        }

        return res.get(0) > 0;
    }

    public static void closeAdolescentMember(String baseEntityID) {
        String sql = "update ec_adolescent set is_closed = 1 where base_entity_id = '" + baseEntityID + "'";
        updateDB(sql);
    }

    public static void addAdolescentMember(CommonPersonObject client) {

        Map<String, String> clientDetails = client.getColumnmaps();
        ContentValues values = getContentValuesForAdolescents(clientDetails);
        values.put("is_closed", client.getClosed());
        values.put("id", client.getCaseId());
        Date currentTime = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault());
        values.put(ChildDBConstants.KEY.DATE_CREATED, dateFormat.format(currentTime));

        CoreChwApplication.getInstance().getRepository().getWritableDatabase().insert(CoreConstants.TABLE_NAME.ADOLESCENT, null, values);

    }

    private static ContentValues getContentValuesForAdolescents(Map<String, String> clientDetails) {

        ContentValues values = new ContentValues();
        values.put(DBConstants.KEY.BASE_ENTITY_ID, clientDetails.get(DBConstants.KEY.BASE_ENTITY_ID));
        values.put(DBConstants.KEY.UNIQUE_ID, clientDetails.get(DBConstants.KEY.UNIQUE_ID));
        values.put(DBConstants.KEY.RELATIONAL_ID, clientDetails.get(DBConstants.KEY.RELATIONAL_ID));
        values.put(DBConstants.KEY.FIRST_NAME, clientDetails.get(DBConstants.KEY.FIRST_NAME));
        values.put(DBConstants.KEY.MIDDLE_NAME, clientDetails.get(DBConstants.KEY.MIDDLE_NAME));
        values.put(DBConstants.KEY.LAST_NAME, clientDetails.get(DBConstants.KEY.LAST_NAME));
        values.put(DBConstants.KEY.DOB, clientDetails.get(DBConstants.KEY.DOB));
        values.put(DBConstants.KEY.DOD, clientDetails.get(DBConstants.KEY.DOD));
        values.put("dob_unknown", clientDetails.get("dob_unknown"));
        values.put(DBConstants.KEY.GENDER, clientDetails.get(DBConstants.KEY.GENDER));
        values.put("physically_challenged", clientDetails.get("disabilities"));
        values.put(DBConstants.KEY.LAST_INTERACTED_WITH, clientDetails.get(DBConstants.KEY.LAST_INTERACTED_WITH));
        values.put(DBConstants.KEY.DATE_REMOVED, clientDetails.get(DBConstants.KEY.DATE_REMOVED));

        return values;
    }

}

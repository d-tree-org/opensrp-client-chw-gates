package org.smartregister.chw.dao;

import org.smartregister.dao.AbstractDao;

import java.text.ParseException;
import java.util.List;

import timber.log.Timber;

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

    public static Long getAdolescentDateCreated(String baseEntityID) {
        String sql = "select date_created from ec_adolescent where base_entity_id = '" + baseEntityID + "' COLLATE NOCASE ";

        DataMap<String> dataMap = c -> getCursorValue(c, "date_created");

        List<String> values = AbstractDao.readData(sql, dataMap);

        if (values == null || values.size() == 0) return null;

        try {
            return getDobDateFormat().parse(values.get(0)).getTime();
        } catch (ParseException e) {
            Timber.e(e);
            return null;
        }

    }

}

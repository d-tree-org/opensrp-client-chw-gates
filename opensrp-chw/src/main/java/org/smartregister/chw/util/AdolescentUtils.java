package org.smartregister.chw.util;

import android.content.Context;

import com.google.gson.Gson;

import org.jeasy.rules.api.Rules;
import org.json.JSONException;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.util.JsonFormUtils;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.dao.VisitDao;
import org.smartregister.chw.core.domain.VisitSummary;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.dao.AdolescentDao;
import org.smartregister.chw.model.AdolescentVisit;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.rule.AdolescentVisitAlertRule;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.family.util.DBConstants;

import java.util.ArrayList;
import java.util.Map;

import timber.log.Timber;

public class AdolescentUtils {

    public static String mainSelect(String tableName, String familyTableName, String familyMemberTableName, String mainCondition) {
        return mainSelectRegisterWithoutGroupby(tableName, familyTableName, familyMemberTableName, tableName + "." + DBConstants.KEY.BASE_ENTITY_ID + " = '" + mainCondition + "'");
    }

    public static String mainSelectRegisterWithoutGroupby(String tableName, String familyTableName, String familyMemberTableName, String mainCondition) {
        SmartRegisterQueryBuilder queryBUilder = new SmartRegisterQueryBuilder();
        queryBUilder.SelectInitiateMainTable(tableName, mainColumns(tableName, familyTableName, familyMemberTableName));
        queryBUilder.customJoin("LEFT JOIN " + familyTableName + " ON  " + tableName + "." + DBConstants.KEY.RELATIONAL_ID + " = " + familyTableName + ".id COLLATE NOCASE ");
        queryBUilder.customJoin("LEFT JOIN " + familyMemberTableName + " ON  " + familyMemberTableName + "." + DBConstants.KEY.BASE_ENTITY_ID + " = " + familyTableName + ".primary_caregiver COLLATE NOCASE ");

        return queryBUilder.mainCondition(mainCondition);
    }

    public static String[] mainColumns(String tableName, String familyTable, String familyMemberTable) {
        ArrayList<String> columnList = new ArrayList<>();
        columnList.add(tableName + "." + DBConstants.KEY.RELATIONAL_ID + " as " + org.smartregister.chw.core.utils.ChildDBConstants.KEY.RELATIONAL_ID);
        columnList.add(tableName + "." + DBConstants.KEY.LAST_INTERACTED_WITH);
        columnList.add(tableName + "." + DBConstants.KEY.BASE_ENTITY_ID);
        columnList.add(tableName + "." + DBConstants.KEY.FIRST_NAME);
        columnList.add(tableName + "." + DBConstants.KEY.MIDDLE_NAME);
        columnList.add(familyMemberTable + "." + DBConstants.KEY.FIRST_NAME + " as " + org.smartregister.chw.core.utils.ChildDBConstants.KEY.FAMILY_FIRST_NAME);
        columnList.add(familyMemberTable + "." + DBConstants.KEY.LAST_NAME + " as " + org.smartregister.chw.core.utils.ChildDBConstants.KEY.FAMILY_LAST_NAME);
        columnList.add(familyMemberTable + "." + DBConstants.KEY.MIDDLE_NAME + " as " + org.smartregister.chw.core.utils.ChildDBConstants.KEY.FAMILY_MIDDLE_NAME);
        columnList.add(familyMemberTable + "." + org.smartregister.chw.core.utils.ChildDBConstants.PHONE_NUMBER + " as " + org.smartregister.chw.core.utils.ChildDBConstants.KEY.FAMILY_MEMBER_PHONENUMBER);
        columnList.add(familyMemberTable + "." + org.smartregister.chw.core.utils.ChildDBConstants.OTHER_PHONE_NUMBER + " as " + org.smartregister.chw.core.utils.ChildDBConstants.KEY.FAMILY_MEMBER_PHONENUMBER_OTHER);
        columnList.add(familyTable + "." + DBConstants.KEY.VILLAGE_TOWN + " as " + org.smartregister.chw.core.utils.ChildDBConstants.KEY.FAMILY_HOME_ADDRESS);
        columnList.add(tableName + "." + DBConstants.KEY.LAST_NAME);
        columnList.add(tableName + "." + DBConstants.KEY.UNIQUE_ID);
        columnList.add(tableName + "." + DBConstants.KEY.GENDER);
        columnList.add(tableName + "." + DBConstants.KEY.DOB);
        columnList.add(tableName + "." + org.smartregister.family.util.Constants.JSON_FORM_KEY.DOB_UNKNOWN);
        columnList.add(tableName + "." + org.smartregister.chw.core.utils.ChildDBConstants.KEY.LAST_HOME_VISIT);
        columnList.add(tableName + "." + org.smartregister.chw.core.utils.ChildDBConstants.KEY.VISIT_NOT_DONE);
        columnList.add(tableName + "." + org.smartregister.chw.core.utils.ChildDBConstants.KEY.CHILD_PHYSICAL_CHANGE);
        columnList.add(tableName + "." + org.smartregister.chw.core.utils.ChildDBConstants.KEY.ILLNESS_DATE);
        columnList.add(tableName + "." + org.smartregister.chw.core.utils.ChildDBConstants.KEY.ILLNESS_DESCRIPTION);
        columnList.add(tableName + "." + org.smartregister.chw.core.utils.ChildDBConstants.KEY.DATE_CREATED);
        columnList.add(tableName + "." + ChildDBConstants.KEY.ILLNESS_ACTION);
        return columnList.toArray(new String[columnList.size()]);
    }

    public static AdolescentVisit getAdolescentVisitStatus(Context context, String yearOfBirth, long lastVisitDate, long visitNotDate, long dateCreated) {
        AdolescentVisitAlertRule homeAlertRule = new AdolescentVisitAlertRule(context, yearOfBirth, lastVisitDate, visitNotDate, dateCreated);
        CoreChwApplication.getInstance().getRulesEngineHelper().getButtonAlertStatus(homeAlertRule, "adolescent-home-visit-rules.yml");
        return getAdolescentVisitStatus(homeAlertRule, lastVisitDate);
    }

    public static AdolescentVisit getAdolescentVisitStatus(Context context, Rules rules, String yearOfBirth, long lastVisitDate, long visitNotDate, long dateCreated) {
        AdolescentVisitAlertRule homeAlertRule = new AdolescentVisitAlertRule(context, yearOfBirth, lastVisitDate, visitNotDate, dateCreated);
        CoreChwApplication.getInstance().getRulesEngineHelper().getButtonAlertStatus(homeAlertRule, rules);
        return getAdolescentVisitStatus(homeAlertRule, lastVisitDate);
    }

    public static AdolescentVisit getAdolescentVisitStatus(AdolescentVisitAlertRule homeAlertRule, long lastVisitDate) {
        AdolescentVisit adolescentVisit = new AdolescentVisit();
        adolescentVisit.setVisitStatus(homeAlertRule.buttonStatus);
        adolescentVisit.setNoOfMonthDue(homeAlertRule.noOfMonthDue);
        adolescentVisit.setLastVisitDays(homeAlertRule.noOfDayDue);
        adolescentVisit.setLastVisitMonthName(homeAlertRule.visitMonthName);
        adolescentVisit.setLastVisitTime(lastVisitDate);
        return adolescentVisit;
    }

    public static AdolescentHomeVisit getAdolescentLastHomeVisit(String baseEntityId) {
        AdolescentHomeVisit adolescentHomeVisit = new AdolescentHomeVisit();

        Map<String, VisitSummary> map = VisitDao.getVisitSummary(baseEntityId);
        if (map == null) {
            return adolescentHomeVisit;
        }

        VisitSummary notDone = map.get(Constants.ADOLESCENT_HOME_VISIT_NOT_DONE);
        VisitSummary lastVisit = map.get(Constants.ADOLESCENT_HOME_VISIT_DONE);

        if (lastVisit != null) {
            adolescentHomeVisit.setLastHomeVisitDate(lastVisit.getVisitDate().getTime());
        }

        if (notDone != null) {
            adolescentHomeVisit.setVisitNotDoneDate(notDone.getVisitDate().getTime());
        }


        Long datecreated = AdolescentDao.getAdolescentDateCreated(baseEntityId);
        if (datecreated != null) {
            adolescentHomeVisit.setDateCreated(datecreated);
        }

        return adolescentHomeVisit;
    }

    public static void undoVisitNotDone(String entityID) {
        AdolescentDao.undoAdolescentVisitNotDone(entityID);
    }

    public static void visitNotDone(String entityId) {
        try {
            Event event = org.smartregister.chw.anc.util.JsonFormUtils.createUntaggedEvent(entityId, Constants.ADOLESCENT_HOME_VISIT_NOT_DONE, CoreConstants.TABLE_NAME.ADOLESCENT);
            Visit visit = NCUtils.eventToVisit(event, JsonFormUtils.generateRandomUUIDString());
            visit.setPreProcessedJson(new Gson().toJson(event));
            AncLibrary.getInstance().visitRepository().addVisit(visit);
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

}
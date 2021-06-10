package org.smartregister.chw.task;

import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.contract.ScheduleTask;
import org.smartregister.chw.core.domain.BaseScheduleTask;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.dao.PersonDao;
import org.smartregister.chw.rule.AdolescentVisitAlertRule;
import org.smartregister.chw.util.AdolescentHomeVisit;
import org.smartregister.chw.util.AdolescentUtils;
import org.smartregister.chw.util.Constants;

import java.util.Date;
import java.util.List;

public class AdolescentHomeVisitScheduler extends BaseTaskExecutor {
    @Override
    public void resetSchedule(String baseEntityID, String scheduleName) {
        super.resetSchedule(baseEntityID, scheduleName);
        ChwApplication.getInstance().getScheduleRepository().deleteScheduleByGroup(getScheduleGroup(), baseEntityID);
    }

    @Override
    public List<ScheduleTask> generateTasks(String baseEntityID, String eventName, Date eventDate) {
        BaseScheduleTask baseScheduleTask = prepareNewTaskObject(baseEntityID);

        AdolescentHomeVisit adolescentHomeVisit = AdolescentUtils.getAdolescentLastHomeVisit(baseEntityID);
        String yearOfBirth = PersonDao.getDob(baseEntityID);

        AdolescentVisitAlertRule alertRule = new AdolescentVisitAlertRule(
                ChwApplication.getInstance().getApplicationContext(),
                yearOfBirth,
                adolescentHomeVisit.getLastHomeVisitDate(),
                adolescentHomeVisit.getVisitNotDoneDate(),
                adolescentHomeVisit.getDateCreated()
        );

        CoreChwApplication.getInstance().getRulesEngineHelper().getButtonAlertStatus(alertRule, Constants.ADOLESCENT_HOMEVISIT_RULE_FILE);

        baseScheduleTask.setScheduleDueDate(alertRule.getDueDate());
        baseScheduleTask.setScheduleNotDoneDate(alertRule.getNotDoneDate());
        baseScheduleTask.setScheduleExpiryDate(alertRule.getExpiryDate());
        baseScheduleTask.setScheduleCompletionDate(alertRule.getCompletionDate());
        baseScheduleTask.setScheduleOverDueDate(alertRule.getOverDueDate());

        return toScheduleList(baseScheduleTask);
    }

    @Override
    public String getScheduleName() {
        return Constants.ADOLESCENT_VISIT;
    }

    @Override
    public String getScheduleGroup() {
        return CoreConstants.SCHEDULE_GROUPS.HOME_VISIT;
    }
}

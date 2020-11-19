package org.smartregister.chw.task;

import org.joda.time.LocalDate;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.core.contract.ScheduleTask;
import org.smartregister.chw.core.domain.BaseScheduleTask;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreReferralUtils;
import org.smartregister.domain.Task;

import java.util.Date;
import java.util.List;

public class ReferralVisitScheduler extends BaseTaskExecutor {

    @Override
    public void resetSchedule(String baseEntityID, String scheduleName) {
        super.resetSchedule(baseEntityID, scheduleName);
        ChwApplication.getInstance().getScheduleRepository().deleteScheduleByGroup(getScheduleGroup(), baseEntityID);
    }

    @Override
    public List<ScheduleTask> generateTasks(String baseEntityID, String eventName, Date eventDate) {
        BaseScheduleTask baseScheduleTask = prepareNewTaskObject(baseEntityID);

        Task recentTask = CoreReferralUtils.getTaskForEntity(baseEntityID, true);

        if(recentTask!=null) {
            LocalDate localDate = new LocalDate(recentTask.getAuthoredOn());

            // due date is the start of the schedule
            baseScheduleTask.setScheduleDueDate(localDate.toDate());

            // expiry date
            baseScheduleTask.setScheduleExpiryDate(localDate.plusYears(5).toDate());

            // completion date
            baseScheduleTask.setScheduleExpiryDate(localDate.plusDays(30).toDate());

            // overdue date
            if(recentTask.getPriority() == 1) {
                baseScheduleTask.setScheduleOverDueDate(localDate.plusDays(3).toDate());
            } else {
                baseScheduleTask.setScheduleOverDueDate(localDate.plusDays(1).toDate());
            }
        }
        return toScheduleList(baseScheduleTask);
    }

    @Override
    public String getScheduleName() {
        return CoreConstants.SCHEDULE_TYPES.REFERRAL_FOLLOWUP_VISIT;
    }

    @Override
    public String getScheduleGroup() {
        return CoreConstants.SCHEDULE_TYPES.REFERRAL_FOLLOWUP_VISIT;
    }
}

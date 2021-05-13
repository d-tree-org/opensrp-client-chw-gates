package org.smartregister.chw.rule;

import android.content.Context;

import org.joda.time.LocalDate;
import org.smartregister.chw.core.rule.HomeAlertRule;

/**
 * Created by ndegwamartin on 09/11/2018.
 */
public class ChildVisitAlertRule extends HomeAlertRule {

    public ChildVisitAlertRule(Context context, String yearOfBirthString, long lastVisitDateLong, long visitNotDoneValue, long dateCreatedLong) {
        super(context, yearOfBirthString, lastVisitDateLong, visitNotDoneValue, dateCreatedLong);
    }

    /* duration is 1->monthly, 3-quarterly, 6-semi-annually */
    private boolean IsGreaterThanPeriod(LocalDate lastVisit, LocalDate todayDate, int duration) {
        return getMonthsDifference(lastVisit, todayDate) > duration;
    }

    public boolean isDueWithinPeriod(int duration) {
        if (getLastVisitDate() == null) {
            return true;
        }
        return IsGreaterThanPeriod(getLastVisitDate(), getTodayDate(), duration);
    }

    public boolean isOverdueWithinMonth(Integer duration) {
        if (getLastVisitDate() == null) {
            return IsGreaterThanPeriod(getDateCreated(), getTodayDate(), duration);
        }
        return IsGreaterThanPeriod(getLastVisitDate(), getTodayDate(), duration);
    }

    public boolean isVisitDone(Integer duration) {
        if (getLastVisitDate() == null) {
            return false;
        }
        return !IsGreaterThanPeriod(getLastVisitDate(), getTodayDate(), duration);
    }
}
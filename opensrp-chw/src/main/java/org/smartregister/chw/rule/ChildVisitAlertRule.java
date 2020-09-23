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

    /* duration is 1->monthly, 3-quaterly, 6-semi-annualy */
    public boolean isVisitWithinThisPeriod(int duration) {
        return (getLastVisitDate() != null) && isVisitThisPeriod(getLastVisitDate(), getTodayDate(), duration);
    }

    private boolean isVisitThisPeriod(LocalDate lastVisit, LocalDate todayDate, int duration) {
        return getMonthsDifference(lastVisit, todayDate) < duration;
    }

    public boolean isDueWithinPeriod(int duration) {
        if (getLastVisitDate() == null) {
            return !isVisitThisPeriod(getDateCreated(), getTodayDate(), duration);
        }

        return !isVisitThisPeriod(getLastVisitDate(), getTodayDate(), duration);
    }
}
package org.smartregister.chw.model;

public class AdolescentVisit {
    private String visitStatus;
    private long lastVisitTime;
    private String lastVisitMonthName;
    private String lastVisitDays;

    private String noOfMonthDue;

    public String getVisitStatus() {
        return visitStatus;
    }

    public void setVisitStatus(String visitStatus) {
        this.visitStatus = visitStatus;
    }

    public long getLastVisitTime() {
        return lastVisitTime;
    }

    public void setLastVisitTime(long lastVisitTime) {
        this.lastVisitTime = lastVisitTime;
    }

    public String getLastVisitMonthName() {
        return lastVisitMonthName;
    }

    public void setLastVisitMonthName(String lastVisitMonthName) {
        this.lastVisitMonthName = lastVisitMonthName;
    }

    public String getLastVisitDays() {
        return lastVisitDays;
    }

    public void setLastVisitDays(String lastVisitDays) {
        this.lastVisitDays = lastVisitDays;
    }

    public String getNoOfMonthDue() {
        return noOfMonthDue;
    }

    public void setNoOfMonthDue(String noOfMonthDue) {
        this.noOfMonthDue = noOfMonthDue;
    }
}

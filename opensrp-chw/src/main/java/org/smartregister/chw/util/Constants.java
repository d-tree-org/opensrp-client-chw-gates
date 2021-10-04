package org.smartregister.chw.util;

import org.smartregister.chw.core.utils.CoreConstants;

public class Constants extends CoreConstants {
    public static final String REFERRAL_TASK_FOCUS = "referral_task_focus";
    public static String pregnancyOutcome = "preg_outcome";
    public static final String REFERRAL_TYPES = "ReferralTypes";
    public static final String ADOLESCENT_HOME_VISIT_NOT_DONE = "Adolescent Home Visit Not Done";
    public static final String ADOLESCENT_HOME_VISIT_DONE = "Adolescent Home Visit";
    public static final String ADOLESCENT_HOME_VISIT_NOT_DONE_UNDO = "Adolescent Home Visit Not Done Undo";
    public static final String ADOLESCENT_VISIT = "ADOLESCENT_VISIT";
    public static final String ADOLESCENT_HOMEVISIT_RULE_FILE = "adolescent-home-visit-rules.yml";
    public static final String ADOLESCENT_REGISTRATION_EVENT = "Adolescent Registration";

    public static class FORM_SUBMISSION_FIELD {
        public static String pncHfNextVisitDateFieldType = "pnc_hf_next_visit_date";

    }

    public enum FamilyRegisterOptionsUtil {Miscarriage, Other}

    public enum FamilyMemberType {ANC, PNC, Other}

    public class EncounterType {
        public static final String SICK_CHILD = "Sick Child Referral";
        public static final String PNC_REFERRAL = "PNC Referral";
        public static final String ANC_REFERRAL = "ANC Referral";
        public static final String REFERRAL_FOLLOW_UP_VISIT = "Referral Follow-up Visit";
        public static final String LINKAGE_FOLLOW_UP_VISIT = "Linkage Follow-up Visit";
    }

    public class EventsType {
        public static final String UPDATE_PNC_REGISTRATION = "Update Pnc Registration";
    }

    public static class TaskBusinessStatus {
        public static final String ATTENDED = "Attended";
        public static final String UNATTENDED = "Unattended";
    }

    public static class ANC_INTENT_KEY {
        public static final String ORIGINATES_FROM_ANC_REGISTER = "originates_from_anc_register";
    }

}

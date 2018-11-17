package uk.gov.defra.datareturns.services.crm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.datareturns.data.model.licences.Activity;

@Getter
@Setter
@Slf4j
public class CrmActivity implements CrmCall<Activity> {
    @JsonProperty("RCRActivityId")
    private String id;
    @JsonProperty("ReturnStatus")
    private String returnStatus;
    @JsonProperty("ErrorMessage")
    private String errorMessage;

    @Override
    @JsonIgnore
    public Activity getBaseEntity() {
        if ("error".equals(returnStatus)) {
            // This is a warning because the activity can be create and removed by the CRM
            log.warn("Error retrieving activity response from CRM - " + errorMessage);
            return null;
        }
        final Activity activity = new Activity();
        activity.setId(id);
        return activity;
    }

    public enum Status {
        STARTED, SUBMITTED
    }

    /**
     * This Query is used to get the contact details from the licence number
     */
    @Getter
    @Setter
    public static class CreateActivity implements CRMQuery<CrmActivity> {
        private Query query;

        public Class<CrmActivity> getEntityClass() {
            return CrmActivity.class;
        }

        public String getCRMStoredProcedureName() {
            return "defra_CreateRCRActivity";
        }

        @Getter
        @Setter
        public static class Query {
            @JsonProperty("ActivityStatus")
            private final Status status = Status.STARTED;
            @JsonProperty("ContactId")
            private String contactId;
            @JsonProperty("Season")
            private int season;
        }
    }

    /**
     * This Query is used to get the contact details from the licence number
     */
    @Getter
    @Setter
    public static class UpdateActivity implements CRMQuery<CrmActivity> {
        private Query query;

        public Class<CrmActivity> getEntityClass() {
            return CrmActivity.class;
        }

        public String getCRMStoredProcedureName() {
            return "defra_UpdateRCRActivity";
        }

        @Getter
        @Setter
        public static class Query {
            @JsonProperty("ActivityStatus")
            private final Status status = Status.SUBMITTED;
            @JsonProperty("ContactId")
            private String contactId;
            @JsonProperty("Season")
            private int season;
        }
    }

}

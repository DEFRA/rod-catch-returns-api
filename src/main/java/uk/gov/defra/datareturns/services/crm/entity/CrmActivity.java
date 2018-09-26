package uk.gov.defra.datareturns.services.crm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import uk.gov.defra.datareturns.data.model.licences.Activity;

@Getter
@Setter
@Slf4j
public class CrmActivity implements CrmEntity<Activity> {
    public enum Status { submitted, started }

    @JsonProperty("RCRActivityId")
    private String id;

    @JsonProperty("ReturnStatus")
    private String returnStatus;

    @JsonProperty("ErrorMessage")
    private String errorMessage;

    @Override
    @JsonIgnore
    public Activity getBaseEntity() {
        if (returnStatus.equals("error")) {
            // This is a warning because the activity can be create and removed by the CRM
            log.warn("Error retrieving activity response from CRM: " + errorMessage);
            return null;
        }
        Activity activity = new Activity();
        activity.setId(id);
        return activity;
    }

    /**
     * This Query is used to get the contact details from the licence number
     */
    @Getter
    @Setter
    public static class CreateActivity implements CRMQuery<CrmActivity> {

        private Query query;
        private Status status;

        public Class<CrmActivity> getEntityClass() {
            return CrmActivity.class;
        }

        public String getCRMStoredProcedureName() {
            return "defra_CreateRCRActivity";
        }

        @Getter
        @Setter
        @ToString
        public static class Query implements CRMQuery.Query {
            @JsonProperty("ContactId")
            private String contactId;

            @JsonProperty("ActivityStatus")
            private final Status status = Status.started;

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
        private Status status;

        public Class<CrmActivity> getEntityClass() {
            return CrmActivity.class;
        }

        public String getCRMStoredProcedureName() {
            return "defra_UpdateRCRActivity";
        }

        @Getter
        @Setter
        @ToString
        public static class Query implements CRMQuery.Query {
            @JsonProperty("ContactId")
            private String contactId;

            @JsonProperty("ActivityStatus")
            private final Status status = Status.submitted;

            @JsonProperty("Season")
            private int season;
        }
    }

}

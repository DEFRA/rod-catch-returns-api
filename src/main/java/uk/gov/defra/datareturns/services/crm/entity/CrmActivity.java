package uk.gov.defra.datareturns.services.crm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class CrmActivity implements CrmCall<Void> {
    @JsonProperty("ReturnStatus")
    private String returnStatus;
    @JsonProperty("ErrorMessage")
    private String errorMessage;

    @Override
    @JsonIgnore
    public Void getBaseEntity() {
        return null;
    }

    public enum Status {
        STARTED, SUBMITTED
    }

    @Getter
    @Setter
    public static class Query {
        @JsonProperty("ActivityStatus")
        private Status status;
        @JsonProperty("ContactId")
        private String contactId;
        @JsonProperty("Season")
        private int season;
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
    }
}

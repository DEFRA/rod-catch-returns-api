package uk.gov.defra.datareturns.services.crm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class CrmActivity implements CrmCall<Void> {
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
    @NoArgsConstructor
    @AllArgsConstructor(staticName = "of")
    public static class QueryParams {
        @JsonProperty("ActivityStatus")
        private Status status;
        @JsonProperty("ContactId")
        private String contactId;
        @JsonProperty("Season")
        private int season;
    }

    /**
     * This query is used to get the contact details from the licence number
     */
    @Getter
    @Setter
    public static class CreateActivity implements CRMQuery<CrmActivity> {
        private QueryParams queryParams;

        public Class<CrmActivity> getEntityClass() {
            return CrmActivity.class;
        }

        public String getCRMStoredProcedureName() {
            return "defra_CreateRCRActivity";
        }

    }

    /**
     * This query is used to get the contact details from the licence number
     */
    @Getter
    @Setter
    public static class UpdateActivity implements CRMQuery<CrmActivity> {
        private QueryParams queryParams;

        public Class<CrmActivity> getEntityClass() {
            return CrmActivity.class;
        }

        public String getCRMStoredProcedureName() {
            return "defra_UpdateRCRActivity";
        }
    }
}

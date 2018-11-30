package uk.gov.defra.datareturns.services.crm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

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
    public static class CrmActivityQuery implements CRMQuery<CrmActivity> {
        private final QueryParams queryParams;
        private final String queryName;

        public CrmActivityQuery(final Status status, final String contactId, final int season) {
            final QueryParams params = new QueryParams();
            params.setStatus(status);
            params.setContactId(contactId);
            params.setSeason(season);
            this.queryParams = params;
            if (Status.STARTED.equals(status)) {
                this.queryName = "defra_CreateRCRActivity";
            } else {
                this.queryName = "defra_UpdateRCRActivity";
            }
        }

        @Override
        public Class<CrmActivity> getEntityClass() {
            return CrmActivity.class;
        }
    }

    @Getter
    @Setter
    public static class QueryParams {
        @JsonProperty("ActivityStatus")
        private Status status;
        @JsonProperty("ContactId")
        private String contactId;
        @JsonProperty("Season")
        private int season;
    }
}

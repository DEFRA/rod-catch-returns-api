package uk.gov.defra.datareturns.services.crm.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Objects;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CrmIdentity implements CrmCall<Identity> {
    @JsonProperty("Roles")
    private String roles;

    @JsonProperty("ReturnStatus")
    private String returnStatus;

    @JsonProperty("ErrorMessage")
    private String errorMessage;

    @Override
    public Identity getBaseEntity() {
        final Identity identity = new Identity();
        identity.getRoles().addAll(Arrays.asList(Objects.toString(roles, "").split(",")));
        return identity;
    }

    /**
     * This query is used to get the user roles for an ADD entry in teh CRM
     */
    public static class IdentityQuery implements CRMQuery<CrmIdentity> {
        @Override
        public Object getQueryParams() {
            return null;
        }

        public Class<CrmIdentity> getEntityClass() {
            return CrmIdentity.class;
        }

        public String getCRMStoredProcedureName() {
            return "defra_GetRcrRolesByUser";
        }
    }
}

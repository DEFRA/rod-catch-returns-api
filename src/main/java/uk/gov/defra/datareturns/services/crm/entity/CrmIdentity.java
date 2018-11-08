package uk.gov.defra.datareturns.services.crm.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Arrays;

@Getter
@Setter
@ToString
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
        if (roles != null) {
            identity.getRoles().addAll(Arrays.asList(roles.split(",")));
        }
        return identity;
    }

    /**
     * This Query is used to get the user roles for an ADD entry in teh CRM
     */
    @Getter
    @Setter
    public static class IdentityQuery implements CRMQuery<CrmIdentity> {
        private CrmIdentity.IdentityQuery.Query query;

        public Class<CrmIdentity> getEntityClass() {
            return CrmIdentity.class;
        }

        public String getCRMStoredProcedureName() {
            return "defra_GetRcrRolesByUser";
        }
    }
}

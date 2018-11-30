package uk.gov.defra.datareturns.services.crm.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CrmRoles implements CrmCall<List<String>> {
    @JsonProperty("Roles")
    private String roles;

    @Override
    public List<String> getBaseEntity() {
        return Arrays.asList(Objects.toString(roles, "").split(","));
    }

    /**
     * This query is used to get the user roles for an identity in the CRM
     */
    public static class CrmRolesQuery implements CRMQuery<CrmRoles> {
        @Override
        public Object getQueryParams() {
            return null;
        }

        public Class<CrmRoles> getEntityClass() {
            return CrmRoles.class;
        }

        public String getQueryName() {
            return "defra_GetRcrRolesByUser";
        }
    }
}

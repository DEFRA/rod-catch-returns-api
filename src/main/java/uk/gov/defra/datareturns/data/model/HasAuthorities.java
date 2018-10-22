package uk.gov.defra.datareturns.data.model;

import java.util.Set;

public interface HasAuthorities {

    /**
     * A set of permissions that allow read access to the entity instance.
     * <p>
     * If any permission is matched against the authenticated user's {@link org.springframework.security.core.GrantedAuthority}'s for the entity
     * then access will be allowed
     *
     * @return set of permissions that allow read access to the entity instance.
     */
    Set<String> getRequiredAuthorities();
}

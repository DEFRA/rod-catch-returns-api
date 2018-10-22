package uk.gov.defra.datareturns.data.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@MappedSuperclass
@Getter
@Setter
public abstract class AbstractSecuredEntity extends AbstractBaseEntity implements HasAuthorities {
    /**
     * Space delimited list of authorities to allow read access to the entity
     */
    @Column
    private String authorities;

    @Override
    @JsonIgnore
    public Set<String> getRequiredAuthorities() {
        if (StringUtils.isNotBlank(getAuthorities())) {
            return new HashSet<>(Arrays.asList(getAuthorities().split("/[ ]+/")));
        }
        return SetUtils.emptySet();
    }
}

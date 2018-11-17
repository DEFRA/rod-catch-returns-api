package uk.gov.defra.datareturns.data.model;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PostAuthorize;

import javax.transaction.Transactional;
import java.io.Serializable;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface RestrictedEntityFilteringRepository<E extends AbstractRestrictedEntity<ID>, ID extends Serializable>
        extends ReferenceDataRepository<E, ID> {
    @Override
    @NonNull
    @RestResource(exported = false)
    @QueryWithInternalFilter
    List<E> findAll();

    @RestResource(exported = false)
    @NonNull
    @Transactional
    @QueryWithInternalFilter
    Page<E> findBy(Pageable pageable);

    @Override
    @NonNull
    default Page<E> findAll(@NonNull final Pageable p) {
        return findBy(p);
    }

    @Override
    @NonNull
    @PostAuthorize("hasPermission(returnObject, 'USE_INTERNAL')")
    E getOne(@NonNull ID targetId);

    @Override
    @NonNull
    @PostAuthorize("hasPermission(returnObject, 'USE_INTERNAL')")
    <S extends E> Optional<S> findOne(@NonNull Example<S> example);

    @Override
    @NonNull
    @PostAuthorize("hasPermission(returnObject, 'USE_INTERNAL')")
    Optional<E> findById(@NonNull ID id);


    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Query("select e from #{#entityName} e where e.internal = false or e.internal = ?#{hasAuthority('USE_INTERNAL')}")
    @interface QueryWithInternalFilter {
    }

}

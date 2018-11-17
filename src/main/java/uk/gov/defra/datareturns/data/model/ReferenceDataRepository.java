package uk.gov.defra.datareturns.data.model;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import uk.gov.defra.datareturns.data.BaseRepository;

import java.io.Serializable;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

/**
 * Reference data repository
 * Prevents reference data from being modified by unauthorized users
 *
 * @param <E>  the entity this repository will serve
 * @param <ID> the generic type of the entity identifier
 * @author Sam Gardner-Dell
 */
@NoRepositoryBean
public interface ReferenceDataRepository<E extends AbstractBaseEntity, ID extends Serializable> extends BaseRepository<E, ID> {
    @Override
    @HasReferenceDataWrite
    void deleteAll(@NonNull Iterable<? extends E> entities);

    @Override
    @NonNull
    @HasReferenceDataWrite
    <S extends E> S saveAndFlush(@NonNull S entity);

    @Override
    @NonNull
    @HasReferenceDataWrite
    <S extends E> S save(@NonNull S entity);

    @Override
    @NonNull
    @HasReferenceDataWrite
    <S extends E> List<S> saveAll(@NonNull Iterable<S> entities);

    @Override
    @HasReferenceDataWrite
    void deleteById(@NonNull ID id);

    @Override
    @HasReferenceDataWrite
    void delete(@NonNull E entity);

    @Override
    @HasReferenceDataWrite
    void deleteAllInBatch();

    @Override
    @HasReferenceDataWrite
    void deleteInBatch(@NonNull Iterable<E> entities);

    @Override
    @HasReferenceDataWrite
    void deleteAll();

    /**
     * Check for reference data write permissions
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @Documented
    @PreAuthorize("hasAuthority('REFERENCE_DATA_WRITE')")
    @interface HasReferenceDataWrite {
    }
}

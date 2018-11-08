package uk.gov.defra.datareturns.data.model;

import org.springframework.data.repository.NoRepositoryBean;
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
    void deleteAll(Iterable<? extends E> entities);

    @Override
    @HasReferenceDataWrite
    <S extends E> S saveAndFlush(S entity);

    @Override
    @HasReferenceDataWrite
    <S extends E> S save(S entity);

    @Override
    @HasReferenceDataWrite
    <S extends E> List<S> saveAll(Iterable<S> entities);

    @Override
    @HasReferenceDataWrite
    void deleteById(ID id);

    @Override
    @HasReferenceDataWrite
    void delete(E entity);

    @Override
    @HasReferenceDataWrite
    void deleteAllInBatch();

    @Override
    @HasReferenceDataWrite
    void deleteInBatch(Iterable<E> entities);

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

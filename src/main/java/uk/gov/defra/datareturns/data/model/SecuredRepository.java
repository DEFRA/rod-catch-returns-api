package uk.gov.defra.datareturns.data.model;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import uk.gov.defra.datareturns.data.BaseRepository;

import java.io.Serializable;
import java.util.List;

@NoRepositoryBean
public interface SecuredRepository<E extends AbstractSecuredEntity, ID extends Serializable> extends BaseRepository<E, ID> {
    @Override
    @PreAuthorize("hasReadAccess()")
    @PostFilter("hasRecordAccess(filterObject)")
    List<E> findAll();

    @Override
    @PreAuthorize("hasReadAccess()")
    @PostFilter("hasRecordAccess(filterObject)")
    List<E> findAll(Sort sort);

    @Override
    @PreAuthorize("hasReadAccess()")
    @PostFilter("hasRecordAccess(filterObject)")
    List<E> findAll(Iterable<ID> longs);

    @Override
    @PreAuthorize("hasReadAccess()")
    @PostFilter("hasRecordAccess(filterObject)")
    <S extends E> List<S> findAll(Example<S> example);

    @Override
    @PreAuthorize("hasReadAccess()")
    @PostFilter("hasRecordAccess(filterObject)")
    <S extends E> List<S> findAll(Example<S> example, Sort sort);

    @Override
    @PreAuthorize("hasReadAccess()")
    @PostAuthorize("hasRecordAccess(returnObject)")
    E getOne(ID targetId);

    @Override
    @PreAuthorize("hasReadAccess()")
    @PostAuthorize("hasRecordAccess(returnObject)")
    E findOne(ID targetId);

    @Override
    @PreAuthorize("hasWriteAccess()")
    <S extends E> List<S> save(Iterable<S> entities);

    @Override
    @PreAuthorize("hasWriteAccess()")
    <S extends E> S saveAndFlush(S entity);

    @Override
    @PreAuthorize("hasWriteAccess()")
    <S extends E> S save(S entity);

    @Override
    @PreAuthorize("hasWriteAccess()")
    void delete(ID targetId);

    @Override
    @PreAuthorize("hasWriteAccess()")
    void delete(E entity);

    @Override
    @PreAuthorize("hasWriteAccess()")
    void delete(Iterable<? extends E> entities);

    @Override
    @PreAuthorize("hasWriteAccess()")
    void deleteAllInBatch();

    @Override
    @PreAuthorize("hasWriteAccess()")
    void deleteInBatch(Iterable<E> entities);

    @Override
    @PreAuthorize("hasWriteAccess()")
    void deleteAll();
}

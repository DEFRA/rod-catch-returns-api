package uk.gov.defra.datareturns.data.model.reporting.filters;

import lombok.Getter;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

@Getter
abstract class AbstractPathVariableFilter<T> {
    private final boolean active;

    AbstractPathVariableFilter(final String filterValue) {
        this.active = filterValue != null && !filterValue.equals("*") && !filterValue.equals("_");
    }

    abstract Predicate toPredicate(final CriteriaBuilder cb, final Path<T> path);

    public Predicate predicate(final CriteriaBuilder cb, final Path<T> path) {
        Predicate predicate = cb.conjunction();
        if (isActive()) {
            predicate = toPredicate(cb, path);
        }
        return predicate;
    }
}

package uk.gov.defra.datareturns.data.model.reporting.filters;

import lombok.Getter;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

@Getter
public class StringValueFilter extends AbstractPathVariableFilter<String> {
    private final String value;

    public StringValueFilter(final String value) {
        super(value);
        this.value = value;
    }

    @Override
    Predicate toPredicate(final CriteriaBuilder cb, final Path<String> path) {
        return cb.equal(path, value);
    }

    @Override
    public String toString() {
        return value;
    }
}

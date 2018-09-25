package uk.gov.defra.datareturns.services.crm.entity;

/**
 * Associate the CRM result object with a base entity class
 * @param <E>
 */
public interface CrmEntity<E> {
    E getBaseEntity();

    /**
     * This interface defines classes that describe the artifacts needed
     * to call a stored procedure on the CRM, that is the query parameters
     * posted to the CRM in the payload, the stored procedure name and
     * the resulting type - a class extending CrmEntity
     * @param <T>
     */
    interface CRMQuery<T extends CrmEntity> {
        @SuppressWarnings("SameReturnValue")
        String getCRMStoredProcedureName();
        Query getQuery();
        Class<T> getEntityClass();
        interface Query { }
    }
}

package uk.gov.defra.datareturns.services.crm.entity;

/**
 * Bundles a CRM query with a response transfer object
 * and associates an API base entity class
 * @param <E>
 */
public interface CrmCall<E extends CrmBaseEntity> {
    E getBaseEntity();

    /**
     * This interface defines classes that describe the artifacts needed
     * to call a stored procedure on the CRM, that is the query parameters
     * posted to the CRM in the payload, the stored procedure name and
     * the resulting type - a class extending CrmCall
     * @param <T>
     */
    interface CRMQuery<T extends CrmCall> {
        @SuppressWarnings("SameReturnValue")
        String getCRMStoredProcedureName();
        Query getQuery();
        Class<T> getEntityClass();
        interface Query { }
    }
}

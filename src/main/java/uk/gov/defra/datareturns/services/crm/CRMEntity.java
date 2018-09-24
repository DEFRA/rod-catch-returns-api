package uk.gov.defra.datareturns.services.crm;

/**
 * Associate the CRM result object with a base entity class
 * @param <E>
 */
interface CRMEntity<E> {
    E getBaseEntity();
}

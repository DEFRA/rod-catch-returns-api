package uk.gov.defra.datareturns.data.model.licences;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import springfox.documentation.annotations.ApiIgnore;
import uk.gov.defra.datareturns.services.crm.CrmLookupService;


/**
 * Controller to enable the lookup of licence information from the CRM
 *
 * @author Sam Gardner-Dell
 */
@BasePathAwareController
@ConditionalOnWebApplication
@Slf4j
@RequiredArgsConstructor
public class LicenceController implements ResourceProcessor<RepositoryLinksResource> {
    /**
     * the service used to lookup licence data
     */
    private final CrmLookupService lookupService;

    /**
     * Retrieve a contact based on the given (partial) licence number
     *
     * @param licenceNumber the licence number used to retrieve licence information
     * @return a {@link ResponseEntity} containing the target {@link Licence} or a 404 status if not found
     */
    @GetMapping(value = "/licence/{licence}")
    public ResponseEntity<Licence> getContact(@PathVariable("licence") final String licenceNumber) {
        final Licence licence = lookupService.getLicenceFromLicenceNumber(licenceNumber);
        if (licence == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(licence, HttpStatus.OK);
    }

    /**
     * @return 405, "Method Not Allowed"
     */
    @RequestMapping(method = {RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.POST, RequestMethod.DELETE}, value = "/licence/*")
    @ApiIgnore
    public ResponseEntity<Licence> disabledMethods() {
        return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Override
    public RepositoryLinksResource process(final RepositoryLinksResource resource) {
        // TODO: Base path should be preprended to href
        resource.add(new Link("/licences", "licences"));
        return resource;
    }
}

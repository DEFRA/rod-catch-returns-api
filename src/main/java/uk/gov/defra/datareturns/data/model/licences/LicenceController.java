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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uk.gov.defra.datareturns.services.crm.CrmLookupService;

import java.util.Optional;

/**
 * Controller to enable the lookup of licence information from the CRM
 *
 * @author Sam Gardner-Dell
 */
@BasePathAwareController
@ConditionalOnWebApplication
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/licence")
public class LicenceController implements ResourceProcessor<RepositoryLinksResource> {
    /**
     * the service used to lookup licence data
     */
    private final CrmLookupService lookupService;

    /**
     * Retrieve a licence and its associated contact based on the given licence and postcode
     *
     * @param licenceNumber the licence number used to retrieve licence information
     * @param verification  used to verify the licence number
     * @return a {@link ResponseEntity} containing the target {@link Licence} or a 404 status if not found
     */
    @GetMapping(value = "/{licence}")
    public ResponseEntity<Licence> getLicence(@PathVariable("licence") final String licenceNumber,
                                              @RequestParam(value = "verification", required = false) final String verification) {
        ResponseEntity<Licence> responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
        final Optional<Licence> licence = lookupService.getLicence(licenceNumber, verification);
        if (licence.isPresent()) {
            responseEntity = new ResponseEntity<>(licence.get(), HttpStatus.OK);
        }
        return responseEntity;
    }

    /**
     * Retrieve a licence and its associated contact based on the full licence number
     *
     * @param fullLicenceNumber the full licence number used to retrieve licence information (will only accept numbers, letters and dashes)
     * @return a {@link ResponseEntity} containing the target {@link Licence} or a 404 status if not found
     */
    @GetMapping(value = "/full/{licence:^[A-Za-z0-9_-]*$}")
    public ResponseEntity<Licence> getLicence(@PathVariable("licence") final String fullLicenceNumber) {
        ResponseEntity<Licence> responseEntity = new ResponseEntity<>(HttpStatus.FORBIDDEN);
        final Optional<Licence> licence = lookupService.getLicence(fullLicenceNumber);
        if (licence.isPresent()) {
            responseEntity = new ResponseEntity<>(licence.get(), HttpStatus.OK);
        }
        return responseEntity;
    }

    @Override
    public RepositoryLinksResource process(final RepositoryLinksResource resource) {
        final String base = ServletUriComponentsBuilder.fromCurrentRequest().toUriString();
        resource.add(new Link(base + "licence/{licence}", "licences"));
        return resource;
    }
}

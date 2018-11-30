package uk.gov.defra.datareturns.data.model.licences;

import io.swagger.annotations.ApiOperation;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import springfox.documentation.annotations.ApiIgnore;
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
    @ApiOperation(value = "Retrieve a licence and its associated contact based on the given licence and postcode")
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
     * @return 405, "Method Not Allowed"
     */
    @RequestMapping(method = {RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.POST, RequestMethod.DELETE}, value = "/*")
    @ApiIgnore
    public ResponseEntity<Licence> disabledMethods() {
        return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Override
    public RepositoryLinksResource process(final RepositoryLinksResource resource) {
        final String base = ServletUriComponentsBuilder.fromCurrentRequest().toUriString();
        resource.add(new Link(base + "licence/{licence}", "licences"));
        return resource;
    }
}

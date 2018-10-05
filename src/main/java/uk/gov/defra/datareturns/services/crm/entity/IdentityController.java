package uk.gov.defra.datareturns.services.crm.entity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.defra.datareturns.services.crm.CrmLookupService;

/**
 * Endpoint for testing active directory authentication - expectes the AAD user and password to
 * encode in the header as basic auth
 */
@BasePathAwareController
@ConditionalOnWebApplication
@Slf4j
@RequiredArgsConstructor
public class IdentityController {

    private final CrmLookupService lookupService;

    @GetMapping(value = "/identity")
    public ResponseEntity<Identity> getIdentity(@RequestParam("username") final String username, @RequestParam("password") final String password) {
        Identity identity = lookupService.getAuthenticatedUserRoles(username, password);
        return new ResponseEntity<>(identity, HttpStatus.OK);
    }
}

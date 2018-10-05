package uk.gov.defra.datareturns.services.authentication;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import uk.gov.defra.datareturns.data.model.licences.Licence;
import uk.gov.defra.datareturns.services.crm.CrmLookupService;
import uk.gov.defra.datareturns.services.crm.DynamicsCrmLookupService;

import javax.inject.Inject;

@Service
@ConditionalOnProperty(name = "dynamics.impl", havingValue = "dynamics")
public class LicenceAuthentication implements AuthenticationProvider {

    private CrmLookupService crmLookupService;

    @Inject
    public LicenceAuthentication(DynamicsCrmLookupService dynamicsCrmLookupService) {
        this.crmLookupService = dynamicsCrmLookupService;
    }

    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        String licenceNumber = authentication.getName();
        String password = authentication.getCredentials().toString();
        try {
            final Licence licence = crmLookupService.getLicenceFromLicenceNumber(licenceNumber);
        } catch (Exception e) {
            throw new BadCredentialsException("AAD authentication failed");
        }
        return null;
    }

    @Override
    public boolean supports(final Class<?> auth) {
        return auth.equals(UsernamePasswordAuthenticationToken.class);
    }
}

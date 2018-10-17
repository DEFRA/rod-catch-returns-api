package uk.gov.defra.datareturns.services.authentication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import uk.gov.defra.datareturns.data.model.licences.Licence;
import uk.gov.defra.datareturns.data.model.licences.MockLicenceData;

import java.util.Collection;
import java.util.Collections;

@Service
@Slf4j
@ConditionalOnProperty(name = "dynamics.impl", havingValue = "mock")
public class MockLicenceAuthentication implements AuthenticationProvider {

    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {

        final String licenceStr = authentication.getName();
        final String postcode = authentication.getCredentials().toString();

        log.debug("Authenticating licence: " + licenceStr);

        if (MockLicenceData.LICENCES.containsKey(licenceStr.toUpperCase().trim())) {
            final Licence licence = MockLicenceData.LICENCES.get(licenceStr.toUpperCase().trim());
            final String contactPostcode = licence.getContact().getPostcode().replaceAll(" ", "");
            if (contactPostcode.equalsIgnoreCase(postcode)) {
                final Collection<? extends GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("LICENCE_HOLDER"));
                return new UsernamePasswordAuthenticationToken(licenceStr, contactPostcode, authorities);
            }
        }

        throw new BadCredentialsException("Mock licence authentication failed");
    }

    @Override
    public boolean supports(final Class<?> auth) {
        return auth.equals(UsernamePasswordAuthenticationToken.class);
    }
}

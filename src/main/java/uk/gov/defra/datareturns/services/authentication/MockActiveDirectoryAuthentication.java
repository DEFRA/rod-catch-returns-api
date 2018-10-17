package uk.gov.defra.datareturns.services.authentication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import uk.gov.defra.datareturns.services.crm.entity.Identity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static java.util.stream.Collectors.toCollection;

@Service
@ConditionalOnProperty(name = "dynamics.impl", havingValue = "mock")
@Slf4j
public class MockActiveDirectoryAuthentication implements AuthenticationProvider {

    @Override
    @Cacheable(cacheNames = "crm-aad-auth", key = "{ #authentication.name, #authentication.credentials }")
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {

        final String username = authentication.getName();
        final String password = authentication.getCredentials().toString();

        log.debug("Authenticating user: " + username);

        if (username.equalsIgnoreCase("captain.beefheart@troutmask.replica.com") && password.equalsIgnoreCase("123456")) {
            final Identity identity = new Identity();
            identity.setRoles(Collections.singletonList("FMT_USER"));
            final Collection<? extends GrantedAuthority> authorities = identity.getRoles()
                    .stream().map(SimpleGrantedAuthority::new).collect(toCollection(ArrayList::new));
            return new UsernamePasswordAuthenticationToken(username, password, authorities);
        } else {
            throw new BadCredentialsException("Mock AAD authentication failed");
        }
    }

    @Override
    public boolean supports(final Class<?> auth) {
        return auth.equals(UsernamePasswordAuthenticationToken.class);
    }
}

package uk.gov.defra.datareturns.services.authentication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import uk.gov.defra.datareturns.services.crm.CrmLookupService;
import uk.gov.defra.datareturns.services.crm.DynamicsCrmLookupService;
import uk.gov.defra.datareturns.services.crm.entity.Identity;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toCollection;

@Service
@RequiredArgsConstructor
@Scope(BeanDefinition.SCOPE_SINGLETON)
@ConditionalOnProperty(name = "dynamics.impl", havingValue = "dynamics")
@Slf4j
public class ActiveDirectoryAuthentication implements AuthenticationProvider {
    private CrmLookupService crmLookupService;

    @Inject
    public ActiveDirectoryAuthentication(DynamicsCrmLookupService dynamicsCrmLookupService) {
        this.crmLookupService = dynamicsCrmLookupService;
    }

    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {

        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        log.debug("Authenticating user: " + username);

        try {
            final Identity identity = crmLookupService.getAuthenticatedUserRoles(username, password);

            if (identity == null) {
                log.debug("User authenticated failed against ADD for: " + username);
                throw new Exception();
            }

            Collection<? extends GrantedAuthority> authorities = Arrays.asList(identity.getRoles())
                    .stream().map(r -> new SimpleGrantedAuthority(r)).collect(toCollection(ArrayList::new));

            return new UsernamePasswordAuthenticationToken(username, password, authorities);
        } catch (Exception e) {
            throw new BadCredentialsException("AAD authentication failed");
        }
    }

    @Override
    public boolean supports(final Class<?> auth) {
        return auth.equals(UsernamePasswordAuthenticationToken.class);
    }
}

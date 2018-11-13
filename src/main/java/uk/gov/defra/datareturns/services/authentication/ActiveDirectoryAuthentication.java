package uk.gov.defra.datareturns.services.authentication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import uk.gov.defra.datareturns.config.CacheManagerConfiguration;
import uk.gov.defra.datareturns.config.SecurityConfiguration;
import uk.gov.defra.datareturns.services.crm.CrmLookupService;
import uk.gov.defra.datareturns.services.crm.entity.Identity;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
@CacheConfig(cacheManager = CacheManagerConfiguration.AUTHENTICATION_CACHE_MANAGER)
@Slf4j
@RequiredArgsConstructor
public class ActiveDirectoryAuthentication implements ActiveDirectoryAuthenticationProvider {
    private final CrmLookupService crmLookupService;
    private final SecurityConfiguration securityConfiguration;

    @Override
    @Cacheable(cacheNames = "crm-aad-auth",
               key = "{ #authentication.name, #authentication.credentials }")
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {

        final String username = authentication.getName();
        final String password = authentication.getCredentials().toString();

        log.debug("Authenticating user: " + username);

        try {
            final Identity identity = crmLookupService.getAuthenticatedUserRoles(username, password);
            if (identity == null) {
                throw new BadCredentialsException("AAD authentication failed - no identity was found for given credentials.");
            }

            final List<GrantedAuthority> authorities = identity.getRoles().stream()
                    .flatMap(crmRole -> securityConfiguration.getRoleAuthorities().get(crmRole).stream())
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            return new UsernamePasswordAuthenticationToken(username, password, authorities);
        } catch (AuthenticationServiceException e) {
            log.error("Authentication service error", e);
            throw e;
        }
    }

    @Override
    public boolean supports(final Class<?> auth) {
        return auth.equals(UsernamePasswordAuthenticationToken.class);
    }
}

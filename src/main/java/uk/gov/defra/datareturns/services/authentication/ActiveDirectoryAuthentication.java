package uk.gov.defra.datareturns.services.authentication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import uk.gov.defra.datareturns.config.AADConfiguration;
import uk.gov.defra.datareturns.services.crm.CrmLookupService;
import uk.gov.defra.datareturns.services.crm.DynamicsCrmLookupService;
import uk.gov.defra.datareturns.services.crm.entity.Identity;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toCollection;

@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
@ConditionalOnProperty(name = "dynamics.impl", havingValue = "dynamics")
@Slf4j
public class ActiveDirectoryAuthentication implements AuthenticationProvider {
    private CrmLookupService crmLookupService;
    private final AADConfiguration aadConfiguration;

    @Inject
    private final ActiveDirectoryAuthentication proxy = null;

    @Inject
    public ActiveDirectoryAuthentication(final DynamicsCrmLookupService dynamicsCrmLookupService, final AADConfiguration aadConfiguration) {
        this.crmLookupService = dynamicsCrmLookupService;
        this.aadConfiguration = aadConfiguration;
    }

    @Override
    @Cacheable(cacheNames = "crm-aad-auth", key = "{ #authentication.name, #authentication.credentials }")
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {

        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        log.debug("Authenticating user: " + username);

        try {
            final Identity identity = crmLookupService.getAuthenticatedUserRoles(username, password);

            if (identity == null) {
                throw new Exception();
            }

            Collection<? extends GrantedAuthority> authorities = identity.getRoles()
                    .stream().map(r -> new SimpleGrantedAuthority(r)).collect(toCollection(ArrayList::new));

            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.schedule(() -> proxy.evictAuthentication(authentication), aadConfiguration.getAadAuthTtlHours(), TimeUnit.HOURS);
            executor.shutdown();

            return new UsernamePasswordAuthenticationToken(username, password, authorities);
        } catch (Exception e) {
            throw new BadCredentialsException("AAD authentication failed");
        }
    }

    @CacheEvict(cacheNames = "crm-aad-auth", key = "{ #authentication.name, #authentication.credentials }")
    public void evictAuthentication(final Authentication authentication) {
        log.debug("Evicting AAD authentication from cache: " + authentication.getName());
    }

    @Override
    public boolean supports(final Class<?> auth) {
        return auth.equals(UsernamePasswordAuthenticationToken.class);
    }
}

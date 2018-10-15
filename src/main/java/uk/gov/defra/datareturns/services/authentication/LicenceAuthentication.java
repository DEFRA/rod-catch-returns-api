package uk.gov.defra.datareturns.services.authentication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import uk.gov.defra.datareturns.config.AADConfiguration;
import uk.gov.defra.datareturns.data.model.licences.Licence;
import uk.gov.defra.datareturns.services.crm.CrmLookupService;
import uk.gov.defra.datareturns.services.crm.DynamicsCrmLookupService;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@ConditionalOnProperty(name = "dynamics.impl", havingValue = "dynamics")
public class LicenceAuthentication implements AuthenticationProvider {

    private final AADConfiguration aadConfiguration;
    private CrmLookupService crmLookupService;

    @Inject
    private final LicenceAuthentication proxy = null;

    @Inject
    public LicenceAuthentication(final DynamicsCrmLookupService dynamicsCrmLookupService, final AADConfiguration aadConfiguration) {
        this.crmLookupService = dynamicsCrmLookupService;
        this.aadConfiguration = aadConfiguration;
    }

    @Override
    @Cacheable(cacheNames = "crm-licence-auth", key = "{ #authentication.name, #authentication.credentials }")
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {

        String licenceStr = authentication.getName();
        String postcode = authentication.getCredentials().toString();

        log.debug("Authenticating licence: " + licenceStr);

        try {
            final Licence licence = crmLookupService.getLicenceFromLicenceNumber(licenceStr);

            if (licence == null) {
                throw new Exception();
            }

            final String contactPostcode = licence.getContact().getPostcode().replaceAll(" ", "");

            if (contactPostcode.equalsIgnoreCase(postcode.replaceAll(" ", ""))) {
                Collection<? extends GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("LICENCE_HOLDER"));

                // Set up a timer to authorization from the cache
                ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
                executor.schedule(() -> proxy.evictAuthentication(authentication), aadConfiguration.getLicenceAuthTtlHours(), TimeUnit.HOURS);
                executor.shutdown();

                return new UsernamePasswordAuthenticationToken(licenceStr, contactPostcode, authorities);
            } else {
                throw new Exception();
            }

        } catch (Exception e) {
            throw new BadCredentialsException("licence authentication failed");
        }
    }

    @CacheEvict(cacheNames = "crm-licence-auth", key = "{ #authentication.name, #authentication.credentials }")
    public void evictAuthentication(final Authentication authentication) {
        log.debug("Evicting licence authentication from cache: " + authentication.getName());
    }

    @Override
    public boolean supports(final Class<?> auth) {
        return auth.equals(UsernamePasswordAuthenticationToken.class);
    }
}

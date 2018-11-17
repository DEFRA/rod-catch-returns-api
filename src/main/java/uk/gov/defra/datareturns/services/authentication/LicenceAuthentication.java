package uk.gov.defra.datareturns.services.authentication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import uk.gov.defra.datareturns.config.CacheManagerConfiguration;
import uk.gov.defra.datareturns.config.SecurityConfiguration;
import uk.gov.defra.datareturns.data.model.licences.Licence;
import uk.gov.defra.datareturns.services.crm.CrmLookupService;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
@CacheConfig(cacheManager = CacheManagerConfiguration.LICENCE_CACHE_MANAGER)
@Slf4j
@RequiredArgsConstructor
public class LicenceAuthentication implements LicenceAuthenticationProvider {
    private final SecurityConfiguration securityConfiguration;
    private final CrmLookupService crmLookupService;

    @Override
    @Cacheable(cacheNames = "crm-licence-auth", key = "{ #authentication.name, #authentication.credentials }")
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        final String licenceStr = authentication.getName();
        final String postcode = authentication.getCredentials().toString();

        final Licence licence = crmLookupService.getLicenceFromLicenceNumber(licenceStr);
        if (licence == null) {
            throw new BadCredentialsException("licence authentication failed - no identity was found for given credentials.");
        }

        final String contactPostcode = StringUtils.deleteWhitespace(licence.getContact().getPostcode());
        final String authPostcode = StringUtils.deleteWhitespace(postcode);
        if (contactPostcode.equalsIgnoreCase(authPostcode)) {
            final List<GrantedAuthority> authorities = securityConfiguration.getRoleAuthorities().get("RCR_END_USER").stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
            return new UsernamePasswordAuthenticationToken(licenceStr, contactPostcode, authorities);
        } else {
            throw new BadCredentialsException("licence authentication failed - no identity could be retrieved for the given credentials");
        }
    }

    @Override
    public boolean supports(final Class<?> auth) {
        return auth.equals(UsernamePasswordAuthenticationToken.class);
    }
}

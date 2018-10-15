package uk.gov.defra.datareturns.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import uk.gov.defra.datareturns.services.authentication.MockActiveDirectoryAuthentication;
import uk.gov.defra.datareturns.services.authentication.MockLicenceAuthentication;

@EnableWebSecurity
@ConditionalOnProperty(name = "dynamics.impl", havingValue = "mock")
@RequiredArgsConstructor
@Order(1)
public class MockSecurityConfig extends WebSecurityConfigurerAdapter {

    private final MockActiveDirectoryAuthentication mockActiveDirectoryAuthentication;
    private final MockLicenceAuthentication mockLicenceAuthentication;

    /**
     * Attempt authentication by licence and postcode then by
     * EA active directory credentials
     */
    @Override
    public void configure(final AuthenticationManagerBuilder builder) {
        builder.authenticationProvider(mockLicenceAuthentication)
                .authenticationProvider(mockActiveDirectoryAuthentication);
    }

    /**
     * Set up basic authentication on all routes except profile routes
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http.httpBasic()
                .and()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/api/**").authenticated();
    }
}

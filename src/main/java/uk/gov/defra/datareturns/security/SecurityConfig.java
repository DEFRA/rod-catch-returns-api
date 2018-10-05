package uk.gov.defra.datareturns.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import uk.gov.defra.datareturns.services.authentication.ActiveDirectoryAuthentication;

import javax.inject.Inject;

@EnableWebSecurity
@ConditionalOnProperty(name = "dynamics.impl", havingValue = "dynamics")
@Order(1)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final ActiveDirectoryAuthentication activeDirectoryAuthentication;

    @Inject
    public SecurityConfig(ActiveDirectoryAuthentication activeDirectoryAuthentication) {
        this.activeDirectoryAuthentication = activeDirectoryAuthentication;
    }

    @Override
    public void configure(AuthenticationManagerBuilder builder) {
//        auth.authenticationProvider(licenceAuthentication);
        builder.authenticationProvider(activeDirectoryAuthentication);
    }

}

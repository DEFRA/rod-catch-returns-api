package uk.gov.defra.datareturns.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.validation.annotation.Validated;
import uk.gov.defra.datareturns.security.DefaultExpressionRoot;
import uk.gov.defra.datareturns.services.authentication.ActiveDirectoryAuthenticationProvider;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * Spring security configuration
 *
 * @author Sam Gardner-Dell
 */
@SuppressWarnings({ "NonFinalUtilityClass", "HideUtilityClassConstructor" })
@Configuration
@ConfigurationProperties(prefix = "security")
@Getter
@Setter
@Validated
@Slf4j
public class SecurityConfiguration {
    @NotNull
    private Map<String, Collection<String>> roleAuthorities;

    @Configuration
    @EnableWebSecurity
    @ConditionalOnWebApplication
    @RequiredArgsConstructor
    public static class WebSecurityConfiguration {
        private final ActiveDirectoryAuthenticationProvider activeDirectoryAuthentication;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http.httpBasic()
                    .and()
                    .csrf().disable()
                    .authorizeRequests()
                    .requestMatchers(PlatformSecurityConfiguration.WebSecurityConfiguration.WHITELIST).permitAll()
                    .requestMatchers("/api/**").permitAll();

            return http.build();
        }

        @Bean
        public AuthenticationManager authManager(HttpSecurity http) throws Exception {
            AuthenticationManagerBuilder authenticationManagerBuilder = http
                    .getSharedObject(AuthenticationManagerBuilder.class);
            authenticationManagerBuilder.authenticationProvider(activeDirectoryAuthentication);
            return authenticationManagerBuilder.build();
        }
    }

    @EnableGlobalMethodSecurity(prePostEnabled = true)
    public static class MethodSecurityConfiguration extends GlobalMethodSecurityConfiguration {
        @Override
        protected MethodSecurityExpressionHandler createExpressionHandler() {
            final DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler() {
                private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();

                @Override
                protected MethodSecurityExpressionOperations createSecurityExpressionRoot(
                        final Authentication authentication,
                        final MethodInvocation invocation) {
                    final DefaultExpressionRoot root = new DefaultExpressionRoot(authentication);
                    root.setTarget(invocation.getThis());
                    root.setPermissionEvaluator(getPermissionEvaluator());
                    root.setTrustResolver(this.trustResolver);
                    root.setRoleHierarchy(getRoleHierarchy());
                    return root;
                }
            };
            expressionHandler.setPermissionEvaluator(new RcrPermissionEvaluator());
            return expressionHandler;
        }
    }

    /**
     * Permission evaluator
     */
    public static class RcrPermissionEvaluator implements PermissionEvaluator {
        /**
         * Permits access to use reference data marked as internal in a submission
         */
        public static final String USE_INTERNAL = "USE_INTERNAL";

        public static boolean hasAuthority(final Authentication auth, final String authority) {
            return authority != null
                    && auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(authority::equals);
        }

        private static boolean hasAuthority(final Authentication auth, final String targetType,
                final String authority) {
            return hasAuthority(auth, StringUtils.upperCase(targetType + "_" + authority));
        }

        @Override
        public boolean hasPermission(final Authentication auth, final Object targetDomainObject,
                final Object permission) {
            return targetDomainObject != null
                    && hasAuthority(auth, targetDomainObject.getClass().getSimpleName(), Objects.toString(permission));
        }

        @Override
        public boolean hasPermission(final Authentication auth, final Serializable targetId, final String targetType,
                final Object permission) {
            return hasAuthority(auth, targetType, Objects.toString(permission));
        }
    }
}
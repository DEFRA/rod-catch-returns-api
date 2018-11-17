package uk.gov.defra.datareturns.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.validation.annotation.Validated;
import uk.gov.defra.datareturns.data.model.AbstractRestrictedEntity;
import uk.gov.defra.datareturns.security.DefaultExpressionRoot;
import uk.gov.defra.datareturns.services.authentication.ActiveDirectoryAuthenticationProvider;
import uk.gov.defra.datareturns.services.authentication.LicenceAuthenticationProvider;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Spring security configuration
 *
 * @author Sam Gardner-Dell
 */
@SuppressWarnings({"NonFinalUtilityClass", "HideUtilityClassConstructor"})
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "security")
@Getter
@Setter
@Validated
@Slf4j
public class SecurityConfiguration {
    @NotNull
    private Map<String, Collection<String>> roleAuthorities;

    @RequiredArgsConstructor
    @EnableWebSecurity
    @ConditionalOnWebApplication
    public static class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
        private final ActiveDirectoryAuthenticationProvider activeDirectoryAuthentication;
        private final LicenceAuthenticationProvider licenceAuthentication;

        /**
         * Attempt authentication by licence and postcode then by EA active directory credentials
         */
        @Override
        public void configure(final AuthenticationManagerBuilder builder) {
            builder.authenticationProvider(licenceAuthentication).authenticationProvider(activeDirectoryAuthentication);
        }

        /**
         * Set up basic authentication on all routes except profile routes
         *
         * @param http the spring {@link HttpSecurity} builder
         * @throws Exception if a problem occurred configuring the authentication handlers
         */
        @Override
        protected void configure(final HttpSecurity http) throws Exception {
            http.httpBasic()
                    .and()
                    .csrf().disable()
                    .authorizeRequests()
                    .antMatchers(PlatformSecurityConfiguration.WebSecurityConfiguration.WHITELIST).permitAll()
                    .antMatchers("/api/**").fullyAuthenticated();
        }
    }

    @EnableGlobalMethodSecurity(prePostEnabled = true)
    public static class MethodSecurityConfiguration extends GlobalMethodSecurityConfiguration {
        @Override
        protected MethodSecurityExpressionHandler createExpressionHandler() {
            final DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler() {
                private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();

                @Override
                protected MethodSecurityExpressionOperations createSecurityExpressionRoot(final Authentication authentication,
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
        private static boolean hasAuthority(final Authentication auth, final String authority) {
            return authority != null && auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(authority::equals);
        }

        private static boolean hasAuthority(final Authentication auth, final String targetType, final String authority) {
            return hasAuthority(auth, StringUtils.upperCase(targetType + "_" + authority));
        }

        private static Object unwrapDomainObject(final Object entity) {
            if (entity instanceof Optional) {
                return ((Optional<?>) entity).orElse(null);
            }
            return entity;
        }

        @Override
        public boolean hasPermission(final Authentication auth, final Object targetDomainObject, final Object permission) {
            if (targetDomainObject == null) {
                return false;
            }
            final String permString = Objects.toString(permission);
            final Object entity = unwrapDomainObject(targetDomainObject);
            if ("USE_INTERNAL".equals(permString) && entity instanceof AbstractRestrictedEntity) {
                return !((AbstractRestrictedEntity) entity).isInternal() || hasAuthority(auth, permString);
            }
            return hasAuthority(auth, entity.getClass().getSimpleName(), permString);
        }

        @Override
        public boolean hasPermission(final Authentication auth, final Serializable targetId, final String targetType, final Object permission) {
            return hasAuthority(auth, targetType, Objects.toString(permission));
        }
    }
}

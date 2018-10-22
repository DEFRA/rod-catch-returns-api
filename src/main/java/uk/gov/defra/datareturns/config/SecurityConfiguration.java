package uk.gov.defra.datareturns.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.SecurityExpressionRoot;
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
import uk.gov.defra.datareturns.data.BaseRepository;
import uk.gov.defra.datareturns.data.model.HasAuthorities;
import uk.gov.defra.datareturns.services.authentication.ActiveDirectoryAuthenticationProvider;
import uk.gov.defra.datareturns.services.authentication.LicenceAuthenticationProvider;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
public class SecurityConfiguration {
    @NotNull
    private Map<String, Collection<String>> roleAuthorities;

    @RequiredArgsConstructor
    @EnableWebSecurity
    @ConditionalOnWebApplication
    public static class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

        private static final String[] AUTH_WHITELIST = {
                // -- swagger ui
                "/",
                "/swagger-resources/**",
                "/swagger-ui.html",
                "/v2/api-docs",
                "/webjars/**"
        };
        private final ActiveDirectoryAuthenticationProvider activeDirectoryAuthentication;
        private final LicenceAuthenticationProvider licenceAuthentication;

        /**
         * Attempt authentication by licence and postcode then by
         * EA active directory credentials
         */
        @Override
        public void configure(final AuthenticationManagerBuilder builder) {
            builder.authenticationProvider(licenceAuthentication).authenticationProvider(activeDirectoryAuthentication);
        }

        /**
         * Set up basic authentication on all routes except profile routes
         *
         * @param http
         * @throws Exception
         */
        @Override
        protected void configure(final HttpSecurity http) throws Exception {
            http.httpBasic()
                    .and()
                    .csrf().disable()
                    .authorizeRequests()
                    .antMatchers(AUTH_WHITELIST).permitAll()
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
                    final RcrExpressionRoot root = new RcrExpressionRoot(authentication);
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

    public static class RcrPermissionEvaluator implements PermissionEvaluator {
        private static boolean hasAuthority(final Authentication auth, final String targetType, final String permission) {
            if (auth == null) {
                return false;
            }
            final String defaultAuthority = StringUtils.upperCase("JPA_ENTITY_DEFAULT_" + permission);
            final String targetAuthority = StringUtils.upperCase(targetType + "_" + permission);
            return auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(a -> defaultAuthority.equals(a) || targetAuthority.equals(a));
        }

        @Override
        public boolean hasPermission(final Authentication auth, final Object targetDomainObject, final Object permission) {
            if (targetDomainObject == null) {
                return false;
            }
            return hasAuthority(auth, targetDomainObject.getClass().getSimpleName(), Objects.toString(permission));
        }

        @Override
        public boolean hasPermission(final Authentication auth, final Serializable targetId, final String targetType, final Object permission) {
            return hasAuthority(auth, targetType, Objects.toString(permission));
        }
    }

    @Getter
    @Setter
    public static class RcrExpressionRoot extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {
        private Object filterObject;
        private Object returnObject;
        private Object target;

        public RcrExpressionRoot(final Authentication authentication) {
            super(authentication);
        }

        private BaseRepository<?, ?> getTargetRepository() {
            if (!(this.target instanceof BaseRepository)) {
                throw new RuntimeException("Target must extend BaseRepository");
            }
            return (BaseRepository) this.target;
        }

        public boolean hasWriteAccess() {
            final String entityName = getTargetRepository().getEntityInformation().getJavaType().getSimpleName().toUpperCase();
            return super.hasPermission(null, entityName, "WRITE");
        }

        public boolean hasReadAccess() {
            final String entityName = getTargetRepository().getEntityInformation().getJavaType().getSimpleName().toUpperCase();
            return super.hasPermission(null, entityName, "READ");
        }

        public boolean hasRecordAccess(final Object entity) {
            boolean permitted = true;
            if (entity instanceof HasAuthorities) {
                final String entityName = entity.getClass().getSimpleName();
                final Set<String> requireOneOf = ((HasAuthorities) entity).getRequiredAuthorities();
                permitted = requireOneOf.isEmpty();

                for (final String perm : requireOneOf) {
                    if (permitted) {
                        break;
                    }
                    permitted = super.hasPermission(null, entityName, perm);
                }
            }
            return permitted;
        }

        @Override
        public Object getThis() {
            return target;
        }
    }
}

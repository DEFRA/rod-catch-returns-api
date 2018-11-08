package uk.gov.defra.datareturns.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

/**
 * Cache manager configuration for rod catch returns
 *
 * @author Sam Gardner-Dell
 */
@Configuration
@EnableCaching
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "cache")
@Getter
@Setter
@Validated
public class CacheManagerConfiguration extends CachingConfigurerSupport {
    /**
     * Bean name for the aad authentication cache
     */
    public static final String AUTHENTICATION_CACHE_MANAGER = "authenticationCacheManager";
    /**
     * Bean name for the crm licence cache
     */
    public static final String LICENCE_CACHE_MANAGER = "licenceCacheManager";

    @NotNull
    private Short activeDirectoryCacheManagerTtlHours;

    @NotNull
    private Short licenceCacheManagerTtlHours;

    @Bean
    @Override
    public CacheManager cacheManager() {
        return new CaffeineCacheManager();
    }

    @Bean(name = AUTHENTICATION_CACHE_MANAGER)
    public CacheManager authenticationCacheManager() {
        final CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCacheSpecification("expireAfterWrite=" + activeDirectoryCacheManagerTtlHours + "h");
        return cacheManager;
    }

    @Bean(name = LICENCE_CACHE_MANAGER)
    public CacheManager licenceCacheManager() {
        final CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCacheSpecification("expireAfterWrite=" + licenceCacheManagerTtlHours + "h");
        return cacheManager;
    }
}

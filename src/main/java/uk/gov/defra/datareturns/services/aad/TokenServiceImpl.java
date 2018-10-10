package uk.gov.defra.datareturns.services.aad;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import com.microsoft.aad.adal4j.AuthenticationCallback;
import uk.gov.defra.datareturns.config.AADConfiguration;
import uk.gov.defra.datareturns.config.DynamicsConfiguration;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Service to retrieve access token from Azure active directory
 *
 * @author Graham Willis
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "dynamics.impl", havingValue = "dynamics")
public class TokenServiceImpl implements TokenService {

    @Inject
    private final TokenServiceImpl proxy = null;

    private final AADConfiguration aadConfiguration;
    private URL tokenPath;
    private final URL resource;

    @Inject
    public TokenServiceImpl(final AADConfiguration aadConfiguration, final DynamicsConfiguration dynamicsConfiguration) {
        this.aadConfiguration = aadConfiguration;

        final URI tenant = aadConfiguration.getTenant();
        final URL authority = aadConfiguration.getAuthority();
        resource = dynamicsConfiguration.getEndpoint();

        try {
            this.tokenPath = new URL(authority, tenant.toString());
        } catch (MalformedURLException e) {
            log.error("Error in token specification: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(cacheNames = "crm-auth-token")
    public String getToken() {
        ExecutorService service = Executors.newSingleThreadExecutor();
        try {
            log.debug("Attempting to fetch AAD token from " + tokenPath);

            String clientId = aadConfiguration.getClientId();
            String clientSecret = aadConfiguration.getClientSecret();

            // Generate a credentials payload from the clientId and secret
            ClientCredential clientCredential = new ClientCredential(clientId, clientSecret);

            AuthenticationContext context = new AuthenticationContext(tokenPath.toString(), true, service);

            // Attempt to acquire a token and fire the callback defined below
            Future<AuthenticationResult> future = context.acquireToken(resource.toString(),
                    clientCredential, new SystemTokenCallback());

            // Return the token as a string
            AuthenticationResult token = future.get();

            return token.getAccessToken();
        } catch (MalformedURLException | InterruptedException | ExecutionException e) {
            log.error("Error fetching token: " + e.getMessage());
        } finally {
            service.shutdown();
        }
        return null;
    }

    /**
     * Token acquire callback
     */
    private class SystemTokenCallback implements AuthenticationCallback<AuthenticationResult> {
        public void onSuccess(final AuthenticationResult result) {

            // Success: execute a timer to evict the token from the cache before it expires
            long seconds = result.getExpiresAfter();
            log.debug("AAD token acquired successfully: expires in " + seconds + " seconds");

            // Log the token in debug mode
            log.debug("Bearer " + result.getAccessToken());
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

            // Remove the token 1 minute before it is due to expire
            long delay = Math.max(seconds - 60, 0);
            executor.schedule(() -> proxy.evictToken(), delay, TimeUnit.SECONDS);
            executor.shutdown();
        }

        public void onFailure(final Throwable throwable) {
        }
    }

    @Override
    @Cacheable(cacheNames = "crm-auth-token-identity")
    public String getTokenForUserIdentity(final String username, final String password) {
        ExecutorService service = Executors.newSingleThreadExecutor();
        try {
            log.debug("Attempting to fetch user identity AAD token from " + tokenPath);

            String clientId = aadConfiguration.getIdentityClientId();
            AuthenticationContext context = new AuthenticationContext(tokenPath.toString(), true, service);

            // Attempt to acquire a token and fire the callback defined below
            Future<AuthenticationResult> future = context.acquireToken(resource.toString(), clientId, username, password,
                    new IdentityTokenCallback(username, password));

            // Return the token as a string
            AuthenticationResult token = future.get();

            return token == null ? null : token.getAccessToken();
        } catch (MalformedURLException | InterruptedException | ExecutionException e) {
            log.error("Error fetching token: " + e.getMessage());
        } finally {
            service.shutdown();
        }
        return null;
    }

    /**
     * Token acquire callback
     */
    private class IdentityTokenCallback implements AuthenticationCallback<AuthenticationResult> {
        private final String username;
        private final String password;

        IdentityTokenCallback(final String username, final String password) {
            this.password = password;
            this.username = username;
        }

        public void onSuccess(final AuthenticationResult result) {

            // Success: execute a timer to evict the token from the cache before it expires
            long seconds = result.getExpiresAfter();
            log.debug("AAD identity token acquired successfully: expires in " + seconds + " seconds");

            // Log the token in debug mode
            log.debug("Bearer " + result.getAccessToken());
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

            // Remove the token 1 minute before it is due to expire
            long delay = Math.max(seconds - 60, 0);
            executor.schedule(() -> proxy.evictIdentityToken(username, password), delay, TimeUnit.SECONDS);
            executor.shutdown();
        }

        public void onFailure(final Throwable throwable) {
        }
    }

    /**
     * Evict the token from the cache for system token
     */
    @CacheEvict(cacheNames = "crm-auth-token", allEntries = true)
    public void evictToken() {
        log.debug("Evicting AAD token from cache");
    }

    /**
     * Evict user identity tokens from the cache
     * @param username - the stored username
     */
    @CacheEvict(cacheNames = "crm-auth-token-identity")
    public void evictIdentityToken(final String username, final String password) {
        log.debug("Evicting AAD token from cache for user: " + username);
    }
}

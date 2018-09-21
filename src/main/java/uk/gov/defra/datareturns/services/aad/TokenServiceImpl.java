package uk.gov.defra.datareturns.services.aad;

import lombok.extern.slf4j.Slf4j;
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
import java.util.TimerTask;
import java.util.concurrent.*;
/**
 * Service to retrieve access token from Azure active directory
 *
 * @author Graham Willis
 */
@Service
@Slf4j
public class TokenServiceImpl implements TokenService {
    @Inject
    private TokenServiceImpl proxy;

    @Inject
    AADConfiguration aadConfiguration;

    @Inject
    DynamicsConfiguration dynamicsConfiguration;

    @Override
    @Cacheable(cacheNames = "crm-auth-token")
    public String getToken() {
        ExecutorService service = Executors.newFixedThreadPool(1);
        try {
            URI tenant = aadConfiguration.getTenant();
            URL authority = aadConfiguration.getAuthority();
            URL tokenPath = new URL(authority, tenant.toString());
            log.debug("Attempting to fetch AAD token from " + tokenPath);

            String clientId = aadConfiguration.getClientId();
            String clientSecret = aadConfiguration.getClientSecret();

            URL resource = dynamicsConfiguration.getEndpoint();

            // Generate a credentials payload from the clientId and secret
            ClientCredential clientCredential = new ClientCredential(clientId, clientSecret);

            AuthenticationContext context = new AuthenticationContext(tokenPath.toString(), true, service);

            // Attempt to acquire a token and fire the callback defined below
            Future<AuthenticationResult> future = context.acquireToken(resource.toString(),
                    clientCredential, new Callback());

            // Return the token as a string
            AuthenticationResult token = future.get();
            String accessToken = token.getAccessToken();
            return accessToken;
        } catch (MalformedURLException|InterruptedException|ExecutionException e) {
            log.error("Error fetching token: " + e.getMessage());
        } finally {
            service.shutdown();
        }
        return null;
    }

    /**
     * Token acquire callback
     */
    private class Callback implements AuthenticationCallback<AuthenticationResult> {
        public void onSuccess(AuthenticationResult result) {
            /**
             * Success: execute a timer to evict the token from the cache before it expires
             */
            long seconds = result.getExpiresAfter();
            log.debug("AAD token acquired successfully: expires in " + seconds + " seconds");
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

            // Remove the token 1 minute before it is due to expire
            long delay  = Math.max(seconds - 60, 0);
            executor.schedule(new TimerTask() {
                public void run() {
                    proxy.evictToken();
                }
            }, delay, TimeUnit.SECONDS);
            executor.shutdown();
        }

        public void onFailure(Throwable throwable) {
            log.error("Failed to acquire token from AAD: " + throwable.toString());
        }
    }

    /**
     * Evict the token from the cache
     */
    @CacheEvict(cacheNames = "crm-auth-token", allEntries = true)
    public void evictToken() {
        log.debug("Evicting AAD token from cache");
    }
}

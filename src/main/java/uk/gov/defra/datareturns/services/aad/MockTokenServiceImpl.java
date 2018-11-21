package uk.gov.defra.datareturns.services.aad;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mock service to emulate retrieving an access token from Azure active directory
 *
 * @author Sam Gardner-Dell
 */
@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnMissingBean(TokenServiceImpl.class)
public class MockTokenServiceImpl implements TokenService {
    public static final String MOCK_BEARER_TOKEN = "MOCK_BEARER_TOKEN_";
    private static final Pattern USER_PTN = Pattern.compile("(?i)^admin.*@example.com$");
    private static final Pattern PASS_PTN = Pattern.compile("(?i)^admin(?<responseCode>\\d{3})?$");

    @Override
    @NonNull
    public String getTokenForUserIdentity(final String username, final String password) {
        final Matcher userMatcher = USER_PTN.matcher(username);
        final Matcher passMatcher = PASS_PTN.matcher(password);
        if (!userMatcher.matches() || !passMatcher.matches()) {
            throw new BadCredentialsException("Mock authentication failed.");
        }
        final String bearerTokenResponseExt = Objects.toString(passMatcher.group("responseCode"), "200");
        return MOCK_BEARER_TOKEN + bearerTokenResponseExt;
    }
}

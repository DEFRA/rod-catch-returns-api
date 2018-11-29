package uk.gov.defra.datareturns.test.security;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.defra.datareturns.config.SecurityConfiguration;
import uk.gov.defra.datareturns.data.model.regions.Region;
import uk.gov.defra.datareturns.testcommons.framework.ApiContextTest;

@RunWith(SpringRunner.class)
@ApiContextTest
@Slf4j
public class PermissionEvaluationTests {
    private static final SecurityConfiguration.RcrPermissionEvaluator EVALUATOR = new SecurityConfiguration.RcrPermissionEvaluator();

    @Test
    @WithMockUser(authorities = {"REGION_READ"})
    public void testPermissionEvaluator() {
        final Region test = new Region();
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Assertions.assertThat(EVALUATOR.hasPermission(auth, test, "READ")).isTrue();
        Assertions.assertThat(EVALUATOR.hasPermission(auth, null, Region.class.getSimpleName(), "READ")).isTrue();
        Assertions.assertThat(EVALUATOR.hasPermission(auth, test, "WRITE")).isFalse();
        Assertions.assertThat(EVALUATOR.hasPermission(auth, null, Region.class.getSimpleName(), "WRITE")).isFalse();
    }

    @Test
    @WithMockUser(authorities = {"REGION_WRITE"})
    public void testPermissionEvaluator2() {
        final Region test = new Region();
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Assertions.assertThat(EVALUATOR.hasPermission(auth, test, "READ")).isFalse();
        Assertions.assertThat(EVALUATOR.hasPermission(auth, null, Region.class.getSimpleName(), "READ")).isFalse();
        Assertions.assertThat(EVALUATOR.hasPermission(auth, test, "WRITE")).isTrue();
        Assertions.assertThat(EVALUATOR.hasPermission(auth, null, Region.class.getSimpleName(), "WRITE")).isTrue();
    }

    @Test
    @WithMockUser(authorities = {"REGION_READ"})
    public void testPermissionEvaluatorNull() {
        Assertions.assertThat(EVALUATOR.hasPermission(SecurityContextHolder.getContext().getAuthentication(), null, "READ")).isFalse();
    }

    @Test
    public void testPermissionAuthNull() {
        Assertions.assertThat(EVALUATOR.hasPermission(null, null, "READ")).isFalse();
    }
}

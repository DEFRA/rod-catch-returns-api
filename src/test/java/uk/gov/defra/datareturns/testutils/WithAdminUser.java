package uk.gov.defra.datareturns.testutils;


import org.springframework.security.test.context.support.WithMockUser;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@WithMockUser(
        username = "admin1@example.com",
        password = "admin",
        authorities = {
                "USE_INTERNAL", "REFERENCE_DATA_WRITE"
        })
public @interface WithAdminUser {
}

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
                "JPA_ENTITY_DEFAULT_READ", "JPA_ENTITY_DEFAULT_WRITE", "JPA_ENTITY_DEFAULT_SUPER"
        })
public @interface WithAdminUser {
}

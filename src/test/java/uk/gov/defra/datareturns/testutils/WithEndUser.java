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
        username = "B7A718",
        password = "WA4 1HT",
        authorities = {"JPA_ENTITY_DEFAULT_READ"}
)
public @interface WithEndUser {
}

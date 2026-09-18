package com.example.rbac.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {

    // Shorthand for a set of required permissions (treated as requireAll).
    String[] value() default {};

    // Every permission listed here must be present on the user.
    String[] requireAll() default {};

    // At least one permission listed here must be present on the user.
    String[] requireAny() default {};
}

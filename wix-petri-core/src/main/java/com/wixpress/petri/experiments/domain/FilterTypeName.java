package com.wixpress.petri.experiments.domain;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author talyag
 * @since 9/8/14
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface FilterTypeName {
    // The value here CANNOT be changed. This is the unique identifier and enables you to rename your filter class if you like.
    public String value();
}

package com.student.ohmyibatis.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author Student
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.METHOD)
public @interface Update
{
    String sql();
}

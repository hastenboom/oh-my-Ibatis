package com.student.ohmyibatis.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = {java.lang.annotation.ElementType.METHOD})
public @interface Insert
{
    String sql();
}

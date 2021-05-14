package io.geek.lib.startup;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface StartupTask {
    ThreadMode threadMode() default ThreadMode.BACKGROUND;
    Class<? extends Task>[] dependencies() default {};
    String[] process() default {};
    int priority() default 0;
}


package io.geek.lib.startup.compiler;

import com.squareup.javapoet.ClassName;

public class ClassNames {

    static final ClassName LIST = ClassName.get("java.util","List");
    static final ClassName ARRAY_LIST = ClassName.get("java.util","ArrayList");
    static final ClassName TASK = ClassName.get("io.geek.lib.startup","Task");
    static final ClassName THREAD_MODE = ClassName.get("io.geek.lib.startup","ThreadMode");
    static final ClassName TASK_COLLECTOR = ClassName.get("io.geek.lib.startup","TaskCollector");
    static final ClassName TEXT_UTILS = ClassName.get("android.text","TextUtils");
}

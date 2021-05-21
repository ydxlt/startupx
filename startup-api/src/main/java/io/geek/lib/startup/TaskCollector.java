package io.geek.lib.startup;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import java.util.List;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface TaskCollector {

    @Nullable
    List<Task> collect(String process);
}

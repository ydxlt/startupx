package io.geek.lib.startup;

import androidx.annotation.Nullable;

import java.util.List;

public interface TaskCollector {

    @Nullable
    List<Task> collect(String process);
}

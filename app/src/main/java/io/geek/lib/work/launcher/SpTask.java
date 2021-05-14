package io.geek.lib.work.launcher;

import android.content.Context;
import android.util.Log;

import io.geek.lib.startup.StartupTask;
import io.geek.lib.startup.Task;
import io.geek.lib.startup.ThreadMode;

@StartupTask(threadMode = ThreadMode.BACKGROUND, process = {"web"},
        dependencies = {ResourceTask.class}, priority = 3)
public class SpTask implements Task {

    @Override
    public void execute(Context context) {
        Log.d("StartupTask", "SpTask execute!");
    }
}

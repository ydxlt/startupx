package io.geek.lib.work.launcher;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import io.geek.lib.startup.StartupTask;
import io.geek.lib.startup.Task;
import io.geek.lib.startup.ThreadMode;

@StartupTask
public class Block5Task implements Task {

    @Override
    public void execute(Application context) {
        Log.d("StartupTask","Block5Task execute!");
    }
}

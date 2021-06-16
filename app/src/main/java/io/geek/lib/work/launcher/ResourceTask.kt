package io.geek.lib.work.launcher

import android.app.Application
import android.content.Context
import android.util.Log
import io.geek.lib.startup.StartupTask
import io.geek.lib.startup.Task
import io.geek.lib.startup.ThreadMode

@StartupTask(threadMode = ThreadMode.MAIN, dependencies = [XmlTask::class], process = ["web"])
class ResourceTask : Task {
    override fun execute(context: Application) {
        Log.d("StartupTask", "ResourceTask execute!")
    }
}
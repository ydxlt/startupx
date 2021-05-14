package io.geek.lib.work.launcher

import android.content.Context
import android.util.Log
import io.geek.lib.startup.StartupTask
import io.geek.lib.startup.Task
import io.geek.lib.startup.ThreadMode

@StartupTask(dependencies = [Block5Task::class],priority = 2,
    threadMode = ThreadMode.BACKGROUND,process = ["web"])
class XmlTask : Task {
    override fun execute(context: Context?) {
        Log.d("StartupTask", "XmlTask execute!")
    }
}
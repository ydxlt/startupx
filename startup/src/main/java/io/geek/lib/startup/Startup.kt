package io.geek.lib.startup

import android.content.Context
import java.util.concurrent.Executor

/**
 * Startup
 */
object Startup {

    private val mScheduler: Scheduler by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { Scheduler() }
    private var mProcessName:String? = null

    fun setExecutor(executor: Executor): Startup {
        mScheduler.setExecutor(executor)
        return this
    }

    fun setContext(context: Context): Startup {
        mScheduler.setContext(context)
        return this
    }

    fun setProcessName(processName:String) : Startup {
        mProcessName = processName
        return this
    }

    fun launch() {
        val tasks = collectTasks() ?: return
        mScheduler.schedule(tasks)
    }

    private fun collectTasks(): List<Task>? {
        try {
            val collector: TaskCollector = Class.forName("io.geek.lib.startup.TaskCollector_Impl")
                .newInstance() as TaskCollector
            return collector.collect(mProcessName)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return mutableListOf()
    }
}
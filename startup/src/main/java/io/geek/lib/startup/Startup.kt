package io.geek.lib.startup

import android.content.Context
import java.util.concurrent.Executor

/**
 * Startup
 */
class Startup private constructor(
    private var context: Context,
    private var processName: String? = null,
    private var backgroundExecutor: Executor? = null,
    private var singleExecutor: Executor? = null
) {

    private val mScheduler: Scheduler by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        Scheduler().context(context).backgroundExecutor(backgroundExecutor)
            .singleExecutor(singleExecutor)
    }

    fun proceed() {
        val tasks = collectTasks() ?: return
        mScheduler.schedule(tasks)
    }

    private fun collectTasks(): List<Task>? {
        try {
            val collector: TaskCollector = Class.forName("io.geek.lib.startup.TaskCollector_Impl")
                .newInstance() as TaskCollector
            return collector.collect(processName)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    companion object {
        inline fun build(block: Builder.() -> Unit) = Builder().apply(block).build()
    }

    class Builder {
        lateinit var context: Context
        var processName: String? = null
        var backgroundExecutor: Executor? = null
        var singleExecutor: Executor? = null

        fun build() = Startup(context, processName, backgroundExecutor, singleExecutor)
    }
}
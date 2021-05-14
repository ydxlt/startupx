package io.geek.lib.startup

import android.content.Context
import android.os.Process
import java.lang.IllegalStateException
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

class Scheduler {

    /** The executor in which the background job will be invoked. */
    private var executor: Executor? = null
    private val mainExecutor: Executor by lazy { MainThreadExecutor() }
    private var context: Context? = null

    private class BackgroundThreadFactory : ThreadFactory {

        private val count = AtomicInteger(1)

        override fun newThread(r: Runnable): Thread {
            return Thread({
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
                r.run()
            }, "Scheduler #" + count.getAndIncrement())
        }
    }

    /** Set the custom executor. */
    fun setExecutor(executor: Executor): Scheduler {
        this.executor = executor
        return this
    }

    fun setContext(context: Context): Scheduler {
        this.context = context
        return this
    }

    fun schedule(taskList: List<Task>) {
        val executor = executor ?: defaultThreadPool()
        // 1. 检测是否有环
        taskList.checkCycle()
        // 2. 从根任务开始执行
        val rootTasks = mutableListOf<ScheduleTask>()
        buildScheduleTask(taskList, executor).forEach {
            if (it.isRoot()) {
                rootTasks.add(it)
            }
        }
        for (task in rootTasks) {
            task.execute()
        }
    }

    /**
     * 组装成ScheduleTask
     */
    private fun buildScheduleTask(taskList: List<Task>, executor: Executor): List<ScheduleTask> {
        val scheduleTasks = mutableListOf<ScheduleTask>()
        val scheduleTaskMap = mutableMapOf<Class<out Task>, ScheduleTask>()
        val taskClassNames = mutableListOf<String>()
        taskList.forEach { task ->
            val scheduleTask = ScheduleTaskImpl(context, executor, mainExecutor, task)
            scheduleTaskMap[task::class.java] = scheduleTask
            scheduleTasks.add(scheduleTask)
            taskClassNames.add(task::class.java.canonicalName)
        }
        taskList.forEach { task ->
            val children = scheduleTaskMap[task::class.java]!!
            task.dependencies()?.forEach { clazz: Class<out Task> ->
                val clazz = clazz.realClass
                if(taskClassNames.contains(clazz.canonicalName)) {
                    val parent: ScheduleTask = scheduleTaskMap[clazz]
                        ?: throw SchedulerException("Dependency $clazz not found，process on ${task.process()}!")
                    parent.addChild(children)
                    children.addParent(parent)
                }
            }
        }
        return scheduleTasks
    }

    companion object {
        private val CPU_COUNT = Runtime.getRuntime().availableProcessors()
        private val CORE_POOL_SIZE = 2.coerceAtLeast((CPU_COUNT - 1).coerceAtMost(4))
        private val MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1
        private const val KEEP_ALIVE_SECONDS = 30L

        private fun defaultThreadPool() = ThreadPoolExecutor(
            CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
            LinkedBlockingQueue(20), BackgroundThreadFactory()
        )

        internal fun runOnNewThread(action: () -> Unit) =
            BackgroundThreadFactory().newThread(action).start()
    }
}
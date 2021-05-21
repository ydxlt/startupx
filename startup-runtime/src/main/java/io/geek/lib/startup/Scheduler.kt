package io.geek.lib.startup

import android.content.Context
import android.os.Process
import androidx.annotation.RestrictTo
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class Scheduler {

    private var backgroundExecutor: Executor? = null
    private val mainExecutor: Executor by lazy { MainThreadExecutor() }
    private var singleExecutor: Executor? = null
    private var context: Context? = null
    internal val BACKGROUND: Executor
        get() {
            if (backgroundExecutor != null) {
                return backgroundExecutor!!
            }
            synchronized(this) {
                if (backgroundExecutor == null) {
                    backgroundExecutor = newSingleThreadPool()
                }
            }
            return backgroundExecutor!!
        }
    internal val SINGLE: Executor
        get() {
            if (singleExecutor != null) {
                return singleExecutor!!
            }
            synchronized(this) {
                if (singleExecutor == null) {
                    singleExecutor = newSingleThreadPool()
                }
            }
            return singleExecutor!!
        }
    internal val MAIN: Executor
        get() = mainExecutor

    internal class BackgroundThreadFactory : ThreadFactory {

        private val count = AtomicInteger(1)

        override fun newThread(r: Runnable): Thread {
            return Thread({
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
                r.run()
            }, "Scheduler #" + count.getAndIncrement())
        }
    }

    fun backgroundExecutor(executor: Executor?): Scheduler {
        if (executor != null) {
            this.backgroundExecutor = executor
        }
        return this
    }

    fun singleExecutor(executor: Executor?): Scheduler {
        if (executor != null) {
            this.backgroundExecutor = executor
        }
        return this
    }

    fun context(context: Context): Scheduler {
        this.context = context
        return this
    }

    fun execute(threadMode: ThreadMode, action: () -> Unit) = when (threadMode) {
        ThreadMode.MAIN -> MAIN.execute(action)
        ThreadMode.BACKGROUND -> BACKGROUND.execute(action)
        ThreadMode.SINGLE -> SINGLE.execute(action)
        ThreadMode.NEW -> action.runOnNewThread()
    }

    fun schedule(taskList: List<Task>) {
        // 1. 检测是否有环
        taskList.checkCycle()
        // 2. 从根任务开始执行
        val rootTasks = mutableListOf<TaskNode>()
        buildTaskNodes(taskList).forEach {
            if (it.isRoot()) {
                rootTasks.add(it)
            }
        }
        for (task in rootTasks) {
            task.execute()
        }
    }

    private fun buildTaskNodes(taskList: List<Task>): List<TaskNode> {
        val scheduleTasks = mutableListOf<TaskNode>()
        val scheduleTaskMap = mutableMapOf<Class<out Task>, TaskNode>()
        val taskClassNames = mutableListOf<String>()
        taskList.forEach { task ->
            val scheduleTask =
                TaskNodeImpl(context, this@Scheduler, task)
            scheduleTaskMap[task::class.java] = scheduleTask
            scheduleTasks.add(scheduleTask)
            taskClassNames.add(task::class.java.canonicalName)
        }
        taskList.forEach { task ->
            val children = scheduleTaskMap[task::class.java]!!
            task.dependencies()?.forEach { clazz: Class<out Task> ->
                val clazz = clazz.realClass
                if (taskClassNames.contains(clazz.canonicalName)) {
                    val parent: TaskNode = scheduleTaskMap[clazz]
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

        private fun newBackgroundThreadPool() = ThreadPoolExecutor(
            CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
            LinkedBlockingQueue(20), BackgroundThreadFactory()
        )

        private fun newSingleThreadPool() = ThreadPoolExecutor(
            1, 1, 3, TimeUnit.SECONDS,
            LinkedBlockingQueue(5), BackgroundThreadFactory()
        )
    }
}

internal fun (() -> Unit).runOnNewThread() =
    Scheduler.BackgroundThreadFactory().newThread(this).start()
package io.geek.lib.startup

import android.content.Context
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicInteger

interface ScheduleTask {

    fun priority() : Int

    /** Execute the job. */
    fun execute()

    /** Add parent job. */
    fun addParent(scheduleTask: ScheduleTask)

    /** Add child job. */
    fun addChild(scheduleTask: ScheduleTask)

    /** Get parents. */
    fun parents(): List<ScheduleTask>

    /** Get children. */
    fun children(): List<ScheduleTask>

    /** For parent to notify children the job finished. */
    fun finished(scheduleTask: ScheduleTask)
}

inline fun ScheduleTask.forEach(block:(child:ScheduleTask)->Unit){
    this.children().forEach(block)
}

inline fun ScheduleTask.isRoot() = this.parents().isEmpty()

internal class ScheduleTaskImpl (
    private val context: Context?,
    private val executor: Executor,
    private val mainExecutor: Executor,
    private val task:Task
    ) : ScheduleTask {

    private val parents = mutableListOf<ScheduleTask>()
    private val children = mutableListOf<ScheduleTask>()
    private var dependencies = AtomicInteger(0)

    override fun priority() : Int {
        return task.priority()
    }

    override fun execute() {
        val action = {
            task.execute(context)
            children.sortByDescending {
                it.priority()
            }
            // notify children
            children.forEach { it.finished(this) }
        }
        try {
            when(task.threadMode()){
                ThreadMode.BACKGROUND -> {
                    executor.execute(action)
                }
                ThreadMode.MAIN -> {
                    mainExecutor.execute(action)
                }
                ThreadMode.NEW -> {
                    Scheduler.runOnNewThread(action)
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun addParent(job: ScheduleTask) {
        parents.add(job)
        dependencies.addAndGet(1)
    }

    override fun addChild(job: ScheduleTask) {
        children.add(job)
    }

    override fun parents(): List<ScheduleTask> = parents

    override fun children(): List<ScheduleTask> = children

    override fun finished(job: ScheduleTask) {
        if (dependencies.decrementAndGet() == 0) {
            // All dependencies finished, commit the job.
            execute()
        }
    }
}
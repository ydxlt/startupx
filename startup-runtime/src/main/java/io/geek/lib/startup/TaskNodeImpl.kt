package io.geek.lib.startup

import android.app.Application
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author xluotong@gmail.com
 */
internal class TaskNodeImpl (
    private val application: Application,
    private val scheduler: Scheduler,
    private val task:Task
) : TaskNode {

    private val parents = mutableListOf<TaskNode>()
    private val children = mutableListOf<TaskNode>()
    private var dependencies = AtomicInteger(0)

    override fun priority() : Int {
        return task.priority()
    }

    override fun execute() {
        val action = {
            task.execute(application)
            children.sortByDescending {
                it.priority()
            }
            // notify children
            children.forEach { it.finished(this) }
        }
        try {
            task.threadMode()?.let { scheduler.execute(it,action) }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun addParent(job: TaskNode) {
        parents.add(job)
        dependencies.addAndGet(1)
    }

    override fun addChild(job: TaskNode) {
        children.add(job)
    }

    override fun parents(): List<TaskNode> = parents

    override fun children(): List<TaskNode> = children

    override fun finished(job: TaskNode) {
        if (dependencies.decrementAndGet() == 0) {
            execute()
        }
    }
}
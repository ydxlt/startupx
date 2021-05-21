package io.geek.lib.startup

interface TaskNode {

    fun priority() : Int

    fun execute()

    fun addParent(taskNode: TaskNode)

    fun addChild(taskNode: TaskNode)

    fun parents(): List<TaskNode>

    fun children(): List<TaskNode>

    fun finished(taskNode: TaskNode)
}

inline fun TaskNode.forEach(block:(child:TaskNode)->Unit){
    this.children().forEach(block)
}

inline fun TaskNode.isRoot() = this.parents().isEmpty()


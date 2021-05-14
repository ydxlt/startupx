package io.geek.lib.startup

import java.util.*
import kotlin.collections.ArrayList
import kotlin.jvm.Throws

fun Task.getTaskDegree(allTask:List<Task>) : Int {
    val allTaskNames = mutableListOf<String>()
    allTask.forEach {
        allTaskNames.add(it::class.java.canonicalName)
    }
    val dependencies = mutableListOf<Class<out Task>>()
    this.dependencies()?.forEach {
        if(allTaskNames.contains(it::class.java.canonicalName)){
            dependencies.add(it)
        }
    }
    return dependencies.size
}

val Class<out Task>.realClass:Class<out Task>
    get() =  Class.forName(this.canonicalName+"_Wrapper") as Class<out Task>

/**
 * 使用出度表示方法检测有向无环图是否存在环
 */
@Throws(SchedulerException::class)
fun List<Task>.checkCycle()  {
    val result = ArrayList<Task>(this)
    // 出度为0的队列
    val queue = LinkedList<Task>()
    val taskOutDegreeMap = mutableMapOf<Class<out Task>, Int>()
    val childrenMap = mutableMapOf<Class<out Task>, MutableList<Class<out Task>>?>()
    val taskClassMap = mutableMapOf<Class<out Task>,Task>()
    this.forEach { task ->
        if(task.getTaskDegree(this) == 0){
            queue.add(task)
        }
        taskOutDegreeMap[task.javaClass] = task.dependencies()?.size?:0
        taskClassMap[task.javaClass] = task
    }
    this.forEach { task ->
        task.dependencies()?.forEach { clazz: Class<out Task> ->
            val clazz = clazz.realClass
            var children = childrenMap[clazz]
            if(children == null){
                children = mutableListOf()
                childrenMap[clazz] = children
            }
            children.add(task.javaClass)
        }
    }
    while(queue.isNotEmpty()){
        // 移除出度为0的顶点
        val task = queue.pop()
        result.remove(task)
        // 此顶点相连的边
        childrenMap[task.javaClass]?.forEach {
            var degree = taskOutDegreeMap[it]?:0
            degree--
            if(degree == 0){
                queue.offer(taskClassMap[it])
            }
            taskOutDegreeMap[it] = degree // 更新出度
        }
    }
    if(result.isNotEmpty()) throw SchedulerException("Cycle dependencies exist!")
}

package io.geek.lib.startup.compiler;

import com.squareup.javapoet.ClassName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.lang.model.type.TypeMirror;

public class Util {

    public static void checkCycle(Map<String, List<ClassName>> classNameMap, List<String> defaultList, Map<String, List<? extends TypeMirror>> dependencies) {
        classNameMap.forEach((processName, classNameList) -> {
            checkCycleInternal(dependencies, processName, classNameList);
        });
        List<ClassName> defaultNameList = new ArrayList<>();
        for(String str : defaultList){
            defaultNameList.add(ClassName.bestGuess(str));
        }
        checkCycleInternal(dependencies, "default process", defaultNameList);
    }

    private static boolean isDegreeZero(List<? extends TypeMirror> dependency,List<ClassName> classNameList){
        if(dependency == null || dependency.isEmpty()){
            return true;
        }
        List<String> classStringList = new ArrayList<>();
        for(ClassName className : classNameList){
            classStringList.add(className.canonicalName());
        }
        dependency = new ArrayList<>(dependency);
        Iterator<? extends TypeMirror> iterator = dependency.listIterator();
        while (iterator.hasNext()){
            String classStr = ClassName.get(iterator.next()).toString();
            if(!classStringList.contains(classStr)){
                iterator.remove();
            }
        }
        return dependency.size() == 0;
    }

    private static void checkCycleInternal(Map<String, List<? extends TypeMirror>> dependencies, String processName, List<ClassName> classNameList) {
        // 出度为0的队列
        LinkedList<String> queue = new LinkedList<>();
        List<String> results = new ArrayList<>();
        for(ClassName className : classNameList){
            results.add(className.canonicalName());
        }
        Map<String, Integer> taskOutDegreeMap = new HashMap<>();
        Map<String, List<ClassName>> childrenMap = new HashMap<>();
        for (ClassName className : classNameList) {
            List<? extends TypeMirror> dependency = dependencies.get(className.canonicalName());
            if (isDegreeZero(dependency,classNameList)) {
                queue.offer(className.canonicalName());
            }
            taskOutDegreeMap.put(className.canonicalName(), dependency == null ? 0 : dependency.size());
            if (dependency != null) {
                for (TypeMirror typeMirror : dependency) {
                    final String clazzStr = ClassName.get(typeMirror).toString();
                    List<ClassName> children = childrenMap.get(clazzStr);
                    if (children == null) {
                        children = new ArrayList<>();
                        childrenMap.put(clazzStr, children);
                    }
                    children.add(className);
                }
            }
        }
        while (!queue.isEmpty()) {
            String target = queue.pop();
            results.remove(target);
            List<ClassName> children = childrenMap.get(target);
            if (children != null) {
                for (ClassName className : children) {
                    Integer degree = taskOutDegreeMap.get(className.canonicalName());
                    if (degree != null) {
                        degree--;
                        if (degree == 0) {
                            queue.offer(className.canonicalName());
                        }
                        // 更新出度
                        taskOutDegreeMap.put(className.canonicalName(),degree);
                    }
                }
            }
        }
        if (results.size() > 0) {
            throw new CompileException("Cycle dependencies on process of " + processName + "!, taskList = "+classNameList
            +" \n, dependencies = " + dependencies);
        }
    }
}

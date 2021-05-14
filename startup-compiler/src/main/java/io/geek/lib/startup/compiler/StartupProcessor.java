package io.geek.lib.startup.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import io.geek.lib.startup.StartupTask;

@AutoService(Processor.class)
public class StartupProcessor extends AbstractProcessor {

    private Filer mFiler;
    private Elements mElements;
    private Types mTypes;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mFiler = processingEnv.getFiler();
        mElements = processingEnv.getElementUtils();
        mTypes = processingEnv.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> startupTaskElements = roundEnvironment.getElementsAnnotatedWith(StartupTask.class);
        if(startupTaskElements != null && startupTaskElements.size() > 0){
            processStartupTaskElements(startupTaskElements);
        }
        return false;
    }

    private void processStartupTaskElements(Set<? extends Element> startupTaskElements) {
        Map<String,List<ClassName>> classNameMap = new HashMap<>();
        Map<String,List<ClassName>> realClassNameMap = new HashMap<>();
        List<ClassName> defaultList = new ArrayList<>();
        List<String> defaultTaskNameList = new ArrayList<>();
        Map<String,List<? extends TypeMirror>> dependencies = new HashMap<>();
        for(Element element : startupTaskElements){
            if(element.getKind() != ElementKind.CLASS){
                throw new CompileException("The annotation target of StartupTask must be a class");
            }
            if(element.getModifiers().contains(Modifier.ABSTRACT)){
                throw new CompileException("The annotation target of StartupTask must not abstract");
            }
            TypeElement typeElement = (TypeElement) element;
            TaskWrapperHelper taskWrapperHelper = new TaskWrapperHelper(typeElement,mElements,mTypes);
            try {
                JavaFile javaFile = taskWrapperHelper.getJavaFile();
                javaFile.writeTo(mFiler);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String[] processArray = taskWrapperHelper.getProcessName();
            for(String processName : processArray){
                List<ClassName> classNameList = classNameMap.get(processName);
                List<ClassName> realClassNameList = realClassNameMap.get(processName);
                if(classNameList == null){
                    classNameList = new ArrayList<>();
                    classNameMap.put(processName,classNameList);
                }
                if(realClassNameList == null){
                    realClassNameList = new ArrayList<>();
                    realClassNameMap.put(processName,realClassNameList);
                }
                classNameList.add(taskWrapperHelper.getWrapperClassName());
                realClassNameList.add(ClassName.bestGuess(taskWrapperHelper.getTaskName()));
            }
            if(processArray == null || processArray.length == 0){
                defaultList.add(taskWrapperHelper.getWrapperClassName());
                defaultTaskNameList.add(taskWrapperHelper.getTaskName());
            }
            dependencies.put(taskWrapperHelper.getTaskName(),taskWrapperHelper.getDependencies());
        }
        Util.checkCycle(realClassNameMap,defaultTaskNameList,dependencies);
        try {
            new TaskCollectorHelper(mElements,classNameMap,defaultList).brewJava().writeTo(mFiler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new HashSet<>();
        annotations.add(StartupTask.class.getCanonicalName());
        return Collections.unmodifiableSet(annotations);
    }
}

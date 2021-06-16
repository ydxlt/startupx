package io.geek.lib.startup;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 * 实现类必须具有无参构造方法
 */
public interface Task {

    int PRIORITY_NORMAL = 0;

    @Nullable
    default String[] process() {
        return null;
    }

    /**
     * 优先级只保证被先通知依赖任务完成可以加入执行队列，真正的执行先后需要看任务执行线程的优先级，
     * 如果是同一个线程，那么肯定是优先级高的先执行
     *
     * @return
     */
    default int priority(){
        return PRIORITY_NORMAL;
    }

    @Nullable
    default ThreadMode threadMode() {
        return null;
    }

    @Nullable
    default List<Class<? extends Task>> dependencies() {
        return null;
    }

    void execute(@NonNull Application context);
}

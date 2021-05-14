package io.geek.lib.startup

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor

class MainThreadExecutor : Executor {

    private val mHandler by lazy {
        Handler(Looper.getMainLooper())
    }

    override fun execute(command: Runnable) {
        if(Looper.myLooper() == Looper.getMainLooper()){
            command.run()
        } else {
            mHandler.post(command)
        }
    }
}
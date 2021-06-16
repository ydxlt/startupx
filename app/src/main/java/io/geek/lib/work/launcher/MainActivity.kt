package io.geek.lib.work.launcher

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import io.geek.lib.startup.Startup

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(io.geek.lib.work.app.launcher.R.layout.activity_main)
    }

    fun start(v: View){
        Thread{
            Startup.build {
                context = application
                processName = "web"
            }.proceed()
        }.start()
    }
}
package com.example.coll.ui

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity

/**
 * Заглушка: активность объявлена в манифесте модуля, полноценный экран можно подключить позже.
 */
class ScheduleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(FrameLayout(this))
        title = "Расписание"
    }
}

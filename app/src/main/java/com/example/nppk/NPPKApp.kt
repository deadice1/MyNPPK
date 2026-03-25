package com.example.nppk

import android.app.Application
import com.example.schedule.di.navigationModule
import com.example.schedule.feature.schedule.di.featureScheduleModule
import com.example.schedule.shared.date.di.sharedDateModule
import com.example.schedule.shared.group.di.sharedGroupModule
import com.example.schedule.shared.schedule.di.sharedScheduleModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class NPPKApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Запускаем Koin, используя модули из проекта Schedule
        startKoin {
            androidContext(this@NPPKApp)

            modules(
                navigationModule,
                sharedDateModule,
                sharedGroupModule,
                sharedScheduleModule,
                featureScheduleModule
            )
        }
    }
}
package com.example.nppk

import android.app.Application
import com.example.nppk.data.di.dataModule
import com.example.nppk.data.di.networkModule
import com.example.schedule.di.navigationModule
import com.example.schedule.feature.schedule.di.featureScheduleModule
import com.example.schedule.shared.date.di.sharedDateModule
import com.example.schedule.shared.group.di.sharedGroupModule
import com.example.schedule.shared.schedule.di.sharedScheduleModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module

class NPPKApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Запускаем Koin, используя модули из проекта Schedule
        startKoin {
            androidContext(this@NPPKApp)

            val appModule = module {
                single(named("SCHEDULE_BASE_URL")) { "http://147.124.204.18:8000/" }
                single(named("GROUP_BASE_URL")) { "http://80.89.199.85:8081/" }
            }

            modules(
                appModule,
                networkModule,
                navigationModule,
                sharedDateModule,
                sharedGroupModule,
                sharedScheduleModule,
                featureScheduleModule,
                dataModule
            )
        }
    }
}
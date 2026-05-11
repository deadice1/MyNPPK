package com.example.schedule.shared.schedule.di

import com.example.schedule.shared.schedule.data.ScheduleApi
import com.example.schedule.shared.schedule.data.repository.ScheduleRepositoryImpl
import com.example.schedule.shared.schedule.data.repository.TeacherPreferencesRepositoryImpl
import com.example.schedule.shared.schedule.data.repository.TeacherScheduleRepositoryImpl
import com.example.schedule.shared.schedule.domain.repository.ScheduleRepository
import com.example.schedule.shared.schedule.domain.repository.TeacherPreferencesRepository
import com.example.schedule.shared.schedule.domain.repository.TeacherScheduleRepository
import com.example.schedule.shared.schedule.domain.usecase.GetScheduleByDateUseCase
import com.example.schedule.shared.schedule.domain.usecase.GetTeacherScheduleUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

val sharedScheduleModule = module {
    single<ScheduleApi> { get<Retrofit>(named("ScheduleRetrofit")).create(ScheduleApi::class.java) }

    single<ScheduleRepository> { ScheduleRepositoryImpl(api = get()) }

    single<TeacherPreferencesRepository> {
        TeacherPreferencesRepositoryImpl(context = androidContext(), gson = get())
    }
    single<TeacherScheduleRepository> {
        TeacherScheduleRepositoryImpl(scheduleRepository = get(), preferencesRepository = get())
    }

    factory { GetScheduleByDateUseCase(repository = get()) }
    factory { GetTeacherScheduleUseCase(repository = get()) }
}
package com.example.nppk.data.di

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val networkModule = module {
    single { Gson() }

    single {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    single(named("ScheduleRetrofit")) {
        Retrofit.Builder()
            .baseUrl(get<String>(named("SCHEDULE_BASE_URL")))
            .client(get())
            .addConverterFactory(GsonConverterFactory.create(get()))
            .build()
    }

    single(named("GroupRetrofit")) {
        Retrofit.Builder()
            .baseUrl(get<String>(named("GROUP_BASE_URL")))
            .client(get())
            .addConverterFactory(GsonConverterFactory.create(get()))
            .build()
    }
}

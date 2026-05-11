package com.example.nppk.data.di

import com.example.nppk.data.api.AuthApi
import com.example.nppk.data.repository.AuthRepository
import com.example.nppk.data.repository.AuthRepositoryImpl
import com.example.nppk.data.repository.AppMainGroupRepository
import com.example.schedule.shared.group.domain.repository.MainGroupRepository
import com.example.nppk.ui.viewmodels.LoginViewModel
import com.example.nppk.ui.viewmodels.GroupSelectionViewModel
import com.example.nppk.ui.viewmodels.MyGroupsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import com.example.nppk.ui.viewmodels.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import retrofit2.Retrofit

val dataModule = module {
    single<AuthApi> { get<Retrofit>(named("GroupRetrofit")).create(AuthApi::class.java) }
    single<AuthRepository> { AuthRepositoryImpl(authApi = get(), context = androidContext()) }
    single<MainGroupRepository> { AppMainGroupRepository(get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { LoginViewModel(get()) }
    viewModel { GroupSelectionViewModel(get()) }
    viewModel { MyGroupsViewModel(get(), get(), get()) }
}
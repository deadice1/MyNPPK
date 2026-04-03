package com.example.nppk.data.di

import com.example.nppk.data.repository.AuthRepository
import com.example.nppk.data.repository.AuthRepositoryImpl
import org.koin.dsl.module
import com.example.nppk.ui.viewmodels.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel

val dataModule = module {
    single<AuthRepository> { AuthRepositoryImpl() }
    viewModel { SettingsViewModel(get()) }
}
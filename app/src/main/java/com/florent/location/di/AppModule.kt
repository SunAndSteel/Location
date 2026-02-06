package com.florent.location.di

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.florent.location.data.db.AppDatabase
import com.florent.location.ui.tenant.TenantViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(
        androidContext(),
        AppDatabase::class.java,
        "locative.db"
        ).build()
    }

    single {
        get<AppDatabase>().tenantDao()
    }

    viewModel { TenantViewModel(get()) }
}

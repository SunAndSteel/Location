package com.florent.location.di

import androidx.room.Room
import com.florent.location.data.db.AppDatabase
import com.florent.location.data.repository.TenantRepositoryImpl
import com.florent.location.domain.repository.TenantRepository
import com.florent.location.domain.usecase.TenantUseCases
import com.florent.location.domain.usecase.TenantUseCasesImpl
import com.florent.location.ui.tenant.TenantViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
// DAO
    single { get<AppDatabase>().tenantDao() }

    // Repository : interface → impl
    single<TenantRepository> {
        TenantRepositoryImpl(dao = get())
    }

    // UseCases
    single<TenantUseCases> {
        TenantUseCasesImpl(repository = get())
    }

    // ViewModel
    viewModel {
        TenantViewModel(useCases = get())
    }
}

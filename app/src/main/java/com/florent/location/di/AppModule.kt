package com.florent.location.di

import androidx.room.Room
import com.florent.location.data.db.AppDatabase
import com.florent.location.data.repository.HousingRepositoryImpl
import com.florent.location.data.repository.LeaseRepositoryImpl
import com.florent.location.data.repository.TenantRepositoryImpl
import com.florent.location.domain.repository.HousingRepository
import com.florent.location.domain.repository.LeaseRepository
import com.florent.location.domain.repository.TenantRepository
import com.florent.location.domain.usecase.bail.BailUseCases
import com.florent.location.domain.usecase.bail.BailUseCasesImpl
import com.florent.location.domain.usecase.housing.HousingUseCases
import com.florent.location.domain.usecase.housing.HousingUseCasesImpl
import com.florent.location.domain.usecase.housing.ObserveHousingSituation
import com.florent.location.domain.usecase.tenant.ObserveTenantSituation
import com.florent.location.domain.usecase.lease.LeaseUseCases
import com.florent.location.domain.usecase.lease.LeaseUseCasesImpl
import com.florent.location.domain.usecase.tenant.TenantUseCases
import com.florent.location.domain.usecase.tenant.TenantUseCasesImpl
import com.florent.location.ui.housing.HousingListViewModel
import com.florent.location.ui.housing.HousingDetailViewModel
import com.florent.location.ui.housing.HousingEditViewModel
import com.florent.location.ui.auth.AuthGateViewModel
import com.florent.location.ui.auth.LoginViewModel
import com.florent.location.ui.tenant.TenantListViewModel
import com.florent.location.ui.tenant.TenantDetailViewModel
import com.florent.location.ui.tenant.TenantEditViewModel
import com.florent.location.ui.lease.LeaseCreateViewModel
import com.florent.location.ui.lease.LeaseDetailViewModel
import com.florent.location.ui.lease.LeaseListViewModel
import com.florent.location.ui.sync.UnifiedSyncManager
import com.florent.location.ui.sync.HousingSyncRequester
import com.florent.location.ui.sync.HousingSyncStateObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // =========================================================================
    // Room Database
    // =========================================================================
    single<AppDatabase> {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "location.db"
        )
            .fallbackToDestructiveMigration()
            // TODO: Remplacer par de vraies migrations en production
            // .addMigrations(MIGRATION_1_2, MIGRATION_2_3, etc.)
            .build()
    }

    // Supabase client
    single { com.florent.location.data.supabase.provideSupabaseClient() }

    // =========================================================================
    // DAOs
    // =========================================================================
    single { get<AppDatabase>().tenantDao() }
    single { get<AppDatabase>().housingDao() }
    single { get<AppDatabase>().leaseDao() }
    single { get<AppDatabase>().keyDao() }
    single { get<AppDatabase>().indexationEventDao() }
    single { get<AppDatabase>().authSessionDao() }

    // =========================================================================
    // Repositories (Domain)
    // =========================================================================
    single<TenantRepository> { TenantRepositoryImpl(dao = get()) }

    single<HousingRepository> {
        HousingRepositoryImpl(
            housingDao = get(),
            keyDao = get(),
            leaseDao = get()
        )
    }

    single<LeaseRepository> {
        LeaseRepositoryImpl(
            db = get(),
            leaseDao = get(),
            indexationEventDao = get()
        )
    }

    // =========================================================================
    // Auth & Sync Repositories
    // =========================================================================
    single {
        com.florent.location.data.auth.AuthRepository(
            supabase = get(),
            sessionDao = get()
        )
    }

    // Sync repositories - un pour chaque entité
    single {
        com.florent.location.data.sync.HousingSyncRepository(
            supabase = get(),
            housingDao = get()
        )
    }

    single {
        com.florent.location.data.sync.TenantSyncRepository(
            supabase = get(),
            tenantDao = get()
        )
    }

    single {
        com.florent.location.data.sync.LeaseSyncRepository(
            supabase = get(),
            leaseDao = get(),
            housingDao = get(),
            tenantDao = get()
        )
    }

    single {
        com.florent.location.data.sync.KeySyncRepository(
            supabase = get(),
            keyDao = get(),
            housingDao = get()
        )
    }

    single {
        com.florent.location.data.sync.IndexationEventSyncRepository(
            supabase = get(),
            indexationEventDao = get(),
            leaseDao = get()
        )
    }

    // Unified sync manager - gère la synchronisation de toutes les entités
    single {
        UnifiedSyncManager(
            housingRepo = get(),
            tenantRepo = get(),
            leaseRepo = get(),
            keyRepo = get(),
            indexationEventRepo = get(),
            supabase = get(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        )
    }

    // Interface pour injecter dans les ViewModels
    single<HousingSyncRequester> { get<UnifiedSyncManager>() }

    // Interface pour observer l'état global de sync dans l'UI
    single<HousingSyncStateObserver> { get<UnifiedSyncManager>() }

    // =========================================================================
    // UseCases
    // =========================================================================
    single<TenantUseCases> { TenantUseCasesImpl(repository = get()) }
    single<HousingUseCases> { HousingUseCasesImpl(repository = get()) }
    single { ObserveHousingSituation(leaseRepository = get()) }
    single { ObserveTenantSituation(leaseRepository = get()) }
    single<LeaseUseCases> { LeaseUseCasesImpl(repository = get(), housingRepository = get()) }
    single<BailUseCases> { BailUseCasesImpl(repository = get()) }

    // =========================================================================
    // ViewModels - Housing
    // =========================================================================
    viewModel { HousingListViewModel(useCases = get(), observeHousingSituation = get(), syncManager = get()) }
    viewModel { params ->
        HousingDetailViewModel(
            housingId = params.get(),
            housingUseCases = get(),
            observeHousingSituation = get(),
            syncManager = get()
        )
    }
    viewModel { params ->
        HousingEditViewModel(
            housingId = params.getOrNull(),
            useCases = get(),
            syncManager = get()
        )
    }

    // =========================================================================
    // ViewModels - Tenant
    // =========================================================================
    viewModel { TenantListViewModel(useCases = get(), observeTenantSituation = get(), syncManager = get()) }
    viewModel { params ->
        TenantDetailViewModel(
            tenantId = params.get(),
            tenantUseCases = get(),
            observeTenantSituation = get(),
            syncManager = get()
        )
    }
    viewModel { params ->
        TenantEditViewModel(
            tenantId = params.getOrNull(),
            useCases = get(),
            syncManager = get() // Utilise le même HousingSyncRequester
        )
    }

    // =========================================================================
    // ViewModels - Lease
    // =========================================================================
    viewModel { LeaseListViewModel(useCases = get()) }
    viewModel {
        LeaseCreateViewModel(
            housingUseCases = get(),
            tenantUseCases = get(),
            leaseUseCases = get(),
            syncManager = get()
        )
    }
    viewModel { params ->
        LeaseDetailViewModel(
            leaseId = params.get(),
            bailUseCases = get(),
            leaseUseCases = get(),
            housingUseCases = get(),
            syncManager = get()
        )
    }

    // =========================================================================
    // ViewModels - Auth
    // =========================================================================
    viewModel {
        AuthGateViewModel(
            authRepository = get(),
            syncManager = get()
        )
    }
    viewModel {
        LoginViewModel(
            authRepository = get(),
            syncManager = get()
        )
    }
}

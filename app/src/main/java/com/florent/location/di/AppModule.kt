package com.florent.location.di

import androidx.room.Room
import com.florent.location.data.db.AppDatabase

// --- Data: repositories (impl) ---
import com.florent.location.data.repository.HousingRepositoryImpl
import com.florent.location.data.repository.LeaseRepositoryImpl
import com.florent.location.data.repository.TenantRepositoryImpl

// --- Domain: repositories (interfaces) ---
import com.florent.location.domain.repository.HousingRepository
import com.florent.location.domain.repository.LeaseRepository
import com.florent.location.domain.repository.TenantRepository

// --- Domain: usecases (bundles) ---
import com.florent.location.domain.usecase.housing.HousingUseCases
import com.florent.location.domain.usecase.housing.HousingUseCasesImpl
import com.florent.location.domain.usecase.indexation.IndexationUseCases
import com.florent.location.domain.usecase.indexation.IndexationUseCasesImpl
import com.florent.location.domain.usecase.lease.LeaseUseCases
import com.florent.location.domain.usecase.lease.LeaseUseCasesImpl
import com.florent.location.domain.usecase.tenant.TenantUseCases
import com.florent.location.domain.usecase.tenant.TenantUseCasesImpl

// --- UI: viewmodels ---
import com.florent.location.ui.housing.HousingListViewModel
import com.florent.location.ui.housing.HousingDetailViewModel
import com.florent.location.ui.housing.HousingEditViewModel

import com.florent.location.ui.tenant.TenantListViewModel
import com.florent.location.ui.tenant.TenantDetailViewModel
import com.florent.location.ui.tenant.TenantEditViewModel

import com.florent.location.ui.lease.LeaseCreateViewModel
import com.florent.location.ui.lease.LeaseDetailViewModel

import com.florent.location.ui.indexation.IndexationViewModel

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * AppModule complet (MVP) pour l'app de gestion:
 * - Logements
 * - Locataires
 * - Baux (incluant compteurs + clés)
 * - Indexations à venir
 *
 * Référence de périmètre: :contentReference[oaicite:0]{index=0}
 */
val appModule = module {

    // ---------------------------------------------------------------------
    // Room DB
    // ---------------------------------------------------------------------
    single<AppDatabase> {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "location.db"
        )
            // OK pour MVP. Quand tu passes en prod: remplace par de vraies migrations.
            .fallbackToDestructiveMigration()
            .build()
    }

    // ---------------------------------------------------------------------
    // DAOs
    // ---------------------------------------------------------------------
    single { get<AppDatabase>().tenantDao() }
    single { get<AppDatabase>().housingDao() }
    single { get<AppDatabase>().leaseDao() }
    single { get<AppDatabase>().keyDao() }

    // ---------------------------------------------------------------------
    // Repositories (Domain interfaces -> Data impl)
    // ---------------------------------------------------------------------
    single<TenantRepository> { TenantRepositoryImpl(dao = get()) }

    single<HousingRepository> {
        HousingRepositoryImpl(
            housingDao = get(),
            leaseDao = get() // utile si tu exposes "logements + bail actif" côté repo
        )
    }

    // LeaseRepositoryImpl recommandé avec transaction via db.withTransaction
    // (createLeaseWithKeys, closeLease, etc.)
    single<LeaseRepository> {
        LeaseRepositoryImpl(
            db = get(),
            leaseDao = get(),
            keyDao = get()
        )
    }

    // ---------------------------------------------------------------------
    // UseCases (bundles)
    // ---------------------------------------------------------------------
    single<TenantUseCases> { TenantUseCasesImpl(repository = get()) }
    single<HousingUseCases> { HousingUseCasesImpl(repository = get()) }
    single<LeaseUseCases> { LeaseUseCasesImpl(repository = get()) }

    // “Indexations à venir” = lecture métier (baux actifs triés + J-60)
    // Impl typique: s’appuie sur LeaseRepository (observe active leases)
    single<IndexationUseCases> { IndexationUseCasesImpl(leaseRepository = get()) }

    // ---------------------------------------------------------------------
    // ViewModels (1 par écran MVP)
    // ---------------------------------------------------------------------

    // Logements
    viewModel { HousingListViewModel(useCases = get()) }
    viewModel { (housingId: Long) -> HousingDetailViewModel(housingId = housingId, housingUseCases = get(), leaseUseCases = get()) }
    viewModel { (housingId: Long?) -> HousingEditViewModel(housingId = housingId, useCases = get()) }

    // Locataires
    viewModel { TenantListViewModel(useCases = get()) }
    viewModel { (tenantId: Long) -> TenantDetailViewModel(tenantId = tenantId, tenantUseCases = get(), leaseUseCases = get()) }
    viewModel { (tenantId: Long?) -> TenantEditViewModel(tenantId = tenantId, useCases = get()) }

    // Baux
    viewModel {
        // workflow: choisir logement + locataire, puis save bail + compteurs + clés
        LeaseCreateViewModel(
            housingUseCases = get(),
            tenantUseCases = get(),
            leaseUseCases = get()
        )
    }
    viewModel { (leaseId: Long) ->
        LeaseDetailViewModel(
            leaseId = leaseId,
            leaseUseCases = get()
        )
    }

    // Indexations à venir
    viewModel { IndexationViewModel(useCases = get()) }
}

@file:OptIn(ExperimentalMaterial3Api::class)

package com.florent.location.ui.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.florent.location.ui.housing.HousingDetailScreen
import com.florent.location.ui.housing.HousingDetailViewModel
import com.florent.location.ui.housing.HousingEditScreen
import com.florent.location.ui.housing.HousingEditViewModel
import com.florent.location.ui.housing.HousingListScreen
import com.florent.location.ui.housing.HousingListViewModel
import com.florent.location.ui.indexation.IndexationScreen
import com.florent.location.ui.indexation.IndexationViewModel
import com.florent.location.ui.lease.LeaseCreateScreen
import com.florent.location.ui.lease.LeaseCreateUiEvent
import com.florent.location.ui.lease.LeaseCreateViewModel
import com.florent.location.ui.lease.LeaseDetailScreen
import com.florent.location.ui.lease.LeaseDetailViewModel
import com.florent.location.ui.tenant.TenantDetailScreen
import com.florent.location.ui.tenant.TenantDetailViewModel
import com.florent.location.ui.tenant.TenantEditScreen
import com.florent.location.ui.tenant.TenantEditViewModel
import com.florent.location.ui.tenant.TenantListScreen
import com.florent.location.ui.tenant.TenantListViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

object LocationRoutes {
    const val HOUSINGS = "housings"
    const val HOUSING_DETAIL = "housing/{housingId}"
    const val HOUSING_EDIT = "housing/edit?housingId={housingId}"
    const val TENANTS = "tenants"
    const val TENANT_DETAIL = "tenant/{tenantId}"
    const val TENANT_EDIT = "tenant/edit?tenantId={tenantId}"
    const val LEASE_CREATE = "lease/create?housingId={housingId}&tenantId={tenantId}"
    const val LEASE_DETAIL = "lease/{leaseId}"
    const val INDEXATIONS = "indexations"

    fun housingDetail(housingId: Long) = "housing/$housingId"

    fun housingEdit(housingId: Long? = null) =
        housingId?.let { "housing/edit?housingId=$it" } ?: "housing/edit"

    fun tenantDetail(tenantId: Long) = "tenant/$tenantId"

    fun tenantEdit(tenantId: Long? = null) =
        tenantId?.let { "tenant/edit?tenantId=$it" } ?: "tenant/edit"

    fun leaseCreate(housingId: Long? = null, tenantId: Long? = null): String {
        val params = buildList {
            housingId?.let { add("housingId=$it") }
            tenantId?.let { add("tenantId=$it") }
        }
        return if (params.isEmpty()) {
            "lease/create"
        } else {
            "lease/create?${params.joinToString("&")}"
        }
    }

    fun leaseDetail(leaseId: Long) = "lease/$leaseId"
}

@Composable
fun LocationNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = LocationRoutes.HOUSINGS,
        modifier = modifier
    ) {
        composable(LocationRoutes.HOUSINGS) {
            HousingListScreen(
                onHousingClick = { housingId ->
                    navController.navigate(LocationRoutes.housingDetail(housingId))
                },
                onAddHousing = { navController.navigate(LocationRoutes.housingEdit()) }
            )
        }

        composable(
            route = LocationRoutes.HOUSING_DETAIL,
            arguments = listOf(navArgument("housingId") { type = NavType.LongType })
        ) { backStackEntry ->
            val housingId = backStackEntry.arguments?.getLong("housingId") ?: 0L
            val viewModel: HousingDetailViewModel =
                koinViewModel(parameters = { parametersOf(housingId) })
            HousingDetailScreen(
                viewModel = viewModel,
                onEdit = { navController.navigate(LocationRoutes.housingEdit(housingId)) },
                onCreateLease = { navController.navigate(LocationRoutes.leaseCreate(housingId = housingId)) }
            )
        }

        composable(
            route = LocationRoutes.HOUSING_EDIT,
            arguments = listOf(
                navArgument("housingId") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val housingId = backStackEntry.arguments?.getString("housingId")?.toLongOrNull()
            val viewModel: HousingEditViewModel =
                koinViewModel(parameters = { parametersOf(housingId) })
            HousingEditScreen(
                viewModel = viewModel,
                onSaved = { navController.popBackStack() }
            )
        }

        composable(LocationRoutes.TENANTS) {
            TenantListScreen(
                onTenantClick = { tenantId ->
                    navController.navigate(LocationRoutes.tenantDetail(tenantId))
                },
                onAddTenant = { navController.navigate(LocationRoutes.tenantEdit()) }
            )
        }

        composable(
            route = LocationRoutes.TENANT_DETAIL,
            arguments = listOf(navArgument("tenantId") { type = NavType.LongType })
        ) { backStackEntry ->
            val tenantId = backStackEntry.arguments?.getLong("tenantId") ?: 0L
            val viewModel: TenantDetailViewModel =
                koinViewModel(parameters = { parametersOf(tenantId) })
            TenantDetailScreen(
                viewModel = viewModel,
                onEdit = { navController.navigate(LocationRoutes.tenantEdit(tenantId)) },
                onCreateLease = { navController.navigate(LocationRoutes.leaseCreate(tenantId = tenantId)) }
            )
        }

        composable(
            route = LocationRoutes.TENANT_EDIT,
            arguments = listOf(
                navArgument("tenantId") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val tenantId = backStackEntry.arguments?.getString("tenantId")?.toLongOrNull()
            val viewModel: TenantEditViewModel =
                koinViewModel(parameters = { parametersOf(tenantId) })
            TenantEditScreen(
                viewModel = viewModel,
                onSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = LocationRoutes.LEASE_CREATE,
            arguments = listOf(
                navArgument("housingId") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("tenantId") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val housingId = backStackEntry.arguments?.getString("housingId")?.toLongOrNull()
            val tenantId = backStackEntry.arguments?.getString("tenantId")?.toLongOrNull()
            val viewModel: LeaseCreateViewModel = koinViewModel()

            LaunchedEffect(housingId) {
                housingId?.let { viewModel.onEvent(LeaseCreateUiEvent.SelectHousing(it)) }
            }
            LaunchedEffect(tenantId) {
                tenantId?.let { viewModel.onEvent(LeaseCreateUiEvent.SelectTenant(it)) }
            }

            LeaseCreateScreen(
                viewModel = viewModel,
                onLeaseCreated = { leaseId ->
                    navController.navigate(LocationRoutes.leaseDetail(leaseId)) {
                        popUpTo(LocationRoutes.LEASE_CREATE) { inclusive = true }
                    }
                },
                onAddHousing = { navController.navigate(LocationRoutes.housingEdit()) },
                onAddTenant = { navController.navigate(LocationRoutes.tenantEdit()) }
            )
        }

        composable(
            route = LocationRoutes.LEASE_DETAIL,
            arguments = listOf(navArgument("leaseId") { type = NavType.LongType })
        ) { backStackEntry ->
            val leaseId = backStackEntry.arguments?.getLong("leaseId") ?: 0L
            val viewModel: LeaseDetailViewModel =
                koinViewModel(parameters = { parametersOf(leaseId) })
            LeaseDetailScreen(
                viewModel = viewModel
            )
        }

        composable(LocationRoutes.INDEXATIONS) {
            val viewModel: IndexationViewModel = koinViewModel()
            IndexationScreen(viewModel = viewModel)
        }
    }
}

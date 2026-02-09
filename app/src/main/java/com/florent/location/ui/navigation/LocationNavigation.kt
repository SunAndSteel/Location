@file:OptIn(ExperimentalMaterial3Api::class)

package com.florent.location.ui.navigation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.florent.location.ui.lease.LeaseDetailScreen
import com.florent.location.ui.lease.LeaseDetailViewModel
import com.florent.location.ui.lease.LeaseListScreen
import com.florent.location.ui.components.EmptyDetailPane
import com.florent.location.ui.components.WindowWidthSize
import com.florent.location.ui.components.windowWidthSize
import com.florent.location.ui.housing.HousingDetailScreen
import com.florent.location.ui.housing.HousingDetailViewModel
import com.florent.location.ui.housing.HousingEditScreen
import com.florent.location.ui.housing.HousingEditViewModel
import com.florent.location.ui.housing.HousingListScreen
import com.florent.location.ui.lease.LeaseCreateScreen
import com.florent.location.ui.lease.LeaseCreateUiEvent
import com.florent.location.ui.lease.LeaseCreateViewModel
import com.florent.location.ui.tenant.TenantDetailScreen
import com.florent.location.ui.tenant.TenantDetailViewModel
import com.florent.location.ui.tenant.TenantEditScreen
import com.florent.location.ui.tenant.TenantEditViewModel
import com.florent.location.ui.tenant.TenantListScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

object LocationRoutes {
    const val BAILS = "bails"
    const val BAILS_LIST = "bails/list"
    const val BAIL_DETAIL = "bails/{leaseId}"
    const val BAIL_CREATE = "bails/create?housingId={housingId}&tenantId={tenantId}"
    const val HOUSINGS = "housings"
    const val HOUSINGS_LIST = "housings/list"
    const val HOUSING_DETAIL = "housing/{housingId}"
    const val HOUSING_EDIT = "housing/edit?housingId={housingId}"
    const val TENANTS = "tenants"
    const val TENANTS_LIST = "tenants/list"
    const val TENANT_DETAIL = "tenant/{tenantId}"
    const val TENANT_EDIT = "tenant/edit?tenantId={tenantId}"

    fun bailDetail(leaseId: Long) = "bails/$leaseId"

    fun housingDetail(housingId: Long) = "housing/$housingId"

    fun housingEdit(housingId: Long? = null) =
        housingId?.let { "housing/edit?housingId=$it" } ?: "housing/edit"

    fun tenantDetail(tenantId: Long) = "tenant/$tenantId"

    fun tenantEdit(tenantId: Long? = null) =
        tenantId?.let { "tenant/edit?tenantId=$it" } ?: "tenant/edit"

    fun bailCreate(housingId: Long? = null, tenantId: Long? = null): String {
        val params = buildList {
            housingId?.let { add("housingId=$it") }
            tenantId?.let { add("tenantId=$it") }
        }
        return if (params.isEmpty()) {
            "bails/create"
        } else {
            "bails/create?${params.joinToString("&")}"
        }
    }
}

private data class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val topLevelDestinations = listOf(
    TopLevelDestination(
        route = LocationRoutes.BAILS,
        label = "Bails",
        icon = Icons.Outlined.Description
    ),
    TopLevelDestination(
        route = LocationRoutes.HOUSINGS,
        label = "Logements",
        icon = Icons.Outlined.Home
    ),
    TopLevelDestination(
        route = LocationRoutes.TENANTS,
        label = "Solitaires",
        icon = Icons.Outlined.People
    )
)

@Composable
fun LocationNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val widthSize = windowWidthSize(maxWidth)
        val currentRoute = currentDestination?.route
        val isTwoPaneRoute = currentRoute in setOf(
            LocationRoutes.BAILS_LIST,
            LocationRoutes.BAIL_DETAIL,
            LocationRoutes.HOUSINGS_LIST,
            LocationRoutes.HOUSING_DETAIL,
            LocationRoutes.TENANTS_LIST,
            LocationRoutes.TENANT_DETAIL
        )

        when (widthSize) {
            WindowWidthSize.Compact -> {
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            topLevelDestinations.forEach { destination ->
                                val selected = currentDestination?.hierarchy
                                    ?.any { it.route == destination.route } == true
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        navController.navigate(destination.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            destination.icon,
                                            contentDescription = destination.label
                                        )
                                    },
                                    label = { Text(destination.label) }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    LocationNavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }

            WindowWidthSize.Medium -> {
                Row(modifier = Modifier.fillMaxSize()) {
                    NavigationRail(
                        modifier = Modifier
                            .width(88.dp)
                            .padding(vertical = 8.dp)
                    ) {
                        topLevelDestinations.forEach { destination ->
                            val selected = currentDestination?.hierarchy
                                ?.any { it.route == destination.route } == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(destination.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(destination.icon, contentDescription = destination.label) },
                                label = { Text(destination.label) }
                            )
                        }
                    }

                    LocationNavGraph(
                        navController = navController,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            WindowWidthSize.Expanded -> {
                Row(modifier = Modifier.fillMaxSize()) {
                    NavigationRail(
                        modifier = Modifier
                            .width(88.dp)
                            .padding(vertical = 8.dp)
                    ) {
                        topLevelDestinations.forEach { destination ->
                            val selected = currentDestination?.hierarchy
                                ?.any { it.route == destination.route } == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(destination.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(destination.icon, contentDescription = destination.label) },
                                label = { Text(destination.label) }
                            )
                        }
                    }

                    if (isTwoPaneRoute) {
                        TwoPaneContent(
                            navController = navController,
                            currentDestination = currentDestination,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        LocationNavGraph(
                            navController = navController,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TwoPaneContent(
    navController: NavHostController,
    currentDestination: NavDestination?,
    modifier: Modifier = Modifier
) {
    val isBails = currentDestination?.hierarchy?.any { it.route == LocationRoutes.BAILS } == true
    val isHousings = currentDestination?.hierarchy?.any { it.route == LocationRoutes.HOUSINGS } == true
    val isTenants = currentDestination?.hierarchy?.any { it.route == LocationRoutes.TENANTS } == true

    Row(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .weight(0.45f)
                .fillMaxHeight()
        ) {
            when {
                isBails -> {
                    LeaseListScreen(
                        onBailClick = { leaseId ->
                            navController.navigate(LocationRoutes.bailDetail(leaseId))
                        },
                        onAddBail = { navController.navigate(LocationRoutes.bailCreate()) }
                    )
                }

                isHousings -> {
                    HousingListScreen(
                        onHousingClick = { housingId ->
                            navController.navigate(LocationRoutes.housingDetail(housingId))
                        },
                        onAddHousing = { navController.navigate(LocationRoutes.housingEdit()) }
                    )
                }

                else -> {
                    TenantListScreen(
                        onTenantClick = { tenantId ->
                            navController.navigate(LocationRoutes.tenantDetail(tenantId))
                        },
                        onAddTenant = { navController.navigate(LocationRoutes.tenantEdit()) }
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .weight(0.55f)
                .fillMaxHeight()
        ) {
            LocationNavGraph(
                navController = navController,
                modifier = Modifier.fillMaxSize(),
                useListPlaceholder = true
            )
        }
    }
}

@Composable
private fun LocationNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    useListPlaceholder: Boolean = false
) {
    NavHost(
        navController = navController,
        startDestination = LocationRoutes.BAILS,
        modifier = modifier
    ) {
        navigation(
            route = LocationRoutes.BAILS,
            startDestination = LocationRoutes.BAILS_LIST
        ) {
            composable(LocationRoutes.BAILS_LIST) {
                if (useListPlaceholder) {
                    EmptyDetailPane(
                        title = "Sélectionnez un bail",
                        message = "Choisissez un bail pour afficher ses détails."
                    )
                } else {
                    LeaseListScreen(
                        onBailClick = { leaseId ->
                            navController.navigate(LocationRoutes.bailDetail(leaseId))
                        },
                        onAddBail = { navController.navigate(LocationRoutes.bailCreate()) }
                    )
                }
            }

            composable(
                route = LocationRoutes.BAIL_DETAIL,
                arguments = listOf(navArgument("leaseId") { type = NavType.LongType })
            ) { backStackEntry ->
                val leaseId = backStackEntry.arguments?.getLong("leaseId") ?: 0L
                val viewModel: LeaseDetailViewModel =
                    koinViewModel(parameters = { parametersOf(leaseId) })
                LeaseDetailScreen(
                    viewModel = viewModel
                )
            }

            composable(
                route = LocationRoutes.BAIL_CREATE,
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
                        navController.navigate(LocationRoutes.bailDetail(leaseId)) {
                            popUpTo(LocationRoutes.BAIL_CREATE) { inclusive = true }
                        }
                    },
                    onAddHousing = { navController.navigate(LocationRoutes.housingEdit()) },
                    onAddTenant = { navController.navigate(LocationRoutes.tenantEdit()) }
                )
            }
        }

        navigation(
            route = LocationRoutes.HOUSINGS,
            startDestination = LocationRoutes.HOUSINGS_LIST
        ) {
            composable(LocationRoutes.HOUSINGS_LIST) {
                if (useListPlaceholder) {
                    EmptyDetailPane(
                        title = "Sélectionnez un logement",
                        message = "Choisissez un logement pour afficher ses détails."
                    )
                } else {
                    HousingListScreen(
                        onHousingClick = { housingId ->
                            navController.navigate(LocationRoutes.housingDetail(housingId))
                        },
                        onAddHousing = { navController.navigate(LocationRoutes.housingEdit()) }
                    )
                }
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
                    onCreateLease = { navController.navigate(LocationRoutes.bailCreate(housingId = housingId)) }
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
        }

        navigation(
            route = LocationRoutes.TENANTS,
            startDestination = LocationRoutes.TENANTS_LIST
        ) {
            composable(LocationRoutes.TENANTS_LIST) {
                if (useListPlaceholder) {
                    EmptyDetailPane(
                        title = "Sélectionnez un locataire",
                        message = "Choisissez un locataire pour afficher ses détails."
                    )
                } else {
                    TenantListScreen(
                        onTenantClick = { tenantId ->
                            navController.navigate(LocationRoutes.tenantDetail(tenantId))
                        },
                        onAddTenant = { navController.navigate(LocationRoutes.tenantEdit()) }
                    )
                }
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
                    onCreateLease = { navController.navigate(LocationRoutes.bailCreate(tenantId = tenantId)) }
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
        }
    }
}

package com.florent.location.ui.housing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import com.florent.location.ui.components.AdaptiveContent
import com.florent.location.ui.components.HousingCard
import com.florent.location.ui.components.keyboardClickable
import org.koin.androidx.compose.koinViewModel

@ExperimentalMaterial3Api
@Composable
fun HousingListScreen(
    viewModel: HousingListViewModel = koinViewModel(),
    onHousingClick: (Long) -> Unit,
    onAddHousing: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    HousingListContent(
        state = state,
        onEvent = viewModel::onEvent,
        onHousingClick = onHousingClick,
        onAddHousing = onAddHousing,
        modifier = modifier
    )
}

@ExperimentalMaterial3Api
@Composable
private fun HousingListContent(
    state: HousingListUiState,
    onEvent: (HousingListUiEvent) -> Unit,
    onHousingClick: (Long) -> Unit,
    onAddHousing: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "Logements") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddHousing, modifier = Modifier.focusable()) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Ajouter un logement")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        AdaptiveContent(innerPadding = innerPadding) {
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Chargement des logements...")
                        }
                    }
                }

                state.errorMessage != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.errorMessage,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Veuillez réessayer plus tard.")
                    }
                }

                state.isEmpty -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Aucun logement enregistré.")
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = onAddHousing) {
                            Text(text = "Ajouter un logement")
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.housings, key = { it.id }) { housing ->
                            HousingCard(
                                housing = housing,
                                onOpen = { onHousingClick(housing.id) },
                                onDelete = {
                                    onEvent(HousingListUiEvent.DeleteHousing(housing.id))
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .keyboardClickable { onHousingClick(housing.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

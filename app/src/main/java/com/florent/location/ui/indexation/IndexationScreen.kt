package com.florent.location.ui.indexation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import java.time.LocalDate

@ExperimentalMaterial3Api
@Composable
fun IndexationScreen(
    viewModel: IndexationViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    IndexationContent(state = state, modifier = modifier)
}

@ExperimentalMaterial3Api
@Composable
private fun IndexationContent(
    state: IndexationUiState,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "Indexations à venir") }) },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Chargement des indexations...")
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
                        Text(text = "Aucune indexation à venir.")
                    }
                }

                else -> {
                    val sortedIndexations = state.upcomingIndexations.sortedBy { it.daysUntil }
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(sortedIndexations, key = { it.leaseId }) { indexation ->
                            val date = LocalDate.ofEpochDay(indexation.nextIndexationEpochDay)
                            Card {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Bail #${indexation.leaseId}",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(text = "dans ${indexation.daysUntil} jours")
                                    Text(text = "Date: $date")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

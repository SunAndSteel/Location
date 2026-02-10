package com.florent.location.ui.lease

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florent.location.domain.model.Housing
import com.florent.location.domain.model.IndexationEvent
import com.florent.location.domain.model.IndexationPolicy
import com.florent.location.domain.model.IndexationSimulation
import com.florent.location.domain.model.Key
import com.florent.location.domain.model.Lease
import com.florent.location.domain.usecase.bail.BailUseCases
import com.florent.location.domain.usecase.housing.HousingUseCases
import com.florent.location.domain.usecase.lease.LeaseUseCases
import com.florent.location.ui.sync.HousingSyncRequester
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IndexationFormState(
    val indexPercent: String = "",
    val effectiveDate: String = "",
    val simulation: IndexationSimulation? = null
)

data class AddKeyDialogState(
    val isOpen: Boolean = false,
    val type: String = "",
    val deviceLabel: String = "",
    val handedOverDate: String = ""
)

data class CloseLeaseDialogState(
    val isOpen: Boolean = false,
    val endDate: String = ""
)

data class LeaseDetailUiState(
    val isLoading: Boolean = true,
    val lease: Lease? = null,
    val housing: Housing? = null,
    val keys: List<Key> = emptyList(),
    val isActive: Boolean = false,
    val indexationPolicy: IndexationPolicy? = null,
    val indexationHistory: List<IndexationEvent> = emptyList(),
    val indexationForm: IndexationFormState = IndexationFormState(),
    val errorMessage: String? = null,
    val addKeyDialog: AddKeyDialogState = AddKeyDialogState(),
    val closeLeaseDialog: CloseLeaseDialogState = CloseLeaseDialogState()
)

private data class LeaseDetailSnapshot(
    val lease: Lease?,
    val housing: Housing?,
    val keys: List<Key>,
    val events: List<IndexationEvent>
)

enum class AddKeyField {
    Type,
    DeviceLabel,
    HandedOverDate
}

sealed interface LeaseDetailUiEvent {
    data object AddKeyClicked : LeaseDetailUiEvent
    data class AddKeyFieldChanged(val field: AddKeyField, val value: String) : LeaseDetailUiEvent
    data class ConfirmAddKey(
        val type: String,
        val deviceLabel: String,
        val handedOverDate: String
    ) : LeaseDetailUiEvent

    data class DeleteKeyClicked(val keyId: Long) : LeaseDetailUiEvent
    data object CloseLeaseClicked : LeaseDetailUiEvent
    data class CloseLeaseFieldChanged(val value: String) : LeaseDetailUiEvent
    data class ConfirmCloseLease(val endDate: String) : LeaseDetailUiEvent
    data object DismissAddKeyDialog : LeaseDetailUiEvent
    data object DismissCloseLeaseDialog : LeaseDetailUiEvent
    data class IndexationPercentChanged(val value: String) : LeaseDetailUiEvent
    data class IndexationEffectiveDateChanged(val value: String) : LeaseDetailUiEvent
    data object SimulateIndexation : LeaseDetailUiEvent
    data object ApplyIndexation : LeaseDetailUiEvent
}

class LeaseDetailViewModel(
    private val leaseId: Long,
    private val bailUseCases: BailUseCases,
    private val leaseUseCases: LeaseUseCases,
    private val housingUseCases: HousingUseCases,
    private val syncManager: HousingSyncRequester
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaseDetailUiState())
    val uiState: StateFlow<LeaseDetailUiState> = _uiState

    private val todayEpochDay =
        LocalDate.now(ZoneId.of("Europe/Brussels")).toEpochDay()

    init {
        observeLeaseDetail()
    }

    fun onEvent(event: LeaseDetailUiEvent) {
        when (event) {
            LeaseDetailUiEvent.AddKeyClicked -> openAddKeyDialog()
            is LeaseDetailUiEvent.AddKeyFieldChanged -> updateAddKeyField(event.field, event.value)
            is LeaseDetailUiEvent.ConfirmAddKey -> addKey(event)
            is LeaseDetailUiEvent.DeleteKeyClicked -> deleteKey(event.keyId)
            LeaseDetailUiEvent.CloseLeaseClicked -> openCloseLeaseDialog()
            is LeaseDetailUiEvent.CloseLeaseFieldChanged -> updateCloseLeaseField(event.value)
            is LeaseDetailUiEvent.ConfirmCloseLease -> closeLease(event.endDate)
            LeaseDetailUiEvent.DismissAddKeyDialog -> dismissAddKeyDialog()
            LeaseDetailUiEvent.DismissCloseLeaseDialog -> dismissCloseLeaseDialog()
            is LeaseDetailUiEvent.IndexationPercentChanged -> updateIndexationPercent(event.value)
            is LeaseDetailUiEvent.IndexationEffectiveDateChanged -> updateIndexationEffectiveDate(event.value)
            LeaseDetailUiEvent.SimulateIndexation -> simulateIndexation()
            LeaseDetailUiEvent.ApplyIndexation -> applyIndexation()
        }
    }

    private fun observeLeaseDetail() {
        viewModelScope.launch {
            val leaseFlow = bailUseCases.observeBail(leaseId)
            val housingFlow = leaseFlow.flatMapLatest { lease ->
                lease?.let { housingUseCases.observeHousing(it.housingId) } ?: flowOf(null)
            }
            val keysFlow = leaseFlow.flatMapLatest { lease ->
                lease?.let { housingUseCases.observeKeysForHousing(it.housingId) } ?: flowOf(emptyList())
            }
            combine(
                leaseFlow,
                housingFlow,
                keysFlow,
                bailUseCases.observeIndexationEvents(leaseId)
            ) { lease, housing, keys, events ->
                LeaseDetailSnapshot(
                    lease = lease,
                    housing = housing,
                    keys = keys,
                    events = events
                )
            }
                .onStart {
                    _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                }
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Erreur lors du chargement du bail."
                        )
                    }
                }
                .collect { snapshot ->
                    val policy = snapshot.lease?.let {
                        bailUseCases.buildIndexationPolicy(it, todayEpochDay)
                    }
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            lease = snapshot.lease,
                            housing = snapshot.housing,
                            keys = snapshot.keys,
                            isActive = snapshot.lease?.endDateEpochDay == null,
                            indexationPolicy = policy,
                            indexationHistory = snapshot.events,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    private fun openAddKeyDialog() {
        _uiState.update {
            it.copy(
                addKeyDialog = AddKeyDialogState(isOpen = true),
                errorMessage = null
            )
        }
    }

    private fun updateAddKeyField(field: AddKeyField, value: String) {
        _uiState.update { current ->
            val dialog = current.addKeyDialog
            val updated = when (field) {
                AddKeyField.Type -> dialog.copy(type = value)
                AddKeyField.DeviceLabel -> dialog.copy(deviceLabel = value)
                AddKeyField.HandedOverDate -> dialog.copy(handedOverDate = value)
            }
            current.copy(addKeyDialog = updated, errorMessage = null)
        }
    }

    private fun addKey(event: LeaseDetailUiEvent.ConfirmAddKey) {
        viewModelScope.launch {
            val lease = _uiState.value.lease
            if (lease == null) {
                _uiState.update { it.copy(errorMessage = "Bail introuvable.") }
                return@launch
            }
            val handedOverEpochDay = parseEpochDay(event.handedOverDate)
                ?: lease.startDateEpochDay

            try {
                housingUseCases.addKey(
                    housingId = lease.housingId,
                    key = Key(
                        housingId = lease.housingId,
                        type = event.type,
                        deviceLabel = event.deviceLabel,
                        handedOverEpochDay = handedOverEpochDay
                    )
                )
                syncManager.requestSync("key_add")
                _uiState.update {
                    it.copy(
                        addKeyDialog = AddKeyDialogState(isOpen = false),
                        errorMessage = null
                    )
                }
            } catch (error: IllegalArgumentException) {
                _uiState.update { it.copy(errorMessage = error.message) }
            }
        }
    }

    private fun deleteKey(keyId: Long) {
        viewModelScope.launch {
            try {
                housingUseCases.deleteKey(keyId)
                syncManager.requestSync("key_delete")
            } catch (error: IllegalArgumentException) {
                _uiState.update { it.copy(errorMessage = error.message) }
            }
        }
    }

    private fun openCloseLeaseDialog() {
        _uiState.update {
            it.copy(
                closeLeaseDialog = CloseLeaseDialogState(isOpen = true),
                errorMessage = null
            )
        }
    }

    private fun updateCloseLeaseField(value: String) {
        _uiState.update { current ->
            current.copy(
                closeLeaseDialog = current.closeLeaseDialog.copy(endDate = value),
                errorMessage = null
            )
        }
    }

    private fun closeLease(endDate: String) {
        viewModelScope.launch {
            val parsed = parseEpochDay(endDate)
            if (parsed == null) {
                _uiState.update { it.copy(errorMessage = "Date de clôture invalide.") }
                return@launch
            }

            try {
                leaseUseCases.closeLease(leaseId, parsed)
                syncManager.requestSync("lease_close")
                _uiState.update {
                    it.copy(
                        closeLeaseDialog = CloseLeaseDialogState(isOpen = false),
                        errorMessage = null
                    )
                }
            } catch (error: IllegalArgumentException) {
                _uiState.update { it.copy(errorMessage = error.message) }
            }
        }
    }

    private fun dismissAddKeyDialog() {
        _uiState.update { it.copy(addKeyDialog = AddKeyDialogState(isOpen = false)) }
    }

    private fun dismissCloseLeaseDialog() {
        _uiState.update { it.copy(closeLeaseDialog = CloseLeaseDialogState(isOpen = false)) }
    }

    private fun updateIndexationPercent(value: String) {
        _uiState.update { current ->
            current.copy(
                indexationForm = current.indexationForm.copy(
                    indexPercent = value,
                    simulation = null
                ),
                errorMessage = null
            )
        }
    }

    private fun updateIndexationEffectiveDate(value: String) {
        _uiState.update { current ->
            current.copy(
                indexationForm = current.indexationForm.copy(
                    effectiveDate = value,
                    simulation = null
                ),
                errorMessage = null
            )
        }
    }

    private fun simulateIndexation() {
        viewModelScope.launch {
            val percent = parsePercent(_uiState.value.indexationForm.indexPercent)
            val effectiveEpochDay = parseEpochDay(_uiState.value.indexationForm.effectiveDate)
            if (percent == null || effectiveEpochDay == null) {
                _uiState.update { it.copy(errorMessage = "Simulation invalide: valeurs manquantes.") }
                return@launch
            }
            try {
                val simulation = bailUseCases.simulateIndexationForBail(
                    leaseId = leaseId,
                    indexPercent = percent,
                    effectiveEpochDay = effectiveEpochDay
                )
                _uiState.update { current ->
                    current.copy(
                        indexationForm = current.indexationForm.copy(simulation = simulation),
                        errorMessage = null
                    )
                }
            } catch (error: IllegalArgumentException) {
                _uiState.update { it.copy(errorMessage = error.message) }
            }
        }
    }

    private fun applyIndexation() {
        viewModelScope.launch {
            val percent = parsePercent(_uiState.value.indexationForm.indexPercent)
            val effectiveEpochDay = parseEpochDay(_uiState.value.indexationForm.effectiveDate)
            if (percent == null || effectiveEpochDay == null) {
                _uiState.update { it.copy(errorMessage = "Application invalide: valeurs manquantes.") }
                return@launch
            }
            try {
                bailUseCases.applyIndexationToBail(
                    leaseId = leaseId,
                    indexPercent = percent,
                    effectiveEpochDay = effectiveEpochDay
                )
                syncManager.requestSync("indexation_apply")
                _uiState.update {
                    it.copy(
                        indexationForm = IndexationFormState(),
                        errorMessage = null
                    )
                }
            } catch (error: IllegalArgumentException) {
                _uiState.update { it.copy(errorMessage = error.message) }
            }
        }
    }

    private fun parseEpochDay(value: String): Long? {
        if (value.isBlank()) return null
        return runCatching {
            LocalDate.parse(value.trim(), DateTimeFormatter.ISO_LOCAL_DATE).toEpochDay()
        }.getOrNull()
    }

    private fun parsePercent(value: String): Double? {
        if (value.isBlank()) return null
        return value.replace(',', '.').toDoubleOrNull()
    }
}

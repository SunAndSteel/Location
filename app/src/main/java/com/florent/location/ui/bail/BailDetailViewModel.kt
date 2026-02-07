package com.florent.location.ui.bail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florent.location.domain.model.Bail
import com.florent.location.domain.model.IndexationEvent
import com.florent.location.domain.model.IndexationPolicy
import com.florent.location.domain.model.IndexationSimulation
import com.florent.location.domain.model.Key
import com.florent.location.domain.usecase.bail.BailUseCases
import com.florent.location.domain.usecase.lease.LeaseUseCases
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
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

data class BailDetailUiState(
    val isLoading: Boolean = true,
    val bail: Bail? = null,
    val keys: List<Key> = emptyList(),
    val isActive: Boolean = false,
    val indexationPolicy: IndexationPolicy? = null,
    val indexationHistory: List<IndexationEvent> = emptyList(),
    val indexationForm: IndexationFormState = IndexationFormState(),
    val errorMessage: String? = null,
    val addKeyDialog: AddKeyDialogState = AddKeyDialogState(),
    val closeLeaseDialog: CloseLeaseDialogState = CloseLeaseDialogState()
)

enum class AddKeyField {
    Type,
    DeviceLabel,
    HandedOverDate
}

sealed interface BailDetailUiEvent {
    data object AddKeyClicked : BailDetailUiEvent
    data class AddKeyFieldChanged(val field: AddKeyField, val value: String) : BailDetailUiEvent
    data class ConfirmAddKey(
        val type: String,
        val deviceLabel: String,
        val handedOverDate: String
    ) : BailDetailUiEvent

    data class DeleteKeyClicked(val keyId: Long) : BailDetailUiEvent
    data object CloseLeaseClicked : BailDetailUiEvent
    data class CloseLeaseFieldChanged(val value: String) : BailDetailUiEvent
    data class ConfirmCloseLease(val endDate: String) : BailDetailUiEvent
    data object DismissAddKeyDialog : BailDetailUiEvent
    data object DismissCloseLeaseDialog : BailDetailUiEvent
    data class IndexationPercentChanged(val value: String) : BailDetailUiEvent
    data class IndexationEffectiveDateChanged(val value: String) : BailDetailUiEvent
    data object SimulateIndexation : BailDetailUiEvent
    data object ApplyIndexation : BailDetailUiEvent
}

class BailDetailViewModel(
    private val leaseId: Long,
    private val bailUseCases: BailUseCases,
    private val leaseUseCases: LeaseUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(BailDetailUiState())
    val uiState: StateFlow<BailDetailUiState> = _uiState

    private val todayEpochDay =
        LocalDate.now(ZoneId.of("Europe/Brussels")).toEpochDay()

    init {
        observeBailDetail()
    }

    fun onEvent(event: BailDetailUiEvent) {
        when (event) {
            BailDetailUiEvent.AddKeyClicked -> openAddKeyDialog()
            is BailDetailUiEvent.AddKeyFieldChanged -> updateAddKeyField(event.field, event.value)
            is BailDetailUiEvent.ConfirmAddKey -> addKey(event)
            is BailDetailUiEvent.DeleteKeyClicked -> deleteKey(event.keyId)
            BailDetailUiEvent.CloseLeaseClicked -> openCloseLeaseDialog()
            is BailDetailUiEvent.CloseLeaseFieldChanged -> updateCloseLeaseField(event.value)
            is BailDetailUiEvent.ConfirmCloseLease -> closeLease(event.endDate)
            BailDetailUiEvent.DismissAddKeyDialog -> dismissAddKeyDialog()
            BailDetailUiEvent.DismissCloseLeaseDialog -> dismissCloseLeaseDialog()
            is BailDetailUiEvent.IndexationPercentChanged -> updateIndexationPercent(event.value)
            is BailDetailUiEvent.IndexationEffectiveDateChanged -> updateIndexationEffectiveDate(event.value)
            BailDetailUiEvent.SimulateIndexation -> simulateIndexation()
            BailDetailUiEvent.ApplyIndexation -> applyIndexation()
        }
    }

    private fun observeBailDetail() {
        viewModelScope.launch {
            combine(
                bailUseCases.observeBail(leaseId),
                leaseUseCases.observeKeysForLease(leaseId),
                bailUseCases.observeIndexationEvents(leaseId)
            ) { bail, keys, events ->
                Triple(bail, keys, events)
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
                .collect { (bail, keys, events) ->
                    val policy = bail?.let { bailUseCases.buildIndexationPolicy(it, todayEpochDay) }
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            bail = bail,
                            keys = keys,
                            isActive = bail?.endDateEpochDay == null,
                            indexationPolicy = policy,
                            indexationHistory = events,
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

    private fun addKey(event: BailDetailUiEvent.ConfirmAddKey) {
        viewModelScope.launch {
            val bail = _uiState.value.bail
            if (bail == null) {
                _uiState.update { it.copy(errorMessage = "Bail introuvable.") }
                return@launch
            }
            val handedOverEpochDay = parseEpochDay(event.handedOverDate)
                ?: bail.startDateEpochDay

            try {
                leaseUseCases.addKey(
                    leaseId = bail.id,
                    key = Key(
                        type = event.type,
                        deviceLabel = event.deviceLabel,
                        handedOverEpochDay = handedOverEpochDay
                    )
                )
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
                leaseUseCases.deleteKey(keyId)
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

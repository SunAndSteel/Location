package com.florent.location.ui.lease

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florent.location.domain.model.Key
import com.florent.location.domain.model.Lease
import com.florent.location.domain.usecase.lease.LeaseUseCases
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    val keys: List<Key> = emptyList(),
    val isActive: Boolean = false,
    val errorMessage: String? = null,
    val addKeyDialog: AddKeyDialogState = AddKeyDialogState(),
    val closeLeaseDialog: CloseLeaseDialogState = CloseLeaseDialogState()
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
}

class LeaseDetailViewModel(
    private val leaseId: Long,
    private val leaseUseCases: LeaseUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaseDetailUiState())
    val uiState: StateFlow<LeaseDetailUiState> = _uiState

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
        }
    }

    private fun observeLeaseDetail() {
        viewModelScope.launch {
            combine(
                leaseUseCases.observeLease(leaseId),
                leaseUseCases.observeKeysForLease(leaseId)
            ) { lease, keys ->
                lease to keys
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
                .collect { (lease, keys) ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            lease = lease,
                            keys = keys,
                            isActive = lease?.endDateEpochDay == null,
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
                leaseUseCases.addKey(
                    leaseId = lease.id,
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

    private fun parseEpochDay(value: String): Long? {
        if (value.isBlank()) return null
        return runCatching {
            LocalDate.parse(value.trim(), DateTimeFormatter.ISO_LOCAL_DATE).toEpochDay()
        }.getOrNull()
    }
}

package com.florent.location.ui.housing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florent.location.domain.model.Housing
import com.florent.location.domain.model.HousingSituation
import com.florent.location.domain.model.IndexationEvent
import com.florent.location.domain.model.IndexationPolicy
import com.florent.location.domain.model.IndexationSimulation
import com.florent.location.domain.model.Key
import com.florent.location.domain.model.Lease
import com.florent.location.domain.model.Tenant
import com.florent.location.domain.usecase.housing.HousingUseCases
import com.florent.location.domain.usecase.housing.ObserveHousingSituation
import com.florent.location.domain.usecase.lease.LeaseUseCases
import com.florent.location.domain.usecase.tenant.TenantUseCases
import com.florent.location.presentation.sync.HousingSyncRequester
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
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

enum class AddKeyField {
    Type,
    DeviceLabel,
    HandedOverDate
}

/**
 * État d'UI pour le détail d'un logement.
 */
data class HousingDetailUiState(
    val isLoading: Boolean = true,
    val housing: Housing? = null,
    val situation: HousingSituation? = null,
    val lease: Lease? = null,
    val tenant: Tenant? = null,
    val keys: List<Key> = emptyList(),
    val isLeaseActive: Boolean = false,
    val indexationPolicy: IndexationPolicy? = null,
    val indexationHistory: List<IndexationEvent> = emptyList(),
    val indexationForm: IndexationFormState = IndexationFormState(),
    val addKeyDialog: AddKeyDialogState = AddKeyDialogState(),
    val closeLeaseDialog: CloseLeaseDialogState = CloseLeaseDialogState(),
    val isEmpty: Boolean = false,
    val errorMessage: String? = null
)

private data class HousingDetailSnapshot(
    val housing: Housing?,
    val situation: HousingSituation?,
    val lease: Lease?,
    val tenant: Tenant?,
    val keys: List<Key>,
    val events: List<IndexationEvent>
)

/**
 * Événements utilisateur pour le détail d'un logement.
 */
sealed interface HousingDetailUiEvent {
    data class UpdateHousing(val housing: Housing) : HousingDetailUiEvent
    data class DeleteHousing(val id: Long) : HousingDetailUiEvent

    data object AddKeyClicked : HousingDetailUiEvent
    data class AddKeyFieldChanged(val field: AddKeyField, val value: String) : HousingDetailUiEvent
    data class ConfirmAddKey(
        val type: String,
        val deviceLabel: String,
        val handedOverDate: String
    ) : HousingDetailUiEvent

    data class DeleteKeyClicked(val keyId: Long) : HousingDetailUiEvent
    data object CloseLeaseClicked : HousingDetailUiEvent
    data class CloseLeaseFieldChanged(val value: String) : HousingDetailUiEvent
    data class ConfirmCloseLease(val endDate: String) : HousingDetailUiEvent
    data object DismissAddKeyDialog : HousingDetailUiEvent
    data object DismissCloseLeaseDialog : HousingDetailUiEvent
    data class IndexationPercentChanged(val value: String) : HousingDetailUiEvent
    data class IndexationEffectiveDateChanged(val value: String) : HousingDetailUiEvent
    data object SimulateIndexation : HousingDetailUiEvent
    data object ApplyIndexation : HousingDetailUiEvent
}

/**
 * ViewModel qui orchestre les cas d'usage liés à un logement.
 */
class HousingDetailViewModel(
    private val housingId: Long,
    private val housingUseCases: HousingUseCases,
    private val observeHousingSituation: ObserveHousingSituation,
    private val leaseUseCases: LeaseUseCases,
    private val tenantUseCases: TenantUseCases,
    private val syncManager: HousingSyncRequester
) : ViewModel() {

    private val _uiState = MutableStateFlow(HousingDetailUiState())
    val uiState: StateFlow<HousingDetailUiState> = _uiState

    private val todayEpochDay =
        LocalDate.now(ZoneId.of("Europe/Brussels")).toEpochDay()

    init {
        observeHousing()
    }

    fun onEvent(event: HousingDetailUiEvent) {
        when (event) {
            is HousingDetailUiEvent.UpdateHousing -> updateHousing(event.housing)
            is HousingDetailUiEvent.DeleteHousing -> deleteHousing(event.id)
            HousingDetailUiEvent.AddKeyClicked -> openAddKeyDialog()
            is HousingDetailUiEvent.AddKeyFieldChanged -> updateAddKeyField(event.field, event.value)
            is HousingDetailUiEvent.ConfirmAddKey -> addKey(event)
            is HousingDetailUiEvent.DeleteKeyClicked -> deleteKey(event.keyId)
            HousingDetailUiEvent.CloseLeaseClicked -> openCloseLeaseDialog()
            is HousingDetailUiEvent.CloseLeaseFieldChanged -> updateCloseLeaseField(event.value)
            is HousingDetailUiEvent.ConfirmCloseLease -> closeLease(event.endDate)
            HousingDetailUiEvent.DismissAddKeyDialog -> dismissAddKeyDialog()
            HousingDetailUiEvent.DismissCloseLeaseDialog -> dismissCloseLeaseDialog()
            is HousingDetailUiEvent.IndexationPercentChanged -> updateIndexationPercent(event.value)
            is HousingDetailUiEvent.IndexationEffectiveDateChanged -> updateIndexationEffectiveDate(event.value)
            HousingDetailUiEvent.SimulateIndexation -> simulateIndexation()
            HousingDetailUiEvent.ApplyIndexation -> applyIndexation()
        }
    }

    private fun observeHousing() {
        viewModelScope.launch {
            val housingFlow = housingUseCases.observeHousing(housingId)
            val situationFlow = housingFlow.flatMapLatest { housing ->
                if (housing == null) {
                    flowOf(null)
                } else {
                    observeHousingSituation(housing)
                }
            }
            val activeLeaseFlow = leaseUseCases.observeLeases()
                .map { bails -> bails.firstOrNull { it.housingId == housingId && it.endDateEpochDay == null } }
            val tenantFlow = activeLeaseFlow.flatMapLatest { lease ->
                lease?.let { tenantUseCases.observeTenant(it.tenantId) } ?: flowOf(null)
            }
            val keysFlow = housingUseCases.observeKeysForHousing(housingId)
            val indexationEventsFlow = activeLeaseFlow.flatMapLatest { lease ->
                lease?.let { leaseUseCases.observeIndexationEvents(it.id) } ?: flowOf(emptyList())
            }

            combine(
                combine(
                    housingFlow,
                    situationFlow,
                    activeLeaseFlow,
                    tenantFlow,
                    keysFlow
                ) { housing, situation, lease, tenant, keys ->
                    HousingDetailSnapshot(
                        housing = housing,
                        situation = situation,
                        lease = lease,
                        tenant = tenant,
                        keys = keys,
                        events = emptyList()
                    )
                },
                indexationEventsFlow
            ) { snapshot, events ->
                snapshot.copy(events = events)
            }
                .onStart {
                    _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                }
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Erreur lors du chargement du logement."
                        )
                    }
                }
                .collect { snapshot ->
                    val policy = snapshot.lease?.let {
                        leaseUseCases.buildIndexationPolicy(it, todayEpochDay)
                    }
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            housing = snapshot.housing,
                            situation = snapshot.situation,
                            lease = snapshot.lease,
                            tenant = snapshot.tenant,
                            keys = snapshot.keys,
                            isLeaseActive = snapshot.lease?.endDateEpochDay == null,
                            indexationPolicy = policy,
                            indexationHistory = snapshot.events,
                            isEmpty = snapshot.housing == null,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    private fun updateHousing(housing: Housing) {
        viewModelScope.launch {
            try {
                housingUseCases.updateHousing(housing)
                syncManager.requestSync("housing_update")
            } catch (error: IllegalArgumentException) {
                _uiState.update { it.copy(errorMessage = error.message) }
            }
        }
    }

    private fun deleteHousing(id: Long) {
        viewModelScope.launch {
            try {
                housingUseCases.deleteHousing(id)
                syncManager.requestSync("housing_delete")
            } catch (error: IllegalArgumentException) {
                _uiState.update { it.copy(errorMessage = error.message) }
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

    private fun addKey(event: HousingDetailUiEvent.ConfirmAddKey) {
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
            val lease = _uiState.value.lease
            if (lease == null) {
                _uiState.update { it.copy(errorMessage = "Bail introuvable.") }
                return@launch
            }

            val parsed = parseEpochDay(endDate)
            if (parsed == null) {
                _uiState.update { it.copy(errorMessage = "Date de clôture invalide.") }
                return@launch
            }

            try {
                leaseUseCases.closeLease(lease.id, parsed)
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
            val lease = _uiState.value.lease
            if (lease == null) {
                _uiState.update { it.copy(errorMessage = "Bail introuvable.") }
                return@launch
            }

            val percent = parsePercent(_uiState.value.indexationForm.indexPercent)
            val effectiveEpochDay = parseEpochDay(_uiState.value.indexationForm.effectiveDate)
            if (percent == null || effectiveEpochDay == null) {
                _uiState.update { it.copy(errorMessage = "Simulation invalide: valeurs manquantes.") }
                return@launch
            }
            try {
                val simulation = leaseUseCases.simulateIndexation(
                    leaseId = lease.id,
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
            val lease = _uiState.value.lease
            if (lease == null) {
                _uiState.update { it.copy(errorMessage = "Bail introuvable.") }
                return@launch
            }

            val percent = parsePercent(_uiState.value.indexationForm.indexPercent)
            val effectiveEpochDay = parseEpochDay(_uiState.value.indexationForm.effectiveDate)
            if (percent == null || effectiveEpochDay == null) {
                _uiState.update { it.copy(errorMessage = "Application invalide: valeurs manquantes.") }
                return@launch
            }
            try {
                leaseUseCases.applyIndexation(
                    leaseId = lease.id,
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

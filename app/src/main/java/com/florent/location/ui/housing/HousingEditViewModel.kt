package com.florent.location.ui.housing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florent.location.domain.model.Housing
import com.florent.location.domain.usecase.housing.HousingUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HousingEditUiState(
    val isLoading: Boolean = false,
    val housingId: Long? = null,
    val city: String = "",
    val address: String = "",
    val defaultRentCents: Long = 0L,
    val defaultChargesCents: Long = 0L,
    val depositCents: Long = 0L,
    val mailboxLabel: String = "",
    val meterGas: String = "",
    val meterElectricity: String = "",
    val meterWater: String = "",
    val peb: String? = null,
    val buildingLabel: String? = null,
    val errorMessage: String? = null,
    val isSaved: Boolean = false
)

sealed interface HousingEditUiEvent {
    data class CityChanged(val value: String) : HousingEditUiEvent
    data class AddressChanged(val value: String) : HousingEditUiEvent
    data class DefaultRentChanged(val value: Long) : HousingEditUiEvent
    data class DefaultChargesChanged(val value: Long) : HousingEditUiEvent
    data class DepositChanged(val value: Long) : HousingEditUiEvent
    data class MailboxLabelChanged(val value: String) : HousingEditUiEvent
    data class MeterGasChanged(val value: String) : HousingEditUiEvent
    data class MeterElectricityChanged(val value: String) : HousingEditUiEvent
    data class MeterWaterChanged(val value: String) : HousingEditUiEvent
    data class PebChanged(val value: String?) : HousingEditUiEvent
    data class BuildingLabelChanged(val value: String?) : HousingEditUiEvent
    data object Save : HousingEditUiEvent
}

class HousingEditViewModel(
    private val housingId: Long?,
    private val useCases: HousingUseCases
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(HousingEditUiState(isLoading = housingId != null, housingId = housingId))
    val uiState: StateFlow<HousingEditUiState> = _uiState

    init {
        if (housingId != null) {
            observeHousing(housingId)
        }
    }

    fun onEvent(event: HousingEditUiEvent) {
        when (event) {
            is HousingEditUiEvent.CityChanged ->
                _uiState.update { it.copy(city = event.value, errorMessage = null) }
            is HousingEditUiEvent.AddressChanged ->
                _uiState.update { it.copy(address = event.value, errorMessage = null) }
            is HousingEditUiEvent.DefaultRentChanged ->
                _uiState.update { it.copy(defaultRentCents = event.value) }
            is HousingEditUiEvent.DefaultChargesChanged ->
                _uiState.update { it.copy(defaultChargesCents = event.value) }
            is HousingEditUiEvent.DepositChanged ->
                _uiState.update { it.copy(depositCents = event.value) }
            is HousingEditUiEvent.MailboxLabelChanged ->
                _uiState.update { it.copy(mailboxLabel = event.value, errorMessage = null) }
            is HousingEditUiEvent.MeterGasChanged ->
                _uiState.update { it.copy(meterGas = event.value, errorMessage = null) }
            is HousingEditUiEvent.MeterElectricityChanged ->
                _uiState.update { it.copy(meterElectricity = event.value, errorMessage = null) }
            is HousingEditUiEvent.MeterWaterChanged ->
                _uiState.update { it.copy(meterWater = event.value, errorMessage = null) }
            is HousingEditUiEvent.PebChanged ->
                _uiState.update { it.copy(peb = event.value) }
            is HousingEditUiEvent.BuildingLabelChanged ->
                _uiState.update { it.copy(buildingLabel = event.value) }
            HousingEditUiEvent.Save -> saveHousing()
        }
    }

    private fun observeHousing(id: Long) {
        viewModelScope.launch {
            useCases.observeHousing(id)
                .onStart { _uiState.update { it.copy(isLoading = true, errorMessage = null) } }
                .catch { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
                .collect { housing ->
                    if (housing == null) {
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Logement introuvable.") }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                housingId = housing.id,
                                city = housing.city,
                                address = housing.address,
                                defaultRentCents = housing.defaultRentCents,
                                defaultChargesCents = housing.defaultChargesCents,
                                depositCents = housing.depositCents,
                                mailboxLabel = housing.mailboxLabel.orEmpty(),
                                meterGas = housing.meterGas.orEmpty(),
                                meterElectricity = housing.meterElectricity.orEmpty(),
                                meterWater = housing.meterWater.orEmpty(),
                                peb = housing.peb,
                                buildingLabel = housing.buildingLabel,
                                errorMessage = null
                            )
                        }
                    }
                }
        }
    }

    private fun saveHousing() {
        viewModelScope.launch {
            val current = _uiState.value
            val housing = Housing(
                id = current.housingId ?: 0L,
                city = current.city,
                address = current.address,
                defaultRentCents = current.defaultRentCents,
                defaultChargesCents = current.defaultChargesCents,
                depositCents = current.depositCents,
                mailboxLabel = current.mailboxLabel.trim().ifBlank { null },
                meterGas = current.meterGas.trim().ifBlank { null },
                meterElectricity = current.meterElectricity.trim().ifBlank { null },
                meterWater = current.meterWater.trim().ifBlank { null },
                peb = current.peb,
                buildingLabel = current.buildingLabel
            )
            try {
                if (current.housingId == null) {
                    useCases.createHousing(housing)
                } else {
                    useCases.updateHousing(housing)
                }
                _uiState.update { it.copy(isSaved = true, errorMessage = null) }
            } catch (error: IllegalArgumentException) {
                _uiState.update { it.copy(errorMessage = error.message, isSaved = false) }
            }
        }
    }
}

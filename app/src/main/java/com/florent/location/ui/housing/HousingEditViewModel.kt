package com.florent.location.ui.housing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florent.location.domain.model.Address
import com.florent.location.domain.model.Housing
import com.florent.location.domain.model.PebRating
import com.florent.location.domain.usecase.housing.HousingUseCases
import com.florent.location.ui.components.formatEuroInput
import com.florent.location.ui.components.parseEuroInputToCents
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class HousingEditUiState(
    val isLoading: Boolean = false,
    val housingId: Long? = null,
    val remoteId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false,
    val street: String = "",
    val number: String = "",
    val box: String = "",
    val zipCode: String = "",
    val city: String = "",
    val country: String = "BE",
    val defaultRent: String = "",
    val defaultCharges: String = "",
    val deposit: String = "",
    val mailboxLabel: String = "",
    val meterGas: String = "",
    val meterElectricity: String = "",
    val meterWater: String = "",
    val pebRating: PebRating = PebRating.UNKNOWN,
    val pebDate: String? = null,
    val buildingLabel: String? = null,
    val internalNote: String = "",
    val errorMessage: String? = null,
    val isSaved: Boolean = false
)

sealed interface HousingEditUiEvent {
    data class StreetChanged(val value: String) : HousingEditUiEvent
    data class NumberChanged(val value: String) : HousingEditUiEvent
    data class BoxChanged(val value: String) : HousingEditUiEvent
    data class ZipCodeChanged(val value: String) : HousingEditUiEvent
    data class CityChanged(val value: String) : HousingEditUiEvent
    data class CountryChanged(val value: String) : HousingEditUiEvent
    data class DefaultRentChanged(val value: String) : HousingEditUiEvent
    data class DefaultChargesChanged(val value: String) : HousingEditUiEvent
    data class DepositChanged(val value: String) : HousingEditUiEvent
    data class MailboxLabelChanged(val value: String) : HousingEditUiEvent
    data class MeterGasChanged(val value: String) : HousingEditUiEvent
    data class MeterElectricityChanged(val value: String) : HousingEditUiEvent
    data class MeterWaterChanged(val value: String) : HousingEditUiEvent
    data class PebRatingChanged(val value: PebRating) : HousingEditUiEvent
    data class PebDateChanged(val value: String?) : HousingEditUiEvent
    data class BuildingLabelChanged(val value: String?) : HousingEditUiEvent
    data class InternalNoteChanged(val value: String) : HousingEditUiEvent
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
            is HousingEditUiEvent.StreetChanged ->
                _uiState.update { it.copy(street = event.value, errorMessage = null) }
            is HousingEditUiEvent.NumberChanged ->
                _uiState.update { it.copy(number = event.value, errorMessage = null) }
            is HousingEditUiEvent.BoxChanged ->
                _uiState.update { it.copy(box = event.value, errorMessage = null) }
            is HousingEditUiEvent.ZipCodeChanged ->
                _uiState.update { it.copy(zipCode = event.value, errorMessage = null) }
            is HousingEditUiEvent.CityChanged ->
                _uiState.update { it.copy(city = event.value, errorMessage = null) }
            is HousingEditUiEvent.CountryChanged ->
                _uiState.update { it.copy(country = event.value, errorMessage = null) }
            is HousingEditUiEvent.DefaultRentChanged ->
                _uiState.update { it.copy(defaultRent = event.value) }
            is HousingEditUiEvent.DefaultChargesChanged ->
                _uiState.update { it.copy(defaultCharges = event.value) }
            is HousingEditUiEvent.DepositChanged ->
                _uiState.update { it.copy(deposit = event.value) }
            is HousingEditUiEvent.MailboxLabelChanged ->
                _uiState.update { it.copy(mailboxLabel = event.value, errorMessage = null) }
            is HousingEditUiEvent.MeterGasChanged ->
                _uiState.update { it.copy(meterGas = event.value, errorMessage = null) }
            is HousingEditUiEvent.MeterElectricityChanged ->
                _uiState.update { it.copy(meterElectricity = event.value, errorMessage = null) }
            is HousingEditUiEvent.MeterWaterChanged ->
                _uiState.update { it.copy(meterWater = event.value, errorMessage = null) }
            is HousingEditUiEvent.PebRatingChanged ->
                _uiState.update { it.copy(pebRating = event.value) }
            is HousingEditUiEvent.PebDateChanged ->
                _uiState.update { it.copy(pebDate = event.value) }
            is HousingEditUiEvent.BuildingLabelChanged ->
                _uiState.update { it.copy(buildingLabel = event.value) }
            is HousingEditUiEvent.InternalNoteChanged ->
                _uiState.update { it.copy(internalNote = event.value) }
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
                                remoteId = housing.remoteId,
                                createdAt = housing.createdAt,
                                updatedAt = housing.updatedAt,
                                isArchived = housing.isArchived,
                                street = housing.address.street,
                                number = housing.address.number,
                                box = housing.address.box.orEmpty(),
                                zipCode = housing.address.zipCode,
                                city = housing.address.city,
                                country = housing.address.country,
                                defaultRent = formatEuroInput(housing.rentCents),
                                defaultCharges = formatEuroInput(housing.chargesCents),
                                deposit = formatEuroInput(housing.depositCents),
                                mailboxLabel = housing.mailboxLabel.orEmpty(),
                                meterGas = housing.meterGasId.orEmpty(),
                                meterElectricity = housing.meterElectricityId.orEmpty(),
                                meterWater = housing.meterWaterId.orEmpty(),
                                pebRating = housing.pebRating,
                                pebDate = housing.pebDate,
                                buildingLabel = housing.buildingLabel,
                                internalNote = housing.internalNote.orEmpty(),
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
            val defaultRentCents = parseEuroInputToCents(current.defaultRent) ?: 0L
            val defaultChargesCents = parseEuroInputToCents(current.defaultCharges) ?: 0L
            val depositCents = parseEuroInputToCents(current.deposit) ?: 0L
            val address =
                Address(
                    street = current.street.trim(),
                    number = current.number.trim(),
                    box = current.box.trim().ifBlank { null },
                    zipCode = current.zipCode.trim(),
                    city = current.city.trim(),
                    country = current.country.trim().ifBlank { "BE" }
                )
            val housing = Housing(
                id = current.housingId ?: 0L,
                remoteId = current.remoteId ?: UUID.randomUUID().toString(),
                address = address,
                createdAt = current.createdAt,
                updatedAt = System.currentTimeMillis(),
                isArchived = current.isArchived,
                rentCents = defaultRentCents,
                chargesCents = defaultChargesCents,
                depositCents = depositCents,
                mailboxLabel = current.mailboxLabel.trim().ifBlank { null },
                meterGasId = current.meterGas.trim().ifBlank { null },
                meterElectricityId = current.meterElectricity.trim().ifBlank { null },
                meterWaterId = current.meterWater.trim().ifBlank { null },
                pebRating = current.pebRating,
                pebDate = current.pebDate?.trim()?.ifBlank { null },
                buildingLabel = current.buildingLabel,
                internalNote = current.internalNote.trim().ifBlank { null }
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

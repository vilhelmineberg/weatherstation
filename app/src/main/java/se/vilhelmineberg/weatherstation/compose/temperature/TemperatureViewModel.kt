package se.vilhelmineberg.weatherstation.compose.temperature

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.shredzone.commons.suncalc.SunTimes
import se.vilhelmineberg.weatherstation.room.TemperatureHumidityRepository
import se.vilhelmineberg.weatherstation.weatherstationData
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private const val LATITUDE = weatherstationData.myLocation.LATITUDE
private const val LONGITUDE = weatherstationData.myLocation.LONGITUDE

@HiltViewModel
class TemperatureViewModel @Inject constructor(
    private val temperatureHumidityRepository: TemperatureHumidityRepository
) : ViewModel() {

    val job = Job()

    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)
    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    init {
        observeStateChanges()
    }

    private fun getLatestReadings() {
        coroutineScope.launch {
            val greenhouseDayHigh =
                temperatureHumidityRepository.getHighestTemperature(weatherstationData.Location.GREENHOUSE.ordinal)
            val greenhouseDayLow =
                temperatureHumidityRepository.getLowestTemperature(weatherstationData.Location.GREENHOUSE.ordinal)

            val breweryDayHigh =
                temperatureHumidityRepository.getHighestTemperature(weatherstationData.Location.BREWERY.ordinal)
            val breweryDayLow =
                temperatureHumidityRepository.getLowestTemperature(weatherstationData.Location.BREWERY.ordinal)

            _state.value = _state.value.copy(
                greenhouse = _state.value.greenhouse.copy(
                    dayHigh = greenhouseDayHigh.toString(),
                    dayLow = greenhouseDayLow.toString()
                ),
                brewery = _state.value.brewery.copy(
                    dayHigh = breweryDayHigh.toString(),
                    dayLow = breweryDayLow.toString()
                )
            )
        }
    }

    fun updateTemperatureGreenHouse(newTemperature: String) {
        _state.value = _state.value.copy(
            greenhouse = _state.value.greenhouse.copy(
                temperature = newTemperature,
                timestamp = getCurrentTime()
            ),
            sunTimeDiffPreviousWeek = getDayTimeDiffFromPreviousWeek(),
            sunTimeDiffFromMidWinter = getDayTimeDiffFromMidWinter(),
            sunriseAndSunsetTimes = getSunriseAndSunset()
        )
    }

    fun updateTemperatureBrewery(newTemperature: String) {
        _state.value = _state.value.copy(
            brewery = _state.value.brewery.copy(
                temperature = newTemperature,
                timestamp = getCurrentTime()
            )
        )
    }

    fun updateHumidityGreenHouse(newHumidity: String) {
        _state.value = _state.value.copy(
            greenhouse = _state.value.greenhouse.copy(
                humidity = newHumidity,
                timestamp = getCurrentTime()
            )
        )
    }

    fun updateHumidityBrewery(newHumidity: String) {
        _state.value = _state.value.copy(
            brewery = _state.value.brewery.copy(
                humidity = newHumidity,
                timestamp = getCurrentTime()
            )
        )
    }

    fun updateConnectionStatus(status: ConnectionStatus) {
        _state.value = _state.value.copy(
            connection = status
        )
    }

    fun clearTemperature() {
        _state.value = State(
            greenhouse = TemperatureClient(temperature = "0.0", humidity = "0.0"),
            brewery = TemperatureClient(temperature = "0.0", humidity = "0.0"),
            connection = ConnectionStatus.DISCONNECTED
        )
    }

    private fun observeStateChanges() {
        coroutineScope.launch {
            state.collect {
                getLatestReadings()
            }
        }
    }
}

fun calculateDaytimeMinutes(rise: ZonedDateTime?, set: ZonedDateTime?): Long {
    return ChronoUnit.MINUTES.between(rise, set)
}

fun getSunriseAndSunset(): Pair<String, String> {
    val zonedDateTime = zonedDateTime()
    val sunTimesToday = SunTimes.compute().on(zonedDateTime).at(LATITUDE, LONGITUDE).execute()
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    val sunrise = sunTimesToday.rise?.format(formatter) ?: "00:00"
    val sunset = sunTimesToday.set?.format(formatter) ?: "00:00"

    return Pair(sunrise, sunset)
}

fun getDayTimeDiffFromPreviousWeek(): Int {
    val zonedDateTime = zonedDateTime()
    val sunTimesToday = SunTimes.compute().on(zonedDateTime).at(LATITUDE, LONGITUDE).execute()
    val sunTimesLastWeek = SunTimes.compute().on(zonedDateTime.minusWeeks(1)).at(LATITUDE, LONGITUDE).execute()
    val numberOfMinutesDaytimeThisWeek = calculateDaytimeMinutes(sunTimesToday.rise, sunTimesToday.set)
    val numberOfMinutesDayTimeLastWeek = calculateDaytimeMinutes(sunTimesLastWeek.rise, sunTimesLastWeek.set)
    return (numberOfMinutesDaytimeThisWeek - numberOfMinutesDayTimeLastWeek).toInt()
}

fun getDayTimeDiffFromMidWinter(): Int {
    val zonedDateTime = zonedDateTime()
    val midWinterDayTime =
        ZonedDateTime.of(ZonedDateTime.now().minusYears(1).year, 12, 21, 0, 0, 0, 0, zonedDateTime.zone)

    val sunTimesToday = SunTimes.compute().on(zonedDateTime).at(LATITUDE, LONGITUDE).execute()
    val sunTimesMidWinter = SunTimes.compute().on(midWinterDayTime).at(LATITUDE, LONGITUDE).execute()
    val numberOfMinutesDaytimeToday = calculateDaytimeMinutes(sunTimesToday.rise, sunTimesToday.set)
    val numberOfMinutesDayTimeMidWinter = calculateDaytimeMinutes(sunTimesMidWinter.rise, sunTimesMidWinter.set)
    return (numberOfMinutesDaytimeToday - numberOfMinutesDayTimeMidWinter).toInt()
}

private fun zonedDateTime(): ZonedDateTime {
    val zonedDateTime = ZonedDateTime.of(
        ZonedDateTime.now().year,
        ZonedDateTime.now().month.value,
        ZonedDateTime.now().dayOfMonth,
        0,
        0,
        0,
        0,
        ZoneId.systemDefault()
    )
    return zonedDateTime
}

private fun getCurrentTime(): String {
    return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
}

fun Long.toFormattedString(): String {
    val date = Date(this)
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return format.format(date)
}

sealed class TemperatureAction {
    data object Connect : TemperatureAction()
    data object Disconnect : TemperatureAction()
}

data class State(
    val greenhouse: TemperatureClient = TemperatureClient(),
    val brewery: TemperatureClient = TemperatureClient(),
    val connection: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val sunriseAndSunsetTimes: Pair<String, String> = getSunriseAndSunset(),
    val sunTimeDiffPreviousWeek: Int = getDayTimeDiffFromPreviousWeek(),
    val sunTimeDiffFromMidWinter: Int = getDayTimeDiffFromMidWinter(),
    val checked: Boolean = false
)

data class TemperatureClient(
    val temperature: String = "0.0",
    val humidity: String = "0.0",
    val timestamp: String = "",
    val dayHigh: String = "0.0",
    val dayLow: String = "0.0"
)

enum class ConnectionStatus {
    CONNECTED,
    DISCONNECTED
}
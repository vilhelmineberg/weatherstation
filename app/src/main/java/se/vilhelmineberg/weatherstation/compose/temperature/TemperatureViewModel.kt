package se.vilhelmineberg.weatherstation.compose.temperature

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.shredzone.commons.suncalc.SunTimes
import se.vilhelmineberg.weatherstation.weatherstationData
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private const val LATITUDE = weatherstationData.myLocation.LATITUDE
private const val LONGITUDE = weatherstationData.myLocation.LONGITUDE

class TemperatureViewModel : ViewModel() {
    private val _temperature = MutableStateFlow(TemperatureState())
    val temperature: StateFlow<TemperatureState> = _temperature

    fun updateTemperatureGreenHouse(newTemperature: String) {
        val oldGreenHouseTemperature = _temperature.value.temperatureGreenHouse
        val newGreenHouseTemperature = TemperatureClient(
            temperature = newTemperature,
            humidity = oldGreenHouseTemperature.humidity,
            timestamp = getCurrentTime()
        )
        _temperature.value = TemperatureState(
            temperatureGreenHouse = newGreenHouseTemperature,
            temperatureBrewery = _temperature.value.temperatureBrewery,
            connection = _temperature.value.connection
        )
    }

    fun updateTemperatureBrewery(newTemperature: String) {
        val oldBreweryTemperature = _temperature.value.temperatureBrewery
        val newBreweryTemperature = TemperatureClient(
            temperature = newTemperature,
            humidity = oldBreweryTemperature.humidity,
            timestamp = getCurrentTime()
        )
        _temperature.value = TemperatureState(
            temperatureGreenHouse = _temperature.value.temperatureGreenHouse,
            temperatureBrewery = newBreweryTemperature,
            connection = _temperature.value.connection
        )
    }

    fun updateHumidityGreenHouse(newHumidity: String) {
        val oldGreenHouseTemperature = _temperature.value.temperatureGreenHouse
        val newGreenHouseTemperature = TemperatureClient(
            temperature = oldGreenHouseTemperature.temperature,
            humidity = newHumidity,
            timestamp = getCurrentTime()
        )
        _temperature.value = TemperatureState(
            temperatureGreenHouse = newGreenHouseTemperature,
            temperatureBrewery = _temperature.value.temperatureBrewery,
            connection = _temperature.value.connection
        )
    }

    fun updateHumidityBrewery(newHumidity: String) {
        val oldBreweryTemperature = _temperature.value.temperatureBrewery
        val newBreweryTemperature = TemperatureClient(
            temperature = oldBreweryTemperature.temperature,
            humidity = newHumidity,
            timestamp = getCurrentTime()
        )
        _temperature.value = TemperatureState(
            temperatureGreenHouse = _temperature.value.temperatureGreenHouse,
            temperatureBrewery = newBreweryTemperature,
            connection = _temperature.value.connection
        )
    }

    fun updateConnectionStatus(status: ConnectionStatus) {
        _temperature.value = TemperatureState(
            temperatureGreenHouse = _temperature.value.temperatureGreenHouse,
            temperatureBrewery = _temperature.value.temperatureBrewery,
            connection = status
        )
    }

    fun clearTemperature() {
        _temperature.value = TemperatureState(
            temperatureGreenHouse = TemperatureClient(temperature = "0.0", humidity = "0.0"),
            temperatureBrewery = TemperatureClient(temperature = "0.0", humidity = "0.0"),
            connection = ConnectionStatus.DISCONNECTED
        )
    }
}

fun calculateDaytimeMinutes(rise: ZonedDateTime?, set: ZonedDateTime?): Long {
    return ChronoUnit.MINUTES.between(rise, set)
}

fun getSunriseAndSunset(): Pair<String, String> {
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

    val sunTimesToday = SunTimes.compute()
        .on(zonedDateTime)
        .at(LATITUDE, LONGITUDE)
        .execute()

    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    val sunrise = sunTimesToday.rise?.format(formatter) ?: "N/A"
    val sunset = sunTimesToday.set?.format(formatter) ?: "N/A"

    return Pair(sunrise, sunset)
}

fun getDayTimeDiffFromPreviousWeek(): Int {
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
    val sunTimesToday = SunTimes.compute()
        .on(zonedDateTime)
        .at(LATITUDE, LONGITUDE)
        .execute()

    val sunTimesLastWeek = SunTimes.compute()
        .on(zonedDateTime.minusWeeks(1))
        .at(LATITUDE, LONGITUDE)
        .execute()

    val numberOfMinutesDaytimeThisWeek = calculateDaytimeMinutes(sunTimesToday.rise, sunTimesToday.set)
    val numberOfMinutesDayTimeLastWeek = calculateDaytimeMinutes(sunTimesLastWeek.rise, sunTimesLastWeek.set)
    val diff = numberOfMinutesDaytimeThisWeek - numberOfMinutesDayTimeLastWeek
    return diff.toInt()
}

fun getDayTimeDiffFromMidWinter(): Int {
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
    val midWinterDayTime = ZonedDateTime.of(ZonedDateTime.now().minusYears(1).year, 12, 21, 0, 0, 0, 0, zonedDateTime.zone)

    val sunTimesToday = SunTimes.compute()
        .on(zonedDateTime)
        .at(LATITUDE, LONGITUDE)
        .execute()

    val sunTimesMidWinter = SunTimes.compute()
        .on(midWinterDayTime)
        .at(LATITUDE, LONGITUDE)
        .execute()

    val numberOfMinutesDaytimeToday = calculateDaytimeMinutes(sunTimesToday.rise, sunTimesToday.set)
    val numberOfMinutesDayTimeMidWinter = calculateDaytimeMinutes(sunTimesMidWinter.rise, sunTimesMidWinter.set)
    val diff = numberOfMinutesDaytimeToday - numberOfMinutesDayTimeMidWinter
    return diff.toInt()
}

fun getCurrentTime(): String {
    val current = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    return current.format(formatter)
}

sealed class TemperatureAction {
    data object Connect : TemperatureAction()
    data object Disconnect : TemperatureAction()
}

data class TemperatureState(
    val temperatureGreenHouse: TemperatureClient = TemperatureClient(temperature = "0.0", humidity = "0.0"),
    val temperatureBrewery: TemperatureClient = TemperatureClient(temperature = "0.0", humidity = "0.0"),
    val connection: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val sunTimes: Pair<String, String> = getSunriseAndSunset(),
    val sunTimeDiffPreviousWeek: Int = getDayTimeDiffFromPreviousWeek(),
    val sunTimeDiffFromMidWinter: Int = getDayTimeDiffFromMidWinter(),
)

data class TemperatureClient(val temperature: String, val humidity: String, val timestamp: String = "")

enum class ConnectionStatus {
    CONNECTED,
    DISCONNECTED
}
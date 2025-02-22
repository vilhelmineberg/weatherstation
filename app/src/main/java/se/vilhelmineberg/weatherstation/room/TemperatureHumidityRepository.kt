package se.vilhelmineberg.weatherstation.room

import androidx.room.Room

class TemperatureHumidityRepository(
    applicationContext: android.content.Context
) {

    val db = Room.databaseBuilder(
        applicationContext,
        AppDatabase::class.java, "database-name"
    ).build()

  fun getAll(): List<TemperatureHumidity> {
        return db.TemperatureHumidityDao().getAll()
    }

    fun loadAllByLocation(locationId: Int): List<TemperatureHumidity> {
        return db.TemperatureHumidityDao().loadAllByLocation(locationId)
    }

    fun getLatestReadings(locationId: Int): List<TemperatureHumidity> {
        return db.TemperatureHumidityDao().getLatestReadingsForLocation(locationId)
    }

    fun getHighestTemperature(locationId: Int): Double {
        return db.TemperatureHumidityDao().getAllLast24(locationId).maxByOrNull {
            it.temperature ?: Double.MIN_VALUE}
            ?.temperature ?: 0.0
    }

    fun getLowestTemperature(locationId: Int): Double {
        return db.TemperatureHumidityDao().loadAllByLocation(locationId).minByOrNull { it.temperature ?: Double.MIN_VALUE }?.temperature ?: 0.0
    }

    fun insertForLocation(locationId: Int, temperature: Double, humidity: Double) {
        val timestamp = System.currentTimeMillis()
        db.TemperatureHumidityDao().insertAll(
            TemperatureHumidity(
                location = locationId,
                temperature = temperature,
                humidity = humidity,
                timestamp = timestamp
            )
        )
    }
}
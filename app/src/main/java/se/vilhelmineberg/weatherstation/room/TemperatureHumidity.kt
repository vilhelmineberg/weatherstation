package se.vilhelmineberg.weatherstation.room

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

@Entity(indices = [Index(value = ["location"])])
data class TemperatureHumidity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "location") val location: Int,
    @ColumnInfo(name = "Temperature") val temperature: Double?,
    @ColumnInfo(name = "Humidity") val humidity: Double?,
    @ColumnInfo(name = "timestamp") val timestamp: Long?
)

@Dao
interface TemperatureHumidityDao {
    @Query("SELECT * FROM TemperatureHumidity")
    fun getAll(): List<TemperatureHumidity>

    @Query("SELECT * FROM TemperatureHumidity WHERE location = :locationId")
    fun loadAllByLocation(locationId: Int): List<TemperatureHumidity>

    @Query("SELECT * FROM TemperatureHumidity WHERE location = :locationId ORDER BY timestamp DESC LIMIT 24" )
    fun getAllLast24(locationId: Int): List<TemperatureHumidity>

    @Insert
    fun insertAll(vararg data: TemperatureHumidity)

    @Delete
    fun delete(location: TemperatureHumidity)

    @Query("SELECT * FROM TemperatureHumidity WHERE location = :locationId ORDER BY timestamp DESC LIMIT 1" )
    fun getLatestReadingsForLocation(locationId: Int): List<TemperatureHumidity>

}

@Database(entities = [TemperatureHumidity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun TemperatureHumidityDao(): TemperatureHumidityDao
}
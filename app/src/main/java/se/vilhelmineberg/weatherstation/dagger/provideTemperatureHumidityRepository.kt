package se.vilhelmineberg.weatherstation.dagger

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import se.vilhelmineberg.weatherstation.compose.temperature.TemperatureViewModel
import se.vilhelmineberg.weatherstation.mqtt.MqttClient
import se.vilhelmineberg.weatherstation.room.TemperatureHumidityRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTemperatureHumidityRepository(
        @ApplicationContext appContext: Context
    ): TemperatureHumidityRepository {
        return TemperatureHumidityRepository(appContext)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object ViewModelModule {

    @Provides
    @Singleton
    fun provideTemperatureViewModel(
        temperatureHumidityRepository: TemperatureHumidityRepository
    ): TemperatureViewModel {
        return TemperatureViewModel(temperatureHumidityRepository)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object MqttModule {

    @Provides
    @Singleton
    fun provideMqttClient(
        temperatureViewModel: TemperatureViewModel,
        temperatureHumidityRepository: TemperatureHumidityRepository
    ): MqttClient {
        return MqttClient(temperatureViewModel, temperatureHumidityRepository)
    }
}
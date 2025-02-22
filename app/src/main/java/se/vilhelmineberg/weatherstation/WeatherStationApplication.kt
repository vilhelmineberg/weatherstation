package se.vilhelmineberg.weatherstation

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WeatherStationApplication: Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
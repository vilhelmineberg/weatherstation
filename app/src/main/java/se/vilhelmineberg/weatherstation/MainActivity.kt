package se.vilhelmineberg.weatherstation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.tooling.preview.Preview
import dagger.hilt.android.AndroidEntryPoint
import se.vilhelmineberg.weatherstation.compose.temperature.Temperature
import se.vilhelmineberg.weatherstation.compose.temperature.TemperaturePreview
import se.vilhelmineberg.weatherstation.compose.temperature.TemperatureViewModel
import se.vilhelmineberg.weatherstation.mqtt.MqttClient
import se.vilhelmineberg.weatherstation.ui.theme.WeatherstationTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    private val temperatureViewModel: TemperatureViewModel by viewModels()

    @Inject
    lateinit var mqttClient: MqttClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            WeatherstationTheme {
                Surface {
                    val temperatureState = temperatureViewModel.state.collectAsState()
                    Temperature(state = temperatureState.value, onAction = {
                        when (it) {
                            se.vilhelmineberg.weatherstation.compose.temperature.TemperatureAction.Connect -> mqttClient.connect()
                            se.vilhelmineberg.weatherstation.compose.temperature.TemperatureAction.Disconnect -> mqttClient.disconnect()
                        }
                    })
                }
            }
        }
    }
}

object weatherstationData {
    const val mqttBrokerUrl = "tcp://192.168.1.103:1883"
    val topics = arrayOf(
        // Greenhouse
        "rtl_433/Mikaels-MacBook-Pro/devices/Altronics-X7064/1/780/temperature_C",
        "rtl_433/Mikaels-MacBook-Pro/devices/Altronics-X7064/1/780/humidity",

        // Brewery
        "rtl_433/Mikaels-MacBook-Pro/devices/Altronics-X7064/2/241/temperature_C",
        "rtl_433/Mikaels-MacBook-Pro/devices/Altronics-X7064/2/241/humidity",
    )

    class myLocation {
        companion object {
            const val LATITUDE = 60.056553
            const val LONGITUDE = 16.793934
        }
    }

    enum class Location {
        NOTVALID,
        GREENHOUSE,
        BREWERY
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WeatherstationTheme {
        TemperaturePreview()
    }
}
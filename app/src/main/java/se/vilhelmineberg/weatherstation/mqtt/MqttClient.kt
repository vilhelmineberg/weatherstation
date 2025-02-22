package se.vilhelmineberg.weatherstation.mqtt

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import se.vilhelmineberg.weatherstation.compose.temperature.ConnectionStatus
import se.vilhelmineberg.weatherstation.compose.temperature.TemperatureViewModel
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import se.vilhelmineberg.weatherstation.room.TemperatureHumidityRepository
import se.vilhelmineberg.weatherstation.weatherstationData
import java.util.Locale
import javax.inject.Inject


private const val TAG = "MQTT"
private lateinit var mqttClient: MqttClient

class MqttClient @Inject constructor(
    private val temperatureViewModel: TemperatureViewModel,
    private val temperatureHumidityRepository: TemperatureHumidityRepository
) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var lastSaveTimeGreenhouse: Long = 0
    private var lastSaveTimeBrewery: Long = 0

    init {
        connectToMqtt()
    }

    private var greenhouseData: Pair<Double?, Double?> = Pair(null, null)
    private var breweryData: Pair<Double?, Double?> = Pair(null, null)

    fun String.formatTemperature(): String {
        val temperature = this.toDouble()
        return String.format(Locale.getDefault(), "%.1f", temperature)
    }

    fun connect() {
        try {
            connectToMqtt()
            Log.d(TAG, "Connect to MQTT broker")
        } catch (e: MqttException) {
            Log.e(TAG, "Error failed to connect to from MQTT broker: ${e.message}")
        }
    }

    fun disconnect() {
        try {
            mqttClient.disconnect()
            Log.d(TAG, "Disconnected from MQTT broker")
            temperatureViewModel.clearTemperature()
        } catch (e: MqttException) {
            e.printStackTrace()
            Log.e(TAG, "Error disconnecting from MQTT broker: ${e.message}")
        }
    }

    private fun saveToDb(locationId: Int, temperature: Double, humidity: Double) {
        coroutineScope.launch {
            temperatureHumidityRepository.insertForLocation(locationId, temperature, humidity)
        }
    }

    private fun connectToMqtt() {
        val clientId = MqttClient.generateClientId()

        mqttClient = MqttClient(weatherstationData.mqttBrokerUrl, clientId, null)

        val options = MqttConnectOptions()
        options.isCleanSession = true
        options.connectionTimeout = 30
        options.keepAliveInterval = 100

        mqttClient.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                Log.e(TAG, "Connection lost: ${cause?.message}")
                temperatureViewModel.updateConnectionStatus(ConnectionStatus.DISCONNECTED)
                connect()
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d(TAG, "Message arrived: Topic = $topic, Message = ${message.toString()}")

                val topicActions = mapOf(
                    weatherstationData.topics[0] to { msg: String ->
                        val temperature = msg.formatTemperature().toDouble()
                        greenhouseData = greenhouseData.copy(first = temperature)
                        checkAndSaveGreenhouseData()
                        temperatureViewModel.updateTemperatureGreenHouse(msg.formatTemperature())
                    },
                    weatherstationData.topics[1] to { msg: String ->
                        val humidity = msg.formatTemperature().toDouble()
                        greenhouseData = greenhouseData.copy(second = humidity)
                        checkAndSaveGreenhouseData()
                        temperatureViewModel.updateHumidityGreenHouse(msg.formatTemperature())
                    },

                    weatherstationData.topics[2] to { msg: String ->
                        val temperature = msg.formatTemperature().toDouble()
                        breweryData = breweryData.copy(first = temperature)
                        checkAndSaveBreweryData()
                        temperatureViewModel.updateTemperatureBrewery(msg.formatTemperature())
                    },
                    weatherstationData.topics[3] to { msg: String ->
                        val humidity = msg.formatTemperature().toDouble()
                        breweryData = breweryData.copy(second = humidity)
                        checkAndSaveBreweryData()
                        temperatureViewModel.updateHumidityBrewery(msg.formatTemperature())
                    }
                )

                topic?.let {
                    message?.toString()?.let { msg ->
                        topicActions[topic]?.invoke(msg)
                    }
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                Log.e(TAG, "Delivery completed:")
            }
        })

        try {
            mqttClient.connect(options)
            Log.d(TAG, "Connected to MQTT broker")
            temperatureViewModel.updateConnectionStatus(ConnectionStatus.CONNECTED)

            weatherstationData.topics.forEach { topic ->
                mqttClient.subscribe(topic)
                Log.d(TAG, "Subscribed to topic: $topic")
            }

        } catch (e: MqttException) {
            e.printStackTrace()
            Log.e(TAG, "Error connecting to MQTT broker: ${e.message}")
        }
    }

    private fun checkAndSaveGreenhouseData() {
        val (temperature, humidity) = greenhouseData
        val currentTime = System.currentTimeMillis()
        if (temperature != null && humidity != null && (currentTime - lastSaveTimeGreenhouse >= 3600000)) {
            saveToDb(weatherstationData.Location.GREENHOUSE.ordinal, temperature, humidity)
            greenhouseData = Pair(null, null) // Reset after saving
            lastSaveTimeGreenhouse = currentTime // Update last save time
        }
    }

    private fun checkAndSaveBreweryData() {
        val (temperature, humidity) = breweryData
        val currentTime = System.currentTimeMillis()
        if (temperature != null && humidity != null && (currentTime - lastSaveTimeBrewery >= 3600000)) {
            saveToDb(weatherstationData.Location.BREWERY.ordinal, temperature, humidity)
            breweryData = Pair(null, null) // Reset after saving
            lastSaveTimeBrewery = currentTime // Update last save time
        }
    }

}
package se.vilhelmineberg.weatherstation.mqtt

import android.util.Log
import se.vilhelmineberg.weatherstation.compose.temperature.ConnectionStatus
import se.vilhelmineberg.weatherstation.compose.temperature.TemperatureViewModel
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import se.vilhelmineberg.weatherstation.weatherstationData
import java.util.Locale


private const val TAG = "MQTT"
private lateinit var mqttClient: MqttClient

class MqttClient(private val temperatureViewModel: TemperatureViewModel) {
    init {
        connectToMqtt()
    }

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
                        temperatureViewModel.updateTemperatureGreenHouse(
                            msg.formatTemperature()
                        )
                    },
                    weatherstationData.topics[1] to { msg: String -> temperatureViewModel.updateHumidityGreenHouse(msg.formatTemperature()) },
                    weatherstationData.topics[2] to { msg: String -> temperatureViewModel.updateTemperatureBrewery(msg.formatTemperature()) },
                    weatherstationData.topics[3] to { msg: String -> temperatureViewModel.updateHumidityBrewery(msg.formatTemperature()) }
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


}
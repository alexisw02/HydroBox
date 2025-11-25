package com.hydrobox.app.mqtt

import android.util.Log
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import kotlinx.coroutines.*
import java.nio.charset.StandardCharsets

object HydroMqtt {
    var host: String = "192.168.3.201"
    var port: Int = 1883
    var user: String? = null
    var pass: String? = null

    private var client: Mqtt3AsyncClient? = null
    @Volatile var isConnected: Boolean = false
        private set

    private const val TAG = "HydroMqtt"

    fun connect() {
        val cli = MqttClient.builder()
            .useMqttVersion3()
            .identifier("hydrobox-android-" + System.currentTimeMillis())
            .serverHost(host)
            .serverPort(port)
            .buildAsync()

        client = cli
        val connectBuilder = cli.connectWith()
            .cleanSession(true)
            .keepAlive(30)

        val u = user; val p = pass
        if (!u.isNullOrBlank() && !p.isNullOrBlank()) {
            connectBuilder.simpleAuth()
                .username(u)
                .password(p.toByteArray())
                .applySimpleAuth()
        }

        connectBuilder.send().whenComplete { _, err ->
            if (err != null) {
                isConnected = false
                Log.e(TAG, "connect error", err)
                GlobalScope.launch(Dispatchers.IO) {
                    delay(2500)
                    connect() // reintento simple
                }
            } else {
                isConnected = true
                Log.d(TAG, "connected to $host:$port")
            }
        }
    }

    fun disconnect() {
        isConnected = false
        client?.disconnect()
        client = null
    }

    fun sendSwitch(deviceId: String, on: Boolean) {
        val payload = """{"on":${if (on) "true" else "false"}}"""
        publish("hydrobox/actuators/$deviceId/set", payload)
    }

    fun sendDose(deviceId: String, ml: Int) {
        val payload = """{"dose_ml":$ml}"""
        publish("hydrobox/actuators/$deviceId/set", payload)
    }

    private fun publish(topic: String, payload: String) {
        val cli = client ?: run {
            Log.w(TAG, "publish skipped: no client")
            return
        }
        cli.publishWith()
            .topic(topic)
            .qos(MqttQos.AT_LEAST_ONCE)
            .retain(false)
            .payload(payload.toByteArray(StandardCharsets.UTF_8))
            .send()
            .whenComplete { _, err ->
                if (err != null) Log.e(TAG, "publish error $topic", err)
                else Log.d(TAG, "publish ok $topic $payload")
            }
    }
}


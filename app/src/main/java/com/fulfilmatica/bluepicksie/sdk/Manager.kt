package com.fulfilmatica.bluepicksie.sdk

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.BOND_NONE
import android.bluetooth.BluetoothSocket
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.delay
import java.util.*
import kotlin.concurrent.thread

class Manager() {
    val TAG = "BLUEPICKSIE"
    var socket: BluetoothSocket? = null
    var device: BluetoothDevice? = null
    var cmdQueue: Queue<String> = LinkedList<String>()

    var initialized: Boolean = false

    fun connect(): Boolean {
        if (!initialized) initialize()

        finddevice@ for (d in BluetoothAdapter.getDefaultAdapter().bondedDevices) {
            if ((d.name.indexOf("picksie") > -1 || d.name.indexOf("rpidenis") > -1)) {
                device = d
                break@finddevice
            }
        }

        if (device == null) return false

        Log.d(TAG, String.format("Connected to device %s", device!!.name))
        socket = device!!.createRfcommSocketToServiceRecord(UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee"))
        socket!!.connect()

        return socket!!.isConnected
    }

    /**
     * Must be called when the activity stops
     */
    fun disconnect() {
        initialized = false

        if (socket != null && socket!!.isConnected)
            socket!!.close()
    }

    fun isConnected(): Boolean {
        return socket!!.isConnected
    }

    fun setScreen(channel: Int, data: String) {
        cmdQueue.add(String.format("=%d%s\n", channel, data))
    }

    fun clearScreen(channel: Int) {
        cmdQueue.add(String.format("=%d$$$$\n", channel))
    }

    private fun initialize() {
        initialized = true
        processQueue()
        readButtons()
    }

    private fun readButtons() {
        thread(start = true) {
            while (initialized) {
                // TODO
                Thread.sleep(100)
            }
        }
    }

    private fun processQueue() {
        var cmd: String

        thread(start = true) {
            while (initialized) {
                if (!cmdQueue.isEmpty()) {
                    cmd = cmdQueue.poll()

                    if (socket == null || !isConnected()) {
                        Thread.sleep(100)
                    } else {
                        Log.d(TAG, String.format("Sending command: %s", cmd))
                        socket!!.outputStream.write(cmd.toByteArray())
                        Thread.sleep(1)
                    }
                } else {
                    Thread.sleep(5)
                }
            }
        }
    }
}
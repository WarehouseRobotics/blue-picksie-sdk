package com.fulfilmatica.bluepicksie.sdk

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.BOND_NONE
import android.bluetooth.BluetoothSocket
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.delay
import java.io.IOException
import java.lang.Exception
import java.util.*
import kotlin.concurrent.thread

interface CartButtonClickDelegate {
    fun onCartButtonDown(channel: Int) {

    }

    fun onCartButtonUp(channel: Int) {

    }
}

class Manager() {
    val TAG = "BLUEPICKSIE"
    var socket: BluetoothSocket? = null
    var device: BluetoothDevice? = null
    var cmdQueue: Queue<String> = LinkedList<String>()
    var isConnecting: Boolean = false

    var initialized: Boolean = false

    lateinit var delegate: CartButtonClickDelegate

    fun connect(): Boolean {
        if (isConnecting)
            throw Exception("Connection already in progress")

        isConnecting = true;

        finddevice@ for (d in BluetoothAdapter.getDefaultAdapter().bondedDevices) {
            if ((d.name.indexOf("picksie") > -1 || d.name.indexOf("rpidenis") > -1)) {
                device = d
                break@finddevice
            }
        }

        if (device == null) {
            isConnecting = false
            return false
        }

        try {
            Log.d(TAG, String.format("Connected to device %s", device!!.name))
            socket = device?.createRfcommSocketToServiceRecord(UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee"))
            socket?.connect()

            if (!initialized) initialize()
        } catch(e: Exception) {
            Log.d(TAG, e.toString() + "\n" + e.stackTrace.toString())
        } finally {
            isConnecting = false
        }

        return socket?.isConnected == true
    }

    /**
     * Must be called when the activity stops
     */
    fun disconnect() {
        initialized = false

        if (socket != null && socket!!.isConnected)
            socket?.close()
    }

    fun isConnected(): Boolean {
        if (isConnecting) return false
        if (socket == null) return false
        return socket?.isConnected == true
    }

    fun setScreen(channel: Int, data: String) {
        cmdQueue.add(String.format("=%d%s\n", channel, data))
    }

    fun clearScreen(channel: Int) {
        cmdQueue.add(String.format("=%d$$$$\n", channel))
    }

    fun setButtonDelegate(delegate: CartButtonClickDelegate) {
        this.delegate = delegate
    }

    private fun initialize() {
        initialized = true
        processQueue()
        readButtons()
    }

    private fun readButtons() {
        thread(start = true) {
            while (initialized) {
                var stream = socket!!.inputStream

                try {
                    if (stream.available() > 0) {
                        var firstByte = stream.read()
                        var secondByte: Int

                        Log.d(TAG, String.format("Got byte: %d", firstByte))

                        if (firstByte.toChar().equals('C')) {
                            secondByte = stream.read()
                            Log.d(TAG, String.format("Got C byte for ch: %d", secondByte))
                            if (this::delegate.isInitialized) {
                                delegate.onCartButtonDown(secondByte.toChar().toString().toInt())
                            }
                        } else if (firstByte.toChar().equals('O')) {
                            secondByte = stream.read()
                            Log.d(TAG, String.format("Got O byte for ch: %d", secondByte))
                            if (this::delegate.isInitialized) {
                                delegate.onCartButtonUp(secondByte.toChar().toString().toInt())
                            }
                        }
                    }
                } catch(e: IOException) {
                    Log.d(TAG, e.toString() + "\n" + e.stackTrace.toString())
                }
                Thread.sleep(10)
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
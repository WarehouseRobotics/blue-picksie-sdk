package com.example.bluepicksiesdk.example

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.fulfilmatica.bluepicksie.sdk.CartButtonClickDelegate
import com.fulfilmatica.bluepicksie.sdk.Manager
import java.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment(), CartButtonClickDelegate {
    lateinit var cartManager: Manager

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.button_connect).setOnClickListener {
            onConnectClick(it)
        }

        view.findViewById<Button>(R.id.button_test1).setOnClickListener {
            onTest1Click(it)
        }

        view.findViewById<Button>(R.id.button_test2).setOnClickListener {
            onTest2Click(it)
        }

        view.findViewById<Button>(R.id.button_test3).setOnClickListener {
            onTest3Click(it)
        }
    }

    fun onConnectClick(view: View) {
        cartManager = Manager()

        if (cartManager.isConnected()) {
            Toast.makeText(context, "Already connected", Toast.LENGTH_SHORT).show()
            return
        }

        if (cartManager.isConnecting) {
            Toast.makeText(context, "Connection in progress", Toast.LENGTH_SHORT).show()
            return
        }

        // Post some test messages on timeout
        Handler(Looper.getMainLooper()).postDelayed({
            var connected = cartManager.connect()
            if (!connected) {
                System.out.println("[BLUEPICKSIE] Could not connect to the cart")
                Toast.makeText(context, "Could not connect", Toast.LENGTH_LONG).show()
            } else {
                cartManager.setButtonDelegate(this)
                Toast.makeText(context, String.format("Connected to %s", cartManager.device?.name), Toast.LENGTH_LONG).show()
            }
        }, 1)
    }

    fun onTest1Click(view: View) {
        if (!this::cartManager.isInitialized || !cartManager.isConnected()) {
            Toast.makeText(context, "Not connected", Toast.LENGTH_LONG).show()
            return
        }

        // Post some test messages on timeout
        Handler(Looper.getMainLooper()).postDelayed({
            System.out.println("[BLUEPICKSIE] Starting test1")
            cartManager.setScreen(0, "0001")
            cartManager.setScreen(1, "0002")
            cartManager.setScreen(2, "0003")
            cartManager.setScreen(3, "0004")
            cartManager.setScreen(4, "0005")
            cartManager.setScreen(5, "0006")
            cartManager.setScreen(6, "0007")
            cartManager.setScreen(7, "0008")

            Handler(Looper.getMainLooper()).postDelayed({
                System.out.println("[BLUEPICKSIE] Test command 2: clear screen no. 1")
                cartManager.clearScreen(0)
                cartManager.clearScreen(1)
                cartManager.clearScreen(2)
                cartManager.clearScreen(3)
                cartManager.clearScreen(4)
                cartManager.clearScreen(5)
                cartManager.clearScreen(6)
                cartManager.clearScreen(7)
            }, 10000)
        }, 1)
    }

    fun onTest2Click(view: View) {
        if (!this::cartManager.isInitialized || !cartManager.isConnected()) {
            Toast.makeText(context, "Not connected", Toast.LENGTH_LONG).show()
            return
        }

        Handler(Looper.getMainLooper()).postDelayed({
            System.out.println("[BLUEPICKSIE] Starting test2 (set all screens to index)")
            cartManager.setScreen(0, "0001")
            cartManager.setScreen(1, "0002")
            cartManager.setScreen(2, "0003")
            cartManager.setScreen(3, "0004")
            cartManager.setScreen(4, "0005")
            cartManager.setScreen(5, "0006")
            cartManager.setScreen(6, "0007")
            cartManager.setScreen(7, "0008")
        }, 1)
    }

    fun onTest3Click(view: View) {
        if (!this::cartManager.isInitialized || !cartManager.isConnected()) {
            Toast.makeText(context, "Not connected", Toast.LENGTH_LONG).show()
            return
        }

        Handler(Looper.getMainLooper()).postDelayed({
            System.out.println("[BLUEPICKSIE] Starting test3 (clear all screens)")
            cartManager.clearScreen(0)
            cartManager.clearScreen(1)
            cartManager.clearScreen(2)
            cartManager.clearScreen(3)
            cartManager.clearScreen(4)
            cartManager.clearScreen(5)
            cartManager.clearScreen(6)
            cartManager.clearScreen(7)
        }, 1)
    }

    override fun onCartButtonDown(channel: Int) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, String.format("Button %d DOWN", channel), Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onCartButtonUp(channel: Int) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, String.format("Button %d UP", channel), Toast.LENGTH_SHORT)
                .show()
        }
    }
}
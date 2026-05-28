package com.example.baseeq

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import com.example.baseeq.ui.theme.BaseEQTheme

class MainActivity : ComponentActivity() {
    private lateinit var audioEngine: AudioEngine
    private lateinit var presetManager: PresetManager
    private var batteryLevel = mutableStateOf("Невідомо")

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "android.bluetooth.device.action.BATTERY_LEVEL_CHANGED") {
                val level = intent.getIntExtra("android.bluetooth.device.extra.BATTERY_LEVEL", -1)
                batteryLevel.value = if (level in 0..100) "$level%" else "Невідомо"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the logic classes
        audioEngine = AudioEngine()
        presetManager = PresetManager(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                registerForActivityResult(ActivityResultContracts.RequestPermission()) {}.launch(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }

        registerReceiver(batteryReceiver, IntentFilter("android.bluetooth.device.action.BATTERY_LEVEL_CHANGED"))

        setContent {
            BaseEQTheme {
                MainScreen(batteryLevel.value, audioEngine, presetManager)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryReceiver)
        audioEngine.disableEQ()
    }
}
package com.example.baseeq

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.audiofx.DynamicsProcessing
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.baseeq.ui.theme.BaseEQTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity()
{
    private var eq: DynamicsProcessing? = null
    private val frequencies = floatArrayOf(10f, 21f, 42f, 83f, 166f, 333f, 577f, 1000f, 2000f, 4000f, 8000f, 16000f, 20000f)

    private var currentGains = floatArrayOf(30f, 7.5f, 6f, 3.5f, 1f, -0.5f, -2f, -2f, -2f, -2f, -2f, -3f, -2f)

    private var batteryLevel = mutableStateOf("Невідомо")

    private val batteryReceiver = object : BroadcastReceiver()
    {
        override fun onReceive(context: Context?, intent: Intent?)
        {
            if (intent?.action == "android.bluetooth.device.action.BATTERY_LEVEL_CHANGED")
            {
                val level = intent.getIntExtra("android.bluetooth.device.extra.BATTERY_LEVEL", -1)
                batteryLevel.value = if (level in 0..100) "$level%" else "Невідомо"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
            {
                registerForActivityResult(ActivityResultContracts.RequestPermission()) {}.launch(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }

        registerReceiver(batteryReceiver, IntentFilter("android.bluetooth.device.action.BATTERY_LEVEL_CHANGED"))

        setContent {
            BaseEQTheme {
                MainScreen(
                    battery = batteryLevel.value,
                    initialGains = currentGains,
                    freqs = frequencies,
                    onToggle = { isEnabled -> if (isEnabled) enableEQ() else disableEQ() },
                    onGainChange = { index, gain -> updateBand(index, gain) }
                )
            }
        }
    }

    private fun enableEQ()
    {
        val eqBandCount = 13
        val builder = DynamicsProcessing.Config.Builder(
            0, 2, false, 0, false, 0, true, eqBandCount, false
        )
        eq = DynamicsProcessing(0, 0, builder.build())

        for (i in 0 until eqBandCount)
        {
            val band = DynamicsProcessing.EqBand(true, frequencies[i], currentGains[i])
            eq?.setPostEqBandAllChannelsTo(i, band)
        }
        eq?.enabled = true
    }

    private fun updateBand(index: Int, gain: Float)
    {
        currentGains[index] = gain
        if (eq?.enabled == true)
        {
            val band = DynamicsProcessing.EqBand(true, frequencies[index], gain)
            eq?.setPostEqBandAllChannelsTo(index, band)
        }
    }

    private fun disableEQ()
    {
        eq?.enabled = false
        eq?.release()
        eq = null
    }

    override fun onDestroy()
    {
        super.onDestroy()
        unregisterReceiver(batteryReceiver)
        disableEQ()
    }
}

@Composable
fun MainScreen(
    battery: String,
    initialGains: FloatArray,
    freqs: FloatArray,
    onToggle: (Boolean) -> Unit,
    onGainChange: (Int, Float) -> Unit
)
{
    var isActive by remember { mutableStateOf(false) }
    val sliderValues = remember { initialGains.map { mutableStateOf(it) } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("ОСНОВА", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("Батарея навушників: $battery", color = Color.Gray, fontSize = 14.sp)
                }
                Switch(
                    checked = isActive,
                    onCheckedChange = {
                        isActive = it
                        onToggle(it)
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00C853))
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(freqs.size) { index ->
                val freqLabel = if (freqs[index] >= 1000) "${(freqs[index] / 1000).toInt()}k" else "${freqs[index].toInt()}"

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("$freqLabel Гц", color = Color.White, fontWeight = FontWeight.Medium)
                            Text("${((sliderValues[index].value * 10).roundToInt() / 10f)} дБ", color = Color(0xFF00C853))
                        }

                        Slider(
                            value = sliderValues[index].value,
                            onValueChange = { newValue ->
                                sliderValues[index].value = newValue
                                onGainChange(index, newValue)
                            },
                            valueRange = -30f..30f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = Color(0xFF00C853),
                                inactiveTrackColor = Color.DarkGray
                            )
                        )
                    }
                }
            }
        }
    }
}
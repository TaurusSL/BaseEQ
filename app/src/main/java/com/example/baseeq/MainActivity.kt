package com.example.baseeq

import android.media.audiofx.DynamicsProcessing
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baseeq.ui.theme.BaseEQTheme

class MainActivity : ComponentActivity()
{
    private var eq: DynamicsProcessing? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContent {
            BaseEQTheme {
                MainScreen(
                    onToggle = { isEnabled ->
                        if (isEnabled) enableEQ() else disableEQ()
                    }
                )
            }
        }
    }

    private fun enableEQ()
    {
        val eqBandCount = 13

        val builder = DynamicsProcessing.Config.Builder(
            0,
            2,
            false, 0,
            false, 0,
            true, eqBandCount,
            false
        )

        eq = DynamicsProcessing(0, 0, builder.build())

        val frequencies = floatArrayOf(10f, 21f, 42f, 83f, 166f, 333f, 577f, 1000f, 2000f, 4000f, 8000f, 16000f, 20000f)
        val gains = floatArrayOf(30f, 7.5f, 6f, 3.5f, 1f, -0.5f, -2f, -2f, -2f, -2f, -2f, -3f, -2f)

        for (i in 0 until eqBandCount)
        {
            val band = DynamicsProcessing.EqBand(true, frequencies[i], gains[i])
            eq?.setPostEqBandAllChannelsTo(i, band)
        }

        eq?.enabled = true
    }

    private fun disableEQ()
    {
        eq?.enabled = false
        eq?.release()
        eq = null
    }
}

@Composable
fun MainScreen(onToggle: (Boolean) -> Unit)
{
    var isActive by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ОСНОВА",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isActive) "Эквалайзер активен" else "Эквалайзер выключен",
                    color = Color.Gray,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Switch(
                    checked = isActive,
                    onCheckedChange = {
                        isActive = it
                        onToggle(it)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF00C853),
                        checkedTrackColor = Color(0xFF00C853).copy(alpha = 0.3f)
                    )
                )
            }
        }
    }
}
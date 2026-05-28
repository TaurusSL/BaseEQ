package com.example.baseeq

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun MainScreen(battery: String, audioEngine: AudioEngine, presetManager: PresetManager) {
    var isActive by remember { mutableStateOf(false) }
    var isEqExpanded by remember { mutableStateOf(false) }
    var showSaveModal by remember { mutableStateOf(false) }
    var newPresetName by remember { mutableStateOf("") }

    var customPresets by remember { mutableStateOf(presetManager.getCustomPresets()) }
    val allPresets = presetManager.defaultPresets + customPresets

    var selectedPreset by remember { mutableStateOf(allPresets[0]) }
    val currentGains = remember { mutableStateListOf(*selectedPreset.gains.toTypedArray()) }

    // We automatically apply the frequencies when the equalizer is turned on
    LaunchedEffect(currentGains.toList(), isActive) {
        if (isActive) audioEngine.updateBands(currentGains.toFloatArray())
    }

    // Save dialog box
    if (showSaveModal) {
        AlertDialog(
            onDismissRequest = { showSaveModal = false },
            title = { Text("Зберегти пресет") },
            text = {
                OutlinedTextField(
                    value = newPresetName,
                    onValueChange = { newPresetName = it },
                    label = { Text("Назва") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newPresetName.isNotBlank()) {
                        presetManager.saveCustomPreset(newPresetName, currentGains.toFloatArray())
                        customPresets = presetManager.getCustomPresets()
                        showSaveModal = false
                        newPresetName = ""
                    }
                }) { Text("Зберегти") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveModal = false }) { Text("Скасувати") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF121212)).padding(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(selectedPreset.name, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("Батарея: $battery", color = Color.Gray, fontSize = 14.sp)
                }
                Switch(
                    checked = isActive,
                    onCheckedChange = {
                        isActive = it
                        if (it) audioEngine.enableEQ(currentGains.toFloatArray()) else audioEngine.disableEQ()
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00C853))
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Drop-down list of presets
        var presetMenuExpanded by remember { mutableStateOf(false) }
        Box(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { presetMenuExpanded = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A)),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Вибрати пресет: ${selectedPreset.name}", color = Color.White) }

            DropdownMenu(expanded = presetMenuExpanded, onDismissRequest = { presetMenuExpanded = false }) {
                allPresets.forEach { preset ->
                    DropdownMenuItem(
                        text = {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(preset.name)
                                // Delete button for custom presets
                                if (preset in customPresets) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Видалити",
                                        modifier = Modifier.clickable {
                                            presetManager.deleteCustomPreset(preset.name)
                                            customPresets = presetManager.getCustomPresets()
                                            presetMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        },
                        onClick = {
                            selectedPreset = preset
                            preset.gains.forEachIndexed { i, gain -> currentGains[i] = gain }
                            presetMenuExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Foldable Equalizer
        Card(
            modifier = Modifier.fillMaxWidth().clickable { isEqExpanded = !isEqExpanded },
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Налаштування частот", color = Color.White, modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (isEqExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }

        AnimatedVisibility(visible = isEqExpanded) {
            Column(modifier = Modifier.fillMaxSize()) {
                Button(
                    onClick = { showSaveModal = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Зберегти як новий пресет", color = Color.White) }

                LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
                    items(audioEngine.frequencies.size) { i ->
                        val freq = audioEngine.frequencies[i]
                        val label = if (freq >= 1000) "${(freq / 1000).toInt()}k" else "${freq.toInt()}"

                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("$label Гц", color = Color.White)
                                Text("${(currentGains[i] * 10).roundToInt() / 10f} дБ", color = Color(0xFF00C853))
                            }
                            Slider(
                                value = currentGains[i],
                                onValueChange = { currentGains[i] = it },
                                valueRange = -30f..30f,
                                colors = SliderDefaults.colors(activeTrackColor = Color(0xFF00C853))
                            )
                        }
                    }
                }
            }
        }
    }
}
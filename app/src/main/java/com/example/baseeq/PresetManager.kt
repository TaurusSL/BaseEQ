package com.example.baseeq

import android.content.Context
import android.content.SharedPreferences

data class Preset(val name: String, val gains: FloatArray)

class PresetManager(context: Context) {
    // Save user presets to the phone's internal memory
    private val prefs: SharedPreferences = context.getSharedPreferences("eq_presets", Context.MODE_PRIVATE)

    val defaultPresets = listOf(
        Preset("ОСНОВА", floatArrayOf(30f, 7.5f, 6f, 3.5f, 1f, -0.5f, -2f, -2f, -2f, -2f, -2f, -3f, -2f)),
        Preset("Bass Boost (Про)", floatArrayOf(15f, 12f, 8f, 4f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)),
        Preset("Vocal Clarity (Про)", floatArrayOf(-2f, -1f, 0f, 1f, 3f, 4f, 5f, 5f, 4f, 2f, 0f, -1f, -2f)),
        Preset("Flat (Вимкнено)", FloatArray(13) { 0f })
    )

    fun saveCustomPreset(name: String, gains: FloatArray) {
        prefs.edit().putString(name, gains.joinToString(",")).apply()
    }

    fun deleteCustomPreset(name: String) {
        prefs.edit().remove(name).apply()
    }

    fun getCustomPresets(): List<Preset> {
        val presets = mutableListOf<Preset>()
        prefs.all.forEach { (name, value) ->
            val gains = (value as String).split(",").map { it.toFloat() }.toFloatArray()
            presets.add(Preset(name, gains))
        }
        return presets
    }
}
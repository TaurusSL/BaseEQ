package com.example.baseeq

import android.media.audiofx.DynamicsProcessing

class AudioEngine {
    private var eq: DynamicsProcessing? = null
    val frequencies = floatArrayOf(10f, 21f, 42f, 83f, 166f, 333f, 577f, 1000f, 2000f, 4000f, 8000f, 16000f, 20000f)

    fun enableEQ(gains: FloatArray) {
        if (eq == null) {
            val builder = DynamicsProcessing.Config.Builder(0, 2, false, 0, false, 0, true, 13, false)
            eq = DynamicsProcessing(0, 0, builder.build())
        }
        updateBands(gains)
        eq?.enabled = true
    }

    fun updateBands(gains: FloatArray) {
        for (i in 0 until 13) {
            val band = DynamicsProcessing.EqBand(true, frequencies[i], gains[i])
            eq?.setPostEqBandAllChannelsTo(i, band)
        }
    }

    fun disableEQ() {
        eq?.enabled = false
        eq?.release()
        eq = null
    }
}
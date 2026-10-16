package com.breakpulse.app.core

import android.media.AudioManager
import android.media.ToneGenerator

object SoundManager {
    private var toneGenerator: ToneGenerator? = null
    private var isInitialized = false

    private fun getToneGenerator(): ToneGenerator? {
        if (!isInitialized) {
            isInitialized = true
            try {
                toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 75)
            } catch (_: Throwable) {
                toneGenerator = null
            }
        }
        return toneGenerator
    }

    fun playLaser() {
        try {
            getToneGenerator()?.startTone(ToneGenerator.TONE_PROP_BEEP, 30)
        } catch (_: Throwable) {}
    }

    fun playHit() {
        try {
            getToneGenerator()?.startTone(ToneGenerator.TONE_PROP_ACK, 45)
        } catch (_: Throwable) {}
    }

    fun playBell() {
        try {
            getToneGenerator()?.startTone(ToneGenerator.TONE_PROP_BEEP2, 100)
        } catch (_: Throwable) {}
    }

    fun playVictory() {
        try {
            getToneGenerator()?.startTone(ToneGenerator.TONE_PROP_PROMPT, 200)
        } catch (_: Throwable) {}
    }
}

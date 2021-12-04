/*
 * Copyright (C) 2021 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.device

import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.AudioSystem
import android.os.IBinder
import android.os.UEventObserver
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.view.KeyEvent

class KeyHandler : Service() {
    private lateinit var audioManager: AudioManager
    private lateinit var notificationManager: NotificationManager
    private lateinit var vibrator: Vibrator

    private var wasMuted = false
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val stream = intent.getIntExtra(AudioManager.EXTRA_VOLUME_STREAM_TYPE, -1)
            val state = intent.getBooleanExtra(AudioManager.EXTRA_STREAM_VOLUME_MUTED, false)
            if (stream == AudioSystem.STREAM_MUSIC && !state) {
                wasMuted = false
            }
        }
    }

    private val alertSliderEventObserver = object : UEventObserver() {
        private val lock = Any()

        override fun onUEvent(event: UEvent) {
            synchronized(lock) {
                event.get("SWITCH_STATE")?.let {
                    handleMode(it.toInt())
                    return
                }
                event.get("STATE")?.let {
                    val none = it.contains("USB=0")
                    val vibration = it.contains("HOST=0")
                    val silent = it.contains("null)=0")

                    if (none && !vibration && !silent) {
                        handleMode(POSITION_BOTTOM)
                    } else if (!none && vibration && !silent) {
                        handleMode(POSITION_MIDDLE)
                    } else if (!none && !vibration && silent) {
                        handleMode(POSITION_TOP)
                    }

                    return
                }
            }
        }
    }

    override fun onCreate() {
        audioManager = getSystemService(AudioManager::class.java)
        notificationManager = getSystemService(NotificationManager::class.java)
        vibrator = getSystemService(Vibrator::class.java)

        registerReceiver(
            broadcastReceiver,
            IntentFilter(AudioManager.STREAM_MUTE_CHANGED_ACTION)
        )
        alertSliderEventObserver.startObserving("tri-state-key")
        alertSliderEventObserver.startObserving("tri_state_key")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun vibrateIfNeeded(mode: Int) {
        when (mode) {
            AudioManager.RINGER_MODE_VIBRATE -> vibrator.vibrate(MODE_VIBRATION_EFFECT)
            AudioManager.RINGER_MODE_NORMAL -> vibrator.vibrate(MODE_NORMAL_EFFECT)
        }
    }

    private fun handleMode(position: Int) {
        val muteMedia = Settings.System.getInt(contentResolver,
                SETTINGS_PREFIX + MUTE_MEDIA_WITH_SILENT, 0) == 1

        val mode = when (position) {
            POSITION_TOP -> Settings.System.getInt(contentResolver,
                    SETTINGS_PREFIX + ALERT_SLIDER_TOP_KEY, 0)
            POSITION_MIDDLE -> Settings.System.getInt(contentResolver,
                    SETTINGS_PREFIX + ALERT_SLIDER_MIDDLE_KEY, 1)
            POSITION_BOTTOM -> Settings.System.getInt(contentResolver,
                    SETTINGS_PREFIX + ALERT_SLIDER_BOTTOM_KEY, 2)
            else -> return
        }

        when (mode) {
            AudioManager.RINGER_MODE_SILENT -> {
                notificationManager.setZenMode(Settings.Global.ZEN_MODE_OFF, null, TAG)
                audioManager.setRingerModeInternal(mode)
                if (muteMedia) {
                    audioManager.adjustVolume(AudioManager.ADJUST_MUTE, 0)
                    wasMuted = true
                }
            }
            AudioManager.RINGER_MODE_VIBRATE, AudioManager.RINGER_MODE_NORMAL -> {
                notificationManager.setZenMode(Settings.Global.ZEN_MODE_OFF, null, TAG)
                audioManager.setRingerModeInternal(mode)
                if (muteMedia && wasMuted) {
                    audioManager.adjustVolume(AudioManager.ADJUST_UNMUTE, 0)
                }
            }
            ZEN_PRIORITY_ONLY, ZEN_TOTAL_SILENCE, ZEN_ALARMS_ONLY -> {
                audioManager.setRingerModeInternal(AudioManager.RINGER_MODE_NORMAL)
                notificationManager.setZenMode(mode - ZEN_OFFSET, null, TAG)
                if (muteMedia && wasMuted) {
                    audioManager.adjustVolume(AudioManager.ADJUST_UNMUTE, 0)
                }
            }
        }
        vibrateIfNeeded(mode)
    }

    companion object {
        private const val TAG = "KeyHandler"

        // Slider key positions
        private const val POSITION_TOP = 1
        private const val POSITION_MIDDLE = 2
        private const val POSITION_BOTTOM = 3

        // Preference keys
        const val ALERT_SLIDER_TOP_KEY = "config_top_position"
        const val ALERT_SLIDER_MIDDLE_KEY = "config_middle_position"
        const val ALERT_SLIDER_BOTTOM_KEY = "config_bottom_position"
        const val MUTE_MEDIA_WITH_SILENT = "config_mute_media"
        const val SETTINGS_PREFIX = "key_handler_"

        // ZEN constants
        private const val ZEN_OFFSET = 2
        private const val ZEN_PRIORITY_ONLY = 3
        private const val ZEN_TOTAL_SILENCE = 4
        private const val ZEN_ALARMS_ONLY = 5

        // Vibration effects
        private val MODE_NORMAL_EFFECT = VibrationEffect.get(VibrationEffect.EFFECT_HEAVY_CLICK)
        private val MODE_VIBRATION_EFFECT = VibrationEffect.get(VibrationEffect.EFFECT_DOUBLE_CLICK)
    }
}

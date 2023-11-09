/*
 * Copyright (C) 2021 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.device

import org.lineageos.settings.device.KeyHandler.Companion.ALERT_SLIDER_TOP_KEY
import org.lineageos.settings.device.KeyHandler.Companion.ALERT_SLIDER_MIDDLE_KEY
import org.lineageos.settings.device.KeyHandler.Companion.ALERT_SLIDER_BOTTOM_KEY
import org.lineageos.settings.device.KeyHandler.Companion.MUTE_MEDIA_WITH_SILENT
import org.lineageos.settings.device.KeyHandler.Companion.SETTINGS_PREFIX

import android.content.ContentResolver
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem

import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragment
import androidx.preference.SwitchPreference

class ButtonSettingsFragment : PreferenceFragment(), Preference.OnPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.button_panel)
        activity.actionBar!!.setDisplayHomeAsUpEnabled(true)

        val resolver: ContentResolver = getContext().getContentResolver()

        findPreference<ListPreference>(ALERT_SLIDER_TOP_KEY)?.let {
            val value = Settings.System.getInt(resolver,
                    SETTINGS_PREFIX + ALERT_SLIDER_TOP_KEY, 0)
            it.setValueIndex(value)
            it.setOnPreferenceChangeListener(this)
        }

        findPreference<ListPreference>(ALERT_SLIDER_MIDDLE_KEY)?.let {
            val value = Settings.System.getInt(resolver,
                    SETTINGS_PREFIX + ALERT_SLIDER_MIDDLE_KEY, 1)
            it.setValueIndex(value)
            it.setOnPreferenceChangeListener(this)
        }

        findPreference<ListPreference>(ALERT_SLIDER_BOTTOM_KEY)?.let {
            val value = Settings.System.getInt(resolver,
                    SETTINGS_PREFIX + ALERT_SLIDER_BOTTOM_KEY, 2)
            it.setValueIndex(value)
            it.setOnPreferenceChangeListener(this)
        }

        findPreference<SwitchPreference>(MUTE_MEDIA_WITH_SILENT)?.let {
            val value = Settings.System.getInt(resolver,
                    SETTINGS_PREFIX + MUTE_MEDIA_WITH_SILENT, 0) == 1
            it.setChecked(value)
            it.setOnPreferenceChangeListener(this)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> {
                activity.finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        val resolver: ContentResolver = getContext().getContentResolver()
        val res = when (preference.key) {
            ALERT_SLIDER_TOP_KEY -> {
                val value: Int = (newValue as String).toInt()
                Settings.System.putInt(resolver,
                        SETTINGS_PREFIX + ALERT_SLIDER_TOP_KEY, value)
                true
            }
            ALERT_SLIDER_MIDDLE_KEY -> {
                val value: Int = (newValue as String).toInt()
                Settings.System.putInt(resolver,
                        SETTINGS_PREFIX + ALERT_SLIDER_MIDDLE_KEY, value)
                true
            }
            ALERT_SLIDER_BOTTOM_KEY -> {
                val value: Int = (newValue as String).toInt()
                Settings.System.putInt(resolver,
                        SETTINGS_PREFIX + ALERT_SLIDER_BOTTOM_KEY, value)
                true
            }
            MUTE_MEDIA_WITH_SILENT -> {
                val value: Boolean = newValue as Boolean
                var valInt = 0
                if (value) valInt = 1
                Settings.System.putInt(resolver,
                        SETTINGS_PREFIX + MUTE_MEDIA_WITH_SILENT, valInt)
                true
            }
            else -> false
        }
        return res
    }
}

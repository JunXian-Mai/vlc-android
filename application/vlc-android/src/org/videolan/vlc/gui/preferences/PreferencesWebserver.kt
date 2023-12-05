/*
 * *************************************************************************
 *  PreferencesWebserver.kt
 * **************************************************************************
 *  Copyright © 2018 VLC authors and VideoLAN
 *  Author: Geoffrey Métais
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *  ***************************************************************************
 */
package org.videolan.vlc.gui.preferences

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import org.videolan.resources.WEBSERVER_ONBOARDING
import org.videolan.resources.util.restartWebserver
import org.videolan.resources.util.startWebserver
import org.videolan.resources.util.stopWebserver
import org.videolan.tools.KEY_ENABLE_WEB_SERVER
import org.videolan.tools.KEY_WEB_SERVER_ML_CONTENT
import org.videolan.tools.Settings
import org.videolan.tools.WEB_SERVER_NETWORK_BROWSER_CONTENT
import org.videolan.tools.putSingle
import org.videolan.vlc.R
import org.videolan.vlc.StartActivity
import org.videolan.vlc.util.TextUtils


class PreferencesWebserver : BasePreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var settings: SharedPreferences
    private lateinit var medialibraryContentPreference: MultiSelectListPreference

    override fun getTitleId() = R.string.remote_access

    override fun getXml() = R.xml.preferences_webserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        if(!settings.getBoolean(WEBSERVER_ONBOARDING,  false)) {
            settings.putSingle(WEBSERVER_ONBOARDING, true)
            startActivity(Intent(Intent.ACTION_VIEW).apply { setClassName(requireActivity(), WEBSERVER_ONBOARDING) })
        }
    }

    override fun onCreatePreferences(bundle: Bundle?, s: String?) {
        super.onCreatePreferences(bundle, s)
        settings = Settings.getInstance(requireActivity())
        medialibraryContentPreference = findPreference(KEY_WEB_SERVER_ML_CONTENT)!!
        manageMLContentSummary()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_webserver_onboarding) startActivity(Intent(Intent.ACTION_VIEW).apply { setClassName(requireActivity(), WEBSERVER_ONBOARDING) })
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.findItem(R.id.menu_webserver_onboarding).isVisible = true
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.menu_webserver_onboarding).isVisible = true
        super.onPrepareOptionsMenu(menu)
    }

    private fun manageMLContentSummary() {
       val value = settings.getStringSet(KEY_WEB_SERVER_ML_CONTENT, resources.getStringArray(R.array.web_server_content_values).toSet())!!
        val values = resources.getStringArray(R.array.web_server_content_values)
        val entries = resources.getStringArray(R.array.web_server_content_entries)
        val currentValues = mutableListOf<String>()
        val currentDisabledValues = mutableListOf<String>()
        value.forEach {
            currentValues.add(entries[values.indexOf(it)])
        }
        values.forEach {
            if (!value.contains(it)) currentDisabledValues.add(entries[values.indexOf(it)])
        }
        val currentString = if (currentValues.isEmpty()) "-" else TextUtils.separatedString(currentValues.toTypedArray())
        val currentDisabledString = if (currentDisabledValues.isEmpty()) "-" else TextUtils.separatedString(currentDisabledValues.toTypedArray())
        medialibraryContentPreference.summary = getString(R.string.remote_access_medialibrary_content_summary, currentString, currentDisabledString)
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        if (preference?.key == "web_server_status") {
            requireActivity().startActivity(Intent(requireActivity(), StartActivity::class.java).apply { action = "vlc.webserver.share" })
        }
        return super.onPreferenceTreeClick(preference)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            KEY_ENABLE_WEB_SERVER -> {
                val serverEnabled = sharedPreferences?.getBoolean(KEY_ENABLE_WEB_SERVER, false) ?: false
                if (serverEnabled) {
                    requireActivity().startWebserver()
                } else {
                    requireActivity().stopWebserver()
                }
            }
            WEB_SERVER_NETWORK_BROWSER_CONTENT -> {
                requireActivity().restartWebserver()
            }
            KEY_WEB_SERVER_ML_CONTENT -> {
                manageMLContentSummary()
            }
        }
    }
}
package com.habs.data.preferences

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

/** Single process-wide preferences file (also used for Google account email in calendar code). */
val Context.habsPreferencesDataStore by preferencesDataStore(name = "habs_prefs")

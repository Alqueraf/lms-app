/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.instructure.androidfoosball.utils

import android.content.Context
import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

open class PrefManager(val context: Context, val prefsFileName: String = "prefs") {
    val prefs: SharedPreferences by lazy { context.getSharedPreferences(prefsFileName, Context.MODE_PRIVATE) }
    val editor: SharedPreferences.Editor by lazy { prefs.edit() }
}

class Pref<T>(val defaultValue: T) : ReadWriteProperty<PrefManager, T> {

    @Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
    override fun getValue(thisRef: PrefManager, property: KProperty<*>): T {
        val prefs = thisRef.prefs
        return when (defaultValue) {
            is Boolean -> prefs.getBoolean(property.name, defaultValue)
            is Float -> prefs.getFloat(property.name, defaultValue)
            is Int -> prefs.getInt(property.name, defaultValue)
            is Long -> prefs.getLong(property.name, defaultValue)
            is String -> prefs.getString(property.name, defaultValue)
            else -> throw UnsupportedOperationException("Unsupported preference type ${property.javaClass} on property ${property.name}")
        } as T
    }

    override fun setValue(thisRef: PrefManager, property: KProperty<*>, value: T) {
        val editor = thisRef.editor
        when (value) {
            is Boolean -> editor.putBoolean(property.name, value)
            is Float -> editor.putFloat(property.name, value)
            is Int -> editor.putInt(property.name, value)
            is Long -> editor.putLong(property.name, value)
            is String -> editor.putString(property.name, value)
            else -> throw UnsupportedOperationException("Unsupported preference type ${property.javaClass} on property ${property.name}")
        }
        editor.commit()
    }

}

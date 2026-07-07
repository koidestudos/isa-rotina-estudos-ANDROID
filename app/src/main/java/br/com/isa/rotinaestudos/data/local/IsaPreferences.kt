package br.com.isa.rotinaestudos.data.local

import android.content.Context
import android.content.SharedPreferences

object IsaPreferences {
    private const val NAME = "isa_prefs"
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    }

    var darkTheme: Boolean?
        get() = if (prefs.contains("dark_theme")) prefs.getBoolean("dark_theme", false) else null
        set(value) = prefs.edit().apply {
            if (value == null) remove("dark_theme") else putBoolean("dark_theme", value)
        }.apply()

    var isGuest: Boolean
        get() = prefs.getBoolean("is_guest", false)
        set(value) = prefs.edit().putBoolean("is_guest", value).apply()

    var guestName: String
        get() = prefs.getString("guest_name", "Visitante") ?: "Visitante"
        set(value) = prefs.edit().putString("guest_name", value).apply()

    var guestHasRoutine: Boolean
        get() = prefs.getBoolean("guest_has_routine", false)
        set(value) = prefs.edit().putBoolean("guest_has_routine", value).apply()

    fun clearGuest() {
        prefs.edit()
            .remove("is_guest")
            .remove("guest_name")
            .remove("guest_has_routine")
            .remove("guest_coins")
            .remove("guest_streak")
            .remove("guest_last_complete")
            .apply()
    }

    fun getGuestInt(key: String, default: Int = 0) = prefs.getInt(key, default)
    fun setGuestInt(key: String, value: Int) = prefs.edit().putInt(key, value).apply()
    fun getGuestString(key: String) = prefs.getString(key, "") ?: ""
    fun setGuestString(key: String, value: String) = prefs.edit().putString(key, value).apply()
}

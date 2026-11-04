package org.slauridev.brightnessfix

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("brightness_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("override", false)) {
            val serviceIntent = Intent(context, BrightnessService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
        }
    }
}
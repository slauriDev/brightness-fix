package org.slauridev.brightnessfix

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat

class BrightnessService : Service() {

    private val brightnessPath = "/sys/class/leds/lcd-backlight/brightness"
    private val targetFile = "/data/local/tmp/.brightness_target"
    private val handler = Handler(Looper.getMainLooper())

    private val runnable = object : Runnable {
        override fun run() {
            try {
                Runtime.getRuntime().exec(arrayOf("su", "-c", "cat $targetFile > $brightnessPath"))
            } catch (_: Exception) {}
            handler.postDelayed(this, 300)
        }
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel("bf", "Brightness", NotificationManager.IMPORTANCE_LOW)
            )
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, NotificationCompat.Builder(this, "bf")
            .setContentTitle("Brightness Fix")
            .setContentText("Яркость зафиксирована")
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build())
        handler.post(runnable)
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(runnable)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
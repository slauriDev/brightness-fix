package org.zxcslauri.brightnessfix

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import com.topjohnwu.superuser.Shell
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val brightnessPath = "/sys/class/leds/lcd-backlight/brightness"
    private val targetFile = "/data/local/tmp/.brightness_target"
    private val prefs by lazy { getSharedPreferences("brightness_prefs", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val slider = findViewById<Slider>(R.id.sliderBrightness)
        val switch = findViewById<MaterialSwitch>(R.id.switchOverride)
        val tvValue = findViewById<TextView>(R.id.tvValue)

        try {
            val p = Runtime.getRuntime().exec("su")
            p.outputStream.write("exit\n".toByteArray())
            p.outputStream.flush()
            p.waitFor()
        } catch (e: IOException) {
            tvValue.text = "Нет root"
            Toast.makeText(this, "Root недоступен", Toast.LENGTH_LONG).show()
            return
        } catch (e: InterruptedException) {
            tvValue.text = "Ошибка"
            return
        }

        val savedPercent = prefs.getFloat("brightness_percent", 100f)
        slider.value = savedPercent
        updateLabel(savedPercent, tvValue)

        slider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                val realValue = (value * 4095 / 100).toInt()
                prefs.edit().putFloat("brightness_percent", value).apply()
                updateLabel(value, tvValue)
                setBrightness(realValue)
            }
        }

        switch.isChecked = prefs.getBoolean("override", false)
        switch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("override", isChecked).apply()
            if (isChecked) {
                startOverrideService()
            } else {
                stopOverrideService()
            }
        }

        if (switch.isChecked) startOverrideService()
    }

    private fun updateLabel(percent: Float, tv: TextView) {
        tv.text = "${percent.toInt()}%"
    }

    private fun setBrightness(value: Int) {
        val safeValue = if (value < 1) 1 else value
        Shell.cmd("echo $safeValue > $brightnessPath").submit()
        Shell.cmd("echo $safeValue > $targetFile").submit()
    }

    private fun startOverrideService() {
        val intent = Intent(this, BrightnessService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    private fun stopOverrideService() {
        stopService(Intent(this, BrightnessService::class.java))
    }
}
package org.zxcslauri.brightnessfix

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import com.topjohnwu.superuser.Shell
import java.io.BufferedReader
import java.io.InputStreamReader

class HomeFragment : Fragment() {

    private val brightnessPath = "/sys/class/leds/lcd-backlight/brightness"
    private val targetFile = "/data/local/tmp/.brightness_target"
    private var maxBrightness = 4095
    private var hasRoot = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val slider = view.findViewById<Slider>(R.id.sliderBrightness)
        val switch = view.findViewById<MaterialSwitch>(R.id.switchOverride)
        val tvValue = view.findViewById<TextView>(R.id.tvValue)
        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
        val dimOverlay = view.findViewById<View>(R.id.dimOverlay)
        val noRootBanner = view.findViewById<MaterialCardView>(R.id.noRootBanner)
        val btnDismiss = view.findViewById<MaterialButton>(R.id.btnDismiss)
        val prefs = requireActivity().getSharedPreferences("brightness_prefs", 0)

        // Проверка root через выполнение реальной команды
        try {
            val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val reader = BufferedReader(InputStreamReader(p.inputStream))
            val output = reader.readLine()
            p.waitFor()
            // Если в выводе есть "uid=0" — root есть
            hasRoot = output != null && output.contains("uid=0")
        } catch (e: Exception) {
            hasRoot = false
        }

        if (!hasRoot) {
            dimOverlay.visibility = View.VISIBLE
            noRootBanner.visibility = View.VISIBLE

            slider.isEnabled = false
            switch.isEnabled = false
            for (i in 0 until radioGroup.childCount) {
                radioGroup.getChildAt(i).isEnabled = false
            }

            btnDismiss.setOnClickListener {
                dimOverlay.visibility = View.GONE
                noRootBanner.visibility = View.GONE
            }
            return
        }

        // Если root есть
        maxBrightness = prefs.getInt("max_brightness", 4095)
        if (maxBrightness == 255) {
            view.findViewById<com.google.android.material.radiobutton.MaterialRadioButton>(R.id.radio255).isChecked = true
        } else {
            view.findViewById<com.google.android.material.radiobutton.MaterialRadioButton>(R.id.radio4095).isChecked = true
        }

        val savedPercent = prefs.getFloat("brightness_percent", 100f)
        slider.value = savedPercent
        tvValue.text = "${savedPercent.toInt()}%"

        slider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                prefs.edit().putFloat("brightness_percent", value).apply()
                tvValue.text = "${value.toInt()}%"
                setBrightness(value)
            }
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            maxBrightness = if (checkedId == R.id.radio255) 255 else 4095
            prefs.edit().putInt("max_brightness", maxBrightness).apply()
            setBrightness(slider.value)
        }

        switch.isChecked = prefs.getBoolean("override", false)
        switch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("override", isChecked).apply()
            if (isChecked) startService() else stopService()
        }

        if (switch.isChecked) startService()
    }

    private fun setBrightness(percent: Float) {
        val realValue = (percent * maxBrightness / 100).toInt()
        val safe = if (realValue < 1) 1 else realValue
        Shell.cmd("echo $safe > $brightnessPath").submit()
        Shell.cmd("echo $safe > $targetFile").submit()
    }

    private fun startService() {
        ContextCompat.startForegroundService(
            requireActivity(),
            Intent(requireActivity(), BrightnessService::class.java)
        )
    }

    private fun stopService() {
        requireActivity().stopService(Intent(requireActivity(), BrightnessService::class.java))
    }
}
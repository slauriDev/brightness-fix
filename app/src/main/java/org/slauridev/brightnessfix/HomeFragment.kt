package org.slauridev.brightnessfix

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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

        applyTheme(view)

        val slider = view.findViewById<SeekBar>(R.id.sliderBrightness)
        val switch = view.findViewById<Switch>(R.id.switchOverride)
        val tvValue = view.findViewById<TextView>(R.id.tvValue)
        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
        val dimOverlay = view.findViewById<View>(R.id.dimOverlay)
        val noRootBanner = view.findViewById<LinearLayout>(R.id.noRootBanner)
        val btnDismiss = view.findViewById<Button>(R.id.btnDismiss)
        val prefs = requireActivity()
            .getSharedPreferences("brightness_prefs", 0)

        try {
            val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val reader = BufferedReader(InputStreamReader(p.inputStream))
            val output = reader.readLine()
            p.waitFor()

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

        maxBrightness = prefs.getInt("max_brightness", 4095)

        if (maxBrightness == 255) {
            view.findViewById<RadioButton>(R.id.radio255).isChecked = true
        } else {
            view.findViewById<RadioButton>(R.id.radio4095).isChecked = true
        }

        val savedPercent = prefs.getFloat("brightness_percent", 100f)

        slider.progress = savedPercent.toInt()
        tvValue.text = "${savedPercent.toInt()}%"

        slider.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        val value = progress.toFloat()

                        prefs.edit()
                            .putFloat("brightness_percent", value)
                            .apply()

                        tvValue.text = "${progress}%"
                        setBrightness(value)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            }
        )

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            maxBrightness = if (checkedId == R.id.radio255) {
                255
            } else {
                4095
            }

            prefs.edit()
                .putInt("max_brightness", maxBrightness)
                .apply()

            setBrightness(slider.progress.toFloat())
        }

        switch.isChecked = prefs.getBoolean("override", false)

        switch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit()
                .putBoolean("override", isChecked)
                .apply()

            if (isChecked) {
                startService()
            } else {
                stopService()
            }
        }

        if (switch.isChecked) {
            startService()
        }
    }

    private fun applyTheme(view: View) {
        val colors = ThemeManager.getColors(requireContext())

        view.setBackgroundColor(colors.background)

        fun applyColors(currentView: View) {
            when (currentView) {
                is TextView -> {
                    currentView.setTextColor(
                        if (currentView.id == R.id.tvValue) {
                            colors.text
                        } else {
                            colors.textSecondary
                        }
                    )
                }

                is LinearLayout -> {
                    currentView.setBackgroundColor(colors.background)
                }
            }

            if (currentView is ViewGroup) {
                for (i in 0 until currentView.childCount) {
                    applyColors(currentView.getChildAt(i))
                }
            }
        }

        applyColors(view)
    }

    private fun updateTextColors(
        view: View,
        primary: Int,
        secondary: Int
    ) {
        val textViews = ArrayList<TextView>()

        findTextViews(view, textViews)

        for (tv in textViews) {
            when (tv.id) {
                R.id.tvValue -> tv.setTextColor(primary)
                else -> tv.setTextColor(secondary)
            }
        }
    }

    private fun findTextViews(
        view: View,
        list: ArrayList<TextView>
    ) {
        if (view is TextView) {
            list.add(view)
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                findTextViews(view.getChildAt(i), list)
            }
        }
    }

    private fun setBrightness(percent: Float) {
        val realValue =
            (percent * maxBrightness / 100).toInt()

        val safe = if (realValue < 1) 1 else realValue

        Shell.cmd(
            "echo $safe > $brightnessPath"
        ).submit()

        Shell.cmd(
            "echo $safe > $targetFile"
        ).submit()

        Logger.i(
            requireContext(),
            "Brightness set to $safe (${percent.toInt()}%, range 0-$maxBrightness)"
        )
    }

    private fun startService() {
        ContextCompat.startForegroundService(
            requireActivity(),
            Intent(
                requireActivity(),
                BrightnessService::class.java
            )
        )
    }

    private fun stopService() {
        requireActivity().stopService(
            Intent(
                requireActivity(),
                BrightnessService::class.java
            )
        )
    }
}
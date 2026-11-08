package org.slauridev.brightnessfix

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.topjohnwu.superuser.Shell
import android.content.SharedPreferences
import android.widget.ImageButton
import android.widget.Toast

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
        val lockButton = view.findViewById<ImageButton>(R.id.btnOverride)
        val tvValue = view.findViewById<TextView>(R.id.tvValue)
        val btnRange255 = view.findViewById<Button>(R.id.btnRange255)
        val btnRange4095 = view.findViewById<Button>(R.id.btnRange4095)
        val dimOverlay = view.findViewById<View>(R.id.dimOverlay)
        val noRootBanner = view.findViewById<LinearLayout>(R.id.noRootBanner)
        val btnDismiss = view.findViewById<Button>(R.id.btnDismiss)
        val prefs = requireActivity()
            .getSharedPreferences("brightness_prefs", 0)

            (requireActivity() as MainActivity).checkRootOnce { rootAvailable ->

                // Проверяем, что View фрагмента всё ещё существует
                if (!isAdded || this@HomeFragment.view !== view) {
                    return@checkRootOnce
                }
            
                hasRoot = rootAvailable
            
                if (!rootAvailable) {
                    dimOverlay.visibility = View.VISIBLE
                    noRootBanner.visibility = View.VISIBLE

                    btnRange255.isEnabled = false
                    btnRange4095.isEnabled = false
            
                    slider.isEnabled = false
                    lockButton.isEnabled = false
            
                    btnDismiss.setOnClickListener {
                        dimOverlay.visibility = View.GONE
                        noRootBanner.visibility = View.GONE
                    }
            
                    return@checkRootOnce
                }
            
                setupBrightnessControls(
                    slider,
                    lockButton,
                    tvValue,
                    btnRange255,
                    btnRange4095,
                    prefs
                )
            }
        }

        private fun setupBrightnessControls(
            slider: SeekBar,
            lockButton: ImageButton,
            tvValue: TextView,
            btnRange255: Button,
            btnRange4095: Button,
            prefs: SharedPreferences
        ) {
            maxBrightness = prefs.getInt("max_brightness", 4095)

            btnRange255.isSelected = maxBrightness == 255
            btnRange4095.isSelected = maxBrightness == 4095

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

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                }
            )

            fun selectRange(value: Int) {
                maxBrightness = value

                btnRange255.isSelected = value == 255
                btnRange4095.isSelected = value == 4095

                prefs.edit()
                    .putInt("max_brightness", value)
                    .apply()

                setBrightness(slider.progress.toFloat())
            }

            btnRange255.setOnClickListener {
                if (maxBrightness != 255) {
                    selectRange(255)
                }
            }

            btnRange4095.setOnClickListener {
                if (maxBrightness != 4095) {
                    selectRange(4095)
                }
            }

            val isLocked = prefs.getBoolean("override", false)

            lockButton.isSelected = isLocked

            lockButton.setOnClickListener {
                val newState = !lockButton.isSelected

                lockButton.isSelected = newState

                prefs.edit()
                    .putBoolean("override", newState)
                    .apply()

                if (newState) {
                    startService()

                    Toast.makeText(
                        requireContext(),
                        "Яркость зафиксирована",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    stopService()
                }
            }

            if (isLocked) {
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
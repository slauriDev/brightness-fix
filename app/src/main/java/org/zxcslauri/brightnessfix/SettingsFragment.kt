package org.zxcslauri.brightnessfix

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyTheme(view)

        val prefs = requireActivity().getSharedPreferences("app_prefs", 0)

        // Язык
        val cardLanguage = view.findViewById<LinearLayout>(R.id.cardLanguage)
        val tvLanguage = view.findViewById<TextView>(R.id.tvLanguageValue)
        val lang = prefs.getString("language", "system")
        tvLanguage.text = when (lang) {
            "en" -> getString(R.string.language_english)
            "ru" -> getString(R.string.language_russian)
            else -> getString(R.string.language_english)
        }
        cardLanguage.setOnClickListener { showLanguageDialog(lang, tvLanguage, prefs) }

        // Контрастность
        val cardContrast = view.findViewById<LinearLayout>(R.id.cardContrast)
        val tvContrast = view.findViewById<TextView>(R.id.tvContrastValue)
        val contrast = prefs.getString("contrast", "medium")
        tvContrast.text = when (contrast) {
            "normal" -> getString(R.string.contrast_normal)
            "high" -> getString(R.string.contrast_high)
            else -> getString(R.string.contrast_medium)
        }
        cardContrast.setOnClickListener { showContrastDialog(contrast, tvContrast, prefs) }

        // Тема
        val cardTheme = view.findViewById<LinearLayout>(R.id.cardTheme)
        val tvTheme = view.findViewById<TextView>(R.id.tvThemeValue)
        val theme = prefs.getString("theme", "amoled")
        tvTheme.text = when (theme) {
            "light" -> getString(R.string.theme_light)
            "dark" -> getString(R.string.theme_dark)
            else -> getString(R.string.theme_amoled)
        }
        cardTheme.setOnClickListener { showThemeDialog(theme, tvTheme, prefs) }

        // Логи
        val cardLogs = view.findViewById<LinearLayout>(R.id.cardLogs)
        cardLogs.setOnClickListener {
            startActivity(android.content.Intent(requireActivity(), LogsActivity::class.java))
        }
    }

    private fun applyTheme(view: View) {
        val prefs = requireActivity().getSharedPreferences("app_prefs", 0)
        val theme = prefs.getString("theme", "amoled")
        val contrast = prefs.getString("contrast", "medium")

        val bgColor = when (theme) {
            "light" -> 0xFFFFFFFF.toInt()
            "amoled" -> 0xFF000000.toInt()
            else -> 0xFF1C1B1F.toInt()
        }
        val alpha = when (contrast) {
            "normal" -> 0.75f
            "high" -> 1.0f
            else -> 0.88f
        }

        view.setBackgroundColor(bgColor)
        view.alpha = alpha
    }

    private fun showLanguageDialog(currentLang: String?, tv: TextView, prefs: android.content.SharedPreferences) {
        val dialog = Dialog(requireContext())
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_language, null)
        dialog.setContentView(view)

        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroupLanguage)
        if (currentLang == "ru") {
            view.findViewById<RadioButton>(R.id.radioRu).isChecked = true
        } else {
            view.findViewById<RadioButton>(R.id.radioEn).isChecked = true
        }

        view.findViewById<Button>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
        view.findViewById<Button>(R.id.btnApply).setOnClickListener {
            val code = if (radioGroup.checkedRadioButtonId == R.id.radioRu) "ru" else "en"
            prefs.edit().putString("language", code).apply()
            tv.text = when (code) {
                "en" -> getString(R.string.language_english)
                "ru" -> getString(R.string.language_russian)
                else -> getString(R.string.language_english)
            }
            requireActivity().recreate()
            dialog.dismiss()
        }
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun showContrastDialog(current: String?, tv: TextView, prefs: android.content.SharedPreferences) {
        val dialog = Dialog(requireContext())
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_radio, null)
        dialog.setContentView(view)

        val title = view.findViewById<TextView>(R.id.dialogTitle)
        title.text = getString(R.string.contrast_title)

        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
        val radioNormal = view.findViewById<RadioButton>(R.id.radio1)
        val radioMedium = view.findViewById<RadioButton>(R.id.radio2)
        val radioHigh = view.findViewById<RadioButton>(R.id.radio3)

        radioNormal.text = getString(R.string.contrast_normal)
        radioMedium.text = getString(R.string.contrast_medium)
        radioHigh.text = getString(R.string.contrast_high)
        radioHigh.visibility = View.VISIBLE

        when (current) {
            "normal" -> radioNormal.isChecked = true
            "high" -> radioHigh.isChecked = true
            else -> radioMedium.isChecked = true
        }

        view.findViewById<Button>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
        view.findViewById<Button>(R.id.btnApply).setOnClickListener {
            val value = when (radioGroup.checkedRadioButtonId) {
                R.id.radio1 -> "normal"
                R.id.radio3 -> "high"
                else -> "medium"
            }
            prefs.edit().putString("contrast", value).apply()
            tv.text = when (value) {
                "normal" -> getString(R.string.contrast_normal)
                "high" -> getString(R.string.contrast_high)
                else -> getString(R.string.contrast_medium)
            }
            requireActivity().recreate()
            dialog.dismiss()
        }
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun showThemeDialog(current: String?, tv: TextView, prefs: android.content.SharedPreferences) {
        val dialog = Dialog(requireContext())
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_radio, null)
        dialog.setContentView(view)

        val title = view.findViewById<TextView>(R.id.dialogTitle)
        title.text = getString(R.string.theme_title)

        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
        val radioLight = view.findViewById<RadioButton>(R.id.radio1)
        val radioDark = view.findViewById<RadioButton>(R.id.radio2)
        val radioAmoled = view.findViewById<RadioButton>(R.id.radio3)

        radioLight.text = getString(R.string.theme_light)
        radioDark.text = getString(R.string.theme_dark)
        radioAmoled.text = getString(R.string.theme_amoled)
        radioAmoled.visibility = View.VISIBLE

        when (current) {
            "light" -> radioLight.isChecked = true
            "dark" -> radioDark.isChecked = true
            else -> radioAmoled.isChecked = true
        }

        view.findViewById<Button>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
        view.findViewById<Button>(R.id.btnApply).setOnClickListener {
            val value = when (radioGroup.checkedRadioButtonId) {
                R.id.radio1 -> "light"
                R.id.radio2 -> "dark"
                else -> "amoled"
            }
            prefs.edit().putString("theme", value).apply()
            requireActivity().recreate()
            dialog.dismiss()
        }
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }
}
package org.slauridev.brightnessfix

import android.app.Dialog
import android.graphics.drawable.GradientDrawable
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
        val colors = ThemeManager.getColors(requireContext())

        view.setBackgroundColor(colors.background)

        fun applyTextColors(currentView: View) {
            if (currentView is TextView) {
                when (currentView.id) {
                    R.id.tvLanguageValue,
                    R.id.tvThemeValue -> {
                        currentView.setTextColor(colors.textSecondary)
                    }

                    else -> {
                        currentView.setTextColor(colors.text)
                    }
                }
            }

            if (currentView is ViewGroup) {
                for (i in 0 until currentView.childCount) {
                    applyTextColors(currentView.getChildAt(i))
                }
            }
        }

        applyTextColors(view)
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
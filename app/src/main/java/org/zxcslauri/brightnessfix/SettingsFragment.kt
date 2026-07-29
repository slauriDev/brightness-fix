package org.zxcslauri.brightnessfix

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

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

        val cardLanguage = view.findViewById<MaterialCardView>(R.id.cardLanguage)
        val tvLanguage = view.findViewById<TextView>(R.id.tvLanguageValue)

        val prefs = requireActivity().getSharedPreferences("app_prefs", 0)
        val lang = prefs.getString("language", "system")

        tvLanguage.text = when (lang) {
            "en" -> getString(R.string.language_english)
            "ru" -> getString(R.string.language_russian)
            else -> getString(R.string.language_english)
        }

        cardLanguage.setOnClickListener {
            showLanguageDialog(lang, tvLanguage, prefs)
        }
    }

    private fun showLanguageDialog(currentLang: String?, tvLanguage: TextView, prefs: android.content.SharedPreferences) {
        val dialog = Dialog(requireContext())
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_language, null)
        dialog.setContentView(dialogView)

        // Установить текущий выбор
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radioGroupLanguage)
        if (currentLang == "ru") {
            dialogView.findViewById<com.google.android.material.radiobutton.MaterialRadioButton>(R.id.radioRu).isChecked = true
        } else {
            dialogView.findViewById<com.google.android.material.radiobutton.MaterialRadioButton>(R.id.radioEn).isChecked = true
        }

        dialogView.findViewById<MaterialButton>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<MaterialButton>(R.id.btnApply).setOnClickListener {
            val code = if (radioGroup.checkedRadioButtonId == R.id.radioRu) "ru" else "en"
            prefs.edit().putString("language", code).apply()

            tvLanguage.text = when (code) {
                "en" -> getString(R.string.language_english)
                "ru" -> getString(R.string.language_russian)
                else -> getString(R.string.language_english)
            }

            val localeList = LocaleListCompat.forLanguageTags(code)
            AppCompatDelegate.setApplicationLocales(localeList)
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }
}
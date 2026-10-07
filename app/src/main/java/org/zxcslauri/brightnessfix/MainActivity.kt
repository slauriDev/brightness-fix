package org.zxcslauri.brightnessfix

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var navHome: LinearLayout
    private lateinit var navHelp: LinearLayout
    private lateinit var navSettings: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)

        val colors = ThemeManager.getColors(this)

        val lang = prefs.getString("language", "system")
        if (lang != null && lang != "system") {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(lang)
            )
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.decorView.setBackgroundColor(colors.background)
        findViewById<TextView>(R.id.tvTitle).setBackgroundColor(colors.surface)
        findViewById<TextView>(R.id.tvTitle).setTextColor(colors.text)
        findViewById<View>(R.id.bottomNav).setBackgroundColor(colors.surfaceAlt)

        tvTitle = findViewById(R.id.tvTitle)

        navHome = findViewById(R.id.navHome)
        navHelp = findViewById(R.id.navHelp)
        navSettings = findViewById(R.id.navSettings)

        navHome.setOnClickListener {
            showFragment(HomeFragment(), getString(R.string.app_name))
        }

        navHelp.setOnClickListener {
            showFragment(HelpFragment(), getString(R.string.help_title))
        }

        navSettings.setOnClickListener {
            showFragment(SettingsFragment(), getString(R.string.settings_title))
        }

        if (savedInstanceState == null) {
            showFragment(HomeFragment(), getString(R.string.app_name))
        }

    }

    private fun showFragment(fragment: Fragment, title: String) {
        tvTitle.text = title

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    companion object {
        fun applyTheme(theme: String?) {
            when (theme) {
                "light" -> {
                    AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_NO
                    )
                }
            }
        }
    }
}
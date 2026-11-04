
package org.slauridev.brightnessfix

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import android.view.ViewGroup

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)

        val themeStyle = when (prefs.getString("theme", "dark")) {
            "light" -> R.style.Theme_BrightnessFix_Light
            "amoled" -> R.style.Theme_BrightnessFix_AMOLED
            else -> R.style.Theme_BrightnessFix_Dark
        }

        setTheme(themeStyle)

        val lang = prefs.getString("language", "system")
        if (lang != null && lang != "system") {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(lang)
            )
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeFragment())
                .commit()
        }

        supportActionBar?.title = getString(R.string.app_name)
        updateActionBar()

        supportFragmentManager.addOnBackStackChangedListener {
            updateActionBar()
        }
    }

    private fun updateActionBar() {
        val isSettings = supportFragmentManager.backStackEntryCount > 0

        supportActionBar?.setDisplayHomeAsUpEnabled(isSettings)
        supportActionBar?.setDisplayShowHomeEnabled(isSettings)

        supportActionBar?.title = if (isSettings) {
            getString(R.string.settings_title)
        } else {
            getString(R.string.app_name)
        }

        invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (supportFragmentManager.backStackEntryCount == 0) {
            menuInflater.inflate(R.menu.menu_main, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }

            R.id.action_settings -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, SettingsFragment())
                    .addToBackStack("settings")
                    .commit()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}

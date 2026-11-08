package org.slauridev.brightnessfix

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {

    companion object {
        private const val GITHUB_REPO =
            "https://github.com/slauriDev/brightness-fix"

        private const val GITHUB_PROFILE =
            "https://github.com/slauriDev"

        private const val FORUM_URL =
            "https://4pda.to/forum/index.php?showtopic=1124534"

        private const val TELEGRAM_URL =
            "https://t.me/brightnessfix"

        private const val LICENSE_URL =
            "https://github.com/slauriDev/brightness-fix/LICENSE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_BrightnessFix_Dark)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_about)

        findViewById<TextView>(R.id.tvVersion).text =
            getString(R.string.about_version, BuildConfig.VERSION_NAME)

        findViewById<View>(R.id.btnSource).setOnClickListener {
            openUrl(GITHUB_REPO)
        }

        findViewById<View>(R.id.developerCard).setOnClickListener {
            openUrl(GITHUB_PROFILE)
        }

        findViewById<View>(R.id.btnForum).setOnClickListener {
            openUrl(FORUM_URL)
        }

        findViewById<View>(R.id.btnTelegram).setOnClickListener {
            if (TELEGRAM_URL.contains("REPLACE_WITH_CHANNEL")) {
                Toast.makeText(
                    this,
                    R.string.about_telegram_not_configured,
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                openUrl(TELEGRAM_URL)
            }
        }

        findViewById<View>(R.id.tvLicense).setOnClickListener {
            openUrl(LICENSE_URL)
        }
    }

    private fun openUrl(url: String) {
        try {
            startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url))
            )
        } catch (e: Exception) {
            Toast.makeText(
                this,
                R.string.about_link_error,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

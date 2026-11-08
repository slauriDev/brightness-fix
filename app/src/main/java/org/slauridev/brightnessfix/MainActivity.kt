
package org.slauridev.brightnessfix

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.view.updateLayoutParams
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.core.view.doOnNextLayout
import android.os.Handler
import android.os.Looper
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var btnHome: View
    private lateinit var btnLogs: View
    private lateinit var btnLanguage: View
    private lateinit var btnAbout: View

    private lateinit var navigationGroup: FrameLayout
    private lateinit var navSelection: View
    private lateinit var tvHomeLabel: TextView
    private lateinit var tvLogsLabel: TextView
    private lateinit var ivHomeIcon: ImageView
    private lateinit var ivLogsIcon: ImageView

    private val homeFragment = HomeFragment()
    private val logsFragment = LogsFragment()

    private var hasRoot: Boolean? = null

    private val rootCheckExecutor = Executors.newSingleThreadExecutor()

    private val rootCheckHandler = Handler(Looper.getMainLooper())

    fun checkRootOnce(onResult: (Boolean) -> Unit) {
        hasRoot?.let {
            onResult(it)
            return
        }

        rootCheckExecutor.execute {
            val result = try {
                val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
                val output = BufferedReader(
                    InputStreamReader(process.inputStream)
                ).use { it.readLine() }

                val exitCode = process.waitFor()
                exitCode == 0 && output?.contains("uid=0") == true
            } catch (e: Exception) {
                false
            }

            rootCheckHandler.post {
                hasRoot = result
                onResult(result)
            }
        }
    }

    private var isHomeVisuallyActive = true

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)

        setTheme(R.style.Theme_BrightnessFix_Dark)

        val language = prefs.getString("language", "system") ?: "system"

        AppCompatDelegate.setApplicationLocales(
            if (language == "system") {
                LocaleListCompat.getEmptyLocaleList()
            } else {
                LocaleListCompat.forLanguageTags(language)
            }
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnHome = findViewById(R.id.btnHome)
        btnLogs = findViewById(R.id.btnLogs)
        btnLanguage = findViewById(R.id.btnLanguage)
        btnAbout = findViewById(R.id.btnAbout)

        navigationGroup = findViewById(R.id.navigationGroup)
        navSelection = findViewById(R.id.navSelection)
        tvHomeLabel = findViewById(R.id.tvHomeLabel)
        tvLogsLabel = findViewById(R.id.tvLogsLabel)

        ivHomeIcon = findViewById(R.id.ivHomeIcon)
        ivLogsIcon = findViewById(R.id.ivLogsIcon)

        btnHome.setOnClickListener {
            showFragment(homeFragment)
        }

        btnLogs.setOnClickListener {
            showFragment(logsFragment)
        }

        btnLanguage.setOnClickListener {
            toggleLanguage()
        }

        btnAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        if (savedInstanceState == null) {
            showFragment(homeFragment, false)

            navigationGroup.post {
                updateNavigation(homeFragment, false)
            }
        } else {
            navigationGroup.post {
                val current = supportFragmentManager
                    .findFragmentById(R.id.fragmentContainer)

                if (current != null) {
                    updateNavigation(current, false)
                }
            }
        }
    }

    private fun installSelectionSwipe(view: View) {
        val touchSlop = ViewConfiguration.get(this).scaledTouchSlop
        val swipeThreshold = 48 * resources.displayMetrics.density
    
        var startX = 0f
        var startY = 0f
        var isSwiping = false
        var hasMoved = false
    
        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    startY = event.y
                    isSwiping = false
                    hasMoved = false
                    true
                }
    
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.x - startX
                    val dy = event.y - startY
                
                    if (kotlin.math.abs(dx) > touchSlop ||
                        kotlin.math.abs(dy) > touchSlop
                    ) {
                        hasMoved = true
                    }
                
                    if (kotlin.math.abs(dx) > touchSlop &&
                        kotlin.math.abs(dx) > kotlin.math.abs(dy)
                    ) {
                        isSwiping = true
                    }
                
                    if (isSwiping) {
                        val maxX = navigationGroup.width -
                                navSelection.width -
                                navigationGroup.paddingLeft -
                                navigationGroup.paddingRight
                
                        val targetX = (navSelection.translationX + dx)
                            .coerceIn(0f, maxX.toFloat())
                
                            navSelection.translationX = targetX

                            val midpoint = (btnHome.left + btnLogs.left) / 2f
                            updateNavigationLabels(
                                navSelection.translationX < midpoint
                            )
                            
                            startX = event.x
                    }
                
                    true
                }
    
                MotionEvent.ACTION_UP -> {
                    if (isSwiping) {
                        val midpoint = (btnHome.left + btnLogs.left) / 2f
                
                        val targetFragment = if (
                            navSelection.translationX >= midpoint
                        ) {
                            logsFragment
                        } else {
                            homeFragment
                        }
                
                        val currentFragment = supportFragmentManager
                            .findFragmentById(R.id.fragmentContainer)
                
                        if (currentFragment === targetFragment) {
                            updateNavigation(targetFragment, true)
                        } else {
                            showFragment(targetFragment)
                        }
                    } else if (!hasMoved) {
                        v.performClick()
                    }
                
                    true
                }
    
                MotionEvent.ACTION_CANCEL -> {
                    val currentFragment = supportFragmentManager
                        .findFragmentById(R.id.fragmentContainer)
                
                    if (currentFragment != null) {
                        updateNavigation(currentFragment, true)
                    }
                
                    true
                }
    
                else -> true
            }
        }
    }

    private fun showFragment(
        fragment: Fragment,
        animate: Boolean = true
    ) {
        if (supportFragmentManager.findFragmentById(
                R.id.fragmentContainer
            ) === fragment
        ) return

        updateNavigation(fragment, animate)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun updateNavigationLabels(isHome: Boolean) {
        if (isHomeVisuallyActive == isHome &&
            (if (isHome) tvHomeLabel else tvLogsLabel).visibility == View.VISIBLE
        ) return
    
        // Save current icon positions before layout changes
        val homeIconX = ivHomeIcon.x
        val logsIconX = ivLogsIcon.x
    
        // Cancel previous animations
        ivHomeIcon.animate().cancel()
        ivLogsIcon.animate().cancel()
        tvHomeLabel.animate().cancel()
        tvLogsLabel.animate().cancel()
    
        isHomeVisuallyActive = isHome
    
        val activeLabel = if (isHome) tvHomeLabel else tvLogsLabel
        val inactiveLabel = if (isHome) tvLogsLabel else tvHomeLabel
    
        // Hide inactive label
        inactiveLabel.visibility = View.GONE
        inactiveLabel.alpha = 0f
        inactiveLabel.translationX = 0f
    
        // Prepare active label
        activeLabel.visibility = View.VISIBLE
        activeLabel.alpha = 0f
        activeLabel.translationX = -12f
    
        // Wait for layout and compensate icon position changes
        navigationGroup.doOnNextLayout {
            val homeOffset = homeIconX - ivHomeIcon.x
            val logsOffset = logsIconX - ivLogsIcon.x

            ivHomeIcon.translationX += homeOffset
            ivLogsIcon.translationX += logsOffset

            ivHomeIcon.animate()
                .translationX(0f)
                .setDuration(300)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()

            ivLogsIcon.animate()
                .translationX(0f)
                .setDuration(300)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }
    
        // Reveal active label
        activeLabel.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(300)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun updateNavigation(
        fragment: Fragment,
        animate: Boolean
    ) {
        val isHome = fragment is HomeFragment

        val activeButton = if (isHome) btnHome else btnLogs
        val activeLabel = if (isHome) tvHomeLabel else tvLogsLabel
        val inactiveLabel = if (isHome) tvLogsLabel else tvHomeLabel
        installSelectionSwipe(activeButton)

        run {
            val targetX = activeButton.left.toFloat()
            val targetWidth = activeButton.width

            if (targetWidth == 0) return@run

            // Cancel previous animations
            navSelection.animate().cancel()
            tvHomeLabel.animate().cancel()
            tvLogsLabel.animate().cancel()

            updateNavigationLabels(isHome)

            // Move selection
            navSelection.layoutParams =
                navSelection.layoutParams.apply {
                    width = targetWidth
                }

            navSelection.animate()
                .translationX(targetX)
                .setDuration(if (animate) 360 else 0)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()

            // Reveal label
            if (animate) {
                activeLabel.animate()
                    .alpha(1f)
                    .translationX(0f)
                    .setDuration(300)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
            }
        }
    }

    private fun toggleLanguage() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)

        val currentLanguage = prefs.getString("language", "system")
        val newLanguage = if (currentLanguage == "ru") "en" else "ru"

        prefs.edit()
            .putString("language", newLanguage)
            .apply()

        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(newLanguage)
        )

        val message = if (newLanguage == "ru") {
            getString(R.string.language_selected_ru)
        } else {
            getString(R.string.language_selected_en)
        }

        android.widget.Toast.makeText(
            this,
            message,
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}

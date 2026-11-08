package org.slauridev.brightnessfix

import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class LogsFragment : Fragment() {

    private val logFile: File
        get() = File(requireContext().filesDir, "app.log")

    private val handler = Handler(Looper.getMainLooper())

    private var clearConfirmActive = false
    private var widthAnimator: ValueAnimator? = null
    private var clearButton: LinearLayout? = null
    private var clearIcon: ImageView? = null
    private var confirmText: TextView? = null
    private var logsText: TextView? = null

    private val resetConfirmation = Runnable {
        resetClearButton()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_logs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        clearButton = view.findViewById(R.id.btnClearContainer)
        clearIcon = view.findViewById(R.id.ivClearIcon)
        confirmText = view.findViewById(R.id.tvClearConfirm)
        logsText = view.findViewById(R.id.tvLogs)

        val btnShare = view.findViewById<ImageButton>(R.id.btnShare)

        clearButton?.setOnClickListener {
            if (clearConfirmActive) {
                clearLogs()
            } else {
                showClearConfirmation()
            }
        }

        btnShare.setOnClickListener {
            shareLogs()
        }

        // Нажатие на область логов отменяет подтверждение.
        view.findViewById<View>(R.id.logsScrollView).setOnClickListener {
            if (clearConfirmActive) resetClearButton()
        }

        logsText?.setOnClickListener {
            if (clearConfirmActive) resetClearButton()
        }
    }

    override fun onResume() {
        super.onResume()
        loadLogs()
    }

    private fun loadLogs() {
        val tv = logsText ?: return

        if (!logFile.exists()) {
            tv.text = getString(R.string.logs_empty)
            return
        }

        try {
            val sb = StringBuilder()

            BufferedReader(FileReader(logFile)).use { reader ->
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    sb.append(line).append("\n")
                }
            }

            tv.text = if (sb.isEmpty()) {
                getString(R.string.logs_empty)
            } else {
                sb.toString()
            }
        } catch (e: Exception) {
            tv.text = "Error reading logs: ${e.message}"
        }
    }

    private fun showClearConfirmation() {
        clearConfirmActive = true

        handler.removeCallbacks(resetConfirmation)
        handler.postDelayed(resetConfirmation, 4000)

        val button = clearButton ?: return
        val icon = clearIcon ?: return
        val text = confirmText ?: return

        icon.animate().alpha(0f).setDuration(100).withEndAction {
            icon.setImageResource(R.drawable.ic_check)
            icon.animate().alpha(1f).setDuration(150).start()
        }

        text.visibility = View.VISIBLE
        text.alpha = 0f
        text.animate().alpha(1f).setDuration(200).start()

        animateButtonWidth(button, dp(48), dp(164))
        button.background = createButtonBackground(
            requireContext().getColor(R.color.dark_accent)
        )
    }

    private fun resetClearButton() {
        if (!clearConfirmActive) return

        clearConfirmActive = false
        handler.removeCallbacks(resetConfirmation)

        val button = clearButton ?: return
        val icon = clearIcon ?: return
        val text = confirmText ?: return

        text.animate().alpha(0f).setDuration(100).withEndAction {
            text.visibility = View.GONE
        }.start()

        icon.animate().alpha(0f).setDuration(100).withEndAction {
            icon.setImageResource(R.drawable.ic_clear)
            icon.animate().alpha(1f).setDuration(150).start()
        }

        animateButtonWidth(button, dp(164), dp(48))
        button.background = createButtonBackground(
            requireContext().getColor(R.color.dark_surface)
        )
    }

    private fun clearLogs() {
        handler.removeCallbacks(resetConfirmation)

        try {
            logFile.writeText("")
            logsText?.text = getString(R.string.logs_empty)

            Toast.makeText(
                requireContext(),
                getString(R.string.logs_cleared),
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Error clearing logs: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }

        resetClearButton()
    }

    private fun shareLogs() {
        if (!logFile.exists() || logFile.length() == 0L) {
            Toast.makeText(
                requireContext(),
                getString(R.string.no_logs),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        try {
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                logFile
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(intent, "Share logs"))
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Error sharing: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun animateButtonWidth(
        button: LinearLayout,
        startWidth: Int,
        endWidth: Int
    ) {
        widthAnimator?.cancel()

        widthAnimator = ValueAnimator.ofInt(startWidth, endWidth).apply {
            duration = 250

            addUpdateListener { animator ->
                val params = button.layoutParams
                params.width = animator.animatedValue as Int
                button.layoutParams = params
            }

            start()
        }
    }

    private fun createButtonBackground(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(color)
            cornerRadius = dp(100).toFloat()
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    override fun onDestroyView() {
        handler.removeCallbacks(resetConfirmation)
        widthAnimator?.cancel()

        clearButton = null
        clearIcon = null
        confirmText = null
        logsText = null

        super.onDestroyView()
    }
}

package org.slauridev.brightnessfix

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import android.widget.Button
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class LogsActivity : AppCompatActivity() {

    private val logFile: File by lazy { File(filesDir, "app.log") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logs)

        applyTheme()

        val tvLogs = findViewById<TextView>(R.id.tvLogs)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnClear = findViewById<Button>(R.id.btnClear)
        val btnShare = findViewById<Button>(R.id.btnShare)

        btnBack.setOnClickListener { finish() }
        
        loadLogs(tvLogs)

        btnClear.setOnClickListener {
            logFile.writeText("")
            tvLogs.text = getString(R.string.logs_empty)
            Toast.makeText(this, getString(R.string.logs_cleared), Toast.LENGTH_SHORT).show()
        }

        btnShare.setOnClickListener {
            shareLogs()
        }
    }

    private fun loadLogs(tv: TextView) {
        if (!logFile.exists()) {
            tv.text = getString(R.string.logs_empty)
            return
        }
        val sb = StringBuilder()
        try {
            BufferedReader(FileReader(logFile)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line).append("\n")
                }
            }
            tv.text = if (sb.isEmpty()) getString(R.string.logs_empty) else sb.toString()
        } catch (e: Exception) {
            tv.text = "Error reading logs: ${e.message}"
        }
    }

    private fun shareLogs() {
        if (!logFile.exists() || logFile.length() == 0L) {
            Toast.makeText(this, "No logs to share", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", logFile)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Share logs"))
        } catch (e: Exception) {
            Toast.makeText(this, "Error sharing: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun applyTheme() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val theme = prefs.getString("theme", "amoled")
        
        val bgColor = when (theme) {
            "light" -> 0xFFFFFFFF.toInt()
            "amoled" -> 0xFF000000.toInt()
            else -> 0xFF1C1B1F.toInt()
        }
        val headerColor = when (theme) {
            "light" -> 0xFFF5F5F5.toInt()
            else -> 0xFF1A1A1E.toInt()
        }
        val textColor = when (theme) {
            "light" -> 0xFF000000.toInt()
            else -> 0xFFFFFFFF.toInt()
        }

        window.decorView.rootView.setBackgroundColor(bgColor)
        findViewById<android.view.View>(R.id.headerBar).setBackgroundColor(headerColor)
        findViewById<TextView>(R.id.tvLogs).setTextColor(textColor)
    }
}
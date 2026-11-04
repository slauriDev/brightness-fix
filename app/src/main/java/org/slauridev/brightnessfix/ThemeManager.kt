package org.slauridev.brightnessfix

import android.content.Context

object ThemeManager {

    data class Colors(
        val background: Int,
        val surface: Int,
        val surfaceAlt: Int,
        val text: Int,
        val textSecondary: Int,
        val accent: Int,
        val accentText: Int,
        val divider: Int,
        val dim: Int,
        val dialogBackground: Int
    )

    fun getColors(context: Context): Colors {
        val prefs = context.getSharedPreferences(
            "app_prefs",
            Context.MODE_PRIVATE
        )

        return when (prefs.getString("theme", "dark")) {
            "light" -> loadPalette(context, "light")
            "amoled" -> loadPalette(context, "amoled")
            else -> loadPalette(context, "dark")
        }
    }

    private fun loadPalette(
        context: Context,
        theme: String
    ): Colors {
        fun color(name: String): Int {
            val resourceName = "${theme}_${name}"
            val resourceId = context.resources.getIdentifier(
                resourceName,
                "color",
                context.packageName
            )

            check(resourceId != 0) {
                "Missing color resource: $resourceName"
            }

            return context.getColor(resourceId)
        }

        return Colors(
            background = color("background"),
            surface = color("surface"),
            surfaceAlt = color("surface_alt"),
            text = color("text"),
            textSecondary = color("text_secondary"),
            accent = color("primary"),
            accentText = color("accent_text"),
            divider = color("divider"),
            dim = color("dim"),
            dialogBackground = color("dialog_bg")
        )
    }
}

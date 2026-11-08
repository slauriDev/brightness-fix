
package org.slauridev.brightnessfix

import android.content.Context
import androidx.core.content.ContextCompat

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
        fun color(name: String): Int {
            val resourceId = context.resources.getIdentifier(
                "dark_$name",
                "color",
                context.packageName
            )

            check(resourceId != 0) {
                "Missing color resource: dark_$name"
            }

            return ContextCompat.getColor(context, resourceId)
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

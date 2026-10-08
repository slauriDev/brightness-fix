package org.zxcslauri.brightnessfix

import android.content.Context
import android.graphics.Color

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

        val theme = prefs.getString("theme", "amoled")
        val contrast = prefs.getString("contrast", "medium")

        return when (theme) {
            "light" -> light(contrast)
            "dark" -> dark(contrast)
            else -> amoled(contrast)
        }
    }

    private fun light(contrast: String?): Colors {
        return when (contrast) {

            "normal" -> Colors(
                background = Color.WHITE,
                surface = 0xFFF2F2F2.toInt(),
                surfaceAlt = 0xFFEAEAEA.toInt(),
                text = Color.BLACK,
                textSecondary = 0xFF555555.toInt(),
                accent = 0xFF6750A4.toInt(),
                accentText = Color.WHITE,
                divider = 0x33000000,
                dim = 0x66000000,
                dialogBackground = 0xFFF2F2F2.toInt()
            )

            "high" -> Colors(
                background = Color.WHITE,
                surface = 0xFFE4E4E4.toInt(),
                surfaceAlt = 0xFFD8D8D8.toInt(),
                text = Color.BLACK,
                textSecondary = 0xFF222222.toInt(),
                accent = 0xFF4F378B.toInt(),
                accentText = Color.WHITE,
                divider = 0x66000000,
                dim = 0x99000000.toInt(),
                dialogBackground = 0xFFE4E4E4.toInt()
            )

            else -> Colors(
                background = Color.WHITE,
                surface = 0xFFF7F7F7.toInt(),
                surfaceAlt = 0xFFF0F0F0.toInt(),
                text = Color.BLACK,
                textSecondary = 0xFF444444.toInt(),
                accent = 0xFF6750A4.toInt(),
                accentText = Color.WHITE,
                divider = 0x22000000,
                dim = 0x77000000,
                dialogBackground = 0xFFF7F7F7.toInt()
            )
        }
    }

    private fun dark(contrast: String?): Colors {
        return when (contrast) {

            "normal" -> Colors(
                background = 0xFF121212.toInt(),
                surface = 0xFF1E1E1E.toInt(),
                surfaceAlt = 0xFF242424.toInt(),
                text = Color.WHITE,
                textSecondary = 0xFFB0B0B0.toInt(),
                accent = 0xFFD0BCFF.toInt(),
                accentText = 0xFF381E72.toInt(),
                divider = 0x33FFFFFF,
                dim = 0x99000000.toInt(),
                dialogBackground = 0xFF2C2C2E.toInt()
            )

            "high" -> Colors(
                background = 0xFF101010.toInt(),
                surface = 0xFF1A1A1A.toInt(),
                surfaceAlt = 0xFF222222.toInt(),
                text = Color.WHITE,
                textSecondary = 0xFFE0E0E0.toInt(),
                accent = 0xFFE8DDFF.toInt(),
                accentText = 0xFF26134F.toInt(),
                divider = 0x66FFFFFF,
                dim = 0xCC000000.toInt(),
                dialogBackground = 0xFF202020.toInt()
            )

            else -> Colors(
                background = 0xFF121212.toInt(),
                surface = 0xFF1C1C1C.toInt(),
                surfaceAlt = 0xFF222222.toInt(),
                text = Color.WHITE,
                textSecondary = 0xFFC0C0C0.toInt(),
                accent = 0xFFD0BCFF.toInt(),
                accentText = 0xFF381E72.toInt(),
                divider = 0x44FFFFFF,
                dim = 0xAA000000.toInt(),
                dialogBackground = 0xFF2C2C2E.toInt()
            )
        }
    }

    private fun amoled(contrast: String?): Colors {
        return when (contrast) {

            "normal" -> Colors(
                background = Color.BLACK,
                surface = 0xFF151515.toInt(),
                surfaceAlt = 0xFF1A1A1A.toInt(),
                text = Color.WHITE,
                textSecondary = 0xFFAAAAAA.toInt(),
                accent = 0xFFD0BCFF.toInt(),
                accentText = 0xFF381E72.toInt(),
                divider = 0x33FFFFFF,
                dim = 0xC3000000.toInt(),
                dialogBackground = 0xFF2C2C2E.toInt()
            )

            "high" -> Colors(
                background = Color.BLACK,
                surface = 0xFF101010.toInt(),
                surfaceAlt = 0xFF181818.toInt(),
                text = Color.WHITE,
                textSecondary = 0xFFE0E0E0.toInt(),
                accent = 0xFFE8DDFF.toInt(),
                accentText = 0xFF26134F.toInt(),
                divider = 0x66FFFFFF,
                dim = 0xE0000000.toInt(),
                dialogBackground = 0xFF202020.toInt()
            )

            else -> Colors(
                background = Color.BLACK,
                surface = 0xFF1A1A1E.toInt(),
                surfaceAlt = 0xFF1C1B1F.toInt(),
                text = Color.WHITE,
                textSecondary = 0xB3FFFFFF.toInt(),
                accent = 0xFFD0BCFF.toInt(),
                accentText = 0xFF381E72.toInt(),
                divider = 0x44FFFFFF,
                dim = 0xD0000000.toInt(),
                dialogBackground = 0xFF2C2C2E.toInt()
            )
        }
    }
}
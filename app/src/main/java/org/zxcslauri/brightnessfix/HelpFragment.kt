package org.zxcslauri.brightnessfix

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class HelpFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_help, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyTheme(view)
    }

    private fun applyTheme(view: View) {
        val prefs = requireActivity().getSharedPreferences("app_prefs", 0)
        val theme = prefs.getString("theme", "amoled")
        val contrast = prefs.getString("contrast", "medium")

        val bgColor = when (theme) {
            "light" -> 0xFFFFFFFF.toInt()
            "amoled" -> 0xFF000000.toInt()
            else -> 0xFF1C1B1F.toInt()
        }
        val textColor = when (theme) {
            "light" -> 0xFF000000.toInt()
            else -> 0xFFFFFFFF.toInt()
        }
        val textSecondary = when (theme) {
            "light" -> 0xFF444444.toInt()
            else -> 0xB3FFFFFF.toInt()
        }
        val alpha = when (contrast) {
            "normal" -> 0.75f
            "high" -> 1.0f
            else -> 0.88f
        }

        view.setBackgroundColor(bgColor)
        view.alpha = alpha

        val textViews = ArrayList<TextView>()
        findTextViews(view, textViews)
        for (tv in textViews) {
            tv.setTextColor(if (tv.currentTextColor == 0xB3FFFFFF.toInt()) textSecondary else textColor)
        }
    }

    private fun findTextViews(view: View, list: ArrayList<TextView>) {
        if (view is TextView) list.add(view)
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                findTextViews(view.getChildAt(i), list)
            }
        }
    }
}
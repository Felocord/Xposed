package io.github.felitendo.xposed

import android.app.AndroidAppHelper
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
data class SysColors(
    val neutral1: List<String>,
    val neutral2: List<String>,
    val accent1: List<String>,
    val accent2: List<String>,
    val accent3: List<String>
)

class SysColorsModule : PyonModule() {
    private lateinit var context: Context
    fun isSupported() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    override fun buildJson(builder: JsonObjectBuilder) {
        context = AndroidAppHelper.currentApplication()
        val accents = arrayOf("accent1", "accent2", "accent3", "neutral1", "neutral2")
        val shades = arrayOf(0, 10, 50, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000)

        builder.apply {
            put("isSysColorsSupported", isSupported())
            if (isSupported()) putJsonObject("sysColors") {
                for (accent in accents) putJsonArray(accent) {
                    for (shade in shades) {
                        val colorName = "system_" + accent + "_" + shade

                        val colorResourceId = runCatching {
                            android.R.color::class.java.getField(colorName).getInt(null)
                        }.getOrElse { 0 }

                        add(convertToColor(colorResourceId))
                    }
                }
            }
        }
    }

    fun convertToColor(id: Int): String {
        val clr = if (isSupported()) ContextCompat.getColor(context, id) else 0
        return String.format("#%06X", 0xFFFFFF and clr)
    }
}
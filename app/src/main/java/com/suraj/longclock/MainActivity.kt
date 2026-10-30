package com.suraj.longclock

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.*

class MainActivity : Activity() {
    private val pickPhoto = 41
    private val prefs by lazy { getSharedPreferences("clock_wallpaper", MODE_PRIVATE) }
    private val styles = listOf("iPhone Long Clock", "Samsung One UI", "Google Pixel", "Minimal Luxury", "Classic Digital", "Tony Tech HUD")
    private val themes = listOf("Midnight AMOLED", "MrBeast Energy", "Tony Tech Reactor", "Anime Energy", "Supercar Night", "Cyberpunk City", "Luxury Gold", "Ocean Aurora", "Photo Wallpaper")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (20 * resources.displayMetrics.density).toInt()
        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad * 2, pad, pad * 2)
            setBackgroundColor(Color.rgb(7, 10, 18))
        }
        fun text(value: String, size: Float, color: Int, bold: Boolean = false) {
            root.addView(TextView(this).apply {
                text = value; textSize = size; setTextColor(color)
                if (bold) typeface = Typeface.create("sans-serif", Typeface.BOLD)
                setPadding(0, 8, 0, 8)
            })
        }
        fun section(value: String) {
            text(value, 13f, Color.rgb(110, 190, 255), true)
        }
        fun spinner(title: String, items: List<String>, key: String, default: Int) {
            section(title)
            val s = Spinner(this)
            s.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
            s.setSelection(prefs.getInt(key, default).coerceIn(items.indices))
            s.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    prefs.edit().putInt(key, position).apply()
                }
            }
            root.addView(s, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, pad * 2))
        }
        text("WALLPAPER X", 31f, Color.WHITE, true)
        text("ULTIMATE LIVE WALLPAPER STUDIO", 12f, Color.rgb(120, 190, 255), true)
        text("Live clock • Cinematic themes • Your own photos • ₹0", 14f, Color.LTGRAY)
        spinner("01  WALLPAPER WORLD", themes, "theme", 0)
        spinner("02  CLOCK DESIGN", styles, "style", 0)
        spinner("03  CLOCK COLOR", listOf("White", "Gold", "Ice Blue", "Pink", "Green", "Red"), "clock_color", 0)
        spinner("04  CLOCK SIZE", listOf("Small", "Medium", "Large"), "clock_size", 1)
        spinner("05  CLOCK POSITION", listOf("Top", "Center", "Bottom"), "clock_position", 0)
        spinner("06  TIME FORMAT", listOf("24-hour", "12-hour"), "format", 0)
        root.addView(CheckBox(this).apply {
            text = "Show seconds"; setTextColor(Color.WHITE)
            buttonTintList = android.content.res.ColorStateList.valueOf(Color.rgb(90,180,255))
            isChecked = prefs.getBoolean("seconds", true)
            setOnCheckedChangeListener { _, checked -> prefs.edit().putBoolean("seconds", checked).apply() }
        })
        root.addView(CheckBox(this).apply {
            text = "Show date"; setTextColor(Color.WHITE)
            buttonTintList = android.content.res.ColorStateList.valueOf(Color.rgb(90,180,255))
            isChecked = prefs.getBoolean("show_date", true)
            setOnCheckedChangeListener { _, checked -> prefs.edit().putBoolean("show_date", checked).apply() }
        })
        section("07  PERSONAL WALLPAPER")
        root.addView(Button(this).apply {
            text = "Choose photo from gallery"
            setOnClickListener {
                val i = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE); type = "image/*"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                }
                startActivityForResult(i, pickPhoto)
            }
        })
        root.addView(Button(this).apply {
            text = "Remove photo • Use built-in theme"
            setOnClickListener {
                prefs.edit().remove("photo_uri").putInt("theme", 0).apply()
                Toast.makeText(this@MainActivity, "Built-in wallpaper selected", Toast.LENGTH_SHORT).show()
            }
        })
        section("08  ACTIVATE")
        root.addView(Button(this).apply {
            text = "PREVIEW & SET LIVE WALLPAPER"
            setTextColor(Color.WHITE)
            setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(25,105,190)))
            setOnClickListener {
                val i = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                    putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                        ComponentName(this@MainActivity, ClockWallpaperService::class.java))
                }
                startActivity(i)
            }
        })
        text("Free built-in themes work offline. Live clock updates automatically. Original inspired artwork only; no copied brand logos or copyrighted character art. Lock-screen availability depends on your phone's software.", 12f, Color.LTGRAY)
        scroll.addView(root)
        setContentView(scroll)
    }

    @Deprecated("Deprecated by Android, retained for broad device compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickPhoto && resultCode == RESULT_OK) {
            val uri: Uri = data?.data ?: return
            try { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            prefs.edit().putString("photo_uri", uri.toString()).putInt("theme", 8).apply()
            Toast.makeText(this, "Photo saved. Set live wallpaper again.", Toast.LENGTH_LONG).show()
        }
    }
}
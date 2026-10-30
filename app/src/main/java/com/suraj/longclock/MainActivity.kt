package com.suraj.longclock

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import android.graphics.Color

class MainActivity : Activity() {
    private val pickPhoto = 41
    private val prefs by lazy { getSharedPreferences("clock_wallpaper", MODE_PRIVATE) }
    private val styles = listOf(
        "iPhone Long Clock", "Samsung One UI", "Google Pixel", "Minimal Luxury", "Classic Digital"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (22 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad * 2, pad, pad)
            setBackgroundColor(Color.rgb(12, 16, 25))
        }
        root.addView(TextView(this).apply {
            text = "LONG CLOCK\nWALLPAPER STUDIO"
            textSize = 27f
            setTextColor(Color.WHITE)
            typeface = android.graphics.Typeface.create("sans-serif-medium", 0)
        })
        root.addView(TextView(this).apply {
            text = "Apni photo + apna favourite phone-style clock"
            textSize = 14f
            setTextColor(Color.LTGRAY)
            setPadding(0, 8, 0, pad)
        })
        fun label(value: String) {
            root.addView(TextView(this).apply {
                text = value
                textSize = 15f
                setTextColor(Color.WHITE)
                setPadding(0, pad / 2, 0, 8)
            })
        }
        label("01  WALLPAPER DESIGN")
        val spinner = Spinner(this)
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, styles)
        spinner.setSelection(prefs.getInt("style", 0).coerceIn(styles.indices))
        spinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                prefs.edit().putInt("style", position).apply()
            }
        }
        root.addView(spinner, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, pad * 2))
        label("02  CLOCK FORMAT")
        val format = Spinner(this)
        format.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("24-hour", "12-hour"))
        format.setSelection(prefs.getInt("format", 0).coerceIn(0, 1))
        format.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                prefs.edit().putInt("format", position).apply()
            }
        }
        root.addView(format, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, pad * 2))
        val seconds = CheckBox(this).apply {
            text = "Show seconds"
            setTextColor(Color.WHITE)
            buttonTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)
            isChecked = prefs.getBoolean("seconds", true)
            setOnCheckedChangeListener { _, checked -> prefs.edit().putBoolean("seconds", checked).apply() }
        }
        root.addView(seconds)
        label("03  YOUR PHOTO")
        root.addView(Button(this).apply {
            text = "Choose photo from gallery"
            setOnClickListener {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "image/*"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                }
                startActivityForResult(intent, pickPhoto)
            }
        })
        label("04  APPLY")
        root.addView(Button(this).apply {
            text = "Preview & set live wallpaper"
            setOnClickListener {
                val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                    putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                        ComponentName(this@MainActivity, ClockWallpaperService::class.java))
                }
                startActivity(intent)
            }
        })
        root.addView(TextView(this).apply {
            text = "\nStyles are inspired by familiar phone design languages; this is an independent Android wallpaper, not Apple's or Samsung's official UI. Lock-screen support depends on your phone."
            textSize = 12f
            setTextColor(Color.LTGRAY)
        })
        val scroll = ScrollView(this)
        scroll.addView(root)
        setContentView(scroll)
    }

    @Deprecated("Deprecated by Android, retained for broad device compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickPhoto && resultCode == RESULT_OK) {
            val uri: Uri = data?.data ?: return
            try { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            prefs.edit().putString("photo_uri", uri.toString()).apply()
            Toast.makeText(this, "Photo saved. Live wallpaper dobara open karein.", Toast.LENGTH_LONG).show()
        }
    }
}

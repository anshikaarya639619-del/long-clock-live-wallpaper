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
    private val styles = listOf("iPhone Long Clock", "Samsung One UI", "Google Pixel", "Minimal Luxury", "Classic Digital")

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
            text = "Live clock pehle se ON hai — photo ke bina bhi clock dikhegi."
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
        fun addSpinner(items: List<String>, key: String, default: Int) {
            val spinner = Spinner(this)
            spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
            spinner.setSelection(prefs.getInt(key, default).coerceIn(items.indices))
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                    prefs.edit().putInt(key, position).apply()
                }
            }
            root.addView(spinner, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, pad * 2))
        }
        label("01  CLOCK DESIGN")
        addSpinner(styles, "style", 0)
        label("02  TIME FORMAT")
        addSpinner(listOf("24-hour", "12-hour"), "format", 0)
        root.addView(CheckBox(this).apply {
            text = "Show seconds"
            setTextColor(Color.WHITE)
            buttonTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)
            isChecked = prefs.getBoolean("seconds", true)
            setOnCheckedChangeListener { _, checked -> prefs.edit().putBoolean("seconds", checked).apply() }
        })
        label("03  CUSTOM CLOCK — COLOR")
        addSpinner(listOf("White", "Gold", "Ice Blue", "Pink", "Green"), "clock_color", 0)
        label("CUSTOM CLOCK — SIZE")
        addSpinner(listOf("Small", "Medium", "Large"), "clock_size", 1)
        label("CUSTOM CLOCK — POSITION")
        addSpinner(listOf("Top", "Center", "Bottom"), "clock_position", 0)
        label("04  YOUR PHOTO")
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
        label("05  APPLY")
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
            text = "\nClock automatically appears and updates every second. Photo is optional. Lock-screen support depends on your phone."
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
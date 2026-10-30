package com.suraj.longclock

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val pickPhoto = 41
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (24 * resources.displayMetrics.density).toInt()
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad * 2, pad, pad)
        }
        layout.addView(TextView(this).apply {
            text = "LONG CLOCK WALLPAPER\n\nApni photo select karein. Phir live clock wallpaper set karein. Clock har minute nahi, har second update hoti hai."
            textSize = 20f
        })
        layout.addView(Button(this).apply {
            text = "1. Select your photo"
            setOnClickListener {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "image/*"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                }
                startActivityForResult(intent, pickPhoto)
            }
        })
        layout.addView(Button(this).apply {
            text = "2. Set live wallpaper"
            setOnClickListener {
                val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                    putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                        ComponentName(this@MainActivity, ClockWallpaperService::class.java))
                }
                startActivity(intent)
            }
        })
        layout.addView(TextView(this).apply {
            text = "\nNote: Android system preview mein 'Set wallpaper' dabakar Lock screen choose karein (agar phone yeh option deta hai)."
            textSize = 14f
        })
        setContentView(layout)
    }

    @Deprecated("Deprecated by Android, retained for broad device compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickPhoto && resultCode == RESULT_OK) {
            val uri: Uri = data?.data ?: return
            try {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) { }
            getSharedPreferences("clock_wallpaper", MODE_PRIVATE).edit().putString("photo_uri", uri.toString()).apply()
            Toast.makeText(this, "Photo saved. Ab live wallpaper set karein.", Toast.LENGTH_LONG).show()
        }
    }
}

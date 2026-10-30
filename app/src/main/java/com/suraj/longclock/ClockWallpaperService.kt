package com.suraj.longclock

import android.service.wallpaper.WallpaperService
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.view.SurfaceHolder
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ClockWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine = ClockEngine()

    inner class ClockEngine : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        private var visible = false
        private var photo: Bitmap? = null
        private val tick = object : Runnable {
            override fun run() {
                drawFrame()
                if (visible) handler.postDelayed(this, 1000L)
            }
        }
        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            loadPhoto()
            drawFrame()
        }
        override fun onVisibilityChanged(visibleNow: Boolean) {
            visible = visibleNow
            handler.removeCallbacks(tick)
            if (visible) tick.run()
        }
        override fun onDestroy() {
            visible = false
            handler.removeCallbacks(tick)
            photo?.recycle()
            photo = null
            super.onDestroy()
        }
        private fun loadPhoto() {
            photo?.recycle()
            photo = null
            val raw = getSharedPreferences("clock_wallpaper", MODE_PRIVATE).getString("photo_uri", null) ?: return
            try { contentResolver.openInputStream(Uri.parse(raw))?.use { photo = BitmapFactory.decodeStream(it) } }
            catch (_: Exception) { photo = null }
        }
        private fun drawFrame() {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas() ?: return
                val w = canvas.width.toFloat()
                val h = canvas.height.toFloat()
                val prefs = getSharedPreferences("clock_wallpaper", MODE_PRIVATE)
                val style = prefs.getInt("style", 0)
                val use24 = prefs.getInt("format", 0) == 0
                val showSeconds = prefs.getBoolean("seconds", true)
                val image = photo
                if (image != null) {
                    val scale = maxOf(w / image.width, h / image.height)
                    val sw = w / scale; val sh = h / scale
                    val src = Rect(((image.width - sw) / 2).toInt(), ((image.height - sh) / 2).toInt(),
                        ((image.width + sw) / 2).toInt(), ((image.height + sh) / 2).toInt())
                    canvas.drawBitmap(image, src, Rect(0, 0, w.toInt(), h.toInt()), bitmapPaint)
                    canvas.drawRect(0f, 0f, w, h, Paint().apply { color = Color.argb(if (style == 1) 35 else 78, 0, 0, 0) })
                } else canvas.drawColor(Color.rgb(18, 20, 28))

                val now = Date()
                val timePattern = if (use24) { if (showSeconds) "HH:mm:ss" else "HH:mm" } else { if (showSeconds) "h:mm:ss" else "h:mm" }
                val time = SimpleDateFormat(timePattern, Locale.getDefault()).format(now)
                val date = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(now)
                val clockColor = when (prefs.getInt("clock_color", 0)) {
                    1 -> Color.rgb(255, 205, 90)
                    2 -> Color.rgb(130, 220, 255)
                    3 -> Color.rgb(255, 145, 190)
                    4 -> Color.rgb(145, 235, 165)
                    else -> Color.WHITE
                }
                val x: Float
                var y: Float
                val size: Float
                val align: Paint.Align
                val face: Typeface
                when (style) {
                    0 -> { // iPhone-inspired oversized, centered long clock
                        x = w / 2f; y = h * 0.34f; size = w * 0.205f; align = Paint.Align.CENTER
                        face = Typeface.create("sans-serif-thin", Typeface.NORMAL)
                    }
                    1 -> { // Samsung One UI-inspired left aligned typography
                        x = w * 0.09f; y = h * 0.29f; size = w * 0.17f; align = Paint.Align.LEFT
                        face = Typeface.create("sans-serif", Typeface.NORMAL)
                    }
                    2 -> { // Pixel-inspired clean centered clock
                        x = w / 2f; y = h * 0.38f; size = w * 0.16f; align = Paint.Align.CENTER
                        face = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                    }
                    3 -> { // Minimal luxury
                        x = w / 2f; y = h * 0.72f; size = w * 0.13f; align = Paint.Align.CENTER
                        face = Typeface.create("serif", Typeface.NORMAL)
                    }
                    else -> {
                        x = w / 2f; y = h * 0.33f; size = w * 0.15f; align = Paint.Align.CENTER
                        face = Typeface.create("monospace", Typeface.NORMAL)
                    }
                }
                val sizeFactor = when (prefs.getInt("clock_size", 1)) {
                    0 -> 0.78f
                    2 -> 1.22f
                    else -> 1f
                }
                val positionY = when (prefs.getInt("clock_position", 0)) {
                    1 -> h * 0.50f
                    2 -> h * 0.72f
                    else -> h * 0.22f
                }
                y = positionY
                val customSize = size * sizeFactor
                val clockPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = clockColor; textAlign = align; typeface = face; textSize = customSize
                    setShadowLayer(14f, 0f, 3f, Color.BLACK)
                }
                val datePaint = Paint(clockPaint).apply {
                    textSize = w * 0.043f
                    typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                    letterSpacingCompat()
                }
                val dateY = y + customSize * 0.62f
                if (style == 1) {
                    canvas.drawText(date.uppercase(Locale.getDefault()), x, y - customSize * 0.85f, datePaint)
                    canvas.drawText(time, x, y, clockPaint)
                } else {
                    canvas.drawText(time, x, y, clockPaint)
                    canvas.drawText(date.uppercase(Locale.getDefault()), x, dateY, datePaint)
                }
                if (!use24) {
                    val ampm = SimpleDateFormat("a", Locale.getDefault()).format(now)
                    val small = Paint(datePaint).apply { textSize = w * 0.035f }
                    canvas.drawText(ampm, x, dateY + w * 0.07f, small)
                }
            } catch (_: Exception) {
            } finally {
                if (canvas != null) try { holder.unlockCanvasAndPost(canvas) } catch (_: Exception) {}
            }
        }
    }
    private fun Paint.letterSpacingCompat() { /* reserved for consistent cross-version typography */ }
}

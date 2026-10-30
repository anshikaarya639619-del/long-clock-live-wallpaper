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
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
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
            try {
                contentResolver.openInputStream(Uri.parse(raw))?.use { photo = BitmapFactory.decodeStream(it) }
            } catch (_: Exception) { photo = null }
        }

        private fun drawFrame() {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    val w = canvas.width.toFloat()
                    val h = canvas.height.toFloat()
                    val image = photo
                    if (image != null) {
                        val scale = maxOf(w / image.width, h / image.height)
                        val sw = w / scale
                        val sh = h / scale
                        val src = Rect(((image.width - sw) / 2).toInt(), ((image.height - sh) / 2).toInt(),
                            ((image.width + sw) / 2).toInt(), ((image.height + sh) / 2).toInt())
                        canvas.drawBitmap(image, src, Rect(0, 0, w.toInt(), h.toInt()), paint)
                        canvas.drawRect(0f, 0f, w, h, Paint().apply { color = Color.argb(70, 0, 0, 0) })
                    } else {
                        canvas.drawColor(Color.rgb(18, 20, 28))
                    }

                    val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                    val date = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())
                    val clockPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.WHITE
                        textAlign = Paint.Align.CENTER
                        typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
                        textSize = w * 0.19f
                        setShadowLayer(12f, 0f, 3f, Color.BLACK)
                    }
                    val datePaint = Paint(clockPaint).apply {
                        textSize = w * 0.045f
                        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                    }
                    val centerY = h * 0.34f
                    canvas.drawText(time, w / 2f, centerY, clockPaint)
                    canvas.drawText(date.uppercase(Locale.getDefault()), w / 2f, centerY + w * 0.09f, datePaint)
                }
            } catch (_: Exception) {
            } finally {
                if (canvas != null) try { holder.unlockCanvasAndPost(canvas) } catch (_: Exception) { }
            }
        }
    }
}

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
import kotlin.math.*

class ClockWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine = ClockEngine()
    inner class ClockEngine : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        private var visible = false
        private var photo: Bitmap? = null
        private val tick = object : Runnable {
            override fun run() { drawFrame(); if (visible) handler.postDelayed(this, 1000L) }
        }
        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height); loadPhoto(); drawFrame()
        }
        override fun onVisibilityChanged(visibleNow: Boolean) {
            visible = visibleNow; handler.removeCallbacks(tick); if (visible) tick.run()
        }
        override fun onDestroy() {
            visible = false; handler.removeCallbacks(tick); photo?.recycle(); photo = null; super.onDestroy()
        }
        private fun loadPhoto() {
            photo?.recycle(); photo = null
            val raw = getSharedPreferences("clock_wallpaper", MODE_PRIVATE).getString("photo_uri", null) ?: return
            try { contentResolver.openInputStream(Uri.parse(raw))?.use { photo = BitmapFactory.decodeStream(it) } } catch (_: Exception) { photo = null }
        }
        private fun glow(canvas: Canvas, cx: Float, cy: Float, radius: Float, color: Int) {
            val p = Paint(Paint.ANTI_ALIAS_FLAG)
            p.shader = RadialGradient(cx, cy, radius, intArrayOf(color, Color.TRANSPARENT), null, Shader.TileMode.CLAMP)
            canvas.drawCircle(cx, cy, radius, p)
        }
        private fun drawBuiltIn(canvas: Canvas, w: Float, h: Float, theme: Int) {
            val colors = when(theme) {
                1 -> intArrayOf(Color.rgb(15,5,25), Color.rgb(100,15,45), Color.rgb(10,8,25))
                2 -> intArrayOf(Color.rgb(2,15,30), Color.rgb(0,75,105), Color.rgb(2,5,20))
                3 -> intArrayOf(Color.rgb(20,5,45), Color.rgb(75,20,110), Color.rgb(5,8,30))
                4 -> intArrayOf(Color.rgb(5,10,25), Color.rgb(25,35,65), Color.rgb(2,4,12))
                5 -> intArrayOf(Color.rgb(5,5,25), Color.rgb(35,10,65), Color.rgb(5,15,35))
                6 -> intArrayOf(Color.rgb(20,12,3), Color.rgb(90,55,10), Color.rgb(8,5,3))
                7 -> intArrayOf(Color.rgb(0,35,55), Color.rgb(10,95,110), Color.rgb(2,8,25))
                else -> intArrayOf(Color.rgb(3,7,18), Color.rgb(12,22,45), Color.rgb(2,4,12))
            }
            val shader = LinearGradient(0f, 0f, w, h, colors, null, Shader.TileMode.CLAMP)
            canvas.drawRect(0f,0f,w,h,Paint(Paint.ANTI_ALIAS_FLAG).apply { this.shader = shader })
            when(theme) {
                1 -> { // high-energy burst
                    glow(canvas,w*.5f,h*.55f,w*.65f,Color.argb(130,255,30,75))
                    val p=Paint(Paint.ANTI_ALIAS_FLAG).apply { color=Color.argb(90,255,210,40); strokeWidth=5f }
                    for(i in 0..17) { val a=i*PI/9; canvas.drawLine(w*.5f,h*.55f,w*.5f+cos(a).toFloat()*w,w*.55f+sin(a).toFloat()*h,p) }
                }
                2,5 -> {
                    glow(canvas,w*.5f,h*.56f,w*.36f,if(theme==2) Color.argb(190,0,190,255) else Color.argb(150,160,40,255))
                    val p=Paint(Paint.ANTI_ALIAS_FLAG).apply { color=Color.argb(180,100,220,255); style=Paint.Style.STROKE; strokeWidth=5f }
                    canvas.drawCircle(w*.5f,h*.56f,w*.19f,p); canvas.drawCircle(w*.5f,h*.56f,w*.25f,p)
                    canvas.drawLine(w*.28f,h*.56f,w*.72f,h*.56f,p)
                }
                3 -> {
                    glow(canvas,w*.5f,h*.55f,w*.55f,Color.argb(120,150,50,255))
                    val p=Paint(Paint.ANTI_ALIAS_FLAG).apply { color=Color.argb(150,210,180,255); strokeWidth=3f }
                    for(i in 0..12) { val x=w*i/12f; canvas.drawLine(x,h*.48f,x-w*.12f,h*.9f,p) }
                }
                4 -> { // stylized supercar silhouette
                    val p=Paint(Paint.ANTI_ALIAS_FLAG).apply { color=Color.rgb(5,8,16) }
                    val path=Path().apply { moveTo(w*.12f,h*.72f); lineTo(w*.22f,h*.66f); lineTo(w*.35f,h*.58f); lineTo(w*.65f,h*.58f); lineTo(w*.8f,h*.67f); lineTo(w*.91f,h*.7f); lineTo(w*.94f,h*.79f); lineTo(w*.1f,h*.79f); close() }
                    canvas.drawPath(path,p); p.color=Color.rgb(0,190,255); canvas.drawCircle(w*.28f,h*.79f,w*.055f,p); canvas.drawCircle(w*.75f,h*.79f,w*.055f,p)
                    glow(canvas,w*.5f,h*.78f,w*.5f,Color.argb(65,0,160,255))
                }
                6 -> {
                    val p=Paint(Paint.ANTI_ALIAS_FLAG).apply { color=Color.argb(100,255,205,100); style=Paint.Style.STROKE; strokeWidth=3f }
                    for(i in 1..5) canvas.drawCircle(w*.5f,h*.55f,w*.08f*i,p)
                }
                7 -> { glow(canvas,w*.5f,h*.48f,w*.7f,Color.argb(150,0,220,200)); glow(canvas,w*.2f,h*.7f,w*.4f,Color.argb(100,40,100,255)) }
                else -> { glow(canvas,w*.5f,h*.5f,w*.55f,Color.argb(45,40,100,200)) }
            }
            val stars=Paint(Paint.ANTI_ALIAS_FLAG).apply { color=Color.argb(130,255,255,255) }
            for(i in 0..55) { val x=((i*97)%997)/997f*w; val y=((i*173)%991)/991f*h; canvas.drawCircle(x,y,if(i%7==0) 2.2f else 1f,stars) }
        }
        private fun drawFrame() {
            val holder=surfaceHolder; var canvas:Canvas?=null
            try {
                canvas=holder.lockCanvas() ?: return
                val w=canvas.width.toFloat(); val h=canvas.height.toFloat()
                val prefs=getSharedPreferences("clock_wallpaper",MODE_PRIVATE)
                val theme=prefs.getInt("theme",0); val style=prefs.getInt("style",0)
                val image=photo
                if(image!=null) {
                    val scale=maxOf(w/image.width,h/image.height); val sw=w/scale; val sh=h/scale
                    val src=Rect(((image.width-sw)/2).toInt(),((image.height-sh)/2).toInt(),((image.width+sw)/2).toInt(),((image.height+sh)/2).toInt())
                    canvas.drawBitmap(image,src,Rect(0,0,w.toInt(),h.toInt()),bitmapPaint)
                    canvas.drawRect(0f,0f,w,h,Paint().apply { color=Color.argb(90,0,0,0) })
                } else drawBuiltIn(canvas,w,h,theme)
                val now=Date(); val use24=prefs.getInt("format",0)==0; val seconds=prefs.getBoolean("seconds",true)
                val pattern=if(use24) { if(seconds) "HH:mm:ss" else "HH:mm" } else { if(seconds) "h:mm:ss" else "h:mm" }
                val time=SimpleDateFormat(pattern,Locale.getDefault()).format(now)
                val date=SimpleDateFormat("EEEE, d MMMM",Locale.getDefault()).format(now)
                val color=when(prefs.getInt("clock_color",0)) {
                    1->Color.rgb(255,205,90); 2->Color.rgb(130,220,255); 3->Color.rgb(255,145,190); 4->Color.rgb(145,235,165); 5->Color.rgb(255,85,85); else->Color.WHITE
                }
                val align=if(style==1) Paint.Align.LEFT else Paint.Align.CENTER
                val x=if(style==1) w*.09f else w*.5f
                val base=when(style) { 0->w*.205f; 1->w*.17f; 2->w*.16f; 3->w*.13f; 4->w*.15f; else->w*.18f }
                val factor=when(prefs.getInt("clock_size",1)) { 0->.78f; 2->1.22f; else->1f }
                val y=when(prefs.getInt("clock_position",0)) { 1->h*.5f; 2->h*.72f; else->if(style==1) h*.29f else h*.22f }
                val face=when(style) { 0->"sans-serif-thin"; 3->"serif"; 4->"monospace"; 5->"sans-serif-condensed"; else->"sans-serif-medium" }
                val cp=Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color=color; textAlign=align; typeface=Typeface.create(face,Typeface.NORMAL); textSize=base*factor; setShadowLayer(18f,0f,3f,Color.BLACK) }
                val dp=Paint(cp).apply { textSize=w*.043f; typeface=Typeface.create("sans-serif-medium",Typeface.NORMAL) }
                canvas.drawText(time,x,y,cp)
                val dateY=y+base*factor*.62f
                if(prefs.getBoolean("show_date",true)) canvas.drawText(date.uppercase(Locale.getDefault()),x,dateY,dp)
                if(!use24) canvas.drawText(SimpleDateFormat("a",Locale.getDefault()).format(now),x,dateY+w*.07f,Paint(dp).apply { textSize=w*.035f })
                if(style==5) {
                    val hud=Paint(Paint.ANTI_ALIAS_FLAG).apply { color=Color.argb(130,100,220,255); style=Paint.Style.STROKE; strokeWidth=2f }
                    canvas.drawCircle(w*.5f,y-base*.48f,base*.8f,hud)
                    canvas.drawLine(w*.12f,y+base*.8f,w*.88f,y+base*.8f,hud)
                }
            } catch (_:Exception) { } finally { if(canvas!=null) try { holder.unlockCanvasAndPost(canvas) } catch(_:Exception){} }
        }
    }
}
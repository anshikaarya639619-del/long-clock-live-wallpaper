package com.suraj.depthpixpro
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import kotlin.math.*

class MainActivity : ComponentActivity() {
 private lateinit var preview: WallpaperPreview
 private lateinit var status: TextView
 private lateinit var count: TextView
 private var index=1
 private var clockColor=Color.WHITE
 private var showDate=true
 private var longClock=true
 private val picker=registerForActivityResult(ActivityResultContracts.GetContent()){ uri:Uri? ->
  if(uri!=null) try {
   val bounds=BitmapFactory.Options().apply{inJustDecodeBounds=true}
   contentResolver.openInputStream(uri)?.use{BitmapFactory.decodeStream(it,null,bounds)}
   var sample=1
   while(max(bounds.outWidth,bounds.outHeight)/sample>1800) sample*=2
   val bmp=BitmapFactory.Options().run{inSampleSize=sample;contentResolver.openInputStream(uri)?.use{BitmapFactory.decodeStream(it,null,this)}}
   preview.photo=bmp;preview.invalidate();status.text="Photo loaded • touch to move in 3D"
  }catch(_:Exception){status.text="Could not open photo"}
 }
 private val saver=registerForActivityResult(ActivityResultContracts.CreateDocument("image/png")){uri:Uri? ->
  if(uri!=null) try{contentResolver.openOutputStream(uri)?.use{preview.export().compress(Bitmap.CompressFormat.PNG,100,it)};status.text="PNG exported"}catch(_:Exception){status.text="Export failed"}
 }
 override fun onCreate(savedInstanceState:Bundle?){
  super.onCreate(savedInstanceState)
  window.statusBarColor=0xFF080C16.toInt();window.navigationBarColor=0xFF080C16.toInt()
  val root=LinearLayout(this).apply{orientation=1;setPadding(18,20,18,18);setBackgroundColor(0xFF080C16.toInt())}
  root.addView(TextView(this).apply{text="DEPTHPIX PRO";textSize=27f;letterSpacing=.12f;setTextColor(Color.WHITE);typeface=Typeface.DEFAULT_BOLD})
  root.addView(TextView(this).apply{text="1000 WALLPAPERS  •  3D MOTION  •  LIVE CLOCK";textSize=10f;letterSpacing=.06f;setTextColor(0xFF73BEFF.toInt());setPadding(0,5,0,14)})
  preview=WallpaperPreview(this);root.addView(preview,LinearLayout.LayoutParams(-1,430))
  count=TextView(this).apply{text="WALLPAPER 001 / 1000";textSize=11f;setTextColor(0xFFA0CDFF.toInt());setPadding(0,10,0,8)};root.addView(count)
  val nav=LinearLayout(this).apply{orientation=0}
  nav.addView(btn("‹ PREVIOUS"){change(-1)},LinearLayout.LayoutParams(0,52,1f))
  nav.addView(btn("RANDOM ↻"){index=(1..1000).random();refresh()},LinearLayout.LayoutParams(0,52,1f))
  nav.addView(btn("NEXT ›"){change(1)},LinearLayout.LayoutParams(0,52,1f));root.addView(nav)
  root.addView(btn("✦  CHOOSE MY PHOTO"){picker.launch("image/*")})
  root.addView(label("3D PARALLAX INTENSITY"))
  root.addView(SeekBar(this).apply{max=100;progress=48;setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
   override fun onProgressChanged(s:SeekBar?,p:Int,u:Boolean){preview.depth=p/100f;preview.invalidate()}
   override fun onStartTrackingTouch(s:SeekBar?){};override fun onStopTrackingTouch(s:SeekBar?){}
  })})
  root.addView(label("LONG CLOCK SIZE"))
  root.addView(SeekBar(this).apply{max=100;progress=58;setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
   override fun onProgressChanged(s:SeekBar?,p:Int,u:Boolean){preview.clockSize=42f+p*.75f;preview.invalidate()}
   override fun onStartTrackingTouch(s:SeekBar?){};override fun onStopTrackingTouch(s:SeekBar?){}
  })})
  val toggles=LinearLayout(this).apply{orientation=0}
  toggles.addView(btn("CLOCK COLOR"){val a=intArrayOf(Color.WHITE,0xFFFFD27D.toInt(),0xFF8EDBFF.toInt(),0xFFFFA5D8.toInt());clockColor=a[(a.indexOf(clockColor)+1)%a.size];preview.clockColor=clockColor;preview.invalidate()},LinearLayout.LayoutParams(0,50,1f))
  toggles.addView(btn("DATE ON / OFF"){showDate=!showDate;preview.showDate=showDate;preview.invalidate()},LinearLayout.LayoutParams(0,50,1f))
  toggles.addView(btn("LONG / CLASSIC"){longClock=!longClock;preview.longClock=longClock;preview.invalidate()},LinearLayout.LayoutParams(0,50,1f));root.addView(toggles)
  val captionInput=EditText(this).apply{hint="Custom text / name";setTextColor(Color.WHITE);setHintTextColor(Color.GRAY);singleLine=true;setBackgroundColor(0xFF182236.toInt())}
  root.addView(captionInput)
  root.addView(btn("APPLY CUSTOM TEXT"){preview.caption=captionInput.text.toString().take(32);preview.invalidate()})
  root.addView(btn("⬇  EXPORT 2× PNG"){saver.launch("DepthPix-Wallpaper.png")})
  root.addView(btn("SET LIVE WALLPAPER"){
   try{getSharedPreferences("depthpix",MODE_PRIVATE).edit().putInt("design",index).putFloat("clockSize",preview.clockSize).putInt("clockColor",clockColor).putBoolean("showDate",showDate).putBoolean("longClock",longClock).putString("caption",preview.caption).apply();val i=Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply{putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,ComponentName(this@MainActivity,DepthLiveWallpaperService::class.java))};startActivity(i)}
   catch(_:Exception){status.text="Live wallpaper setting unavailable"}
  })
  status=TextView(this).apply{text="1000 original designs • offline";textSize=11f;setTextColor(Color.LTGRAY);setPadding(0,12,0,8)};root.addView(status)
  root.addView(TextView(this).apply{text="3D note: layered parallax is supported. Automatic AI photo cut-out / true foreground separation is not included.";textSize=10f;setTextColor(0xFF9AA8BC.toInt())})
  setContentView(ScrollView(this).apply{isFillViewport=true;addView(root)});refresh()
 }
 private fun label(s:String)=TextView(this).apply{text=s;textSize=10f;letterSpacing=.08f;setTextColor(0xFF73BEFF.toInt());setPadding(0,10,0,2)}
 private fun btn(s:String,a:()->Unit)=Button(this).apply{text=s;isAllCaps=false;textSize=12f;setTextColor(Color.WHITE);setBackgroundColor(0xFF1C2B43.toInt());setOnClickListener{a()}}
 private fun change(d:Int){index=(index-1+d+1000)%1000+1;refresh()}
 private fun refresh(){preview.designId=index;preview.invalidate();count.text="WALLPAPER "+index.toString().padStart(3,'0')+" / 1000"}
}

class WallpaperPreview(c:android.content.Context):View(c){
 var designId=1;var depth=.48f;var clockSize=85f;var clockColor=Color.WHITE
 var showDate=true;var longClock=true;var caption="";var photo:Bitmap?=null
 private var dx=0f;private var dy=0f
 override fun onDraw(canvas:Canvas){super.onDraw(canvas);WallpaperArt.draw(canvas,width.toFloat(),height.toFloat(),designId,dx*depth,dy*depth,clockSize,clockColor,showDate,longClock,caption,photo)}
 override fun onTouchEvent(e:MotionEvent):Boolean{if(e.action==MotionEvent.ACTION_DOWN||e.action==MotionEvent.ACTION_MOVE){dx=(e.x/max(1,width)-.5f)*2;dy=(e.y/max(1,height)-.5f)*2;invalidate()};return true}
 fun export():Bitmap{val b=Bitmap.createBitmap(max(1,width*2),max(1,height*2),Bitmap.Config.ARGB_8888);val c=Canvas(b);c.scale(2f,2f);WallpaperArt.draw(c,width.toFloat(),height.toFloat(),designId,0f,0f,clockSize,clockColor,showDate,longClock,caption,photo);return b}
}

object WallpaperArt{
 private val palettes=arrayOf(
 intArrayOf(0xFF07111F.toInt(),0xFF123B58.toInt(),0xFF36A5B8.toInt()),intArrayOf(0xFF140D26.toInt(),0xFF4C286F.toInt(),0xFFE38BBD.toInt()),
 intArrayOf(0xFF080F16.toInt(),0xFF34414C.toInt(),0xFFB9C9D5.toInt()),intArrayOf(0xFF1D100A.toInt(),0xFF75421D.toInt(),0xFFFFD27D.toInt()),
 intArrayOf(0xFF071B18.toInt(),0xFF176B58.toInt(),0xFF8DE0B5.toInt()),intArrayOf(0xFF10162F.toInt(),0xFF263D83.toInt(),0xFF8BA8FF.toInt()),
 intArrayOf(0xFF260C18.toInt(),0xFF8B244A.toInt(),0xFFFFA5C8.toInt()),intArrayOf(0xFF050505.toInt(),0xFF303030.toInt(),0xFFB4B4B4.toInt()),
 intArrayOf(0xFF0B1424.toInt(),0xFF225B9A.toInt(),0xFF77D4FF.toInt()),intArrayOf(0xFF17152A.toInt(),0xFF5B4C9B.toInt(),0xFFFFC77D.toInt()))
 fun draw(c:Canvas,w:Float,h:Float,id:Int,px:Float,py:Float,size:Float,color:Int,date:Boolean,long:Boolean,caption:String,photo:Bitmap?){
  val n=(id-1).coerceIn(0,999);val pal=palettes[n%10];val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
  c.drawColor(pal[0]);p.shader=LinearGradient(0f,0f,w,h,pal[1],pal[2],Shader.TileMode.CLAMP);c.drawRect(0f,0f,w,h,p);p.shader=null
  val r=java.util.Random((n*7919+31).toLong());val pattern=(n/10)%10
  for(k in 0 until 14){val x=r.nextFloat()*w;val y=r.nextFloat()*h;val rad=w*(.08f+r.nextFloat()*.25f);p.color=pal[2];p.alpha=20+r.nextInt(45)
   when(pattern){0,5->c.drawCircle(x+px*28,y+py*28,rad,p);1,6->c.drawRoundRect(x-rad,y-rad,x+rad,y+rad,rad*.25f,rad*.25f,p)
    2,7->{p.strokeWidth=3f;p.style=Paint.Style.STROKE;c.drawLine(x,y,x+rad*1.8f,y-rad,p);p.style=Paint.Style.FILL}
    3,8->c.drawOval(x-rad,y-rad*.35f,x+rad,y+rad*.35f,p)
    else->{val path=Path().apply{moveTo(x,y-rad);lineTo(x+rad,y+rad);lineTo(x-rad,y+rad);close()};c.drawPath(path,p)}}
   p.alpha=255
  }
  if(photo!=null){val s=max(w/photo.width,h/photo.height)*1.05f;val bw=photo.width*s;val bh=photo.height*s
   p.alpha=220;c.drawBitmap(photo,null,RectF((w-bw)/2+px*22,(h-bh)/2+py*22,(w+bw)/2+px*22,(h+bh)/2+py*22),p);p.alpha=255
   p.shader=LinearGradient(0f,h*.4f,0f,h,0x00000000,0xC9000000.toInt(),Shader.TileMode.CLAMP);c.drawRect(0f,h*.4f,w,h,p);p.shader=null
  }
  p.setShadowLayer(12f,0f,4f,0x99000000.toInt());p.color=color;p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.create("sans-serif-thin",0);p.textSize=if(long)size*1.18f else size*.8f
  val time=java.text.SimpleDateFormat(if(long)"HH:mm" else "h:mm",java.util.Locale.getDefault()).format(java.util.Date());val y=h*.57f
  if(long){val a=time.split(":");c.drawText(a[0],w/2,y-size*.12f,p);c.drawText(a.getOrElse(1){"00"},w/2,y+size*.95f,p)}else c.drawText(time,w/2,y,p)
  p.clearShadowLayer()
  if(date){p.textSize=max(12f,size*.19f);p.typeface=Typeface.create("sans-serif-medium",0);c.drawText(java.text.SimpleDateFormat("EEEE  •  d MMMM",java.util.Locale.getDefault()).format(java.util.Date()),w/2,h*.78f,p)}
  if(caption.isNotBlank()){p.textSize=max(12f,size*.22f);c.drawText(caption.take(32),w/2,h*.88f,p)}
 }
}

class DepthLiveWallpaperService:android.service.wallpaper.WallpaperService(){
 override fun onCreateEngine():Engine=DepthEngine()
 inner class DepthEngine:Engine(){
  private val handler=android.os.Handler(mainLooper);private var visible=false;private var xOffset=.5f
  private val task=object:Runnable{override fun run(){if(!visible)return;var c:Canvas?=null;try{c=surfaceHolder.lockCanvas();if(c!=null){val id=getSharedPreferences("depthpix",MODE_PRIVATE).getInt("design",1);WallpaperArt.draw(c,c.width.toFloat(),c.height.toFloat(),id,(xOffset-.5f)*1.4f,0f,118f,Color.WHITE,true,true,"",null)}}catch(_:Exception){}finally{if(c!=null)surfaceHolder.unlockCanvasAndPost(c)};handler.removeCallbacks(this);handler.postDelayed(this,1000)}}
  override fun onVisibilityChanged(v:Boolean){visible=v;handler.removeCallbacks(task);if(v)handler.post(task)}
  override fun onOffsetsChanged(x:Float,y:Float,xStep:Float,yStep:Float,xPixels:Int,yPixels:Int){xOffset=x;if(visible){handler.removeCallbacks(task);handler.post(task)}}
  override fun onSurfaceChanged(h:android.view.SurfaceHolder,f:Int,w:Int,he:Int){super.onSurfaceChanged(h,f,w,he);if(visible){handler.removeCallbacks(task);handler.post(task)}}
  override fun onDestroy(){visible=false;handler.removeCallbacks(task);super.onDestroy()}
 }
}

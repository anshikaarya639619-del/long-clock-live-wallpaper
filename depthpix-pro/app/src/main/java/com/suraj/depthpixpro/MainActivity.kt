package com.suraj.depthpixpro
import android.app.Activity
import android.graphics.*
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import kotlin.math.max
import kotlin.math.min

class MainActivity : Activity() {
 private lateinit var preview: Preview
 private lateinit var status: TextView
 private val picker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
  if (uri != null) try {
   val opts=BitmapFactory.Options().apply{inJustDecodeBounds=true}
   contentResolver.openInputStream(uri)?.use{BitmapFactory.decodeStream(it,null,opts)}
   val sample=max(1,max(opts.outWidth,opts.outHeight)/1600)
   val bmp=BitmapFactory.Options().run{inSampleSize=sample;contentResolver.openInputStream(uri)?.use{BitmapFactory.decodeStream(it,null,this)}}
   if(bmp!=null){preview.photo=bmp;preview.invalidate();status.text="Photo loaded"}else status.text="Image could not be opened"
  }catch(_:Exception){status.text="Image could not be opened"}
 }
 private val saver=registerForActivityResult(ActivityResultContracts.CreateDocument("image/png")){uri->
  if(uri!=null)try{contentResolver.openOutputStream(uri)?.use{preview.export().compress(Bitmap.CompressFormat.PNG,100,it)};status.text="PNG exported"}catch(_:Exception){status.text="Export failed"}
 }
 override fun onCreate(savedInstanceState:Bundle?){
  super.onCreate(savedInstanceState)
  val root=LinearLayout(this).apply{orientation=1;setPadding(18,28,18,18);setBackgroundColor(Color.rgb(7,10,18))}
  root.addView(TextView(this).apply{text="DEPTHPIX PRO";textSize=27f;setTextColor(Color.WHITE);typeface=Typeface.DEFAULT_BOLD})
  root.addView(TextView(this).apply{text="CINEMATIC PHOTO DEPTH STUDIO  •  OFFLINE";textSize=10f;setTextColor(Color.rgb(110,190,255));setPadding(0,5,0,12)})
  preview=Preview(this)
  root.addView(preview,LinearLayout.LayoutParams(-1,0,1f))
  status=TextView(this).apply{text="Choose a photo to start";setTextColor(Color.LTGRAY);textSize=12f}
  root.addView(status)
  root.addView(btn("＋  CHOOSE PHOTO"){picker.launch("image/*")})
  root.addView(TextView(this).apply{text="PARALLAX INTENSITY";setTextColor(Color.rgb(110,190,255));textSize=10f})
  root.addView(SeekBar(this).apply{max=100;progress=45;setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
   override fun onProgressChanged(s:SeekBar?,p:Int,u:Boolean){preview.amount=p/100f;preview.invalidate()}
   override fun onStartTrackingTouch(s:SeekBar?){}
   override fun onStopTrackingTouch(s:SeekBar?){}
  })})
  val styles=Spinner(this).apply{adapter=ArrayAdapter(this@MainActivity,android.R.layout.simple_spinner_dropdown_item,listOf("Minimal White","Luxury Gold","Ice Blue","Neon Pink"));onItemSelectedListener=object:AdapterView.OnItemSelectedListener{
   override fun onNothingSelected(p:android.widget.AdapterView<*>?){}
   override fun onItemSelected(p:android.widget.AdapterView<*>?,v:View?,pos:Int,id:Long){preview.style=pos;preview.invalidate()}
  }}
  root.addView(styles)
  val text=EditText(this).apply{hint="Custom text (optional)";setTextColor(Color.WHITE);setHintTextColor(Color.GRAY);setSingleLine()}
  root.addView(text)
  root.addView(btn("APPLY TEXT"){preview.caption=text.text.toString();preview.invalidate()})
  root.addView(btn("EXPORT WALLPAPER PNG"){if(preview.photo==null)status.text="Choose a photo first" else saver.launch("DepthPix-Wallpaper.png")})
  root.addView(TextView(this).apply{text="Honest limitation: touch-driven 2.5D movement only; no automatic AI depth segmentation. No paid API or account.";textSize=10f;setTextColor(Color.LTGRAY);setPadding(0,8,0,0)})
  setContentView(ScrollView(this).apply{addView(root)})
 }
 private fun btn(t:String,action:()->Unit)=Button(this).apply{text=t;isAllCaps=false;setOnClickListener{action()}}
 private class Preview(c:android.content.Context):View(c){
  var photo:Bitmap?=null;var amount=.45f;var style=0;var caption=""
  var xoff=0f;var yoff=0f
  private val p=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
  private val colors=intArrayOf(Color.WHITE,Color.rgb(255,205,110),Color.rgb(110,205,255),Color.rgb(255,100,190))
  override fun onDraw(c:Canvas){
   super.onDraw(c);c.drawColor(Color.rgb(20,27,40))
   val b=photo?:run{p.color=Color.LTGRAY;p.textSize=16f;c.drawText("PHOTO PREVIEW",20f,height/2f,p);return}
   val s=max(width.toFloat()/b.width,height.toFloat()/b.height);val w=b.width*s;val h=b.height*s
   c.drawBitmap(b,null,RectF((width-w)/2+xoff*amount*24,(height-h)/2+yoff*amount*24,(width+w)/2+xoff*amount*24,(height+h)/2+yoff*amount*24),p)
   c.drawRect(0f,height*.58f,width.toFloat(),height.toFloat(),Paint().apply{shader=LinearGradient(0f,height*.58f,0f,height.toFloat(),Color.TRANSPARENT,0xCC000000.toInt(),Shader.TileMode.CLAMP)})
   p.shader=null;p.color=colors[style];p.textAlign=Paint.Align.CENTER;p.typeface=Typeface.create("sans-serif-light",0);p.textSize=min(width*.16f,72f)
   c.drawText(java.text.SimpleDateFormat("HH:mm",java.util.Locale.getDefault()).format(java.util.Date()),width/2f,height*.76f,p)
   p.textSize=14f;c.drawText(java.text.SimpleDateFormat("EEEE, d MMMM",java.util.Locale.getDefault()).format(java.util.Date()),width/2f,height*.81f,p)
   if(caption.isNotBlank()){p.textSize=18f;c.drawText(caption.take(40),width/2f,height*.89f,p)}
   p.textAlign=Paint.Align.LEFT
  }
  override fun onTouchEvent(e:MotionEvent):Boolean{if(e.action==MotionEvent.ACTION_DOWN||e.action==MotionEvent.ACTION_MOVE){xoff=(e.x/width-.5f)*2;yoff=(e.y/height-.5f)*2;invalidate()};return true}
  fun export():Bitmap{val b=Bitmap.createBitmap(max(1,width),max(1,height),Bitmap.Config.ARGB_8888);draw(Canvas(b));return b}
 }
}

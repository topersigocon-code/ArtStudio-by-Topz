package com.topz.artstudio

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

class DrawingCanvasView(context: Context) : View(context) {
    private var bitmap = Bitmap.createBitmap(1600, 1600, Bitmap.Config.ARGB_8888)
    private var canvasBitmap = Canvas(bitmap)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 12f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val path = Path()
    private val undo = ArrayDeque<Bitmap>()
    private val redo = ArrayDeque<Bitmap>()
    private var drawing = false
    private var eraser = false
    private var scale = 1f
    private var offsetX = 0f
    private var offsetY = 0f
    private var lastX = 0f
    private var lastY = 0f
    private val scaleDetector = ScaleGestureDetector(context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                scale = (scale * detector.scaleFactor).coerceIn(0.2f, 8f)
                invalidate()
                return true
            }
        })

    init { setLayerType(View.LAYER_TYPE_SOFTWARE, null) }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.rgb(55, 60, 70))
        val fit = min(width.toFloat() / bitmap.width, height.toFloat() / bitmap.height)
        val s = fit * scale
        val left = (width - bitmap.width * s) / 2f + offsetX
        val top = (height - bitmap.height * s) / 2f + offsetY
        canvas.save()
        canvas.translate(left, top)
        canvas.scale(s, s)
        val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        canvas.drawRect(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat(), shadow)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        if (event.pointerCount > 1) return true
        val fit = min(width.toFloat() / bitmap.width, height.toFloat() / bitmap.height)
        val s = fit * scale
        val left = (width - bitmap.width * s) / 2f + offsetX
        val top = (height - bitmap.height * s) / 2f + offsetY
        val x = (event.x - left) / s
        val y = (event.y - top) / s

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (x !in 0f..bitmap.width.toFloat() || y !in 0f..bitmap.height.toFloat()) return true
                pushUndo()
                redo.clear()
                path.reset()
                path.moveTo(x, y)
                lastX = x; lastY = y
                drawing = true
                return true
            }
            MotionEvent.ACTION_MOVE -> if (drawing) {
                path.quadTo(lastX, lastY, (x + lastX) / 2f, (y + lastY) / 2f)
                lastX = x; lastY = y
                paint.xfermode = if (eraser) PorterDuffXfermode(PorterDuff.Mode.CLEAR) else null
                canvasBitmap.drawPath(path, paint)
                path.reset()
                path.moveTo(lastX, lastY)
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                drawing = false
                paint.xfermode = if (eraser) PorterDuffXfermode(PorterDuff.Mode.CLEAR) else null
                canvasBitmap.drawPath(path, paint)
                path.reset()
                invalidate()
                return true
            }
        }
        return true
    }

    private fun snapshot(): Bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    private fun pushUndo() {
        if (undo.size >= 15) undo.removeFirst()
        undo.addLast(snapshot())
    }
    fun undo() {
        if (undo.isNotEmpty()) {
            redo.addLast(snapshot())
            bitmap = undo.removeLast()
            canvasBitmap = Canvas(bitmap)
            invalidate()
        }
    }
    fun redo() {
        if (redo.isNotEmpty()) {
            pushUndo()
            bitmap = redo.removeLast()
            canvasBitmap = Canvas(bitmap)
            invalidate()
        }
    }
    fun setColor(color: Int) {
        eraser = false
        paint.color = color
        paint.xfermode = null
    }
    fun setEraser(enabled: Boolean) {
        eraser = enabled
        paint.xfermode = if (enabled) PorterDuffXfermode(PorterDuff.Mode.CLEAR) else null
    }
    fun clearCanvas() {
        pushUndo()
        redo.clear()
        bitmap.eraseColor(Color.TRANSPARENT)
        invalidate()
    }
    fun loadImage(file: File) {
        BitmapFactory.decodeFile(file.absolutePath)?.let {
            bitmap = it.copy(Bitmap.Config.ARGB_8888, true)
            canvasBitmap = Canvas(bitmap)
            invalidate()
        }
    }
    fun saveImage(file: File) {
        file.parentFile?.mkdirs()
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}

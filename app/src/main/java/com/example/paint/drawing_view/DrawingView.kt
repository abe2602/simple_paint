package com.example.paint.drawing_view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    // Where our draws exist
    private var canvas: Canvas? = null

    // Bitmap to be used on canvas
    private var canvasBitmap: Bitmap? = null

    // Path from the draws
    private var drawPath: CustomPath? = null

    // Contains information about how to draw thing into canvas
    private var drawPaint: Paint? = null

    private var canvasPaint: Paint? = null

    // Draw Paths
    private val paths = ArrayList<CustomPath>()

    // Brush Color
    private var colors: MutableList<Int> = mutableListOf(Color.BLACK, Color.WHITE, Color.WHITE, Color.WHITE)
    private var currentColor = Color.BLACK
    // Brush Size
    private var brushSize: Float = 0.toFloat()

    private var currentColorIndex = 1

    init {
        drawPaint = Paint()
        drawPaint?.color = currentColor
        drawPaint?.style = Paint.Style.STROKE
        drawPaint?.strokeJoin = Paint.Join.ROUND
        drawPaint?.strokeCap = Paint.Cap.ROUND
        drawPaint?.isAntiAlias = true

        drawPath = CustomPath(colors[0], brushSize)

        canvasPaint = Paint(Paint.DITHER_FLAG)
        canvasPaint?.isAntiAlias = true

        brushSize = 20.toFloat()
    }

    // Instantiate the canvas here to access the screen size
    override fun onSizeChanged(w: Int, h: Int, wprev: Int, hprev: Int) {
        super.onSizeChanged(w, h, wprev, hprev)
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvasBitmap?.let {
            canvas = Canvas(it)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvasBitmap?.let {
            canvas?.drawBitmap(it, 0f, 0f, canvasPaint)
        }

        // Want to retain what is being drawn
        for(path in paths) {
            drawPaint?.let {
                it.strokeWidth = path.brushThickness
                it.color = path.color
                canvas?.drawPath(path, it)
            }
        }

        drawPath?.let {
            if (!it.isEmpty) {
                drawPaint?.strokeWidth = drawPath?.brushThickness ?: 20.toFloat()
                drawPaint?.color = drawPath?.color ?: Color.BLACK
                drawPaint?.let { drawPaint ->
                    canvas?.drawPath(it, drawPaint)
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        val x = event?.x ?: 0.toFloat()
        val y = event?.y ?: 0.toFloat()

        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                // where, and how, to start to drawing
                drawPath?.color = currentColor
                drawPath?.brushThickness = brushSize
                drawPath?.reset()
                drawPath?.moveTo(x , y)
            }

            MotionEvent.ACTION_MOVE -> {
                // draw the lines
                drawPath?.lineTo(x, y)
            }

            MotionEvent.ACTION_UP -> {
                drawPath?.let {
                    paths.add(it)
                }
                drawPath = CustomPath(currentColor, brushSize)
            }

            else -> return false
        }
        invalidate()
        return true
    }

    fun undoLastDraw() {
        if(paths.isNotEmpty()) {
            paths.remove(paths.last())
            invalidate()
        }
    }

    fun setBrushColor(color: Int) {
        currentColor =  color
    }

    fun addUsedColorToColorsBar(color: Int) {
        if(currentColorIndex == 4) {
            currentColorIndex = 1
        }
        colors[currentColorIndex] = color
        currentColorIndex++
    }

    fun getPreviousColors() : List<Int> = colors

    fun getUsedColor(index: Int) : Int = colors[index]

    fun setBrushSize(brushSize: Float) {
        this.brushSize = brushSize
    }
}
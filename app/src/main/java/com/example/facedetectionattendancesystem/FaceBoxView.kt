package com.example.facedetectionattendancesystem

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.google.mlkit.vision.face.Face

// This class now holds all the information needed for drawing.
data class DisplayableFace(
    val box: Rect,
    val trackingId: Int?,
    val name: String?
)

class FaceBoxView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    // This list is now public to allow MainActivity to access tracking IDs for enrollment.
    val displayableFaces = mutableListOf<DisplayableFace>()

    private var imageWidth = 0
    private var imageHeight = 0
    private var isFrontCamera = false

    private val boxPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.RED
        strokeWidth = 6.0f
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 40f
    }

    fun update(faces: List<DisplayableFace>, width: Int, height: Int, isFront: Boolean, enrolledFaces: Map<String, String>) {
        displayableFaces.clear()
        imageWidth = width
        imageHeight = height
        isFrontCamera = isFront
        displayableFaces.addAll(faces)
        postInvalidate() // Trigger a redraw
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (imageWidth == 0 || imageHeight == 0) return

        for (face in displayableFaces) {
            // Scale and mirror the coordinates just before drawing.
            val scaledBox = Rect(
                scaleX(face.box.left.toFloat()).toInt(),
                scaleY(face.box.top.toFloat()).toInt(),
                scaleX(face.box.right.toFloat()).toInt(),
                scaleY(face.box.bottom.toFloat()).toInt()
            )

            canvas.drawRect(scaledBox, boxPaint)
            face.name?.let {
                canvas.drawText(it, scaledBox.left.toFloat(), scaledBox.top.toFloat() - 10, textPaint)
            }
        }
    }

    private fun scaleX(x: Float): Float {
        val viewWidth = width.toFloat()
        val scale = viewWidth / imageWidth.toFloat()
        val scaledX = x * scale
        return if (isFrontCamera) viewWidth - scaledX else scaledX
    }

    private fun scaleY(y: Float): Float {
        val viewHeight = height.toFloat()
        val scale = viewHeight / imageHeight.toFloat()
        return y * scale
    }
}

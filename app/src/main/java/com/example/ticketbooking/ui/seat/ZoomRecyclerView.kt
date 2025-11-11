package com.example.ticketbooking.ui.seat

import android.content.Context
import android.util.AttributeSet
import android.view.ScaleGestureDetector
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max
import kotlin.math.min

class ZoomRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : RecyclerView(context, attrs) {

    private var scaleFactor = 1.0f
    private val minScale = 1.0f
    private val maxScale = 2.5f

    private val scaleDetector = ScaleGestureDetector(context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                scaleFactor *= detector.scaleFactor
                scaleFactor = max(minScale, min(scaleFactor, maxScale))
                scaleX = scaleFactor
                scaleY = scaleFactor
                return true
            }
        }
    )

    private val gestureDetector = GestureDetector(context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                if (scaleFactor <= 1.0f) return false

                // Move opposite to the finger movement
                translationX -= distanceX
                translationY -= distanceY

                // Clamp translations so content stays within bounds
                clampTranslation()
                return true
            }
        }
    )

    override fun onTouchEvent(e: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(e)
        gestureDetector.onTouchEvent(e)
        return super.onTouchEvent(e)
    }

    private fun clampTranslation() {
        val maxTransX = (width * (scaleFactor - 1f)) / 2f
        val maxTransY = (height * (scaleFactor - 1f)) / 2f

        translationX = when {
            translationX > maxTransX -> maxTransX
            translationX < -maxTransX -> -maxTransX
            else -> translationX
        }
        translationY = when {
            translationY > maxTransY -> maxTransY
            translationY < -maxTransY -> -maxTransY
            else -> translationY
        }
    }
}
package com.example.focusstart5

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class CustomView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attributeSet, defStyleAttr, defStyleRes) {

    private companion object {
        const val SUPER_STATE = "super_state"
    }

    private var size = 800
    private var bgColor: Int? = null
    private var borderColor: Int? = null
    private var arrowColor: Int? = null
    private var initArrowColor: Int? = null
    private var textColor: Int? = null
    private var borderWidth = 10.0f

    private var speed = 0f
    private val fullAccelerateTime = 10000
    private val fullDecelerateTime = 5000
    private var radius: Float = size * 0.8f / 2f
    private var centerY = size / 2 - 0.05f * size
    private var centerX = size / 2f

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val speedometerBackground = RectF()

    private val arrowColorValueAnimator = ValueAnimator()
    private val pressGasSpeedValueAnimator = ValueAnimator()
    private val clickGasSpeedValueAnimator = ValueAnimator()
    private val pressStopSpeedValueAnimator = ValueAnimator()
    private val clickStopSpeedValueAnimator = ValueAnimator()
    private val naturalInhibitionSpeedValueAnimator = ValueAnimator()

    init {
        val typedArray = context.obtainStyledAttributes(
            attributeSet,
            R.styleable.CustomView,
            defStyleAttr,
            defStyleRes
        )
        try {
            bgColor = typedArray.getColor(R.styleable.CustomView_colorBackground, Color.LTGRAY)
            borderColor = typedArray.getColor(R.styleable.CustomView_borderColor, Color.BLACK)
            initArrowColor = typedArray.getColor(R.styleable.CustomView_arrowColor, Color.RED)
            arrowColor = initArrowColor
            textColor = typedArray.getColor(R.styleable.CustomView_textColor, Color.BLACK)
        } finally {
            typedArray.recycle()
        }
    }

    override fun onSaveInstanceState(): Parcelable? =
        Bundle().apply {
            putFloat("speed", speed)
            putParcelable(SUPER_STATE, super.onSaveInstanceState())
        }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var superState = state

        if (state is Bundle) {
            this.speed = state.getFloat("speed")
            Log.e("this.speed", this.speed.toString())
            this.updatePaintByNaturalInhibition()
            superState = state.getParcelable(SUPER_STATE)
        }
        super.onRestoreInstanceState(superState)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = measureDimension(size, widthMeasureSpec)
        val height = measureDimension(size / 2, heightMeasureSpec)
        setMeasuredDimension(width, height)
        size = min(width, height)
        Log.e("size", size.toString())
        onSizeChanged()
    }

    private fun measureDimension(minSize: Int, measureSpec: Int): Int {
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        return when (specMode) {
            MeasureSpec.EXACTLY -> specSize
            MeasureSpec.AT_MOST -> minSize.coerceAtMost(specSize)
            else -> minSize
        }
    }

    private fun onSizeChanged() {
        radius = size / 2f * 0.8f
        Log.e("radius", radius.toString())
        centerY = size / 2 - 0.05f * size
        Log.e("centerY", centerY.toString())
        centerX = size / 2f
        Log.e("centerX", centerX.toString())
    }

    private fun startArrowColorValueAnimation() {
        if (!arrowColorValueAnimator.isStarted) {
            arrowColorValueAnimator.apply {
                setIntValues(
                    initArrowColor!!,
                    bgColor!!
                )
                setEvaluator(ArgbEvaluator())
                addUpdateListener {
                    (this.animatedValue as? Int)?.let { arrowColor = it }
                    invalidate()
                }
                duration = 400
                start()
                repeatCount = ValueAnimator.INFINITE
            }
        }
    }

    private fun stopArrowColorValueAnimation() {
        if (arrowColorValueAnimator.isStarted) {
            arrowColorValueAnimator.cancel()
            arrowColor = initArrowColor
        }
    }

    private fun pressGasSpeedValueAnimation() {
        pressGasSpeedValueAnimator.apply {
            setFloatValues(speed, 180f)
            addUpdateListener {
                (this.animatedValue as? Float)?.let {
                    speed = it.toInt().toFloat()
                    if (it >= 100) {
                        startArrowColorValueAnimation()
                    } else {
                        stopArrowColorValueAnimation()
                    }
                }
                invalidate()
            }
            interpolator = AccelerateDecelerateInterpolator()
            duration = (fullAccelerateTime * (1 - speed / 180)).toLong()
            start()
        }
    }

    private fun clickGasSpeedValueAnimation() {
        clickGasSpeedValueAnimator.apply {
            var finishSpeed = speed
            finishSpeed = if (speed + 3 < 180) speed + 3
            else 180f
            setFloatValues(speed, finishSpeed)
            addUpdateListener {
                (this.animatedValue as? Float)?.let {
                    speed = it.toInt().toFloat()
                    if (it >= 100) {
                        startArrowColorValueAnimation()
                    } else {
                        stopArrowColorValueAnimation()
                    }
                }
                invalidate()
            }
            interpolator = AccelerateDecelerateInterpolator()
            duration = ((fullAccelerateTime / 180) * (finishSpeed - speed)).toLong()
            doOnEnd { updatePaintByNaturalInhibition() }
            start()
        }
    }

    private fun pressStopSpeedValueAnimation() {
        pressStopSpeedValueAnimator.apply {
            setFloatValues(speed, 0f)
            addUpdateListener {
                (this.animatedValue as? Float)?.let {
                    speed = it.toInt().toFloat()
                    if (it >= 100) {
                        startArrowColorValueAnimation()
                    } else {
                        stopArrowColorValueAnimation()
                    }
                }
                invalidate()
            }
            interpolator = LinearInterpolator()
            duration = (fullDecelerateTime * speed / 180).toLong()
            start()
        }
    }

    private fun clickStopSpeedValueAnimation() {
        clickStopSpeedValueAnimator.apply {
            var finishSpeed = speed
            finishSpeed = if (speed - 6 > 0) speed - 6
            else 0f
            setFloatValues(speed, finishSpeed)
            addUpdateListener {
                (this.animatedValue as? Float)?.let {
                    speed = it.toInt().toFloat()
                    if (it >= 100) {
                        startArrowColorValueAnimation()
                    } else {
                        stopArrowColorValueAnimation()
                    }
                }
                invalidate()
            }
            interpolator = LinearInterpolator()
            duration = ((fullDecelerateTime / 180) * (speed - finishSpeed)).toLong()
            doOnEnd { updatePaintByNaturalInhibition() }
            start()
        }
    }

    private fun naturalInhibitionSpeedValueAnimation() {
        naturalInhibitionSpeedValueAnimator.apply {
            setFloatValues(speed, 0f)
            addUpdateListener {
                (this.animatedValue as? Float)?.let {
                    speed = it.toInt().toFloat()
                    if (it >= 100) {
                        startArrowColorValueAnimation()
                    } else {
                        stopArrowColorValueAnimation()
                    }
                }
                invalidate()
            }
            interpolator = DecelerateInterpolator()
            duration = (60000 * speed / 180).toLong()
            start()
        }
    }

    private fun stopAllAnimations() {
        if (pressGasSpeedValueAnimator.isStarted) pressGasSpeedValueAnimator.cancel()
        if (clickGasSpeedValueAnimator.isStarted) clickGasSpeedValueAnimator.cancel()
        if (pressStopSpeedValueAnimator.isStarted) pressStopSpeedValueAnimator.cancel()
        if (clickStopSpeedValueAnimator.isStarted) clickStopSpeedValueAnimator.cancel()
        if (naturalInhibitionSpeedValueAnimator.isStarted) naturalInhibitionSpeedValueAnimator.cancel()
        if (arrowColorValueAnimator.isStarted) arrowColorValueAnimator.cancel()
    }

    fun updatePaintByPressGas() {
        stopAllAnimations()
        pressGasSpeedValueAnimation()
    }

    fun updatePaintByClickGas() {
        stopAllAnimations()
        clickGasSpeedValueAnimation()
    }

    fun updatePaintByPressStop() {
        stopAllAnimations()
        pressStopSpeedValueAnimation()
    }

    fun updatePaintByClickStop() {
        stopAllAnimations()
        clickStopSpeedValueAnimation()
    }

    fun updatePaintByNaturalInhibition() {
        stopAllAnimations()
        naturalInhibitionSpeedValueAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        speedometerBackground.apply {
            set(
                centerX - radius, centerY - radius, centerX + radius,
                centerY + radius
            )
        }

        paint.color = bgColor!!
        paint.style = Paint.Style.FILL
        canvas.drawArc(speedometerBackground, -180f, 180f, true, paint)//фон спидометра

        paint.color = arrowColor!!
        paint.style = Paint.Style.STROKE
        canvas.drawLine(
            centerX,
            centerY,
            centerX - radius * cos(speed * PI.toFloat() / 180),
            centerY - radius * sin(speed * PI.toFloat() / 180),
            paint
        )//стрелка спидометра

        paint.color = borderColor!!
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = borderWidth
        canvas.drawArc(speedometerBackground, -180f, 180f, true, paint)//границы спидометра

        paint.color = textColor!!
        paint.textSize = 0.05f * size
        paint.style = Paint.Style.FILL
        canvas.drawText(
            "$speed km/h",
            centerX - paint.textSize * 2,
            size / 2f,
            paint
        )//текущая скорость
        canvas.drawText("0", paint.textSize, centerY + paint.textSize * 0.3f, paint)
        canvas.drawText("10", paint.textSize * 0.7f, centerY - paint.textSize * 1.15f, paint)
        canvas.drawText("20", paint.textSize * 1.1f, centerY - paint.textSize * 2.55f, paint)
        canvas.drawText("30", paint.textSize * 1.7f, centerY - paint.textSize * 4f, paint)
        canvas.drawText("40", paint.textSize * 2.63f, centerY - paint.textSize * 5.25f, paint)
        canvas.drawText("50", paint.textSize * 3.62f, centerY - paint.textSize * 6.3f, paint)
        canvas.drawText("60", paint.textSize * 4.92f, centerY - paint.textSize * 7.25f, paint)
        canvas.drawText("70", paint.textSize * 6.4f, centerY - paint.textSize * 7.8f, paint)
        canvas.drawText("80", paint.textSize * 7.9f, centerY - paint.textSize * 8.2f, paint)
        canvas.drawText("90", paint.textSize * 9.5f, centerY - paint.textSize * 8.3f, paint)
        canvas.drawText("100", paint.textSize * 10.8f, centerY - paint.textSize * 8.2f, paint)
        canvas.drawText("110", paint.textSize * 12.4f, centerY - paint.textSize * 7.8f, paint)
        canvas.drawText("120", paint.textSize * 13.9f, centerY - paint.textSize * 7.15f, paint)
        canvas.drawText("130", paint.textSize * 15.2f, centerY - paint.textSize * 6.3f, paint)
        canvas.drawText("140", paint.textSize * 16.2f, centerY - paint.textSize * 5.25f, paint)
        canvas.drawText("150", paint.textSize * 17.1f, centerY - paint.textSize * 4f, paint)
        canvas.drawText("160", paint.textSize * 17.7f, centerY - paint.textSize * 2.55f, paint)
        canvas.drawText("170", paint.textSize * 18.1f, centerY - paint.textSize * 1.15f, paint)
        canvas.drawText("180", paint.textSize * 18.2f, centerY + paint.textSize * 0.3f, paint)
    }
}
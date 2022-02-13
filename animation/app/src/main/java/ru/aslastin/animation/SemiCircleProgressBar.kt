package ru.aslastin.animation

import android.animation.ValueAnimator
import android.animation.ValueAnimator.INFINITE
import android.animation.ValueAnimator.REVERSE
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import kotlin.math.max
import kotlin.math.min


class SemiCircleProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs), ValueAnimator.AnimatorUpdateListener {

    // For drawing
    private val borderPath = Path()
    private val borderRect = RectF()
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        width = 10f
    }

    private val fillPath = Path()
    private val fillRect = RectF()
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    var colorBorder = Default.COLOR_BORDER
    var colorFill = Default.COLOR_FILL
    var minValue = Default.MIN_VALUE
    var maxValue = Default.MAX_VALUE
    var progress = Default.CUR_VALUE
        set(value) {
            if (value < minValue || value > maxValue) return
            field = value
            if (curValue > field) {
                curValue = field
            }
            invalidate()
        }

    var curValue = Default.MIN_VALUE
        private set
    private var shift = -1

    // Helpful stuff
    private var thickness: Float = 0f
    private var width: Float = 0f

    // Animators
    private var animator: ValueAnimator? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        animator?.cancel()
        animator = ValueAnimator.ofInt(100, 255).apply {
            duration = 1000L
            repeatCount = INFINITE
            repeatMode = REVERSE
            addUpdateListener(this@SemiCircleProgressBar)
            start()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
        animator = null
    }

    init {
        val a: TypedArray =
            context.obtainStyledAttributes(attrs, R.styleable.SemiCircleProgressBar, 0, 0)
        try {
            colorBorder =
                a.getColor(R.styleable.SemiCircleProgressBar_colorFill, Default.COLOR_BORDER)
            colorFill =
                a.getColor(R.styleable.SemiCircleProgressBar_colorBorder, Default.COLOR_FILL)
            minValue = a.getColor(R.styleable.SemiCircleProgressBar_minValue, Default.MIN_VALUE)
            maxValue = a.getColor(R.styleable.SemiCircleProgressBar_maxValue, Default.MAX_VALUE)
            progress = a.getColor(R.styleable.SemiCircleProgressBar_progress, Default.CUR_VALUE)
            curValue = minValue
        } finally {
            a.recycle()
        }

        borderPaint.color = colorBorder
        fillPaint.color = colorFill
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        var desiredHeight = width / 2
        desiredHeight += (desiredHeight * Default.PADDING_COEF).toInt()
        setMeasuredDimension(
            width,
            when (MeasureSpec.getMode(heightMeasureSpec)) {
                MeasureSpec.AT_MOST -> min(height, desiredHeight)
                MeasureSpec.EXACTLY -> height
                MeasureSpec.UNSPECIFIED -> desiredHeight
                else -> desiredHeight
            }
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        thickness = w / 8f
        width = w.toFloat()

        borderRect.set(0f, 0f, width, width)

        borderPath.reset()
        borderPath.moveTo(0f, width / 2)
        borderPath.arcTo(borderRect, 180f, 180f)
        borderPath.rLineTo(-thickness, 0f)

        borderRect.inset(thickness, thickness)

        borderPath.addArc(borderRect, 0f, -180f)
        borderPath.rLineTo(-thickness, 0f)
    }

    override fun onAnimationUpdate(animation: ValueAnimator?) {
        if (animation == null) return

        fillPaint.alpha = animation.animatedValue as Int

        val degreedValue = toDegree(nextValue())

        fillPath.reset()

        fillRect.set(0f, 0f, width, width)

        fillPath.moveTo(0f, width / 2)
        fillPath.arcTo(fillRect, 180f, degreedValue)

        fillRect.inset(thickness, thickness)

        fillPath.arcTo(fillRect, 180 + degreedValue, -degreedValue)
        fillPath.rLineTo(-thickness, 0f)

        invalidate()
    }

    private fun nextValue(): Int {
        if (curValue < progress) {
            curValue += if (shift != -1) shift else 1
            return curValue
        }
        shift = -1
        curValue = progress
        return progress
    }

    private fun toDegree(value: Int) =
        if (maxValue != minValue) ((value - minValue) * 180).toFloat() / (maxValue - minValue) else 180f

    override fun onDraw(canvas: Canvas?) {
        if (canvas == null) return
        val save = canvas.save()
        canvas.drawPath(borderPath, borderPaint)
        canvas.drawPath(fillPath, fillPaint)
        canvas.restoreToCount(save)
    }

    override fun onSaveInstanceState(): Parcelable {
        return InstanceState(
            super.onSaveInstanceState(),
            colorBorder,
            colorFill,
            minValue,
            maxValue,
            progress,
            curValue
        )
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        state as InstanceState
        super.onRestoreInstanceState(state.superState)
        colorBorder = state.colorBorder
        colorFill = state.colorFill
        minValue = state.minValue
        maxValue = state.maxValue
        progress = state.progress
        curValue = state.prevValue
    }

    private class InstanceState : BaseSavedState {
        val colorBorder: Int
        val colorFill: Int
        val minValue: Int
        val maxValue: Int
        val progress: Int
        val prevValue: Int

        constructor(
            superState: Parcelable?,
            colorBorder: Int,
            colorFill: Int,
            minValue: Int,
            maxValue: Int,
            progress: Int,
            prevValue: Int
        ) : super(superState) {
            this.colorBorder = colorBorder
            this.colorFill = colorFill
            this.minValue = minValue
            this.maxValue = maxValue
            this.progress = progress
            this.prevValue = prevValue
        }

        constructor(parcel: Parcel) : super(parcel) {
            colorBorder = parcel.readInt()
            colorFill = parcel.readInt()
            minValue = parcel.readInt()
            maxValue = parcel.readInt()
            progress = parcel.readInt()
            prevValue = parcel.readInt()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(colorBorder)
            out.writeInt(colorFill)
            out.writeInt(minValue)
            out.writeInt(maxValue)
            out.writeInt(progress)
            out.writeInt(prevValue)
        }

        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<InstanceState> {
                override fun createFromParcel(source: Parcel): InstanceState = InstanceState(source)
                override fun newArray(size: Int): Array<InstanceState?> = arrayOfNulls(size)
            }
        }
    }

    fun speedUp() {
        shift = max(1, (progress - curValue) / 20)
    }

    fun restart() {
        progress = 0
        curValue = 0
    }

    object Default {
        const val COLOR_BORDER = Color.BLACK
        const val COLOR_FILL = Color.CYAN
        const val MIN_VALUE = 0
        const val MAX_VALUE = 100
        const val CUR_VALUE = MIN_VALUE

        const val PADDING_COEF = 0.05
    }
}

package com.petterp.floatingview

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import kotlin.math.abs
import kotlin.math.min

/**
 * 仿截图效果的浮动view
 * @author petterp
 */
class FloatingView @JvmOverloads constructor(
    context: Context,
    @LayoutRes
    private val layoutId: Int = 0,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    /** y轴额外允许的滑动距离 */
    private var downTouchX = 0F
    private var downTouchY = 0F
    private var touchDownId = 0
    private var view: View? = null
    private var scaledTouchSlop = 0
    private var yAdditionalHeight = 0F
    private val mMaximumVelocity: Float
    private val mMinimumVelocity: Float
    private var mVelocityTracker: VelocityTracker? = null

    init {
        isClickable = true
        if (layoutId != 0) view = inflate(context, layoutId, this)
        layoutParams = LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.END or Gravity.TOP
        }
        setBackgroundColor(Color.TRANSPARENT)
        val configuration = ViewConfiguration.get(context)
        scaledTouchSlop = configuration.scaledTouchSlop
        mMaximumVelocity = configuration.scaledMaximumFlingVelocity.toFloat()
        mMinimumVelocity = configuration.scaledMinimumFlingVelocity.toFloat()
        // 允许额外滑动的距离
        yAdditionalHeight = y + 50.dp
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        var intercepted = false
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                initTouchDown(ev)
                mVelocityTracker = VelocityTracker.obtain()
            }

            MotionEvent.ACTION_MOVE -> {
                intercepted = abs(downTouchX - ev.x) >= scaledTouchSlop
            }
        }
        return intercepted
    }

    private fun initTouchDown(ev: MotionEvent) {
        touchDownId = ev.getPointerId(ev.actionIndex)
        downTouchX = ev.getX(ev.actionIndex)
        downTouchY = ev.getY(ev.actionIndex)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (touchDownId == INVALID_TOUCH_ID) {
                    val eventX = event.getX(event.actionIndex)
                    val eventY = event.getY(event.actionIndex)
                    if (eventX >= 0 && eventX <= width && eventY >= 0 && eventY <= height) {
                        initTouchDown(event)
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (touchDownId == INVALID_TOUCH_ID) {
                    return super.onTouchEvent(event)
                }
                val pointIdx = event.findPointerIndex(touchDownId)
                if (pointIdx != INVALID_TOUCH_IDX) {
                    // 屏蔽x轴的移动
//                    x = event.getX(pointIdx)
                    val disY =
                        min(yAdditionalHeight, y.plus(event.getY(pointIdx)).minus(downTouchY))
                    y = disY
                    mVelocityTracker?.addMovement(event)
                }
            }

            MotionEvent.ACTION_POINTER_UP -> {
                if (event.getPointerId(event.actionIndex) == touchDownId) {
                    actionTouchCancel()
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                actionTouchCancel()
            }
        }
        return super.onTouchEvent(event)
    }

    private fun actionTouchCancel() {
        touchDownId = INVALID_TOUCH_ID
        moveToEdge()
    }

    private fun moveToEdge() {
        val viewHeight = -(view?.height ?: 0)
        mVelocityTracker?.let {
            it.computeCurrentVelocity(1000, mMaximumVelocity)
            val velocity = it.yVelocity
            it.recycle()
            mVelocityTracker = null
            if (abs(velocity) > mMinimumVelocity) {
                // 上滑时
                if (velocity < 0) {
                    animate().translationY(viewHeight.toFloat()).setDuration(ANIMATE_SCALE_TIME)
                        .start()
                    return
                }
            }
        }
        if (y <= -viewHeight / 2) {
            animate().translationY(viewHeight.toFloat()).setDuration(ANIMATE_SCALE_TIME).start()
            return
        }
    }

    fun reset() {
        animate().translationY(0F).setDuration(ANIMATE_SCALE_TIME).start()
    }

    companion object {
        private const val INVALID_TOUCH_ID = -1
        private const val INVALID_TOUCH_IDX = -1
        private const val ANIMATE_SCALE_TIME = 200L
    }
}

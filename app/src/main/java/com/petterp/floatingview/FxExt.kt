package com.petterp.floatingview

import android.app.Activity
import android.content.res.Resources
import android.util.TypedValue
import android.widget.FrameLayout

/**
 *
 * @author petterp
 */

val Activity.contentView: FrameLayout?
    get() = try {
        window.decorView.findViewById(android.R.id.content)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

val Number.dp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()

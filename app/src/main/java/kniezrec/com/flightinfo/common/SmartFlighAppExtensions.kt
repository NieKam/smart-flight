package kniezrec.com.flightinfo.common

import android.content.Context
import android.content.ContextWrapper
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Looper
import android.text.Html
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import kniezrec.com.flightinfo.R
import org.osmdroid.views.overlay.Marker

/**
 * Copyright by Kamil Niezrecki
 */

fun String.Companion.empty(): String = ""

fun Marker.setCustomMarker(context: Context, @DrawableRes drawableResId: Int) {
    val color = ContextCompat.getColor(context, R.color.purple_dark)
    val porterDuffColorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)

    val drawable = ContextCompat.getDrawable(context, drawableResId)
    drawable?.colorFilter = porterDuffColorFilter
    this.setIcon(drawable)
}

fun Boolean.toggle(): Boolean {
    return !this
}

fun requireNonUi() = require(Looper.getMainLooper() != Looper.myLooper())

fun String.fromHtml(): CharSequence {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(this)
    }
}

fun <T> Context.findContextOfType(clazz: Class<out T>): T? {
    var ctx = this
    while (!clazz.isInstance(this)) {
        if (this is ContextWrapper) {
            val baseContext = this.baseContext
            if (this === baseContext) {
                return null
            } else {
                ctx = baseContext
            }
        } else {
            return null
        }
    }

    return ctx as T
}
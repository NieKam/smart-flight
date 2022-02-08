package kniezrec.com.flightinfo.common

import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.view.MotionEvent
import android.widget.TextView
import timber.log.Timber

/**
 * Copyright by Kamil Niezrecki
 */
class MovementCheck : LinkMovementMethod() {

  override fun onTouchEvent(widget: TextView?, buffer: Spannable?, event: MotionEvent?): Boolean =
      try {
        super.onTouchEvent(widget, buffer, event)
      } catch (ex: Exception) {
        Timber.e(ex, "Cannot load link")
        true
      }
}
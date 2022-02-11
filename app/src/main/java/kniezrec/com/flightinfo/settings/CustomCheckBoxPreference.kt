package kniezrec.com.flightinfo.settings

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.preference.CheckBoxPreference
import androidx.preference.PreferenceViewHolder
import kniezrec.com.flightinfo.R


/**
 * Copyright by Kamil Niezrecki
 */
class CustomCheckBoxPreference(context: Context, attrs: AttributeSet) :
    CheckBoxPreference(context, attrs) {

  var animateBackground = false

  override fun onBindViewHolder(holder: PreferenceViewHolder) {
    super.onBindViewHolder(holder)
    if (animateBackground) {
      startBackgroundAnimation(holder.itemView)
      // Prevent animating when user tap on the preference because onBindViewHolder is called once again.
      animateBackground = false
    }
  }

  private fun startBackgroundAnimation(itemView: View?) {
    if (itemView == null) {
      return
    }

    val colorFrom = ContextCompat.getColor(context, R.color.cyan_light)
    val colorTo = ContextCompat.getColor(context, R.color.purple_dark)
    ValueAnimator().apply {
      setIntValues(colorFrom, colorTo)
      setEvaluator(ArgbEvaluator())
      addUpdateListener { animator -> itemView.setBackgroundColor(animator.animatedValue as Int) }
      duration = 1350
    }.start()
  }
}
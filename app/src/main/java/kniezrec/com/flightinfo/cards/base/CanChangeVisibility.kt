package kniezrec.com.flightinfo.cards.base

/**
 * Copyright by Kamil Niezrecki
 */
interface CanChangeVisibility {

  fun registerOnVisibilityChangedListener(listener: OnVisibilityChangedListener)

  interface OnVisibilityChangedListener {

    fun onVisibilityChanged(isVisible: Boolean)
  }
}
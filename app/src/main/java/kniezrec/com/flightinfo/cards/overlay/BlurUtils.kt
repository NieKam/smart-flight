package kniezrec.com.flightinfo.cards.overlay

import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import jp.wasabeef.blurry.Blurry
import kniezrec.com.flightinfo.R
import timber.log.Timber

/**
 * Copyright by Kamil Niezrecki
 */
class BlurUtils(private val root: ViewGroup) {
    private val mOverlayView = root.findViewById<OverlayView>(R.id.overlay_view)
    private var mBlurContent: View? = null

    init {
        requireNotNull(mOverlayView, { "You need to add OverlayView to your layout." })
    }

    fun on(@IdRes blurViewId: Int): BlurUtils {
        mBlurContent = root.findViewById(blurViewId)
        return this
    }

    fun blur(@StringRes messageResId: Int, onClick: View.OnClickListener) {
        mOverlayView.visibility = View.VISIBLE
        mOverlayView.init(messageResId, R.string.hide, onClick)
        root.post {
            try {
                Blurry.with(root.context).capture(mBlurContent).into(mOverlayView.getImageView())
            } catch (npe: NullPointerException) {
                Timber.e(npe, "Something strange happened in external lib.")
            }
        }
        root.post { mBlurContent?.visibility = View.GONE }
    }
}
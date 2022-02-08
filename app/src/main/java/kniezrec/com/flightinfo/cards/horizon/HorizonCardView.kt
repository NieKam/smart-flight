package kniezrec.com.flightinfo.cards.horizon

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageView
import androidx.core.content.ContextCompat
import kniezrec.com.flightinfo.R
import kniezrec.com.flightinfo.cards.base.CanChangeVisibility
import kniezrec.com.flightinfo.cards.base.ServiceBasedCardView
import kniezrec.com.flightinfo.cards.overlay.BlurUtils
import kniezrec.com.flightinfo.databinding.HorizonCardLayoutBinding
import kniezrec.com.flightinfo.databinding.MapCardLayoutBinding
import kniezrec.com.flightinfo.settings.FlightAppPreferences

/**
 * Copyright by Kamil Niezrecki
 */

class HorizonCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ServiceBasedCardView<HorizonCardViewPresenter>(context, attrs, defStyleAttr),
    HorizonCardViewPresenter.ViewContract,
    CanChangeVisibility {

    private var mVisibilityChangeListener: CanChangeVisibility.OnVisibilityChangedListener? = null
    private val mBinding : HorizonCardLayoutBinding =
        HorizonCardLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        mBinding.calibrate.apply {
            setOnClickListener { mPresenter.onCalibrateClicked() }
            setOnLongClickListener { mPresenter.onCalibrateLongClicked() }
        }
        setColors()
    }

    override fun initPresenter(): HorizonCardViewPresenter {
        return HorizonCardViewPresenter()
    }

    private fun setColors() {
        findViewById<ImageView>(R.id.left_scale).setColorFilter(
            ContextCompat.getColor(context, R.color.text_color_dark)
        )
        findViewById<ImageView>(R.id.right_scale).setColorFilter(
            ContextCompat.getColor(context, R.color.text_color_dark)
        )
        mBinding.plane.setColorFilter(
            ContextCompat.getColor(context, R.color.text_color_light)
        )
    }

    override fun updateCoordinates(roll: Float, pitch: Float, yaw: Float) {
        mBinding.plane.apply {
            rotation = yaw
            translationY = pitch
        }
    }

    override fun showOverlay() {
        BlurUtils(this).on(R.id.main_content).blur(R.string.missing_rotation_content, mHideCardClickListener)
    }

    override fun hide() {
        FlightAppPreferences(context).apply {
            hideHorizonCard()
        }
        mVisibilityChangeListener?.onVisibilityChanged(false)
    }

    override fun registerOnVisibilityChangedListener(listener: CanChangeVisibility.OnVisibilityChangedListener) {
        mVisibilityChangeListener = listener
    }

    private val mHideCardClickListener by lazy {
        OnClickListener {
            mPresenter.onOverlayButtonClicked()
        }
    }
}

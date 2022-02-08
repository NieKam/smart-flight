package kniezrec.com.flightinfo.cards.course

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import kniezrec.com.flightinfo.R
import kniezrec.com.flightinfo.cards.base.CanChangeVisibility
import kniezrec.com.flightinfo.cards.base.ServiceBasedCardView
import kniezrec.com.flightinfo.cards.overlay.BlurUtils
import kniezrec.com.flightinfo.databinding.CourseCardLayoutBinding
import kniezrec.com.flightinfo.settings.FlightAppPreferences

/**
 * Copyright by Kamil Niezrecki
 */

private const val PIVOT_XY_VALUE: Float = .5f
private const val DURATION: Long = 200

class CourseCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ServiceBasedCardView<CourseCardViewPresenter>(context, attrs, defStyleAttr),
    CourseCardViewPresenter.ViewContract,
    CanChangeVisibility {

    private val interpolator = LinearInterpolator()
    private var mCurrentAngle: Float = 0f
    private var mVisibilityChangedListener: CanChangeVisibility.OnVisibilityChangedListener? = null

    private val mBinding : CourseCardLayoutBinding =
        CourseCardLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    override fun initPresenter(): CourseCardViewPresenter {
        return CourseCardViewPresenter()
    }

    override fun showOverlay() {
        BlurUtils(this).on(R.id.main_content).blur(R.string.missing_sensor_content, mHideCard)
    }

    override fun setCourseText(label: String) {
        mBinding.courseTextLabel.text = label
    }

    override fun setCourseLiteralText(label: String) {
        mBinding.courseLiteralLabel.text = label
    }

    override fun hideCard() {
        val prefs = FlightAppPreferences(context)
        prefs.hideCourseCard()
        mVisibilityChangedListener?.onVisibilityChanged(false)
    }

    override fun rotatePlane(azimuth: Float) {
        val rotateAnimation = RotateAnimation(
            mCurrentAngle,
            azimuth,
            Animation.RELATIVE_TO_SELF,
            PIVOT_XY_VALUE,
            Animation.RELATIVE_TO_SELF,
            PIVOT_XY_VALUE
        ).also {
            it.interpolator = interpolator
            it.duration = DURATION
            it.fillAfter = true
        }

        mCurrentAngle = azimuth
        mBinding.planeView.startAnimation(rotateAnimation)
    }

    override fun registerOnVisibilityChangedListener(listener: CanChangeVisibility.OnVisibilityChangedListener) {
        mVisibilityChangedListener = listener
    }

    private val mHideCard by lazy {
        OnClickListener {
            mPresenter.onOverlayButtonClicked()
        }
    }
}
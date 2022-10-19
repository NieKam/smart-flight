package kniezrec.com.flightinfo.cards.satellites

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import kniezrec.com.flightinfo.R
import kniezrec.com.flightinfo.databinding.NoSatellitesLayoutBinding

class NoSatellitesFoundView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private object Timeouts {
        const val CHANGE_TEXT = 10_000L
    }

    private val mUiHandler = Handler(Looper.getMainLooper())

    private val changeTextTask = object : Runnable {
        private var isTipVisible = false

        override fun run() {
            if (isTipVisible) {
                mBinding.textLabel.setText(R.string.wait_for_gps)
            } else {
                mBinding.textLabel.setText(R.string.gps_tip)
            }
            isTipVisible = !isTipVisible
            mUiHandler.postDelayed(this, Timeouts.CHANGE_TEXT)
        }
    }

    private val mBinding: NoSatellitesLayoutBinding =
        NoSatellitesLayoutBinding.inflate(LayoutInflater.from(context), this)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mUiHandler.postDelayed(changeTextTask, Timeouts.CHANGE_TEXT)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mUiHandler.removeCallbacksAndMessages(null)
    }

    fun show() {
        startLottie()
        visibility = View.VISIBLE
    }

    fun hide() {
        stopLottie()
        visibility = View.GONE
    }

    private fun startLottie() {
        if (mBinding.animationView.isAnimating) {
            return
        }

        mBinding.animationView.playAnimation()
    }

    private fun stopLottie() {
        if (!mBinding.animationView.isAnimating) {
            return
        }

        mBinding.animationView.pauseAnimation()
    }
}
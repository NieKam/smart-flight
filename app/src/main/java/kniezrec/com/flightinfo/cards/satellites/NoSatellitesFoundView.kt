package kniezrec.com.flightinfo.cards.satellites

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import kniezrec.com.flightinfo.databinding.NoSatellitesLayoutBinding

class NoSatellitesFoundView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

  private val mBinding : NoSatellitesLayoutBinding =
    NoSatellitesLayoutBinding.inflate(LayoutInflater.from(context), this)

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
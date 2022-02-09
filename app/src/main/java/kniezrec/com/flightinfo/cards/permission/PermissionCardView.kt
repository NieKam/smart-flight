package kniezrec.com.flightinfo.cards.permission

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import kniezrec.com.flightinfo.R
import kniezrec.com.flightinfo.common.findContextOfType
import kniezrec.com.flightinfo.databinding.PermissionViewBinding

class PermissionCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {
    private val mBinding : PermissionViewBinding =
        PermissionViewBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        mBinding.grantPermissionText.setOnClickListener { requestPermission() }
    }

    fun onPermissionDeniedPermanently() {
        val activityCtx = context.findContextOfType(Activity::class.java)
        if (activityCtx != null && PermissionManager.shouldShowSettingsPermission(activityCtx)) {
            mBinding.grantPermissionText.apply {
                setText(R.string.open_settings)
                setOnClickListener { openSettings() }
            }
        }
    }

    private fun openSettings() {
        PermissionManager.openSettings(context)
    }

    private fun requestPermission() {
        val activity = context.findContextOfType(Activity::class.java) ?: return
        PermissionManager.requestLocationPermission(activity)
    }
}
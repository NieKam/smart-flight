package kniezrec.com.flightinfo.cards.overlay

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import kniezrec.com.flightinfo.R
import kniezrec.com.flightinfo.databinding.CardOverlayLayoutBinding

/**
 * Copyright by Kamil Niezrecki
 */
class OverlayView
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    ConstraintLayout(context, attrs, defStyleAttr) {
  private val mBinding : CardOverlayLayoutBinding =
    CardOverlayLayoutBinding.inflate(LayoutInflater.from(context), this)


  fun init(contentId: Int, buttonTextId: Int, onClick: OnClickListener) {
    mBinding.infoLabel.setText(contentId)
    mBinding.actionButton.apply {
      setText(buttonTextId)
      setOnClickListener(onClick)
    }
  }

  fun getImageView(): ImageView = findViewById(R.id.overlay_background)
}

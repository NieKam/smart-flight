package kniezrec.com.flightinfo.common

import android.content.Context
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import kniezrec.com.flightinfo.R

/**
 * Copyright by Kamil Niezrecki
 */

object DialogUtils {

  fun showErrorDialog(context: Context, title: Int, body: Int) {
    MaterialDialog(context)
        .title(title)
        .cancelable(false)
        .positiveButton(R.string.ok) { dialog ->
          dialog.cancel()
        }
        .message(body)
        .show()
  }

  fun showGpsNotEnabledDialog(context: Context) {
    MaterialDialog(context).show {
      title(R.string.enable_gps_title)
      message(R.string.enable_gsp_message)
      positiveButton(R.string.yes) { dialog ->
        Navigation.goToLocationSettings(context)
        dialog.cancel()
      }
      negativeButton(R.string.no)
    }
  }

  fun showAboutDialog(context: Context) {
    val dialog = MaterialDialog(context)
        .title(R.string.app_name)
        .icon(R.mipmap.ic_launcher)
        .customView(R.layout.about_app_dialog_layout, scrollable = true)
        .positiveButton(R.string.ok)

    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val versionName = "${packageInfo.versionName} (${packageInfo.versionCode})"
    val customView = requireNotNull(dialog.getCustomView())

    customView.findViewById<TextView>(R.id.app_version).text = versionName

    val feedbackLink = context.getString(R.string.contact)
    customView.findViewById<TextView>(R.id.send_feedback).let {
      it.text = feedbackLink.fromHtml()
      it.movementMethod = MovementCheck()
    }

    val rateLink = context.getString(R.string.rate)
    customView.findViewById<TextView>(R.id.rate_app).let {
      it.text = rateLink.fromHtml()
      it.movementMethod = MovementCheck()
    }

    dialog.show()
  }
}

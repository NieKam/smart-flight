package kniezrec.com.flightinfo.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import kniezrec.com.flightinfo.common.AppVisibilityManager

/**
 * Copyright by Kamil Niezrecki
 */
abstract class BaseActivity : AppCompatActivity() {

  private lateinit var appVisibilityManager: AppVisibilityManager

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    appVisibilityManager = AppVisibilityManager(this)
  }

  override fun onStart() {
    super.onStart()
    appVisibilityManager.onStart()
  }

  override fun onStop() {
    super.onStop()
    appVisibilityManager.onStop()
  }
}
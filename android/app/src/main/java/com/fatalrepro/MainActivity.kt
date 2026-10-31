package com.fatalrepro

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.facebook.react.ReactActivity
import com.facebook.react.ReactApplication
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.fabricEnabled
import com.facebook.react.defaults.DefaultReactActivityDelegate

class MainActivity : ReactActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val mode = intent.getStringExtra("mode") ?: return
    Handler(Looper.getMainLooper()).postDelayed({
      val host = (application as ReactApplication).reactHost
      Log.i("Repro", "mode=$mode host=${host?.javaClass?.name} reactContextLive=${host?.currentReactContext != null}")
      Thread { triggerFatal(mode == "control") }.start()
    }, 8000)
  }

  companion object {
    init { System.loadLibrary("fatalrepro") }
    @JvmStatic external fun triggerFatal(control: Boolean)
  }

  /**
   * Returns the name of the main component registered from JavaScript. This is used to schedule
   * rendering of the component.
   */
  override fun getMainComponentName(): String = "FatalRepro"

  /**
   * Returns the instance of the [ReactActivityDelegate]. We use [DefaultReactActivityDelegate]
   * which allows you to enable New Architecture with a single boolean flags [fabricEnabled]
   */
  override fun createReactActivityDelegate(): ReactActivityDelegate =
      DefaultReactActivityDelegate(this, mainComponentName, fabricEnabled)
}

package pen.pluma.mibetngu

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pen.pluma.mibetngu.databinding.ActivityGameBinding
import java.util.*
import kotlin.math.abs

class GameViewActivity : AppCompatActivity() {
    private lateinit var _gameBinding : ActivityGameBinding

    private val GAME_ACTIVITY_TAG = "GAME_ACTIVITY_TAG"

    private var gWebView : WebView? = null
    private var gLastBackPress : Long = 0
    private val gBackPressThreshold : Long = 3500
    private val IS_FULLSCREEN_PREF = "is_fullscreen_pref"
    private var gLastTouch: Long = 0
    private val gTouchThreshold : Long = 2000
    private var pressBackToast : Toast? = null

    @SuppressLint("ClickableViewAccessibility", "SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        _gameBinding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(_gameBinding.root)

        gWebView = _gameBinding.gameWebView

        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )
        applyFullScreen(isFullScreen())

        var isOrientationEnabled = false
        try {
            isOrientationEnabled = Settings.System.getInt(
                contentResolver,
                Settings.System.ACCELEROMETER_ROTATION
            ) == 1
        } catch (e: Settings.SettingNotFoundException) {
            Log.d(GAME_ACTIVITY_TAG, "Settings could not be loaded")
        }

        val screenLayout = (resources.configuration.screenLayout
                and Configuration.SCREENLAYOUT_SIZE_MASK)

        if ((screenLayout == Configuration.SCREENLAYOUT_SIZE_LARGE
                    || screenLayout == Configuration.SCREENLAYOUT_SIZE_XLARGE)
            && isOrientationEnabled
        ) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }

        val settings = gWebView?.settings
        if (settings != null) {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.useWideViewPort = false
            settings.loadWithOverviewMode = true
            settings.databaseEnabled = true
            settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
            settings.databasePath = filesDir.parentFile!!.path + "/databases"
            settings.allowFileAccess = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
        }

        if (savedInstanceState != null) {
            gWebView?.restoreState(savedInstanceState)
        } else {
            gWebView?.loadUrl("file:///android_asset/cssTTT/dist/index.html?lang=" + Locale.getDefault().language)
        }

//        Toast.makeText(application, R.string.toggle_fullscreen, Toast.LENGTH_SHORT).show()
        gWebView?.setOnTouchListener { _: View?, event: MotionEvent ->
            val currentTime = System.currentTimeMillis()
            if (event.action == MotionEvent.ACTION_UP
                && abs(currentTime - gLastTouch) > gTouchThreshold
            ) {
                val toggledFullScreen = !isFullScreen()
                saveFullScreen(toggledFullScreen)
                applyFullScreen(toggledFullScreen)
            } else if (event.action == MotionEvent.ACTION_DOWN) {
                gLastTouch = currentTime
            }
            false
        }
        pressBackToast = Toast.makeText(
            applicationContext, R.string.press_back,
            Toast.LENGTH_SHORT
        )
    }

    override fun onResume() {
        super.onResume()
        gWebView!!.loadUrl("file:///android_asset/cssTTT/dist/index.html?lang=" + Locale.getDefault().language)

    }

    private fun saveFullScreen(isFullScreen: Boolean) {
        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        editor.putBoolean(IS_FULLSCREEN_PREF, isFullScreen)
        editor.apply()
    }

    private fun isFullScreen(): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
            IS_FULLSCREEN_PREF,
            true
        )
    }

    private fun applyFullScreen(isFullScreen: Boolean) {
        if (isFullScreen) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val currentTime = System.currentTimeMillis()
        if (abs(currentTime - gLastBackPress) > gBackPressThreshold) {
            pressBackToast!!.show()
            gLastBackPress = currentTime
        } else {
            pressBackToast!!.cancel()
            super.onBackPressed()
        }
    }

    override fun onPause() {
        super.onPause()
        _gameBinding.gameWebView.onPause()
    }
}
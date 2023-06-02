package pen.pluma.mibetngu

import android.annotation.SuppressLint
import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import pen.pluma.mibetngu.connection.NetworkHandler
import pen.pluma.mibetngu.databinding.FragmentSecondBinding
import pen.pluma.mibetngu.viewmodel.MibetViewModel

class FragmentSecond : Fragment(){

    private var fragmentSecondBinding : FragmentSecondBinding? = null
    private val _secondBinding get() = fragmentSecondBinding!!

    private lateinit var viewModel : MibetViewModel

    private var _db = Firebase.firestore

    private var connCheck = NetworkHandler()
    private var checkNet = false

    var title: String? = null
    var imgUrl: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        fragmentSecondBinding = FragmentSecondBinding.inflate(inflater,container,false)

        viewModel = ViewModelProvider(this)[MibetViewModel::class.java]


        checkNetConnection()
        title = arguments?.getString("title")
        return _secondBinding.root
    }

    private fun checkNetConnection() {
        checkNet = connCheck.connectionError(requireActivity())
        if (checkNet) {
            viewModel.getData()
            getJumpCode()
        } else {
            Toast.makeText(context, "Connection error, Please check internet connection.",
                Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getJumpCode() {
        viewModel.getData().observe(viewLifecycleOwner) {
            _db.collection("05May").document("22MIBETNGU").get()
                .addOnSuccessListener {
                    it.let {
                        if (it != null) {
                            for (i in it.id){
                                val packName = it.getString("AppPackage")
                                val url = it.getString("UrlLink")
                                val state = it.getBoolean("UrlStatus")
                                val infoWeb : WebView = _secondBinding.infoWebView
                                if(packName == context?.packageName) {
                                    Log.e(ContentValues.TAG, packName.toString())
                                    when(state){
                                        true -> {
                                            infoWeb.loadUrl(url.toString())
                                        } else -> {
                                        if (title == "webView"){
                                                infoWeb.loadUrl("file:///android_asset/webFiles/webview1.html")
                                            } else {
                                                infoWeb.loadUrl("file:///android_asset/webFiles/webview1.html")
                                            }
                                        }
                                    }
                                    webViewInit()
                                }
                            }
                        }
                    }
                }
            return@observe
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun webViewInit() {
        with(_secondBinding.infoWebView) {
            with(settings) {
                javaScriptEnabled = true
                defaultTextEncodingName = "UTF-8"
                cacheMode = WebSettings.LOAD_NO_CACHE
                useWideViewPort = true
                pluginState = WebSettings.PluginState.ON
                domStorageEnabled = true
                builtInZoomControls = false
                layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
                loadWithOverviewMode = true
                blockNetworkImage = true
                loadsImagesAutomatically = true
                setSupportZoom(false)
                setSupportMultipleWindows(true)
            }
            requestFocusFromTouch()
            scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        }

        val webSetting: WebSettings = _secondBinding.infoWebView.settings
        with(webSetting) {
            context?.getDir(
                "cache", AppCompatActivity.MODE_PRIVATE
            )?.path
            domStorageEnabled = true
            allowFileAccess = true
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        _secondBinding.infoWebView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                _secondBinding.infoProgressBar.progress = newProgress
                if (newProgress == 100) {
                    _secondBinding.infoWebView.settings.blockNetworkImage = false
                }
            }
        }

        val settings: WebSettings = _secondBinding.infoWebView.settings
        settings.javaScriptEnabled = true
        _secondBinding.infoWebView.setOnLongClickListener { v: View ->
            val result = (v as WebView).hitTestResult
            val type = result.type
            if (type == WebView.HitTestResult.UNKNOWN_TYPE) return@setOnLongClickListener false
            when (type) {
                WebView.HitTestResult.PHONE_TYPE -> {}
                WebView.HitTestResult.EMAIL_TYPE -> {}
                WebView.HitTestResult.GEO_TYPE -> {}
                WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> {}
                WebView.HitTestResult.IMAGE_TYPE -> {
                    imgUrl = result.extra
                }
                else -> {}
            }
            true
        }

        _secondBinding.infoWebView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                _secondBinding.infoProgressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView, url: String) {
                _secondBinding.infoProgressBar.visibility = View.GONE
            }

            override fun onReceivedError(
                view: WebView, request: WebResourceRequest, error: WebResourceError,
            ) {
                super.onReceivedError(view, request, error)
            }
        }

        _secondBinding.infoWebView.setOnKeyListener { _: View?, i: Int, keyEvent: KeyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_DOWN) {
                if (i == KeyEvent.KEYCODE_BACK && _secondBinding.infoWebView.canGoBack()) {
                    _secondBinding.infoWebView.goBack()
                    return@setOnKeyListener true
                }
            }
            false
        }

    }
}
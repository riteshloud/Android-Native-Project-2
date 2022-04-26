package com.demo.navigationdemo

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.demo2.R
import com.demo2.utilities.Pref
import com.demo2.view.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_pdf_view.*


class WebViewActivity : BaseActivity() {

    var strUrl: String? = null
    var isPdf: Boolean? = null
    var pdfLoadBaseURL = "https://drive.google.com/viewerng/viewer?embedded=true&url="
    //var pdfLoadBaseURL = "https://docs.google.com/gview?embedded=true&url="
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_view)
        Pref.setLocale(this!!, Pref.getLocalization(this!!))

        init()
        setup()
        addListeners()
    }

    private fun init() {
        if (intent != null) {
            strUrl = intent.getStringExtra("url")
            Log.e("TestingUrl", "strUrl ${strUrl}")
        }
        //showProgressDialog()
    }

    private fun setup() {
        webview.settings.javaScriptEnabled = true
        webview.settings.loadWithOverviewMode = true
        webview.settings.useWideViewPort = false
        webview.settings.domStorageEnabled = true
        loadUrl(pdfLoadBaseURL+strUrl)


    }


    private fun loadUrl(pageUrl: String) {

        var loadResource=0
        var pageStarted=false
        webview.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Log.e("====Started",""+url)
                pageStarted=true
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            override fun onLoadResource(view: WebView, url: String) {
                Log.e("====Loading",""+url)
                        loadResource++
            }

            override fun onPageFinished(view: WebView, url: String) {
                Log.e("====finish","==="+url)
                Log.e("====pageStarted","==="+pageStarted)
                Log.e("====pageStarted","==="+pageStarted)

                if(!pageStarted || loadResource < 2){
                    loadUrl(pdfLoadBaseURL+strUrl)

                }
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                Log.e("====Error",""+error)
            }
        }
        webview.loadUrl(pageUrl)

    }
    private fun addListeners() {


        iv_cancel.setOnClickListener {
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        webview!!.clearCache(true)
    }
}

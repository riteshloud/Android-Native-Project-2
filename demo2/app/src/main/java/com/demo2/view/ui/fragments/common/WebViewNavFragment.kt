package com.demo2.view.ui.fragments.common


import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.demo2.R
import com.demo2.utilities.Pref
import com.demo2.view.ui.base.BaseFragment
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_industry_tools_nav.view.*

const val ARG_PARAM1 = "param1"
const val ARG_PARAM2 = "param2"

class WebViewNavFragment : BaseFragment() {

    var rootview: View? = null
    private var TAG = WebViewNavFragment::class.java.simpleName
    var industry_link: String? = null
    var title = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            industry_link = it.getString(ARG_PARAM1)
            title = it.getString(ARG_PARAM2)!!
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //if (rootview == null) {
        rootview = inflater.inflate(R.layout.fragment_industry_tools_nav, container, false)
        init()
        setup()
        onClickListeners()
        //}
        return rootview
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Pref.setLocale(activity!!, Pref.getLocalization(activity!!))

        homeController.tv_title.text = title
        homeController.tv_title.visibility = View.VISIBLE
        homeController.iv_navigation.visibility = View.GONE
        homeController.ll_bottombar.visibility = View.GONE
        homeController.iv_back.visibility = View.VISIBLE
        homeController.rl_message.visibility = View.GONE
        homeController.viewVisibleDrawerBottomBar(1)
        //homeController.resideMenu!!.setSwipeDirectionDisable(ResideMenu.DIRECTION_LEFT)

    }

    private fun init() {

    }

    private fun setup() {
        var loadResource = 0
        var pageStarted = false
        Log.v("====URL", "-" + industry_link)
        rootview!!.webview.settings.javaScriptEnabled = true
        rootview!!.webview.settings.loadWithOverviewMode = true
        rootview!!.webview.settings.useWideViewPort = false
        rootview!!.webview.settings.domStorageEnabled = true
        homeController!!.showProgressDialog()
        rootview!!.webview.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
                view.loadUrl(url.toString())
                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                pageStarted = true
                Log.e("====Started",""+url)

                super.onPageStarted(view, url, favicon)


            }

            override fun onPageFinished(view: WebView?, url: String?) {
                Log.e("====finish",""+url)

                if (pageStarted && loadResource <= 1) {
                    setup()

                } else {
                    homeController!!.dismissProgressDialog()
                }
            }

            override fun onLoadResource(view: WebView?, url: String?) {
                loadResource++
                Log.e("====loadresource",""+url)

                super.onLoadResource(view, url)

            }

            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String,
                failingUrl: String?
            ) {
                homeController!!.dismissProgressDialog()
                Toast.makeText(activity!!, "Error:$description", Toast.LENGTH_SHORT)
                    .show()
            }

        }
        industry_link?.let { rootview!!.webview.loadUrl(it) }
    }


    private fun onClickListeners() {

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BlankFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            WebViewNavFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

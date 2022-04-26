package com.demo2.view.ui.fragments.demo.tabs


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.special.ResideMenu.ResideMenu
import com.demo2.R
import com.demo2.utilities.Pref
import com.demo2.view.ui.base.BaseFragment
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_news_and_update.view.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class NewsAndUpdateFragment : BaseFragment() {

    var rootview: View? = null
    private var TAG = NewsAndUpdateFragment::class.java.simpleName
    var latestNews: TradeDashboardModel.LatestNews? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //if (rootview == null) {
        rootview = inflater.inflate(R.layout.fragment_news_and_update, container, false)
        init()
        setup()
        onClickListeners()
        //}
        return rootview
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Pref.setLocale(activity!!, Pref.getLocalization(activity!!))


        homeController.tv_title.text = getString(R.string.news_updates_tag)
        homeController.tv_title.visibility = View.VISIBLE
        homeController.iv_navigation.visibility = View.GONE
        homeController.ll_bottombar.visibility = View.GONE
        homeController.iv_back.visibility = View.VISIBLE
        homeController.rl_message.visibility = View.GONE
        homeController.viewVisibleDrawerBottomBar(2)
        homeController.resideMenu!!.setSwipeDirectionDisable(ResideMenu.DIRECTION_LEFT)


    }

    private fun init() {
        if (arguments != null) {
            latestNews = Gson().fromJson(
                arguments!!.getString("latestNewsData"),
                TradeDashboardModel.LatestNews::class.java
            )
            Log.e(TAG, "init: ${latestNews!!.title}")
            rootview!!.tv_news_title.text = latestNews!!.title

            var htmlRegex = "<(\"[^\"]*\"|'[^']*'|[^'\">])*>"
            var pattern = Pattern.compile(htmlRegex);
            val matcher: Matcher = pattern.matcher(latestNews!!.details!!)
            rootview!!.webview.settings.javaScriptEnabled = true
            rootview!!.webview.settings.loadWithOverviewMode = true
            rootview!!.webview.settings.useWideViewPort = false
            rootview!!.webview.settings.domStorageEnabled = true

            if (matcher.find()) {
                //     rootview!!.tv_news_desc.setHtmlData(latestNews!!.details!!)
                var webString = latestNews!!.details!!.replace("\n", "")

                rootview!!.webview.loadDataWithBaseURL(
                    "",
                    webString.replace("\r", ""),
                    "text/html",
                    "utf-8",
                    ""
                )
                rootview!!.webview.makeVisible()
                rootview!!.tv_news_desc.makeGone()
            } else {
                rootview!!.webview.makeGone()
                rootview!!.tv_news_desc.makeVisible()
                rootview!!.tv_news_desc.text = latestNews!!.details!!
            }
            // rootview!!.tv_news_desc.setHtmlData(latestNews!!.details!!)


            Glide.with(activity!!)
                .load(latestNews!!.image)
                .into(rootview!!.img_news)
        }

    }

    private fun setup() {
        addObservers()

    }

    private fun onClickListeners() {

    }

    private fun addObservers() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        homeController.rl_message.visibility = View.VISIBLE
        //homeController.visibleBottomBar(0)
        homeController.resideMenu!!.enableDirection(ResideMenu.DIRECTION_LEFT)

        homeController.tv_title.text = getString(R.string.dashboard_nav)

        homeController.iv_navigation.visibility = View.VISIBLE
        homeController.iv_back.visibility = View.GONE


    }
}

package com.demo2.view.ui.fragments.common


import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

import com.demo2.R
import com.demo2.view.ui.base.BaseFragment
import com.bumptech.glide.Glide
import com.special.ResideMenu.ResideMenu
import com.demo2.utilities.Pref
import com.demo2.view.service.MyViewModelFactory
import com.demo2.view.service.NetworkUtil
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.dialog_package_view_program.*
import kotlinx.android.synthetic.main.fragment_news_and_update.view.*
import kotlinx.android.synthetic.main.fragment_news_and_update_nav.view.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class NewsAndUpdateNavFragment : BaseFragment() {

    var rootview: View? = null
    private var newsAndUpdateViewModel: NewsAndUpdateViewModel? = null
    private var newsAndUpdateModel: NewsAndUpdateModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootview = inflater.inflate(R.layout.fragment_news_and_update_nav, container, false)
        init()
        setup()
        onClickListeners()
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
        homeController.viewVisibleDrawerBottomBar(1)
        //homeController.resideMenu!!.setSwipeDirectionDisable(ResideMenu.DIRECTION_LEFT)

    }

    private fun init() {
        newsAndUpdateViewModel = ViewModelProviders.of(
            this@NewsAndUpdateNavFragment,
            MyViewModelFactory(NewsAndUpdateViewModel(activity!!))
        )[NewsAndUpdateViewModel::class.java]
    }

    private fun setup() {
        addObservers()
        if (!NetworkUtil.isInternetAvailable(activity!!)) {
            Toast.makeText(
                activity!!,
                getString(R.string.no_internet_connection),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            newsAndUpdateViewModel!!.getNewsAndUpdateList()
        }

    }

    private fun onClickListeners() {

    }

    private fun addObservers() {
        newsAndUpdateViewModel!!.newsAndUpdateModel!!.observe(this@NewsAndUpdateNavFragment, Observer {
            it?.let {
                newsAndUpdateModel = it
                (rootview!!.rv_result.adapter as NewsAndUpdateNavViewAdapter?)?.let { adapter ->
                    adapter.notifyDataSetChanged()
                } ?: run {
                    rootview!!.rv_result.adapter = NewsAndUpdateNavViewAdapter(
                        activity!!,
                        it.payload!!.news!!,
                        this@NewsAndUpdateNavFragment
                    )
                }
            }
        })
    }

    override fun onListClickSimple(position: Int, string: String?) {
        super.onListClickSimple(position, string)
        openDetailNewsDialog(position)
    }

    private fun openDetailNewsDialog(position: Int) {
        var dialog = Dialog(activity!!)
        dialog.setContentView(R.layout.dialog_package_view_program)
        dialog.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        dialog.window?.setGravity(Gravity.CENTER)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.view_content.visibility = View.GONE
        dialog.tv_program_name.text = newsAndUpdateModel!!.payload!!.news!![position].title
    //    dialog.tv_program_content.text = newsAndUpdateModel!!.payload!!.news!![position].details!!


        var htmlRegex = "<(\"[^\"]*\"|'[^']*'|[^'\">])*>"
        var pattern = Pattern.compile(htmlRegex);
        val matcher: Matcher = pattern.matcher(newsAndUpdateModel!!.payload!!.news!![position].details!!)

        dialog.webview.settings.javaScriptEnabled = true
        dialog.webview.settings.loadWithOverviewMode = true
        dialog.webview.settings.useWideViewPort = false
        dialog.webview.settings.domStorageEnabled = true

        if (matcher.find()) {
            //     rootview!!.tv_news_desc.setHtmlData(latestNews!!.details!!)
            var webString = newsAndUpdateModel!!.payload!!.news!![position].details!!.replace("\n", "")

            dialog.webview.loadDataWithBaseURL(
                "",
                webString.replace("\r", ""),
                "text/html",
                "utf-8",
                ""
            )
            dialog.webview.makeVisible()
            dialog.tv_program_content.makeGone()
        } else {
            dialog.webview.makeGone()
            dialog.tv_program_content.makeVisible()
            dialog.tv_program_content.text = newsAndUpdateModel!!.payload!!.news!![position].details!!
        }
       // dialog.tv_program_content.setHtmlData(newsAndUpdateModel!!.payload!!.news!![position].details!!)

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            dialog.tv_program_content.text =
//                Html.fromHtml(newsAndUpdateModel!!.payload!!.news!![position].details!!, Html.FROM_HTML_MODE_LEGACY)
//        } else {
//            dialog.tv_program_content.text = Html.fromHtml(newsAndUpdateModel!!.payload!!.news!![position].details!!)
//        }

        Glide.with(context!!).load(newsAndUpdateModel!!.payload!!.news!![position].image).into(dialog.img_banner)

        dialog.iv_close.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        homeController.rl_message.visibility = View.GONE
        homeController.resideMenu!!.enableDirection(ResideMenu.DIRECTION_LEFT)
        homeController.tv_title.text = getString(R.string.dashboard_nav)
        homeController.iv_navigation.visibility = View.VISIBLE
        homeController.iv_back.visibility = View.GONE
    }
}

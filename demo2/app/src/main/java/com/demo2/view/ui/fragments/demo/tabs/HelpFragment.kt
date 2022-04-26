package com.demo2.view.ui.fragments.demo.tabs


import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.demo2.R
import com.demo2.utilities.Pref
import com.demo2.view.service.MyViewModelFactory
import com.demo2.view.ui.base.BaseFragment
import com.demo2.view.ui.fragments.demo.help.HelpAndSupportFragment
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_help.view.*
import java.lang.Exception


class HelpFragment : BaseFragment() {

    var rootView: View? = null
    private var faqViewModel: FaqViewModel? = null
    private var faqModel: FaqModel? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_help, container, false)
            init()
            setup()
            onClickListeners()
        }
        return rootView
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Pref.setLocale(activity!!, Pref.getLocalization(activity!!))

        homeController.unSelectBottomBar()
        //homeController.visibleBottomBar(0)
        homeController.ll_bottombar.visibility = View.GONE
        homeController.tv_title.visibility = View.VISIBLE

        homeController.tv_title.text = getString(R.string.faq_nav)
        homeController.iv_navigation.visibility = View.GONE
        homeController.rl_message.visibility = View.GONE

        homeController.iv_back.visibility = View.VISIBLE

        if (activity!!.supportFragmentManager!!.findFragmentByTag("HelpFragment_D") != null) {
            homeController.viewVisibleDrawerBottomBar(1)
        } else {
            homeController.viewVisibleDrawerBottomBar(2)
        }


        homeController.iv_help.setColorFilter(
            ContextCompat.getColor(
                activity!!,
                R.color.dashboard_selected
            )
        )
        homeController.tv_help.setTextColor(
            ContextCompat.getColor(
                activity!!,
                R.color.dashboard_selected
            )
        )

    }

    private fun init() {
        faqViewModel = ViewModelProviders.of(
            this@HelpFragment,
            MyViewModelFactory(FaqViewModel(activity!!))
        )[FaqViewModel::class.java]

        faqViewModel!!.faqListCall()

        /*if (homeController.faqModel != null) {
            rootView!!.ll_main.visibility = View.VISIBLE
            faqModel = homeController.faqModel
            rootView!!.rv_faqs.adapter = FaqsAdapter(
                activity!!,
                faqModel!!.payload!!.faq as List<FaqModel.Payload.Faq>
            )

        } else {
            faqViewModel!!.faqListCall()
        }*/
    }

    private fun setup() {
        rootView!!.tv_pdf_title.underline()
        rootView!!.tv_video_guides_title.underline()

        rootView!!.webView.settings.javaScriptEnabled = true
        rootView!!.webView.settings.useWideViewPort = false
        rootView!!.webView.settings.domStorageEnabled = true

        addObservers()

    }

    private fun loadUrl(pageUrl: String) {
        rootView!!.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Log.e("====Started", "" + url)
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                Log.e("====Load", "" + url.substring(url.lastIndexOf(".")))
                try {
                    val openURL = Intent(Intent.ACTION_VIEW)
                    openURL.data = Uri.parse(url)
                    startActivity(openURL)
                } catch (e: Exception) {
                    Toast.makeText(
                        homeController,
                        resources.getString(R.string.no_app_installed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return true
            }

            override fun onLoadResource(view: WebView, url: String) {
                Log.e("====Loading", "" + url)
            }

            override fun onPageFinished(view: WebView, url: String) {
                Log.e("====finish", "===" + url)
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                Log.e("====Error", "" + error)
            }
        }
        rootView!!.webView.postDelayed(
            Runnable {
                rootView!!.webView.loadUrl(pageUrl)
            },
            500
        )
    }

    /*override fun onPause() {
        super.onPause()
        rootView!!.webView.clearCache(true)
    }*/

    private fun onClickListeners() {
        /*
                                    commented as per client requirements

         rootView!!.ll_account.setOnClickListener {
                if (!NetworkUtil.isInternetAvailable(activity!!)) {
                    Toast.makeText(
                        activity!!,
                        getString(R.string.no_internet_connection),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                startActivity(
                    Intent(activity!!, PdfViewActivity::class.java).putExtra(
                        "url",
                        faqModel!!.payload!!.otmtradeAccountRegistrationReset
                    ).putExtra("isPdf", true)
                )

            }

            rootView!!.ll_user_guide.setOnClickListener {
                if (!NetworkUtil.isInternetAvailable(activity!!)) {
                    Toast.makeText(
                        activity!!,
                        getString(R.string.no_internet_connection),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                startActivity(
                    Intent(activity!!, PdfViewActivity::class.java).putExtra(
                        "url",
                        faqModel!!.payload!!.otmtradeMemberLoginUserGuide
                    ).putExtra("isPdf", true)
                )
            }

            rootView!!.ll_faq.setOnClickListener {
                if (!NetworkUtil.isInternetAvailable(activity!!)) {
                    Toast.makeText(
                        activity!!,
                        getString(R.string.no_internet_connection),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                startActivity(
                    Intent(activity!!, PdfViewActivity::class.java).putExtra(
                        "url",
                        faqModel!!.payload!!.otmtradeFaqFile
                    ).putExtra("isPdf", true)
                )
            }

        */
        rootView!!.ll_download.setOnClickListener {
            faqViewModel!!.downloadMT4Call()
        }

        rootView!!.ll_help.setOnClickListener {
            homeController.loadFragment(
                HelpAndSupportFragment(),
                "HelpAndSupportFragment",
                this.javaClass.simpleName
            )
        }
    }

    private fun addObservers() {
        faqViewModel!!.responseError!!.observe(this@HelpFragment, Observer {
            it?.let {
                homeController.errorBody(it)
            }
        })

        faqViewModel?.isLoading!!.observe(this@HelpFragment, Observer {
            it?.let {
                if (it) {
                    homeController.showProgressDialog()
                } else {
                    homeController.dismissProgressDialog()
                }
            }
        })

        faqViewModel?.faqViewModel!!.observe(this@HelpFragment, Observer {

            rootView!!.ll_main.visibility = View.VISIBLE
            faqModel = it
            homeController.faqModel = faqModel

            updateUI()

//            rootView!!.rv_pdf.adapter = GuideFaqListAdapter(
//                activity!!
//            )
//            rootView!!.rv_video_guides.adapter = GuideFaqListAdapter(
//                activity!!
//            )
        })

        faqViewModel?.downlaodMT4Response!!.observe(this@HelpFragment, Observer {
            it.let {
                homeController.messageToast(it)
            }
        })

    }

    private fun updateUI() {
        /*val labelColor = ContextCompat.getColor(activity!!, R.color.red_dark_btn)
        val сolorString =
            String.format("%X", labelColor).substring(2) // !!strip alpha value!!

        HtmlFormatter.formatHtml(
            HtmlFormatterBuilder().setHtml(String.format("<font color=\"#%s\">text</font>", сolorString)).setImageGetter(
                HtmlResImageGetter(activity!!)
            )
        )
        Html.fromHtml(
            String.format("<font color=\"#%s\">text</font>", сolorString),
            TextView.BufferType.SPANNABLE
        )*/

        /*rootView!!.tv_helpone.setHtmlData(faqModel!!.payload!!.helpone!!)
        rootView!!.tv_helptwo.setHtmlData(faqModel!!.payload!!.helptwo!!)
        rootView!!.tv_helpone.movementMethod = LinkMovementMethod.getInstance()
        rootView!!.tv_helptwo.movementMethod = LinkMovementMethod.getInstance()
        rootView!!.tv_pdf_title.text = faqModel!!.payload!!.pdfTitle
        rootView!!.tv_pdf_link.setHtmlData(faqModel!!.payload!!.pdfLinks!!)
        rootView!!.tv_pdf_link.movementMethod = LinkMovementMethod.getInstance()
        rootView!!.tv_video_guides_title.text = faqModel!!.payload!!.videoTitle
        rootView!!.tv_video_guides_link.setHtmlData(faqModel!!.payload!!.videoLinks!!)
        rootView!!.tv_video_guides_link.movementMethod = LinkMovementMethod.getInstance()*/

        loadUrl(faqModel!!.payload!!)

    }


}

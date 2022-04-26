package com.demo2.view.ui.fragments.demo.stock

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.demo.navigationdemo.WebViewActivity
import com.bumptech.glide.Glide
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.*
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.demo2.R
import com.demo2.model.*
import com.demo2.utilities.*
import com.demo2.view.adapters.PerformanceDataAdapter
import com.demo2.view.interfaces.OnListClickListener
import com.demo2.view.service.MyViewModelFactory
import com.demo2.view.service.NetworkUtil
import com.demo2.view.ui.base.BaseFragment
import com.demo2.view.ui.fragments.common.WebViewNavFragment
import kotlinx.android.synthetic.main.dialog_amount_breakdown.iv_close
import kotlinx.android.synthetic.main.dialog_disclaimer.*
import kotlinx.android.synthetic.main.dialog_invest.*
import kotlinx.android.synthetic.main.fragment_stock_market_detail.*
import kotlinx.android.synthetic.main.fragment_stock_market_detail.view.*
import java.util.*
import kotlin.collections.ArrayList

class StockMarketDetailFragment : BaseFragment() {
    var rootView: View? = null
    private var stockMarketViewModel: StockMarketViewModel? = null
    private var stockStateModel: StockStatesModel? = null
    private var stockPortfolioModel: StockPortfolioModel? = null
    private var stockChartModel: StockChartModel? = null
    private var investMentHistory: StockInvestHistoryModel? = null
    var backImage = ""
    var stockGroupModel: StockMarketListModel.Payload.StockCategory? = null
    var stock_cat_id: String = ""
    var totalInvestedAmt = 0.0
    var totalProfit = 0.0
    var totalEquityAmt = 0.0
    private var firstCall = true
    private var toast: Toast? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_stock_market_detail, container, false)
            init()
            if (!NetworkUtil.isInternetAvailable(activity!!)) {
                Toast.makeText(
                    activity!!,
                    getString(R.string.no_internet_connection),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                stockMarketViewModel!!.getStockStates(stockGroupModel!!.id.toString())
                stockMarketViewModel!!.getInvestHistory(0, stockGroupModel!!.id.toString())
            }

        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addObserver()
        addOnClickListeners()
    }


    private fun init() {
        stockMarketViewModel = ViewModelProviders.of(
            this@StockMarketDetailFragment,
            MyViewModelFactory(StockMarketViewModel(activity!!))
        )[StockMarketViewModel::class.java]
    }


    private fun updateUI(firstUpdateCall: Boolean) {
        var strOverView = stockStateModel!!.payload!!.detail!!.overviewUrl
        var strVideoUrl = stockStateModel!!.payload!!.detail!!.videoUrl
        var strDisclaimer = stockStateModel!!.payload!!.detail!!.disclaimer

        //test
//        strOverView = ""
//        strVideoUrl = ""
//        strDisclaimer = ""

        if (TextUtils.isEmpty(strOverView) && TextUtils.isEmpty(strVideoUrl) && TextUtils.isEmpty(
                strDisclaimer
            )
        ) {
            llLinks.makeGone()
            view_dashed.makeGone()
        } else {
            llLinks.makeVisible()
            view_dashed.makeVisible()

            tv_overview.visibility =
                if (!TextUtils.isEmpty(strOverView)) View.VISIBLE else View.GONE
            tv_video.visibility = if (!TextUtils.isEmpty(strVideoUrl)) View.VISIBLE else View.GONE
            tv_disclaimer.visibility =
                if (!TextUtils.isEmpty(strDisclaimer)) View.VISIBLE else View.GONE

        }

        firstCall = false
        stockStateModel?.payload?.detail?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                expandable_text.setText(
                    Html.fromHtml(
                        it.description,
                        0
                    )
                )
            } else {
                expandable_text.setText(
                    Html.fromHtml(it.description)
                )
            }

            tv_stock_type.text = it.name
            tv_stock_type_name.text = it.type

        }
        if (stockGroupModel!!.invested == 1) {
            tv_status.makeVisible()
            iv_detail_investor.makeVisible()
        }

//        tv_total_invested_amount.text = "${getString(R.string.currency_text)}${totalInvestedAmt}"
//        tv_total_profilt_amount.text = "${getString(R.string.currency_text)}${parseDouble(totalProfit.toString())}"
//        tv_total_equity_amount.text = "${getString(R.string.currency_text)}${totalEquityAmt}"

        var stockWalletBalance =
            UTILS.parseDouble(stockStateModel?.payload?.stockWalletBalance!!.toString()).toDouble()
        var stockWalletReferralBalance =
            UTILS.parseDouble(stockStateModel?.payload?.stockWalletReferralBalance!!.toString())
                .toDouble()
        var total: Double = stockWalletBalance + stockWalletReferralBalance
        tv_total_invested_amount.text = "${getString(R.string.currency_text)}${stockWalletBalance}"
        tv_total_profilt_amount.text =
            "${getString(R.string.currency_text)}${stockWalletReferralBalance}"
        tv_total_equity_amount.text =
            "${getString(R.string.currency_text)}${UTILS.parseDouble(total.toString())}"

        backImage?.let {
            Glide.with(homeController).load(backImage).into(rootView!!.iv_stock_image)
        }
        if (firstUpdateCall) {
            stockStateModel?.payload?.performance?.let {

                var yearArray: ArrayList<String> = ArrayList()
                it.forEach { it2 ->
                    yearArray.add(it2!!.name.toString().trim())
                }
                Log.e("StockDetail", " yearArray size " + yearArray.size)

                rootView!!.sp_yer_type.adapter = HighLightArrayAdapterV2(
                    context = activity!!,
                    dropdownResource = R.layout.row_spinner_login_dropdown,
                    viewResource = R.layout.row_spinner_item_profile,
                    objects = yearArray
                )

                rootView!!.sp_yer_type.setSelection(stockStateModel!!.payload!!.performance!!.size - 1)


                if (it.size > 0) {
                    rootView!!.cvPerformanceData.makeVisible()
                } else {
                    rootView!!.cvPerformanceData.makeGone()

                }

                var l: ArrayList<StockStatesModel.Payload.Performance.Value?> = arrayListOf()
                if (it.isNotEmpty()) {
                    for (i in stockStateModel?.payload?.performance!![0]!!.value!!.indices) {
                        l.add(
                            StockStatesModel.Payload.Performance.Value(
                                stockStateModel?.payload?.performance!![0]!!.value!![i]!!.date!!,
                                0.0
                            )
                        )
                    }
                    var s = StockStatesModel.Payload.Performance(getString(R.string.year_tag), l)
                    it.add(0, s)
                }

                var performanceAdapter = PerformanceDataAdapter(activity!!, it)
                rootView!!.rvPerformanceChartYearWise.adapter = performanceAdapter


            }
        }
        rootView!!.sp_yer_type.setOnTouchListener { v, event ->
            hideSoftKeyboard()
            activity!!.currentFocus?.clearFocus()
            false
        }
        rootView!!.sp_yer_type.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {
                }

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    rootView!!.sp_yer_type.adapter.let {
                        var x: HighLightArrayAdapterV2 = it as HighLightArrayAdapterV2
                        x.setSelection(p2)
                        //    rootView!!.chart_performance.data.clearValues()
                        rootView!!.chart_performance.invalidate()
                        initPerformanceGraph(p2, it.objects.get(p2))

                    }
                }
            }

        tv_states.performClick()
    }


    private fun addOnClickListeners() {

        tv_overview.setOnMyClickListener {

            homeController.loadFragment(
                WebViewNavFragment.newInstance(
                    "https://drive.google.com/viewerng/viewer?embedded=true&url=" + stockStateModel!!.payload!!.detail!!.overviewUrl!!,
                    getString(R.string.stock_markets_nav_child)
                ).apply {

                },
                "WebViewNavFragment",
                homeController.supportFragmentManager.findFragmentById(R.id.fragment_container)!!.javaClass.simpleName
            )

        }
        tv_video.setOnMyClickListener {

            homeController.loadFragment(
                WebViewNavFragment.newInstance(
                    stockStateModel!!.payload!!.detail!!.videoUrl!!,
                    getString(R.string.stock_markets_nav_child)
                ).apply {

                },
                "WebViewNavFragment",
                homeController.supportFragmentManager.findFragmentById(R.id.fragment_container)!!.javaClass.simpleName
            )

        }


        tv_disclaimer.setOnMyClickListener {
            showDisclaimerDialog()

        }

        tv_invest.setOnMyClickListener {

            showInvestDialog()
        }

        tv_states.setOnClickListener {
            ll_states_graph.makeVisible()
            ll_states.makeVisible()
            ll_portfolio.makeGone()
            chart_horizontal_scroll.makeGone()
            setBackgroundOfButton(tv_states)
            setStates()
        }


        tv_portfolio.setOnMyClickListener {
            stockMarketViewModel!!.getStockPortfolio(0, stockGroupModel!!.id.toString())

            ll_states_graph.makeGone()
            ll_states.makeGone()
            ll_portfolio.makeVisible()
            chart_horizontal_scroll.makeGone()
            setBackgroundOfButton(tv_portfolio)
            //    setUpPortFolio()
        }

        tv_chart.setOnMyClickListener {
            stockMarketViewModel!!.getStockChart(stockGroupModel!!.id.toString())

            ll_states_graph.makeGone()
            ll_states.makeGone()
            ll_portfolio.makeGone()
            chart_horizontal_scroll.makeVisible()
            setBackgroundOfButton(tv_chart)

        }

        rootView!!.ns_scroll.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (v.getChildAt(v.childCount - 1) != null) {
                if ((scrollY >= (v.getChildAt(v.childCount - 1).measuredHeight - v.measuredHeight))
                    && scrollY > oldScrollY
                ) {

                    if (ll_states.isVisible()) {
                        if (investMentHistory != null && !investMentHistory!!.paginationEnded) {
                            stockMarketViewModel!!.getInvestHistory(
                                investMentHistory!!.payload!!.investmentHistory!!.size,
                                stockGroupModel!!.id.toString()
                            )

                        }
                    } else if (ll_portfolio.isVisible()) {
                        if (stockPortfolioModel != null && !stockPortfolioModel!!.paginationEnded) {
                            stockMarketViewModel!!.getStockPortfolio(
                                stockPortfolioModel!!.payload!!.portfolio!!.size,
                                stockGroupModel!!.id.toString()
                            )

                        }
                    }
                }

            }
        })


    }


    private fun setStates() {
        if (!stockStateModel!!.payload!!.investors.isNullOrEmpty()) {
            initInvestorBarChartDownFill()
        }
        if (stockStateModel!!.payload!!.allocationExposure != null && stockStateModel!!.payload!!.allocationExposure?.isNotEmpty()!!) {
            initAllocationLineChart()
        }
        //initRiskAvgBarChartDownFill()
        if (stockStateModel!!.payload!!.risk_investor != null && stockStateModel!!.payload!!.risk_investor?.isNotEmpty()!!) {
            initAverageStackBarChart()
        }
        if (stockStateModel!!.payload!!.allocationValue != null && stockStateModel!!.payload!!.allocationValue?.isNotEmpty()!!) {
            initPieChart()
        }
        initPerformanceGraph(stockStateModel!!.payload!!.performance!!.size - 1, "")
        rootView!!.tv_aum_range.text =
            context!!.getString(R.string.currency_text) + stockStateModel!!.payload!!.detail!!.aumRange
        rootView!!.tv_investor.text =
            stockStateModel!!.payload!!.detail!!.totalInvestors.toString()

        rootView!!.tv_investor_percentage.text =
            stockStateModel!!.payload!!.detail!!.increaseInvestorsPercent.toString() + "%"
        if (stockStateModel!!.payload!!.detail!!.increaseInvestorsPercent.toString()
                .contains("-")
        ) {
            rootView!!.tv_investor_percentage.setTextColor(
                ContextCompat.getColor(
                    homeController,
                    R.color.red_btn
                )
            )

            rootView!!.iv_investor.setImageResource(R.mipmap.ic_red_tringle)

        } else {
            rootView!!.tv_investor_percentage.setTextColor(
                ContextCompat.getColor(
                    homeController,
                    R.color.green_bg
                )
            )
            rootView!!.iv_investor.setImageResource(R.mipmap.ic_triangle)

        }


    }


    private fun setBackgroundOfButton(textview: TextView) {
        rootView!!.tv_states.background = null
        rootView!!.tv_portfolio.background = null
        rootView!!.tv_chart.background = null

        textview.background =
            ContextCompat.getDrawable(homeController, R.drawable.bg_register_tab_selected)

    }


    private fun setUpPortFolio() {
        stockPortfolioModel!!.payload!!.portfolio?.let {
            tv_last_update.text = "${getString(R.string.last_updated)} ${it.get(0)!!.updatedAt}"
            rv_portfolio.adapter =
                StockPortFolioAdapter(homeController, it)

        }
    }


    private fun addObserver() {
        stockMarketViewModel?.responseError!!.observe(
            this@StockMarketDetailFragment,
            Observer {
                homeController.errorBody(it)
                Log.e("qweqwe", "responseError observer")
            })

        stockMarketViewModel!!.isLoading!!.observe(
            this@StockMarketDetailFragment,
            Observer {
                Log.e("qweqwe", "isLoading observer")
                it?.let {
                    if (it) {
                        homeController.showProgressDialog()
                    } else {
                        homeController.dismissProgressDialog()
                    }
                }
            })

        stockMarketViewModel!!.stockStates!!.observe(
            this@StockMarketDetailFragment,
            Observer {
                Log.e("======", "stockstate observer")
                it?.let {

                    stockStateModel = it
                    updateUI(firstCall)
                    rootView!!.cl_main.makeVisible()
                }
            })
        stockMarketViewModel!!.stockPortfolio!!.observe(
            this@StockMarketDetailFragment,
            Observer {
                Log.e("qweqwe", "myNetworkModel observer")
                it?.let {

                    stockPortfolioModel = it
                    if (it.payload!!.portfolio.isNullOrEmpty()) {
                        rootView!!.cv_portfolio.makeGone()
                    } else {
                        rootView!!.cv_portfolio.makeVisible()
                        setUpPortFolio()

                    }
                }
            })

        stockMarketViewModel!!.stockChart!!.observe(
            this@StockMarketDetailFragment,
            Observer {
                Log.e("qweqwe", "myNetworkModel observer")
                it?.let {

                    stockChartModel = it
                    initLineChartDownFill()
                }
            })

        stockMarketViewModel!!.investHistory!!.observe(
            this@StockMarketDetailFragment,
            Observer {
                Log.e("qweqwe", "InvestHistory observer")
                it?.let {

                    investMentHistory = it
                    if (it.payload!!.investmentHistory.isNullOrEmpty()) {
                        rootView!!.cv_investment_history.makeGone()
                    } else {
                        rootView!!.cv_investment_history.makeVisible()
                        rv_investment.adapter =
                            InvestmentHistoryAdapter(
                                homeController,
                                investMentHistory!!.payload!!.investmentHistory,
                                onListClickListener = object : OnListClickListener {
                                    override fun onListClick(position: Int, obj: Any?) {
                                        sellDialog(obj as StockInvestHistoryModel.Payload.InvestmentHistory)

                                    }

                                    override fun onListClickSimple(
                                        position: Int,
                                        string: String?
                                    ) {
                                    }

                                    override fun onListShow(position: Int, obj: Any?) {
                                    }

                                })
                    }
                }
            })
        stockMarketViewModel!!.investAmount!!.observe(
            this@StockMarketDetailFragment,
            Observer {
                it?.let {
                    homeController.message(it.message!!)
                    stockMarketViewModel!!.getInvestHistory(0, stockGroupModel!!.id.toString())

                    var totalInvestmentAmount =
                        UTILS.parseDouble(it.total_investment_amount!!).toFloat()
                    var stockWalletProfit = UTILS.parseDouble(it.stock_wallet_profit!!).toFloat()
                    var total = stockWalletProfit + totalInvestmentAmount

                    tv_total_invested_amount.text =
                        "${getString(R.string.currency_text)}${totalInvestmentAmount}"
                    tv_total_profilt_amount.text =
                        "${getString(R.string.currency_text)}${stockWalletProfit}"
                    tv_total_equity_amount.text = "${getString(R.string.currency_text)}${total}"
                }
            })

        stockMarketViewModel!!.closeInvestment!!.observe(
            this@StockMarketDetailFragment,
            Observer {
                it?.let {
                    homeController.message(it.message!!)

                    stockMarketViewModel!!.getInvestHistory(0, stockGroupModel!!.id.toString())

                    var stockWalletProfit = UTILS.parseDouble(it.stock_wallet_profit!!).toFloat()
                    var totalInvestmentAmount =
                        UTILS.parseDouble(it.total_investment_amount!!).toFloat()
                    var total = stockWalletProfit + totalInvestmentAmount

                    tv_total_invested_amount.text =
                        "${getString(R.string.currency_text)}${totalInvestmentAmount}"
                    tv_total_profilt_amount.text =
                        "${getString(R.string.currency_text)}${stockWalletProfit}"
                    tv_total_equity_amount.text = "${getString(R.string.currency_text)}${total}"
                }
            })

    }


    private fun showDisclaimerDialog() {
        var dialog = Dialog(activity!!, R.style.DialogSlideAnim)
        //  var dialog = Dialog(activity!!)
        dialog.setContentView(R.layout.dialog_disclaimer)
        dialog.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setGravity(Gravity.CENTER)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)


        dialog.tv_description.text =
            Html.fromHtml(stockStateModel!!.payload!!.detail!!.disclaimer)
        dialog.iv_close.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }


    private fun showInvestDialog() {
        var strTitle = getString(R.string.msg_select_investment_plan)
        var planName = ""

        var dialog = Dialog(activity!!, R.style.DialogSlideAnim)
        //  var dialog = Dialog(activity!!)
        dialog.setContentView(R.layout.dialog_invest)
        dialog.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setGravity(Gravity.CENTER)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.tvPlanTC.setText(
            "${getString(R.string.msg_investment_plan_tc_1)} ${getString(R.string.msg_investment_plan_tc_2)} ${
                getString(
                    R.string.msg_investment_plan_tc_3
                )
            } ${getString(R.string.msg_investment_plan_tc_4)} ${getString(R.string.msg_investment_plan_tc_5)}"
        )

        dialog.tvPlanTitle.text = "$strTitle:*"
        dialog.spPlan.adapter = HighLightArrayAdapterV2(
            context = activity!!,
            dropdownResource = R.layout.row_spinner_login_dropdown,
            viewResource = R.layout.row_spinner_login,
            objects = ArrayList<String>()
        ).apply {

            this.add(strTitle)
            stockStateModel!!.payload!!.plans!!.forEach {
                this.add(it!!.name.toString().trim())
            }
        }

        dialog.spPlan.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {
                }

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    dialog!!.spPlan.adapter.let {
                        var x: HighLightArrayAdapterV2 = it as HighLightArrayAdapterV2
                        x.setSelection(p2)
                    }

                    planName = if (p2 == 0) {
                        ""
                    } else {
                        stockStateModel!!.payload!!.plans!![p2 - 1]?.name!! // minus 1 as we added title at 0 position
                    }
                }
            }

        var termsPdf = homeController.oldDashboardModel!!.payload!!.termsPdf
        var riskDisclosurePdf = homeController.oldDashboardModel!!.payload!!.riskDisclosurePdf

        var strTermsAndCondition = getString(R.string.msg_investment_plan_tc_2)
        var strRiskDisclosure = getString(R.string.msg_investment_plan_tc_4)

        dialog.tvPlanTC.makeLinks(
            Pair(strTermsAndCondition, View.OnClickListener {
                startActivity(
                    Intent(activity!!, WebViewActivity::class.java).putExtra(
                        "url",
                        termsPdf
                    ).putExtra("isPdf", true)
                )

            }),
            Pair(strRiskDisclosure, View.OnClickListener {
                startActivity(
                    Intent(activity!!, WebViewActivity::class.java).putExtra(
                        "url",
                        riskDisclosurePdf
                    ).putExtra("isPdf", true)
                )
            })
        )


        dialog.rl_invest.setOnMyClickListener {

            homeController.hideSoftKeyboard()
            var strAmount = dialog.edt_amount.text.toString().trim()
            var minAmount = stockStateModel?.payload?.minStockWalletInvestmentAmount!!.toInt()
            if (strAmount.isEmpty()) {
                homeController.message(getString(R.string.please_enter_amount))
            } else if (strAmount.toInt() < minAmount) {
                homeController.message(getString(R.string.valid_amount_validation) + " " + minAmount)
            } else if (dialog.spPlan.selectedItemPosition == 0) {
                homeController.message(strTitle)

            } else if (!dialog.cbPlanTC.isChecked) {
                homeController.message(getString(R.string.msg_please_accept_agreement))

            } else {
                UTILS.showDialog(
                    homeController,
                    getString(R.string.are_you_sure_want_to_invest),
                    onListClickListener = object : OnListClickListener {
                        override fun onListClick(position: Int, obj: Any?) {

                        }

                        override fun onListClickSimple(position: Int, string: String?) {
                        }

                        override fun onListShow(position: Int, obj: Any?) {
                            stockMarketViewModel!!.investAmount(
                                stockGroupModel!!.id.toString(),
                                dialog.edt_amount.text.toString(),
                                planName
                            )
                            dialog.dismiss()
                        }

                    })
            }
        }
        dialog.iv_close.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }


    private fun sellDialog(invest: StockInvestHistoryModel.Payload.InvestmentHistory) {
        UTILS.showDialog(
            homeController,
            getString(R.string.are_you_sure_want_to_sell),
            onListClickListener = object : OnListClickListener {
                override fun onListClick(position: Int, obj: Any?) {

                }

                override fun onListClickSimple(position: Int, string: String?) {
                }

                override fun onListShow(position: Int, obj: Any?) {
                    stockMarketViewModel!!.closeInvestment(
                        invest!!.id.toString()

                    )

                }

            })
    }

    /**
     * Line chart of Chart module
     */

    private fun initLineChartDownFill() {
        /*   stockMarketDetailModel!!.payload!!.chart?.let {
               if (it.isNotEmpty()) {
                   if (it[0]!!.date == "0" && it[0]!!.value == "0") {

                   } else {
                       stockMarketDetailModel!!.payload!!.chart!!.add(
                           0,
                           StockDetaiModel.Payload.Chart("0", "0")
                       )
                   }
               }
           }*/
        //    rootView!!.chart.minimumWidth = (tradeDashboardModel!!.payload!!.earning!!.size*30)
//            UTILS.dpToPixel(
//                activity!!,
//                100 * tradeDashboardModel!!.payload!!.earning!!.size
//            )

        rootView!!.line_chart.setDrawGridBackground(false)
        rootView!!.line_chart.description.isEnabled = false
        rootView!!.line_chart.setScaleEnabled(false)
        rootView!!.line_chart.setPinchZoom(false)
        rootView!!.line_chart.axisRight.isEnabled = false
        rootView!!.line_chart.xAxis.setDrawGridLines(false)
        rootView!!.line_chart.axisLeft.setDrawGridLines(false)
        rootView!!.line_chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        rootView!!.line_chart.xAxis.labelRotationAngle = -45f
        rootView!!.line_chart.xAxis.isGranularityEnabled = true
        rootView!!.line_chart.xAxis.granularity = 1F
        // rootView!!.line_chart.extraRightOffset = 30f
        rootView!!.line_chart.extraTopOffset = 30f

        rootView!!.line_chart.xAxis.labelCount =
            stockChartModel!!.payload!!.chart!!.size
        rootView!!.line_chart.xAxis.textColor =
            ContextCompat.getColor(activity!!, R.color.white)
        rootView!!.line_chart.axisLeft.textColor =
            ContextCompat.getColor(activity!!, R.color.white)
        val formatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                if (value < 0 || value >= stockChartModel!!.payload!!.chart!!.size) {
                    return ""
                }
                return UTILS.convertDate(
                    "yyyy-MM-dd HH:mm:ss",
                    "yyyy-MM-dd",
                    stockChartModel!!.payload!!.chart?.get(value.toInt())!!.date.toString()
                )
            }
        }
        rootView!!.line_chart.xAxis.valueFormatter = formatter
        //  rootView!!.line_chart.axisLeft.axisMinimum = 0f
        //   rootView!!.chart.xAxis.mAxisMaximum=60f
        //  rootView!!.line_chart.xAxis.axisMinimum = 0f
        rootView!!.line_chart.xAxis.mAxisMaximum =
            (stockChartModel!!.payload!!.chart!!.size).toFloat()
        // rootView!!.chart.setVisibleXRangeMaximum(5f)

        if (stockChartModel!!.payload!!.chart!!.size!! > 0)
            rootView!!.line_chart.axisLeft.axisMaximum =
                stockChartModel!!.payload!!.chart!!.maxBy { it!!.value!!.toFloat() }!!.value!!.toFloat()



        lineChartDownFillWithData()
    }


    private fun lineChartDownFillWithData() {

        val entryArrayList = ArrayList<Entry>()
        for (i in stockChartModel!!.payload!!.chart!!.indices) {
            entryArrayList.add(
                Entry(
                    i.toFloat(),
                    stockChartModel!!.payload!!.chart?.get(i)!!.value!!.toFloat()
                )
            )
        }

        /*for (i in 0 until 45) {
            entryArrayList.add(
                Entry(
                    i.toFloat(),
                    ((Math.random() * 180) - 30).toFloat()
                )
            )
        }*/

        //LineDataSet is the line on the graph
        val lineDataSet = LineDataSet(entryArrayList, "")
        lineDataSet.setDrawIcons(false)

        lineDataSet.mode = LineDataSet.Mode.LINEAR
        //lineDataSet.cubicIntensity = 0.20f
        lineDataSet.color = ContextCompat.getColor(activity!!, R.color.register_tab_color)
        lineDataSet.setCircleColor(ContextCompat.getColor(activity!!, R.color.register_tab_color))
        lineDataSet.lineWidth = 1f
        lineDataSet.circleRadius = 1f
        lineDataSet.setDrawCircleHole(true)
        lineDataSet.circleHoleColor = Color.parseColor("#1BADB8")
        lineDataSet.formLineWidth = 1f
        lineDataSet.formSize = 0f
        lineDataSet.fillAlpha = 1
        val drawable = ContextCompat.getDrawable(context!!, R.drawable.graph_gradient)
        lineDataSet.fillDrawable = drawable
        lineDataSet.setValueFormatter(YValyeFormatter())
        lineDataSet.setDrawFilled(false)
        lineDataSet.setDrawValues(false)
        val iLineDataSetArrayList = ArrayList<ILineDataSet>()

        /**Temporary removed in order to make same as IOS */
        /**following code was for make lable in double and remove 0th lable*/

/*

        val formatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                if (value <= 0) {
                    return ""
                }
                return parseDouble(value.toString())
            }

            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                Log.e("zxczxc", " getAxisLabel - $value ")
                if (value < 0 || value >= tradeDashboardModel!!.payload!!.groupSalesChart!!.size) {
                    return ""
                }

                return parseDouble(
                    tradeDashboardModel!!.payload!!.groupSalesChart!![value.toInt()].value.toString()
                )
            }
        }
        lineDataSet.valueFormatter = formatter

*/
        iLineDataSetArrayList.add(lineDataSet)

        //LineData is the data accord
        val lineData = LineData(iLineDataSetArrayList)
        lineData.setValueTextSize(12f)

        lineData.setValueTextColor(Color.WHITE)
        rootView!!.line_chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        rootView!!.line_chart.axisRight.isEnabled = false

        rootView!!.line_chart.data = lineData
        rootView!!.line_chart.invalidate()


        rootView!!.line_chart.setOnChartValueSelectedListener(object :
            OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry, h: Highlight?) {
                var xValue = e.x
                var yValue = e.y
//                var data = e.data
//                Log.e("LineChart", " data e : "+e)
//                homeController.message( "x: $xValue , y: $yValue")
                //   homeController.message("$yValue")

                if (toast != null) toast!!.cancel()
                toast = Toast.makeText(context, "$yValue", Toast.LENGTH_SHORT)
                toast!!.show()

            }

            override fun onNothingSelected() {}
        })
    }


    //Bar graph for sales
    private fun initInvestorBarChartDownFill() {

        stockStateModel!!.payload!!.investors?.let {
            if (it.isNotEmpty()) {
                if (it[0]!!.date == "0" && it[0]!!.value == "0") {

                } else {
                    stockStateModel!!.payload!!.investors?.add(
                        0,
                        StockStatesModel.Payload.Investor("0", "0")
                    )
                }
            }
        }
        /*     rootView!!.chartBar.minimumWidth =
                 UTILS.dpToPixel(
                     activity!!,
                     30 * tradeDashboardModel!!.payload!!.groupSalesChart!!.size
                 )*/
        //rootView!!.chartBar.setPadding(20,0,20,0)
        rootView!!.investor_chart.setDrawGridBackground(false)
        rootView!!.investor_chart.description.isEnabled = false
        rootView!!.investor_chart.setScaleEnabled(false)
        rootView!!.investor_chart.setPinchZoom(false)
        rootView!!.investor_chart.axisRight.isEnabled = false
        rootView!!.investor_chart.xAxis.setDrawGridLines(false)
        rootView!!.investor_chart.axisLeft.setDrawGridLines(false)
        rootView!!.investor_chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        rootView!!.investor_chart.xAxis.isGranularityEnabled = true
        rootView!!.investor_chart.xAxis.granularity = 1F
        rootView!!.investor_chart.xAxis.labelRotationAngle = -40f
        //    rootView!!.investor_chart.xAxis.spaceMin = -0.9f
        //  rootView!!.investor_chart.extraLeftOffset = 30f
        //rootView!!.chartBar.extraRightOffset = 30f
        //rootView!!.chartBar.extraTopOffset = 30f

        rootView!!.investor_chart.xAxis.labelCount =
            stockStateModel!!.payload!!.investors!!.size
        rootView!!.investor_chart.xAxis.textColor =
            ContextCompat.getColor(context!!, R.color.white)
        rootView!!.investor_chart.axisLeft.textColor =
            ContextCompat.getColor(context!!, R.color.white)
        val formatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                if (value < 0 || value >= stockStateModel!!.payload!!.investors!!.size) {
                    return ""
                }
                return UTILS.convertDate(
                    "yyyy-MM-dd",
                    "yyyy-MM-dd", //"dd-MM"
                    stockStateModel!!.payload!!.investors!![value.toInt()]!!.date.toString()
                )
            }
        }
        rootView!!.investor_chart.xAxis.valueFormatter = formatter

        //    rootView!!.investor_chart.xAxis.axisMaximum = stockStateModel!!.payload!!.investors!!.size.toFloat()
        //    rootView!!.investor_chart.setVisibleXRange(1f,stockStateModel!!.payload!!.investors!!.size.toFloat())

        /*val mv = XYMarkerView(context!!, formatter)
        mv.chartView = chart // For bounds control
        rootView!!.chartBar.marker = mv*/

        //  rootView!!.investor_chart.axisLeft.axisMinimum = 0f
        /*   rootView!!.investor_chart.axisLeft.axisMaximum =
               stockMarketDetailModel!!.payload!!.stats!!.investors?.maxBy { it!!.value!!.toFloat() }!!.value!!.toInt() + 800.toFloat()*/

        // add a nice and smooth animation
        rootView!!.investor_chart.animateY(1500)

        rootView!!.investor_chart.legend.isEnabled = false

        barChartDownFillWithData()

    }


    private fun barChartDownFillWithData() {

        val entryArrayList = ArrayList<BarEntry>()
        for (i in stockStateModel!!.payload!!.investors!!.indices) {
            entryArrayList.add(
                BarEntry(
                    i.toFloat(),
                    UTILS.parseDouble(stockStateModel!!.payload!!.investors!![i]!!.value!!)
                        .toFloat()
                )
            )


        }
        var set1: BarDataSet? = null

        if (rootView!!.investor_chart.data != null &&
            rootView!!.investor_chart.data.dataSetCount > 0
        ) {
            set1 = rootView!!.investor_chart.data.getDataSetByIndex(0) as BarDataSet
            set1.values = entryArrayList
            rootView!!.investor_chart.data.notifyDataChanged()
            rootView!!.investor_chart.notifyDataSetChanged()
        } else {
            set1 = BarDataSet(entryArrayList, "Data Set")
            set1.setColors(
                ContextCompat.getColor(
                    context!!,
                    R.color.light_blue
                )
            )  //*ColorTemplate.VORDIPLOM_COLORS
            set1.setDrawValues(true)
            set1.setValueFormatter(YValyeFormatter())

            val dataSets = java.util.ArrayList<IBarDataSet>()
            dataSets.add(set1)
            val data = BarData(dataSets)
            data.setValueTextSize(12f)
            data.setDrawValues(true)
            data.setValueTextColor(Color.WHITE)
            //data.barWidth=0.5f
            //data.getGroupWidth(1f,2f)
            rootView!!.investor_chart.data = data
            rootView!!.investor_chart.setDrawValueAboveBar(true)
            //  rootView!!.investor_chart.setFitBars(true)

        }
        rootView!!.investor_chart.data = BarData(set1).apply {
            barWidth = 0.5f
        }
        rootView!!.investor_chart.invalidate()


    }


    //Bar graph for sales
    private fun initRiskAvgBarChartDownFill() {

        /*   stockStateModel!!.payload!!.risk_investor?.let {
               if (it.isNotEmpty()) {
                   if (it[0]!!.date == "0" && it[0]!!.value == "0") {

                   } else {
                       stockStateModel!!.payload!!.risk_investor?.add(
                           0,
                           StockStatesModel.Payload.RiskAvg("0", "0")
                       )
                   }
               }
           }*/
        /*     rootView!!.chartBar.minimumWidth =
                 UTILS.dpToPixel(
                     activity!!,
                     30 * tradeDashboardModel!!.payload!!.groupSalesChart!!.size
                 )*/
        //rootView!!.chartBar.setPadding(20,0,20,0)
        rootView!!.risk_avg_chart.setDrawGridBackground(false)
        rootView!!.risk_avg_chart.description.isEnabled = false
        rootView!!.risk_avg_chart.setScaleEnabled(false)
        rootView!!.risk_avg_chart.setPinchZoom(false)
        rootView!!.risk_avg_chart.axisRight.isEnabled = false
        rootView!!.risk_avg_chart.xAxis.setDrawGridLines(false)
        rootView!!.risk_avg_chart.axisLeft.setDrawGridLines(false)
        rootView!!.risk_avg_chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        rootView!!.risk_avg_chart.xAxis.isGranularityEnabled = true
        rootView!!.risk_avg_chart.xAxis.granularity = 1F
        rootView!!.risk_avg_chart.xAxis.labelRotationAngle = -40f
        //   rootView!!.risk_avg_chart.xAxis.spaceMin = -0.9f
        //rootView!!.chartBar.extraLeftOffset = 30f
        //rootView!!.chartBar.extraRightOffset = 30f
        //rootView!!.chartBar.extraTopOffset = 30f

        rootView!!.risk_avg_chart.xAxis.labelCount =
            stockStateModel!!.payload!!.risk_investor!!.size
        rootView!!.risk_avg_chart.xAxis.textColor =
            ContextCompat.getColor(context!!, R.color.white)
        rootView!!.risk_avg_chart.axisLeft.textColor =
            ContextCompat.getColor(context!!, R.color.white)
        val formatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                if (value < 0 || value >= stockStateModel!!.payload!!.risk_investor!!.size) {
                    return ""
                }
                return UTILS.convertDate(
                    "yyyy-MM-dd",
                    "MMM,yyyy", //"dd-MM"
                    stockStateModel!!.payload!!.risk_investor!![value.toInt()]!!.date.toString()
                )
            }
        }
        rootView!!.risk_avg_chart.xAxis.valueFormatter = formatter

        //    rootView!!.investor_chart.xAxis.axisMaximum = stockStateModel!!.payload!!.investors!!.size.toFloat()
        //    rootView!!.investor_chart.setVisibleXRange(1f,stockStateModel!!.payload!!.investors!!.size.toFloat())

        /*val mv = XYMarkerView(context!!, formatter)
        mv.chartView = chart // For bounds control
        rootView!!.chartBar.marker = mv*/

        //  rootView!!.investor_chart.axisLeft.axisMinimum = 0f
        /*   rootView!!.investor_chart.axisLeft.axisMaximum =
               stockMarketDetailModel!!.payload!!.stats!!.investors?.maxBy { it!!.value!!.toFloat() }!!.value!!.toInt() + 800.toFloat()*/

        // add a nice and smooth animation
        rootView!!.risk_avg_chart.animateY(1500)

        rootView!!.risk_avg_chart.legend.isEnabled = false

        riskAvgbarChartWithData()

    }


    private fun initAllocationLineChart() {
        /*   stockMarketDetailModel!!.payload!!.chart?.let {
               if (it.isNotEmpty()) {
                   if (it[0]!!.date == "0" && it[0]!!.value == "0") {

                   } else {
                       stockMarketDetailModel!!.payload!!.chart!!.add(
                           0,
                           StockDetaiModel.Payload.Chart("0", "0")
                       )
                   }
               }
           }*/
        //    rootView!!.chart.minimumWidth = (tradeDashboardModel!!.payload!!.earning!!.size*30)
//            UTILS.dpToPixel(
//                activity!!,
//                100 * tradeDashboardModel!!.payload!!.earning!!.size
//            )
        rootView!!.allocation_line_chart.setDrawGridBackground(false)
        rootView!!.allocation_line_chart.setExtraOffsets(5f, 10f, 5f, 15f)

        rootView!!.allocation_line_chart.description.isEnabled = false
        rootView!!.allocation_line_chart.setScaleEnabled(false)
        rootView!!.allocation_line_chart.setPinchZoom(false)
        rootView!!.allocation_line_chart.axisRight.isEnabled = false
        rootView!!.allocation_line_chart.xAxis.setDrawGridLines(false)
        rootView!!.allocation_line_chart.axisLeft.setDrawGridLines(false)
        rootView!!.allocation_line_chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        rootView!!.allocation_line_chart.xAxis.labelRotationAngle = -35f
        rootView!!.allocation_line_chart.xAxis.isGranularityEnabled = true
        rootView!!.allocation_line_chart.xAxis.granularity = 1F
        // rootView!!.line_chart.extraRightOffset = 30f
        //   rootView!!.allocation_line_chart.extraTopOffset = 30f

        //    rootView!!.allocation_line_chart.xAxis.labelCount =
        //        stockStateModel!!.payload!!.allocationExposure!!.size
        //
        //

        rootView!!.allocation_line_chart.xAxis.textColor =
            ContextCompat.getColor(activity!!, R.color.white)
        rootView!!.allocation_line_chart.axisLeft.textColor =
            ContextCompat.getColor(activity!!, R.color.white)
        val formatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                if (value < 0 || value >= stockStateModel!!.payload!!.allocationExposure!!.get(0)!!.data!!.size) {
                    return ""
                }
                return UTILS.convertDate(
                    "dd MMM, yy",
                    "dd MMM, yy",
                    stockStateModel!!.payload!!.allocationExposure!!.get(0)!!.data!!.get(value.toInt())!!.name.toString()
                )

            }
        }

        rootView!!.allocation_line_chart.xAxis.valueFormatter = formatter
        rootView!!.allocation_line_chart.xAxis.labelCount =
            stockStateModel!!.payload!!.allocationExposure!!.get(0)!!.data!!.size

        //   rootView!!.allocation_line_chart.axisLeft.axisMinimum = 0f
        //   rootView!!.chart.xAxis.mAxisMaximum=60f

        // rootView!!.allocation_line_chart.xAxis.mAxisMaximum =
        //     (stockStateModel!!.payload!!.allocationExposure!!.size).toFloat()

        // rootView!!.chart.setVisibleXRangeMaximum(5f)

        /*  if (stockMarketDetailModel!!.payload!!.chart!!.size!! > 0)
              rootView!!.allocation_line_chart.axisLeft.axisMaximum =
                  stockMarketDetailModel!!.payload!!.chart!!.maxBy { it!!.value!!.toFloat() }!!.value!!.toFloat()

  */

        rootView!!.allocation_line_chart.legend.isEnabled = true
        val l: Legend = rootView!!.allocation_line_chart.getLegend()
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.textColor = ContextCompat.getColor(homeController, R.color.white)
        l.setDrawInside(false)
        l.xEntrySpace = 7f
        l.yEntrySpace = 0f
        l.yOffset = 0f

        lineChartAllocationData()

    }


    private
    val colors = intArrayOf(
        R.color.line_1,
        R.color.line_2,
        R.color.line_3,
        R.color.line_4,
        R.color.line_5,
        R.color.line_7,
        R.color.line_8,
        R.color.line_9,
        R.color.line_10
    )

    private fun lineChartAllocationData() {
        val iLineDataSetArrayList = ArrayList<ILineDataSet>()
        val androidColors =
            resources.getIntArray(R.array.androidcolors)

        for (i in 0..stockStateModel!!.payload!!.allocationExposure!!.size - 1) {


            val entryArrayList = ArrayList<Entry>()
            for (j in stockStateModel!!.payload!!.allocationExposure!!.get(i)!!.data!!.indices) {
                entryArrayList.add(
                    Entry(
                        j.toFloat(),
                        stockStateModel!!.payload!!.allocationExposure!!.get(i)!!.data!!.get(
                            j
                        )!!.value!!.toFloat()
                    )
                )
            }

            val lineDataSet = LineDataSet(entryArrayList, "")
            lineDataSet.setDrawIcons(false)

            lineDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
            //lineDataSet.cubicIntensity = 0.20f
            var color = androidColors[Random().nextInt(androidColors.size)]
            lineDataSet.color = color
            lineDataSet.setCircleColor(color)
            lineDataSet.lineWidth = 1f
            lineDataSet.circleRadius = 2f
            lineDataSet.setDrawCircleHole(true)
            //       lineDataSet.circleHoleColor = Color.parseColor("#131230")
            lineDataSet.formLineWidth = 1f
            lineDataSet.formSize = 0f
            //  lineDataSet.fillAlpha = 1
            lineDataSet.fillColor = color
            //     lineDataSet.setDrawFilled(true)
            lineDataSet.setDrawValues(false)

            lineDataSet.setValueFormatter(YValyeFormatter())

            iLineDataSetArrayList.add(lineDataSet)
        }
        //LineData is the data accord
        val lineData = LineData(iLineDataSetArrayList)
        lineData.setValueTextSize(12f)

        lineData.setValueTextColor(Color.WHITE)
        rootView!!.allocation_line_chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        rootView!!.allocation_line_chart.axisRight.isEnabled = false
        rootView!!.allocation_line_chart.legend.isEnabled = true


        //TODO: Set Legend to Graph
        var legnedEntry = arrayListOf<LegendEntry>()
        for (i in stockStateModel!!.payload!!.allocationExposure!!.indices) {
            legnedEntry.add(
                LegendEntry(
                    stockStateModel!!.payload!!.allocationExposure!![i]!!.name,
                    Legend.LegendForm.CIRCLE, 10f, 10f, null, iLineDataSetArrayList[i].color
                )
            )
        }
        val l: Legend = rootView!!.allocation_line_chart.getLegend()
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.textColor = ContextCompat.getColor(homeController, R.color.white)
        l.setCustom(legnedEntry)
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.mTextHeightMax = 30f
        l.xEntrySpace = 10f

        rootView!!.allocation_line_chart.data = lineData
        rootView!!.allocation_line_chart.invalidate()

        rootView!!.allocation_line_chart.setOnChartValueSelectedListener(object :
            OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry, h: Highlight?) {
                var xValue = e.x
                var yValue = e.y
//                var data = e.data
//                Log.e("LineChart", " data e : "+e)
//                homeController.message( "x: $xValue , y: $yValue")
                homeController.message("$yValue")
            }

            override fun onNothingSelected() {}
        })

    }

    private fun initPieChart() {
        //    rootView!!.pieChart.setUsePercentValues(true)
        rootView!!.pieChart.getDescription().setEnabled(false)
        rootView!!.pieChart.setExtraOffsets(5f, 10f, 5f, 5f)

        //  rootView!!.pieChart.setDragDecelerationFrictionCoef(0.95f)

        //   rootView!!.pieChart.setCenterTextTypeface(tfLight)
        //   rootView!!.pieChart.setCenterText(generateCenterSpannableText())

        rootView!!.pieChart.setDrawHoleEnabled(false)
        //  rootView!!.pieChart.setHoleColor(Color.WHITE)

        //     rootView!!.pieChart.setTransparentCircleColor(Color.WHITE)
        //    rootView!!.pieChart.setTransparentCircleAlpha(110)

        //rootView!!.pieChart.setHoleRadius(10f)
        //    rootView!!.pieChart.setTransparentCircleRadius(61f)

        rootView!!.pieChart.setDrawCenterText(true)

        rootView!!.pieChart.setRotationAngle(0f)
        // enable rotation of the chart by touch
        // enable rotation of the chart by touch
        rootView!!.pieChart.setRotationEnabled(true)
        rootView!!.pieChart.setHighlightPerTapEnabled(true)

        // chart.setUnit(" €");
        // chart.setDrawUnitsInChart(true);

        // add a selection listener

        // chart.setUnit(" €");
        // chart.setDrawUnitsInChart(true);

        // add a selection listener

        rootView!!.pieChart.animateY(1400, Easing.EaseInOutQuad)
        // chart.spin(2000, 0, 360);

        // chart.spin(2000, 0, 360);
        val l: Legend = rootView!!.pieChart.getLegend()
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.textColor = ContextCompat.getColor(homeController, R.color.white)
        l.setDrawInside(false)
        l.xEntrySpace = 7f
        l.yEntrySpace = 0f
        l.yOffset = 0f

        // entry label styling

        // entry label styling
        rootView!!.pieChart.setDrawEntryLabels(false)
        rootView!!.pieChart.setEntryLabelColor(Color.WHITE)
        //   rootView!!.pieChart.setEntryLabelTypeface(tfRegular)
        rootView!!.pieChart.setEntryLabelTextSize(13f)

        setPieChartData()
    }


    private fun setPieChartData() {
        val entries: ArrayList<PieEntry> = ArrayList()

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        for (i in 0 until stockStateModel!!.payload!!.allocationValue!!.size) {
            var value = stockStateModel!!.payload!!.allocationValue!!.get(i)!!.value!!.toFloat()
            var name = stockStateModel!!.payload!!.allocationValue!!.get(i)!!.name
//            entries.add(PieEntry(value, "$name(${parseDouble(value.toString())})"))

            entries.add(PieEntry(value, "$name"))

            /*entries.add(
                PieEntry(
                    stockStateModel!!.payload!!.allocationValue!!.get(i)!!.value!!.toFloat(),
                    stockStateModel!!.payload!!.allocationValue!!.get(i)!!.name
                )
            )*/
        }

        val dataSet = PieDataSet(entries, "")

        dataSet.setDrawIcons(false)

        dataSet.sliceSpace = 1f
        // dataSet.iconsOffset = MPPointF(0, 40)
        dataSet.selectionShift = 5f

        // add a lot of colors


        // add a lot of colors
        val colors: ArrayList<Int> = ArrayList()
        colors.add(ContextCompat.getColor(homeController, R.color.line_1))
        colors.add(ContextCompat.getColor(homeController, R.color.line_2))
        colors.add(ContextCompat.getColor(homeController, R.color.line_3))
        colors.add(ContextCompat.getColor(homeController, R.color.line_4))
        colors.add(ContextCompat.getColor(homeController, R.color.line_5))
        colors.add(ContextCompat.getColor(homeController, R.color.line_10))
        colors.add(ContextCompat.getColor(homeController, R.color.line_7))
        colors.add(ContextCompat.getColor(homeController, R.color.line_8))
        colors.add(ContextCompat.getColor(homeController, R.color.line_9))

        dataSet.colors = colors
        //dataSet.setSelectionShift(0f);
        //dataSet.setSelectionShift(0f);
        val data = PieData(dataSet)
        data.setValueFormatter(YValyeFormatter())
        data.setValueTextSize(13f)
        data.setValueTextColor(Color.WHITE)
        //    data.setValueTypeface(tfLight)
        rootView!!.pieChart.setData(data)

        // undo all highlights

        // undo all highlights
        rootView!!.pieChart.highlightValues(null)

        rootView!!.pieChart.invalidate()
    }


    private fun initPerformanceGraph(position: Int, year: String) {
        // rootView!!.chart_performance.setExtraTopOffset(-30f)
        rootView!!.chart_performance.setExtraBottomOffset(10f)
        //    rootView!!.chart_performance.setExtraLeftOffset(70f)
        //    rootView!!.chart_performance.setExtraRightOffset(70f)

        rootView!!.chart_performance.setDrawBarShadow(false)
        rootView!!.chart_performance.setDrawValueAboveBar(true)

        rootView!!.chart_performance.getDescription().setEnabled(false)

        // scaling can now only be done on x- and y-axis separately

        // scaling can now only be done on x- and y-axis separately
        rootView!!.chart_performance.setPinchZoom(false)

        rootView!!.chart_performance.setDrawGridBackground(false)

        val xAxis: XAxis = rootView!!.chart_performance.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        //     xAxis.typeface = tfRegular
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)
        xAxis.textColor = Color.LTGRAY
        xAxis.textSize = 13f
        xAxis.setCenterAxisLabels(true)
        xAxis.granularity = 1f


        val left: YAxis = rootView!!.chart_performance.axisLeft
        left.setDrawLabels(true)
//        left.setSpaceBottom(25f)
        // left.setDrawAxisLine(false)
        left.setDrawGridLines(false)
        left.setDrawZeroLine(true) // draw a zero line
        left.textColor =
            ContextCompat.getColor(context!!, R.color.white)
        left.setZeroLineColor(Color.GRAY)
        left.setZeroLineWidth(0.7f)

        rootView!!.chart_performance.getAxisRight().setEnabled(false)
        rootView!!.chart_performance.getLegend().setEnabled(false)
        stockStateModel!!.payload!!.performance!!.forEach {

            if (it!!.name.equals(year)) {
                xAxis.labelCount = it.value!!.size

                val formatter = object : ValueFormatter() {
                    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                        if (value < 0 || value >= it.value!!.size) {
                            return ""
                        }
                        return it.value!![value.toInt()]!!.date.toString()
                    }
                }
                rootView!!.chart_performance.xAxis.valueFormatter = formatter
                /* rootView!!.chart_performance.axisLeft.axisMaximum =
                     it.value?.maxBy { it!!.value!!.toFloat() }!!.value!!.toInt() + 800.toFloat()

                 rootView!!.chart_performance.axisLeft.axisMinimum =
                     it.value?.maxBy { it!!.value!!.toFloat() }!!.value!!.toInt() - 200.toFloat()
                 rootView!!.chart_performance.minimumWidth =
                     UTILS.dpToPixel(
                         activity!!,
                         30 * it.value!!.size
                     )
 */
                setData(it.value)

            }

        }
    }


    private fun setData(dataList: List<StockStatesModel.Payload.Performance.Value?>?) {
        val values: ArrayList<BarEntry> = ArrayList()
        val colors: ArrayList<Int> = ArrayList()

        for (i in 0 until dataList!!.size) {
            val d: StockStatesModel.Payload.Performance.Value = dataList[i]!!

            val entry = BarEntry(i.toFloat(), UTILS.parseDouble(d.value!!.toString()).toFloat())
            values.add(entry)

            // specific colors
            if (d.value!! >= 0.0) {
                colors.add(ContextCompat.getColor(homeController, R.color.green_bg))
            } else {
                colors.add(ContextCompat.getColor(homeController, R.color.red_btn))
            }

        }


        val set: BarDataSet = BarDataSet(values, "Values")
        set.colors = colors
        set.setValueTextColors(colors)
        set.setValueFormatter(YValyeFormatter())

        val data = BarData(set)
        data.setValueTextSize(13f)
        // data.setValueTypeface(tfRegular)
        data.setValueFormatter(YValyeFormatter())
        rootView!!.chart_performance.setFitBars(true)

        data.barWidth = 0.5f
        rootView!!.chart_performance.setData(data)
        rootView!!.chart_performance.invalidate()

    }


    private fun initAverageStackBarChart() {
        rootView!!.risk_avg_chart.setDrawGridBackground(false)
        rootView!!.risk_avg_chart.description.isEnabled = false
        rootView!!.risk_avg_chart.setScaleEnabled(false)
        rootView!!.risk_avg_chart.setPinchZoom(false)
        rootView!!.risk_avg_chart.axisRight.isEnabled = false
        rootView!!.risk_avg_chart.xAxis.setDrawGridLines(false)
        rootView!!.risk_avg_chart.axisLeft.setDrawGridLines(false)
        rootView!!.risk_avg_chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        rootView!!.risk_avg_chart.xAxis.isGranularityEnabled = true
        rootView!!.risk_avg_chart.xAxis.granularity = 1F
        rootView!!.risk_avg_chart.xAxis.labelRotationAngle = -40f
        //   rootView!!.risk_avg_chart.xAxis.spaceMin = f
        rootView!!.risk_avg_chart.extraBottomOffset = 30f
        //rootView!!.chartBar.extraRightOffset = 30f
        //rootView!!.chartBar.extraTopOffset = 30f

        rootView!!.risk_avg_chart.xAxis.labelCount =
            stockStateModel!!.payload!!.risk_investor!!.size
        rootView!!.risk_avg_chart.xAxis.textColor =
            ContextCompat.getColor(context!!, R.color.white)
        rootView!!.risk_avg_chart.axisLeft.textColor =
            ContextCompat.getColor(context!!, R.color.white)
        val formatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                if (value < 0 || value >= stockStateModel!!.payload!!.risk_investor!!.size) {
                    return ""
                }
                return UTILS.convertDate(
                    "yyyy-MM-dd",
                    "MMM,yyyy", //"dd-MM"
                    stockStateModel!!.payload!!.risk_investor!![value.toInt()]!!.date.toString()
                )
            }
        }
        rootView!!.risk_avg_chart.xAxis.valueFormatter = formatter

        //    rootView!!.investor_chart.xAxis.axisMaximum = stockStateModel!!.payload!!.investors!!.size.toFloat()
        //    rootView!!.investor_chart.setVisibleXRange(1f,stockStateModel!!.payload!!.investors!!.size.toFloat())

        /*val mv = XYMarkerView(context!!, formatter)
        mv.chartView = chart // For bounds control
        rootView!!.chartBar.marker = mv*/

        //  rootView!!.investor_chart.axisLeft.axisMinimum = 0f
        /*   rootView!!.investor_chart.axisLeft.axisMaximum =
               stockMarketDetailModel!!.payload!!.stats!!.investors?.maxBy { it!!.value!!.toFloat() }!!.value!!.toInt() + 800.toFloat()*/

        // add a nice and smooth animation
        rootView!!.risk_avg_chart.animateY(1500)

        rootView!!.risk_avg_chart.legend.isEnabled = true

        riskAvgbarChartWithData()


    }


    private fun riskAvgbarChartWithData() {

        val entryArrayList = ArrayList<BarEntry>()
        for (i in stockStateModel!!.payload!!.risk_investor!!.indices) {


            entryArrayList.add(
                BarEntry(
                    i.toFloat(),
                    floatArrayOf(
                        stockStateModel!!.payload!!.risk_investor!![i]!!.max_value!!.toFloat(),
                        stockStateModel!!.payload!!.risk_investor!![i]!!.value!!.toFloat()
                    ),
                    null

                )
            )
        }
        var set1: BarDataSet? = null

        set1 = BarDataSet(entryArrayList, "Data Set")
        set1.setColors(
            getColors()
        )  //*ColorTemplate.VORDIPLOM_COLORS
        set1.setDrawValues(true)
        set1.setValueFormatter(YValyeFormatter())


        val dataSets = java.util.ArrayList<IBarDataSet>()
        dataSets.add(set1)
        val data = BarData(dataSets)


        data.setValueTextSize(12f)
        data.setDrawValues(true)
        data.setValueTextColor(Color.WHITE)
        //data.barWidth=0.5f
        //data.getGroupWidth(1f,2f)

        rootView!!.risk_avg_chart.setFitBars(true)


        rootView!!.risk_avg_chart.data = data.apply {
            barWidth = 0.5f
        }


        var legnedEntry = arrayListOf<LegendEntry>()
        legnedEntry.add(
            LegendEntry(
                getString(R.string.avg_risk),
                Legend.LegendForm.CIRCLE,
                10f,
                10f,
                null,
                ContextCompat.getColor(homeController, R.color.bar_avg_risk)
            )
        )
        legnedEntry.add(
            LegendEntry(
                getString(R.string.max_risk),
                Legend.LegendForm.CIRCLE,
                10f,
                10f,
                null,
                ContextCompat.getColor(homeController, R.color.light_blue)
            )
        )
        val l: Legend = rootView!!.risk_avg_chart.getLegend()
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.textColor = ContextCompat.getColor(homeController, R.color.white)
        l.setCustom(legnedEntry)
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.mTextHeightMax = 30f
        l.xEntrySpace = 10f
        rootView!!.risk_avg_chart.setDrawValueAboveBar(true)

        rootView!!.risk_avg_chart.invalidate()


    }


    private fun getColors(): List<Int>? {

        // have as many colors as stack-values per entry
        val colors = ArrayList<Int>()
        colors.add(ContextCompat.getColor(homeController, R.color.bar_avg_risk))
        colors.add(ContextCompat.getColor(homeController, R.color.light_blue))
        return colors
    }


}
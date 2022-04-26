package com.demo2.view.ui.fragments.demo.stock

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.demo2.R
import com.demo2.utilities.UTILS
import com.demo2.view.service.MyViewModelFactory
import com.demo2.view.service.NetworkUtil
import com.demo2.view.ui.base.BaseFragment
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_network_new.*
import kotlinx.android.synthetic.main.fragment_stock_market.*
import kotlinx.android.synthetic.main.fragment_stock_market.cv_close
import kotlinx.android.synthetic.main.fragment_stock_market.cv_search
import kotlinx.android.synthetic.main.fragment_stock_market.et_search
import kotlinx.android.synthetic.main.fragment_stock_market.view.*


class StockMarketFragment : BaseFragment() {

    var rootView: View? = null
    private var stockMarketViewModel: StockMarketViewModel? = null
    private var stockMarketModel: StockMarketListModel? = null
    private var firstCall = true
    var total = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_stock_market, container, false)
            init()
        }
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        homeController.tv_title.text = getString(R.string.stock_markets_nav_child)
        homeController.tv_title.visibility = View.VISIBLE
        homeController.viewVisibleDrawerBottomBar(1)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addObserver()
        addOnClickListeners()
        if (!NetworkUtil.isInternetAvailable(activity!!)) {
            Toast.makeText(
                activity!!,
                getString(R.string.no_internet_connection),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            stockMarketViewModel!!.getStockMarketList(0, "")
        }
    }



    private fun setUpView() {
        rootView!!.tv_total_invested_amount.text =
            "${getString(R.string.currency_text)} ${ UTILS.parseDouble(stockMarketModel!!.payload!!.stock_investment_amount.toString())}"
        rootView!!.tv_total_profilt_amount.text = "${getString(R.string.currency_text)} ${
            UTILS.parseDouble(stockMarketModel!!.payload!!.stock_wallet_profit.toString()!!)}"

        total =
            stockMarketModel!!.payload!!.stock_wallet_profit!! + stockMarketModel!!.payload!!.stock_investment_amount!!

        rootView!!.tv_total_equity_amount.text = "${getString(R.string.currency_text)} ${UTILS.parseDouble(total.toString())}"

    }


    private fun addOnClickListeners() {
        rootView!!.scrollMain.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (v.getChildAt(v.childCount - 1) != null) {
                if ((scrollY >= (v.getChildAt(v.childCount - 1).measuredHeight - v.measuredHeight))
                    && scrollY > oldScrollY
                ) {
                    firstCall = false

                    if (!stockMarketModel!!.paginationEnded) {
                        stockMarketViewModel!!.getStockMarketList(
                            stockMarketModel!!.payload!!.stockCategories!!.size,
                            rootView!!.et_search.text.toString()
                        )
                    }
                }
            }
        })


        rootView!!.cv_search.setOnClickListener {

            if (!stockMarketModel?.payload?.stockCategories.isNullOrEmpty()) {

                if (et_search.text.trim().isNotEmpty()) {
                    cv_search.visibility = View.GONE
                    cv_close.visibility = View.VISIBLE
                    et_search.requestFocus()

                    showSoftKeyboard()
                    if (stockMarketModel?.payload?.stockCategories != null) {
                        // dataSize = myNetworkModel?.payload?.userDetail!!.size
                        stockMarketModel?.payload?.stockCategories!!.clear()
                    }
                    if (!NetworkUtil.isInternetAvailable(activity!!)) {
                        Toast.makeText(
                            activity!!,
                            getString(R.string.no_internet_connection),
                            Toast.LENGTH_SHORT
                        ).show()
                        true
                    }
                    firstCall = false
                    stockMarketViewModel!!.getStockMarketList(
                        0,

                        et_search.text.toString()
                    )
                    hideSoftKeyboard()
                } else {
                    Toast.makeText(
                        activity!!,
                        getString(R.string.validation_username),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }



        rootView!!.cv_close.setOnClickListener {

            cv_search.visibility = View.VISIBLE
            cv_close.visibility = View.GONE
            et_search.setText("")

            if (!NetworkUtil.isInternetAvailable(activity!!)) {
                Toast.makeText(
                    activity!!,
                    getString(R.string.no_internet_connection),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            } else {
                firstCall = false

                stockMarketModel?.payload?.stockCategories!!.clear()
                stockMarketViewModel!!.getStockMarketList(
                    0,

                    ""
                )
            }
            hideSoftKeyboard()
        }

    }

    private fun setUpViewPager() {
        view_pager_image.setAdapter(
            StockMarketAdapter(
                homeController,
                stockMarketModel!!.payload!!.sliders!!
            )
        )

        view_pager_indicator.attachToPager(view_pager_image)

        view_pager_image.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }
        })
    }


    private fun init() {
        stockMarketViewModel = ViewModelProviders.of(
            this@StockMarketFragment,
            MyViewModelFactory(StockMarketViewModel(activity!!))
        )[StockMarketViewModel::class.java]

    }


    private fun addObserver() {
        stockMarketViewModel?.responseError!!.observe(this@StockMarketFragment, Observer {
            homeController.errorBody(it)
            Log.e("qweqwe", "responseError observer")
        })

        stockMarketViewModel!!.isLoading!!.observe(this@StockMarketFragment, Observer {
            Log.e("qweqwe", "isLoading observer")
            it?.let {
                if (it) {
                    homeController.showProgressDialog()
                } else {
                    homeController.dismissProgressDialog()
                }
            }
        })

        stockMarketViewModel!!.stockMarketList!!.observe(
            this@StockMarketFragment,
            Observer {
                Log.e("qweqwe", "myNetworkModel observer")
                it?.let {

                    stockMarketModel = it
                    rootView!!.rv_stocks.adapter = StockMarketListAdapter(
                        activity!!,

                        it.payload!!.stockCategories!!,
                        this
                    )
                    if (firstCall) {
                        setUpViewPager()
                        setUpView()
                    }
                    rootView!!.cl_main.makeVisible()
                }
            })

    }

    override fun onListClick(position: Int, obj: Any?) {
        super.onListClick(position, obj)

        StockMarketDetailFragment().apply {
            this.stockGroupModel = obj as StockMarketListModel.Payload.StockCategory
            this.totalInvestedAmt = stockMarketModel!!.payload!!.stock_investment_amount!!
            this.totalProfit = stockMarketModel!!.payload!!.stock_wallet_profit!!
            this.backImage =
                (obj as StockMarketListModel.Payload.StockCategory).imageUrl?.toString().toString()

            this.totalEquityAmt=total
        }.let {
            homeController.loadFragment(
                it,
                it.javaClass.simpleName,
                this@StockMarketFragment.javaClass.simpleName
            )
        }
    }
}
package com.demo2.view.ui.fragments.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.demo2.R
import com.demo2.model.*
import com.demo2.utilities.Pref
import com.demo2.view.adapters.*
import com.demo2.view.service.MyViewModelFactory
import com.demo2.view.ui.base.BaseFragment
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_capital_withdrawal.view.*
import kotlinx.android.synthetic.main.fragment_total_report_view.view.*
import org.json.JSONObject
import java.lang.Exception

class NotificationListFragment : BaseFragment() {

    var rootView: View? = null
    var myMainViewModel: WalletMt4ViewModel? = null
    var historyMt4WalletTransferModel: HistoryWalletTransferModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_notification_list, container, false)
        }
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Pref.setLocale(activity!!, Pref.getLocalization(activity!!))

        homeController.tv_title.text = getString(R.string.notification)
        homeController.iv_navigation.visibility = View.GONE
        homeController.ll_bottombar.visibility = View.GONE
        homeController.iv_back.visibility = View.VISIBLE
        init()
        setup()
        addListeners()
    }

    private fun init() {
        myMainViewModel = ViewModelProviders.of(
            this@NotificationListFragment,
            MyViewModelFactory(WalletMt4ViewModel(activity!!))
        )[WalletMt4ViewModel::class.java]

    }

    private fun setup() {

               addObservers()
       // myMainViewModel!!.getMt4RequestHistory(0)


        //temp
        rootView!!.rv_result.adapter = NotificationListAdapter(
            activity!!,
          //  historyMt4WalletTransferModel!!.payload!!.history,
            this@NotificationListFragment
        )
    }

    private fun addListeners() {

        /*rootView!!.cv_capital_withdrawal_form.setOnClickListener{
            homeController.addLoadFragment(
                ProfitCapitalWalletCapitalFormStep1().apply {
                    this.historyMt4RequestDataModel = historyMt4RequestModel
                    this.onReqestCompleteFlowListenerStep1 = this@TotalReportViewFragment

                },
                "MT4WalletCapitalFormStep1",
                this.javaClass.simpleName
            )
        }*/

        /*rootView!!.scroll.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (v.getChildAt(v.childCount - 1) != null) {
                if ((scrollY >= (v.getChildAt(v.childCount - 1).measuredHeight - v.measuredHeight))
                    && scrollY > oldScrollY
                ) {
                    if (currentSelectedHistoryTab == typeMt4WalletTransferHistory) {

                        if (!historyMt4WalletTransferModel!!.paginationEnded) {
                            myMainViewModel?.getMt4WalletTransferHistory(
                                historyMt4WalletTransferModel!!.payload!!.history!!.size
                            )

                        }

                    }
                    if (currentSelectedHistoryTab == typeMt4RequestHistory) {
                        if (!historyMt4RequestModel!!.paginationEnded) {
                            myMainViewModel?.getMt4RequestHistory(historyMt4RequestModel!!.payload!!.history!!.size)

                        }

                    }

                    if (currentSelectedHistoryTab == typeMamDetachHistory) {
                        if (!historyMamDetachModel!!.paginationEnded) {
                            myMainViewModel?.getMt4MamDetachHistory(historyMamDetachModel!!.payload!!.history!!.size)

                        }
                    }

                }
            }
        })*/


    }

    private fun addObservers() {
        myMainViewModel!!.isLoading?.observe(this@NotificationListFragment, Observer {
            if (it) {
                homeController.showProgressDialog()
            } else {
                homeController.dismissProgressDialog()

            }
        })
        myMainViewModel!!.responseError?.observe(this@NotificationListFragment, Observer {
            // homeController.errorBody(it)

            try {
                val res = it.string()
                val jsonObject = JSONObject(res)
                if (jsonObject.optInt("code") == 305) {
                    Toast.makeText(
                        activity!!,
                        jsonObject.getString("message"),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    homeController.errorBodyFromJson(jsonObject)
                }
            } catch (e: Exception) {
                Toast.makeText(
                    activity!!,
                    getString(R.string.something_wrong_message),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

       myMainViewModel!!.historyMt4WalletTransferModel?.observe(
            this@NotificationListFragment,
            Observer {

                if (historyMt4WalletTransferModel == null) {
                    historyMt4WalletTransferModel = it

                    /**api called first time need to set new adapter*/
                    historyMt4WalletTransferModel!!.payload!!.history?.let {
                        rootView!!.rv_history.adapter = CommonWalletTransferHistoryAdapter(
                            activity!!,
                            historyMt4WalletTransferModel!!.payload!!.history,
                            this@NotificationListFragment
                        )
                    }
                } else {
                    historyMt4WalletTransferModel = it
                    historyMt4WalletTransferModel!!.payload!!.history?.let {

                        (rootView!!.rv_history.adapter as CommonWalletTransferHistoryAdapter?)?.let { adapter ->
                            /**already adapter set just need to notify*/
                            adapter.notifyItemRangeInserted(
                                adapter.lastPosition + 1,
                                it.size - (adapter.lastPosition + 1)
                            )
                        } ?: run {
                            /**need to set new adapter*/
                            rootView!!.rv_history.adapter = CommonWalletTransferHistoryAdapter(
                                activity!!,
                                historyMt4WalletTransferModel!!.payload!!.history,
                                this@NotificationListFragment
                            )
                        }

                    }
                }
            })


    }

    override fun onResume() {
        super.onResume()
    }

    override fun onListClickSimple(position: Int, string: String?) {
        super.onListClickSimple(position, string)

    }

    override fun onListClick(position: Int, obj: Any?) {
        super.onListClick(position, obj)
        /**this method will call from step4 of capital withdraw request flow*/



    }

}

package com.demo2.view.ui.fragments.demo.account

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.demo2.R
import com.demo2.utilities.Pref
import com.demo2.view.service.MyViewModelFactory
import com.demo2.view.service.NetworkUtil
import com.demo2.view.ui.base.BaseFragment
import kotlinx.android.synthetic.main.activity_home.tv_title
import kotlinx.android.synthetic.main.dialog_network_people_detail.*
import kotlinx.android.synthetic.main.dialog_network_people_detail.tv_no_data
import kotlinx.android.synthetic.main.fragment_network_new.*

import kotlinx.android.synthetic.main.fragment_network_tree.view.*
import java.io.IOException
import java.io.InputStream

class NetworkNewFragment : BaseFragment() {
    var rootView: View? = null
    private var myNetworkViewModel: MyNetworkViewModel? = null
    private var myNetworkModel: MyNetworkNewModel? = null
    private var rootNetworkUserId: String? = null
    private var dataSize = 0
    private var isExpanded = false
    private var myNetworkList: ArrayList<MyNetworkNewModel.Payload.UserDetail?>? = ArrayList()
    private var fromSearch = false
    private var actualHeight = 0
    private var found = false
    private var mainheight = 0.0
    private var height=0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_network_new, container, false)
            init()
        }
        return rootView
    }

    private fun init() {
        myNetworkViewModel = ViewModelProviders.of(
            this@NetworkNewFragment,
            MyViewModelFactory(MyNetworkViewModel(activity!!))
        )[MyNetworkViewModel::class.java]
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Pref.setLocale(activity!!, Pref.getLocalization(activity!!))

        homeController.tv_title.text = getString(R.string.title_network_tree)
        homeController.tv_title.visibility = View.VISIBLE
        if (activity!!.supportFragmentManager!!.findFragmentByTag("NetworkTreeFragment_D") != null) {
            homeController.viewVisibleDrawerBottomBar(1)
        } else {
            homeController.viewVisibleDrawerBottomBar(2)
        }
        addObservers()
        addOnClickListeners()
        callApi()
    }


    private fun addOnClickListeners() {
        cv_expand_view.setOnClickListener {

            myNetworkModel!!.payload!!.userDetail.let {
                if (isExpanded) {
                    rv_network.adapter = NetworkTreeNewAdapter(
                        activity!!,
                        it,
                        onListClickListner = this@NetworkNewFragment, fromchild = false,
                        expandAll = false,
                        fromSearch = false,
                                          gotHeighlight = false
                    )
                    isExpanded = false

                } else {
                    isExpanded = true
                    rv_network.adapter = NetworkTreeNewAdapter(
                        activity!!,
                        it,
                        onListClickListner = this@NetworkNewFragment, fromchild = false,
                        expandAll = true,
                        fromSearch = false,

                        gotHeighlight = false
                    )

                }
            }
        }


        cv_search.setOnClickListener {

            if (myNetworkModel?.payload?.userDetail != null) {

                dataSize = myNetworkModel?.payload?.userDetail!!.size
                if (et_search.text.trim().isNotEmpty()) {
                    cv_search.visibility = View.GONE
                    cv_close.visibility = View.VISIBLE
                    et_search.requestFocus()

                    showSoftKeyboard()
                    if (myNetworkModel?.payload?.userDetail != null) {
                        // dataSize = myNetworkModel?.payload?.userDetail!!.size
                        myNetworkModel!!.payload!!.userDetail!!.clear()
                    }
                    if (!NetworkUtil.isInternetAvailable(activity!!)) {
                        Toast.makeText(
                            activity!!,
                            getString(R.string.no_internet_connection),
                            Toast.LENGTH_SHORT
                        ).show()
                        true
                    }
                    found = false
                    fromSearch = true
                    myNetworkViewModel!!.myNetworkCallSearch(
                        rootNetworkUserId!!,
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
        et_search.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                cv_search.visibility = View.VISIBLE
                cv_close.visibility = View.GONE
                actualHeight=0
                found=false
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }


        })
        cv_close.setOnClickListener {
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
                fromSearch = false
                myNetworkModel!!.payload!!.userDetail!!.clear()
                myNetworkViewModel?.myNetworkCallSearch(rootNetworkUserId!!, 0, "")
            }
            hideSoftKeyboard()
        }


    }

    override fun onListClick(position: Int, obj: Any?) {
        super.onListClick(position, obj)
        var payload = obj as MyNetworkNewModel.Payload.UserDetail
        obj.let { showNetworkDetailDialog(payload) }
    }

    override fun onStart() {
        super.onStart()


    }


    private fun callApi() {
        rootNetworkUserId = Pref.getUserModel(activity!!)!!.payload!!.user!!.id
        if (!NetworkUtil.isInternetAvailable(activity!!)) {
            Toast.makeText(
                activity!!,
                getString(R.string.no_internet_connection),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            myNetworkViewModel!!.myNetworkCall(rootNetworkUserId!!, 0)
        }
    }

    fun loadJSONFromAsset(): String? {
        var json: String? = null
        json = try {
            val `is`: InputStream = activity!!.assets.open("myaccount.json")
            val size: Int = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            String(buffer, Charsets.UTF_8)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return json
    }

    private fun addObservers() {
        myNetworkViewModel?.responseError!!.observe(this, Observer {
            homeController.errorBody(it)
            Log.e("qweqwe", "responseError observer")
        })

        myNetworkViewModel!!.isLoading!!.observe(this@NetworkNewFragment, Observer {
            Log.e("qweqwe", "isLoading observer")
            it?.let {
                if (it) {
                    homeController.showProgressDialog()
                } else {
                    homeController.dismissProgressDialog()
                }
            }
        })

        myNetworkViewModel!!.myNetworkModel!!.observe(this@NetworkNewFragment, Observer {
            Log.e("qweqwe", "myNetworkModel observer")
            it?.let {
                myNetworkModel = it
                ll_main.makeVisible()
                rv_rank.adapter = RankListAdapter(context!!, it.payload!!.rank_detail!!)
                mainheight = rv_rank.height.toDouble() + 40 + 32 + 10 + 16

                var adapter = NetworkTreeNewAdapter(
                    activity!!,
                    it.payload!!.userDetail!!,
                    onListClickListner = this@NetworkNewFragment,
                    fromchild = false,
                    expandAll = false,
                    fromSearch = fromSearch,
                    gotHeighlight = false
                )

                rv_network.adapter = adapter
                // comment this one
                if (fromSearch) {
                    Handler().postDelayed(Runnable {
                        Log.v("=====height", "-" + actualHeight)

                        var total = actualHeight * height + mainheight.toInt()
                        Log.v("===========SIZE", "-" + total)
                        ns_scroll.smoothScrollBy(0, total)
                        myNetworkViewModel!!.isLoading!!.value = false

                    }, 650)

                    // to this one
                } else {
                    myNetworkViewModel!!.isLoading!!.value = false

                    found = false
                    actualHeight = 0
                }
                if (it.payload!!.userDetail!!.size > 0) {
                    tv_no_data.makeGone()
                } else {
                    tv_no_data.makeVisible()

                }
            }
        })
    }

    override fun onListClickSimple(position: Int, string: String?) {
        super.onListClickSimple(position, string)
        height=string!!.toInt()
    }

    override fun onListShow(position: Int, obj: Any?) {
        super.onListShow(position, obj)
        if (!(obj as Boolean) && !found && fromSearch) {
            actualHeight++
            Log.v("===Actual", "-" + actualHeight)

        } else {
            found = true
        }
    }

    private fun showNetworkDetailDialog(obj: MyNetworkNewModel.Payload.UserDetail) {
        var monthlyGroupSalesModel: MonthlyGroupSalesModel? = null
        var dialog = Dialog(activity!!, R.style.DialogSlideAnim)
        dialog.setContentView(R.layout.dialog_network_people_detail)
        dialog.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setGravity(Gravity.CENTER)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        /*  dialog.scroll.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
              if (v.getChildAt(v.childCount - 1) != null) {
                  if ((scrollY >= (v.getChildAt(v.childCount - 1).measuredHeight - v.measuredHeight))
                      && scrollY > oldScrollY
                  ) {

                      if (!monthlyGroupSalesModel!!.paginationEnded) {
                          myNetworkViewModel!!.getMonthlyGroupSalesCall(
                              obj.id!!,
                              monthlyGroupSalesModel!!.payload!!.groupSales!!.size
                          )

                      }

                  }
              }

          })
    */

        dialog.iv_close.setOnClickListener { dialog.dismiss() }

        myNetworkViewModel!!.getMonthlyGroupSalesCall(obj.id!!, 0)
        Log.e("qweqwe", "adding observer")

        if (!myNetworkViewModel!!.monthlyGroupSalesModel!!.hasObservers()) {
            myNetworkViewModel!!.monthlyGroupSalesModel!!.observe(
                this@NetworkNewFragment,
                Observer {
                    if (it.payload!!.groupSales!!.isEmpty()) {
                        dialog.tv_no_data.visibility = View.VISIBLE
                        dialog!!.rv_monthly_group_list.visibility = View.GONE
                    } else {
                        dialog.tv_no_data.visibility = View.GONE

                        dialog!!.rv_monthly_group_list.visibility = View.VISIBLE
                        monthlyGroupSalesModel = it
                        dialog!!.rv_monthly_group_list.adapter = MonthlyGroupSalesAdapter(
                            activity!!,
                            monthlyGroupSalesModel!!.payload!!.groupSales as List<MonthlyGroupSalesModel.Payload.GroupSale>
                        )

                    }
                    dialog.show()

/*
                    if (monthlyGroupSalesModel == null) {
                        */
                    /**api called for first time*//*

                        monthlyGroupSalesModel = it
                        it?.let {
                            Log.e("qweqwe", "monthly model null")
                            dialog.tv_no_data.visibility =
                                if (it.payload!!.groupSales!!.isNotEmpty()) View.GONE else View.VISIBLE
                            dialog!!.rv_monthly_group_list.adapter = MonthlyGroupSalesAdapter(
                                activity!!,
                                it.payload!!.groupSales as List<MonthlyGroupSalesModel.Payload.GroupSale>
                            )
                        }
                    } else {
                        monthlyGroupSalesModel = it
                        (dialog.rv_monthly_group_list.adapter as MonthlyGroupSalesAdapter)?.let { adapter ->
                            adapter.notifyItemRangeInserted(
                                adapter.lastPosition + 1,
                                it.payload!!.groupSales!!.size - (adapter.lastPosition + 1)
                            )
                        }
                    }
*/
                })
        }
    }

}
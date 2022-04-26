package com.demo2.view.ui.fragments.demo.tabs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricConstants
import androidx.biometric.BiometricConstants.ERROR_LOCKOUT
import androidx.biometric.BiometricConstants.ERROR_LOCKOUT_PERMANENT
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.gson.Gson
import com.special.ResideMenu.ResideMenu
import com.demo2.R
import com.demo2.utilities.*
import com.demo2.view.adapters.*
import com.demo2.view.interfaces.BiometricCallback
import com.demo2.view.service.MyViewModelFactory
import com.demo2.view.ui.base.BaseFragment
import com.demo2.view.ui.fragments.common.PackagesFragment
import com.demo2.view.ui.fragments.common.SettingsFragment
import com.demo2.view.ui.fragments.demo.wallets.FundWallets
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.dialog_announcement_layout.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.view_enter_otp.view.*
import java.util.*
import kotlin.collections.ArrayList

/**know before start
 * "kyc_status" - (Display setting 0 = Not Requested, 1 Pending request ,2 Approved Request & 3 Rejected Request)
"is_new_user" - (Display Setting If 1 then show all wallet and 0 only points wallet condition base)
 * */

class HomeFragment : BaseFragment() {


    val TAG = this.javaClass.simpleName
    var rootView: View? = null
    var myMainViewModel: TradeDashboardViewModel? = null
    var tradeDashboardModel: TradeDashboardModel? = null
    var uuid: String? = null
    var dialogEnableOptions: AlertDialog? = null
    var dialogInvestmentOptions: AlertDialog? = null
    var dialogEnterOtpOptions: AlertDialog? = null
    var dialogAddFundOptions: AlertDialog? = null
    var checkFingerPrintConditions: Boolean = true
    var biometricCancelled: Boolean = false
    var currentKycAlertDialog: android.app.AlertDialog? = null

    /**put fragments list in below array after which we don't need to update homefragment*/
    var destroyedFragments = arrayListOf(
        SettingsFragment().javaClass.simpleName,
        PackagesFragment().javaClass.simpleName
    )

    /*  private var yAxisArray =
          doubleArrayOf(
              0.0,
              2500.0,
              1850.0,
              1500.0,
              450.0,
              1000.0,
              1600.0,
              2450.0,
              300.0,
              450.0,
              460.0,
              470.0,
              480.0
          )
  */
    /* private var xAxisArray =
         arrayOf(
             "1 FEB",
             "3 FEB",
             "7 FEB",
             "9 FEB",
             "14 FEB",
             "17 FEB",
             "17 FEB",
             "20 FEB",
             "23 FEB",
             "26 FEB",
             "28 FEB",
             "29 FEB",
             "30 FEB",
             "31 FEB"
         )
 */


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        Pref.setLocale(activity!!, Pref.getLocalization(activity!!))
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_home, container, false)
            Log.e(TAG, "onCreateView - $myMainViewModel")
            if (homeController.oldDashboardModel == null) {
                /**home fragment loading firstTime*/
                checkFingerPrintConditions = true
            } else {
                updateUiWithOldData()
                checkFingerPrintConditions = false
            }
        } else {
            checkFingerPrintConditions = false
        }
        return rootView
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.e(TAG, "onActivityCreated")
        homeController.unSelectBottomBar()
        homeController.visibleBottomBar(0)
        homeController.resideMenu!!.enableDirection(ResideMenu.DIRECTION_LEFT)
        homeController.rl_message.visibility = View.VISIBLE
        homeController.tv_title.text = getString(R.string.dashboard_nav)
        homeController.iv_navigation.visibility = View.VISIBLE
        homeController.iv_back.visibility = View.GONE

        homeController.viewVisibleDrawerBottomBar(0)

        //  homeController.resideMenu!!.addIgnoredView(rootView!!.horizontal_scroll)
        homeController.resideMenu!!.addIgnoredView(rootView!!.horizontal_scroll_barchart)
        homeController.resideMenu!!.addIgnoredView(rootView!!.rv_news_updates)
        homeController.iv_message.tag = getString(R.string.otmtrade_key)
        homeController.iv_message.setColorFilter(
            ContextCompat.getColor(
                activity!!,
                R.color.white
            )
        )

        homeController.tv_home.setTextColor(
            ContextCompat.getColor(
                activity!!,
                R.color.dashboard_selected
            )
        )
        /**no need to update screen for some last destroyed fragments*/
        if (!destroyedFragments.contains(homeController.lastDestroyedFragment)) {
            init()
            setup()
            onClickListener()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        homeController.resideMenu!!.setSwipeDirectionDisable(ResideMenu.DIRECTION_LEFT)

        homeController.rl_message.visibility = View.GONE

    }

    override fun onResume() {
        super.onResume()
        homeController.llBottomNew.makeVisible()
        if (biometricCancelled) { //finger print popup for new changes on demo2
            showBiometricPrompt()
        }
    }

    private fun showBiometricPrompt() {
        biometricCancelled = false
        if (!userModel.payload!!.user!!.fingerPrintSetInThisDevice) return
        if (!BiometricUtils.checkBiometricPossible(activity!!)) return

        showBiometric(
            description = getString(R.string.biometric_msg_on_home),
            biometricCallback = object : BiometricCallback {
                override fun onSuccess(result: BiometricPrompt.AuthenticationResult) {
                }

                override fun onFail() {
                    Log.e("zxczxc", "onFail()")
                }

                override fun onError(errorCode: Int, errString: CharSequence) {
                    if (errorCode == BiometricConstants.ERROR_CANCELED) {
                        biometricCancelled = true

                    }

                    if (errorCode == BiometricConstants.ERROR_USER_CANCELED) {
//                        biometricCancelled = true
                        Handler().postDelayed({
                            showBiometric(
                                description = getString(R.string.biometric_msg_on_home),
                                biometricCallback = this
                            )
                        }, 100)
                    }

                    if (errorCode == ERROR_LOCKOUT_PERMANENT || errorCode == ERROR_LOCKOUT) {
                        /* Toast.makeText(
                             activity!!,
                             getString(R.string.too_many_attempts),
                             Toast.LENGTH_SHORT
                         ).show()*/


                        val builder =
                            android.app.AlertDialog.Builder(activity!!, R.style.MyDialogTheme)
                        builder.setMessage(getString(R.string.too_many_attempts))
                        builder.setCancelable(false)
                        builder.setPositiveButton(getString(R.string.ok),
                            DialogInterface.OnClickListener { dialog, which ->
                                activity!!.finishAffinity()

                            })
                        builder.create().apply {

                            this.setOnShowListener {
                                this.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                                    .setTextColor(ContextCompat.getColor(activity!!, R.color.black))

                            }
                        }.show()
                    }

                    if (errorCode == BiometricConstants.ERROR_TIMEOUT) {

                        val builder =
                            android.app.AlertDialog.Builder(activity!!, R.style.MyDialogTheme)
                        builder.setMessage(getString(R.string.timeout_biometric))
                        builder.setCancelable(false)
                        builder.setPositiveButton(getString(R.string.ok),
                            DialogInterface.OnClickListener { dialog, which ->
                                activity!!.finishAffinity()

                            })
                        builder.create().apply {

                            this.setOnShowListener {
                                this.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                                    .setTextColor(ContextCompat.getColor(activity!!, R.color.black))

                            }
                        }.show()
                    }

                    Log.e("zxczxc", "onError($errorCode - $errString)")
                }
            })
    }

    private fun showInvestmentDialog() {
        dialogInvestmentOptions = AlertDialog.Builder(activity!!, R.style.MyDialogTheme).apply {
            this.setTitle(getString(R.string.investment_dialog_title))
            this.setMessage(getString(R.string.investment_dialog_msg))

            this.setPositiveButton(getString(R.string.ok)) { dialog, which ->
                /**on OK*/
                homeController.loadFragment(
                    StockWalletInvestmentListFragment().apply {
                        this.strExpired = "1"
                    },
                    "StockWalletInvestmentList_D",
                    activity!!.supportFragmentManager.findFragmentById(R.id.fragment_container)!!.javaClass.simpleName
                )
            }
            this.setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                /**on cancel*/

            }
        }.create()
        dialogInvestmentOptions!!.setOnShowListener {
            dialogInvestmentOptions!!.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(activity!!, R.color.black))
            dialogInvestmentOptions!!.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(activity!!, R.color.black))
        }
        dialogInvestmentOptions!!.show()
    }

    private fun showFingerPrintDialog() {
        /**only need to continue if following conditions true*/
        if (!BiometricUtils.checkBiometricPossible(activity!!)) return

        if (userModel.payload!!.user!!.askedFingerPrint) {
            return
        }
        uuid = UUID.randomUUID().toString()

        dialogEnableOptions = AlertDialog.Builder(activity!!, R.style.MyDialogTheme).apply {
            this.setTitle(getString(R.string.fingerprint_title))
            this.setMessage(getString(R.string.fingerprint_description))

            this.setPositiveButton(getString(R.string.enable_button)) { dialog, which ->
                /**on enable*/
                if (userModel.payload!!.user!!.alreadyFingerPrintAdded == "1") {
                    myMainViewModel!!.sendOTPFingerprint()
                } else {
                    showBiometric(biometricCallback = object : BiometricCallback {
                        override fun onSuccess(result: BiometricPrompt.AuthenticationResult) {
                            myMainViewModel!!.enableFingerPrint(uuid!!)

                        }

                        override fun onFail() {}
                        override fun onError(errorCode: Int, errString: CharSequence) {
                            if (errorCode == 7) {
                                Toast.makeText(
                                    activity!!,
                                    getString(R.string.too_many_attempts),
                                    Toast.LENGTH_SHORT
                                ).show()

                            }
                        }
                    })

                }
            }
            this.setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                /**on cancel*/

            }
        }.create()
        dialogEnableOptions!!.setOnShowListener {
            dialogEnableOptions!!.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(activity!!, R.color.black))
            dialogEnableOptions!!.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(activity!!, R.color.black))
        }
        dialogEnableOptions!!.show()


        /**updating asked as it should ask only first time*/
        //temporary commented
        userModel.apply {
            this.payload!!.user!!.askedFingerPrint = true
        }

        Pref.setValue(activity!!, Constants.prefUserData, Gson().toJson(userModel))
    }

    private fun showOTPDialog() {
        var inflater =
            activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var v = inflater.inflate(R.layout.view_enter_otp, null)

        var alertDialog = AlertDialog.Builder(activity!!, R.style.MyDialogTheme).apply {
            this.setTitle(getString(R.string.confitm_otp))
            this.setMessage(getString(R.string.confitm_otp_description))
            this.setCancelable(false)
            this.setView(v)
            v.tv_resend.setOnClickListener {
                /**on resend*/
                myMainViewModel!!.reSendOTPFingerprint()
            }
            v.tv_submit.setOnClickListener {
                /**on submit*/
                if (v.edt_otp.text.toString().trim().isEmpty()) {
                    Toast.makeText(
                        activity!!,
                        getString(R.string.validation_provide_otp),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    showBiometric(biometricCallback = object : BiometricCallback {
                        override fun onSuccess(result: BiometricPrompt.AuthenticationResult) {
                            Log.e("zxczxc", "home onSuccess")
                            myMainViewModel!!.enableFingerPrintWithOTP(
                                uuid = uuid!!,
                                otp = v.edt_otp.text.toString().trim()
                            )

                        }

                        override fun onFail() {}
                        override fun onError(errorCode: Int, errString: CharSequence) {
                            if (errorCode == 7) {
                                Toast.makeText(
                                    activity!!,
                                    getString(R.string.too_many_attempts),
                                    Toast.LENGTH_SHORT
                                ).show()

                            }
                        }
                    })


                }

            }
            v.tv_cancel.setOnClickListener {
                /**on cancel*/
                dialogEnterOtpOptions!!.dismiss()
            }
            v.requestFocus()
        }
        showSoftKeyboard()
        dialogEnterOtpOptions = alertDialog.create()
        dialogEnterOtpOptions!!.show()
    }

    private fun init() {
        if (myMainViewModel == null) {
            myMainViewModel =
                ViewModelProviders.of(
                    this@HomeFragment,
                    MyViewModelFactory(
                        TradeDashboardViewModel(
                            activity!!
                        )
                    )
                )[TradeDashboardViewModel::class.java]
            addObservers()
        }
    }

    private fun setup() {
        rootView!!.chart.setNoDataText("")
        rootView!!.tv_welcome.text =
            "${getString(R.string.welcome)}, ${userModel.payload!!.user!!.name}"


        userModel.payload?.user?.balance?.let {
            if (it.isNotEmpty()) {
                // rootView!!.tv_balance_amount.text = "$${parseDouble(it)}"
            }
        }
        rootView!!.rv_news_updates.layoutManager =
            LinearLayoutManager(activity!!, LinearLayoutManager.VERTICAL, false)
        rootView!!.rv_ranking_to_go_criteria.layoutManager =
            GridLayoutManager(activity!!, 2)
        rootView!!.rv_last_month_plan.layoutManager =
            GridLayoutManager(activity!!, 2)
        myMainViewModel?.tradeDashboardCall()

    }

    private fun onClickListener() {

    }

    private fun initLineChartDownFill() {
        Log.d(TAG, "initLineChartDownFill")
        tradeDashboardModel!!.payload!!.earning?.let {
            if (it.isNotEmpty()) {
                if (it[0].date == "0" && it[0].value == "0") {

                } else {
//                    tradeDashboardModel!!.payload!!.earning!!.add(
//                        0,
//                        TradeDashboardModel.EarningChart().apply {
//                            this.date = "0"
//                            this.value = "0"
//                        })
                }
            }
        }
        //    rootView!!.chart.minimumWidth = (tradeDashboardModel!!.payload!!.earning!!.size*30)
//            UTILS.dpToPixel(
//                activity!!,
//                100 * tradeDashboardModel!!.payload!!.earning!!.size
//            )
        rootView!!.chart.setDrawGridBackground(false)
        rootView!!.chart.description.isEnabled = false
        rootView!!.chart.setScaleEnabled(false)
        rootView!!.chart.setPinchZoom(false)
        rootView!!.chart.axisRight.isEnabled = false
        rootView!!.chart.xAxis.setDrawGridLines(false)
        rootView!!.chart.axisLeft.setDrawGridLines(false)
        rootView!!.chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        rootView!!.chart.xAxis.isGranularityEnabled = true
        rootView!!.chart.xAxis.granularity = 1F
        rootView!!.chart.extraRightOffset = 30f
        rootView!!.chart.extraTopOffset = 30f

        rootView!!.chart.xAxis.labelCount =
            tradeDashboardModel!!.payload!!.groupSalesChart!!.size
        rootView!!.chart.xAxis.textColor =
            ContextCompat.getColor(activity!!, R.color.gray)
        rootView!!.chart.axisLeft.textColor =
            ContextCompat.getColor(activity!!, R.color.gray)
        val formatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                if (value < 0 || value >= tradeDashboardModel!!.payload!!.earning!!.size) {
                    return ""
                }
                return UTILS.convertDate(
                    "yyyy-MM-dd",
                    "yyyy-MM-dd",
                    tradeDashboardModel!!.payload!!.earning!![value.toInt()].date.toString()
                )
            }
        }
        rootView!!.chart.xAxis.valueFormatter = formatter
        rootView!!.chart.axisLeft.axisMinimum = 0f

        //   rootView!!.chart.xAxis.mAxisMaximum=60f
        rootView!!.chart.xAxis.axisMinimum = 0f
        rootView!!.chart.xAxis.mAxisMaximum =
            (tradeDashboardModel!!.payload!!.groupSalesChart!!.size).toFloat()
        // rootView!!.chart.setVisibleXRangeMaximum(5f)

        if (tradeDashboardModel!!.payload!!.earning?.size!! > 0)
            rootView!!.chart.axisLeft.axisMaximum =
                tradeDashboardModel!!.payload!!.earning?.maxBy { it.value!!.toFloat() }!!.value!!.toFloat()


        //     rootView!!.chart.setOnChartValueSelectedListener(this);

        lineChartDownFillWithData()
    }


    private fun lineChartDownFillWithData() {

        val entryArrayList = ArrayList<Entry>()
        for (i in tradeDashboardModel!!.payload!!.earning!!.indices) {
            entryArrayList.add(
                Entry(
                    i.toFloat(),
                    UTILS.parseDouble(tradeDashboardModel!!.payload!!.earning!![i].value!!)
                        .toFloat()
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

        lineDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        //lineDataSet.cubicIntensity = 0.20f
        lineDataSet.color = ContextCompat.getColor(activity!!, R.color.blue_ribbon)
        lineDataSet.setCircleColor(ContextCompat.getColor(activity!!, R.color.blue_ribbon))
        lineDataSet.lineWidth = 1f
        lineDataSet.circleRadius = 2f
        lineDataSet.setDrawCircleHole(false)
        lineDataSet.circleHoleColor = Color.parseColor("#131230")
        lineDataSet.formLineWidth = 1f
        lineDataSet.formSize = 0f
        lineDataSet.fillAlpha = 1
        val drawable = ContextCompat.getDrawable(context!!, R.drawable.graph_gradient)
        lineDataSet.fillDrawable = drawable
        lineDataSet.setDrawFilled(true)
        lineDataSet.setDrawValues(true)
        lineDataSet.setValueFormatter(YValyeFormatter())
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
        lineData.setValueTextSize(11f)

        lineData.setValueTextColor(Color.WHITE)
        rootView!!.chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        rootView!!.chart.axisRight.isEnabled = false

        rootView!!.chart.data = lineData
        rootView!!.chart.invalidate()


    }

    private fun oldInitLineChartDownFill() {

        Log.d(TAG, "initLineChartDownFill")
        homeController.oldDashboardModel!!.payload!!.earning?.let {
            if (it.isNotEmpty()) {
                if (it[0].date == "0" && it[0].value == "0") {

                } else {
                    homeController.oldDashboardModel!!.payload!!.earning!!.add(
                        0,
                        TradeDashboardModel.EarningChart().apply {
                            this.date = "0"
                            this.value = "0"
                        })
                }
            }
        }
        rootView!!.chart.minimumWidth =
            UTILS.dpToPixel(
                activity!!,
                80 * homeController.oldDashboardModel!!.payload!!.earning!!.size
            )
        rootView!!.chart.setDrawGridBackground(false)
        rootView!!.chart.description.isEnabled = false
        rootView!!.chart.setScaleEnabled(false)
        rootView!!.chart.setPinchZoom(false)
        rootView!!.chart.axisRight.isEnabled = false
        rootView!!.chart.xAxis.setDrawGridLines(false)
        rootView!!.chart.axisLeft.setDrawGridLines(false)
        rootView!!.chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        rootView!!.chart.xAxis.isGranularityEnabled = true
        rootView!!.chart.xAxis.granularity = 1F
        rootView!!.chart.extraRightOffset = 30f
        rootView!!.chart.extraTopOffset = 30f

        rootView!!.chart.xAxis.labelCount =
            homeController.oldDashboardModel!!.payload!!.groupSalesChart!!.size
        rootView!!.chart.xAxis.textColor =
            ContextCompat.getColor(activity!!, R.color.gray)
        rootView!!.chart.axisLeft.textColor =
            ContextCompat.getColor(activity!!, R.color.gray)
        val formatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                if (value < 0 || value >= homeController.oldDashboardModel!!.payload!!.earning!!.size) {
                    return ""
                }
                return UTILS.convertDate(
                    "yyyy-MM-dd",
                    "dd MMM",
                    homeController.oldDashboardModel!!.payload!!.earning!![value.toInt()].date.toString()
                )
            }
        }

        rootView!!.chart.xAxis.valueFormatter = formatter
        rootView!!.chart.xAxis.axisMinimum = 0f
//        rootView!!.chart.xAxis.mAxisMaximum = (tradeDashboardModel!!.payload!!.groupSalesChart!!.size).toFloat() //
        rootView!!.chart.xAxis.mAxisMaximum =
            (homeController.oldDashboardModel!!.payload!!.groupSalesChart!!.size).toFloat()

        //  rootView!!.chart.axisLeft.axisMinimum = 0f

        if (homeController.oldDashboardModel!!.payload!!.earning?.size!! > 0) rootView!!.chart.axisLeft.axisMaximum =
            homeController.oldDashboardModel!!.payload!!.earning?.maxBy { it.value!!.toFloat() }!!.value!!.toFloat()

        oldLineChartDownFillWithData()


    }


    private fun oldLineChartDownFillWithData() {


        val entryArrayList = ArrayList<Entry>()
        for (i in homeController.oldDashboardModel!!.payload!!.earning!!.indices) {
            entryArrayList.add(
                Entry(
                    i.toFloat(),
                    homeController.oldDashboardModel!!.payload!!.earning!![i].value!!.toFloat()
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
        lineDataSet.setDrawValues(true)
        lineDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        //lineDataSet.cubicIntensity = 0.20f
        lineDataSet.color = ContextCompat.getColor(activity!!, R.color.blue_ribbon)
        lineDataSet.setCircleColor(ContextCompat.getColor(activity!!, R.color.blue_ribbon))
        lineDataSet.lineWidth = 1f
        lineDataSet.circleRadius = 1f
        lineDataSet.setDrawCircleHole(true)
        lineDataSet.circleHoleColor = Color.parseColor("#131230")
        lineDataSet.formLineWidth = 1f
        lineDataSet.formSize = 0f
        lineDataSet.fillAlpha = 1
        val drawable = ContextCompat.getDrawable(context!!, R.drawable.graph_gradient)
        lineDataSet.fillDrawable = drawable
        lineDataSet.setDrawFilled(true)
        lineDataSet.setValueFormatter(YValyeFormatter())

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
                if (value < 0 || value >= homeController.oldDashboardModel!!.payload!!.groupSalesChart!!.size) {
                    return ""
                }

                return parseDouble(
                    homeController.oldDashboardModel!!.payload!!.groupSalesChart!![value.toInt()].value.toString()
                )
            }
        }
        lineDataSet.valueFormatter = formatter

*/
        iLineDataSetArrayList.add(lineDataSet)

        //LineData is the data accord
        val lineData = LineData(iLineDataSetArrayList)
        lineData.setValueTextSize(10f)

        lineData.setValueTextColor(Color.WHITE)
        rootView!!.chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        rootView!!.chart.axisRight.isEnabled = false

        rootView!!.chart.data = lineData

        rootView!!.chart.invalidate()

    }


    //Bar graph for sales
    /* private fun initBarChartDownFill() {
         Log.d(TAG, "initBarChartDownFill")
         tradeDashboardModel!!.payload!!.groupSalesChart?.let {
             if (it.isNotEmpty()) {
                 if (it[0].date == "0" && it[0].value == "0") {

                 } else {
                     tradeDashboardModel!!.payload!!.groupSalesChart!!.add(
                         0,
                         TradeDashboardModel.GroupSalesChart().apply {
                             this.date = "0"
                             this.value = "0"
                         })
                 }
             }
         }
         rootView!!.chartBar.minimumWidth =
             UTILS.dpToPixel(
                 activity!!,
                 30 * tradeDashboardModel!!.payload!!.groupSalesChart!!.size
             )
         //rootView!!.chartBar.setPadding(20,0,20,0)
         rootView!!.chartBar.setDrawGridBackground(false)
         rootView!!.chartBar.description.isEnabled = false
         rootView!!.chartBar.setScaleEnabled(false)
         rootView!!.chartBar.setPinchZoom(false)
         rootView!!.chartBar.axisRight.isEnabled = false
         rootView!!.chartBar.xAxis.setDrawGridLines(false)
         rootView!!.chartBar.axisLeft.setDrawGridLines(false)
         rootView!!.chartBar.xAxis.position = XAxis.XAxisPosition.BOTTOM
         rootView!!.chartBar.xAxis.isGranularityEnabled = true
         rootView!!.chartBar.xAxis.granularity = 1F
         rootView!!.chartBar.xAxis.labelRotationAngle = -42f
         rootView!!.chartBar.xAxis.spaceMin = -0.9f
         //rootView!!.chartBar.extraLeftOffset = 30f
         //rootView!!.chartBar.extraRightOffset = 30f
         //rootView!!.chartBar.extraTopOffset = 30f

         rootView!!.chartBar.xAxis.labelCount =
             tradeDashboardModel!!.payload!!.groupSalesChart!!.size
         rootView!!.chartBar.xAxis.textColor =
             ContextCompat.getColor(context!!, R.color.gray)
         rootView!!.chartBar.axisLeft.textColor =
             ContextCompat.getColor(context!!, R.color.gray)
         val formatter = object : ValueFormatter() {
             override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                 if (value < 0 || value >= tradeDashboardModel!!.payload!!.groupSalesChart!!.size) {
                     return ""
                 }
                 return UTILS.convertDate(
                     "yyyy-MM-dd",
                     "dd-MM", //"dd-MM"
                     tradeDashboardModel!!.payload!!.groupSalesChart!![value.toInt()].date.toString()
                 )
             }
         }
         rootView!!.chartBar.xAxis.valueFormatter = formatter
         *//*val mv = XYMarkerView(context!!, formatter)
        mv.chartView = chart // For bounds control
        rootView!!.chartBar.marker = mv*//*

        rootView!!.chartBar.axisLeft.axisMinimum = 0f
        rootView!!.chartBar.axisLeft.axisMaximum =
            tradeDashboardModel!!.payload!!.groupSalesChart?.maxBy { it.value!!.toFloat() }!!.value!!.toInt() + 800.toFloat()

        // add a nice and smooth animation
        rootView!!.chartBar.animateY(1500)

        rootView!!.chartBar.legend.isEnabled = false

        barChartDownFillWithData()
    }*/

    /*private fun barChartDownFillWithData() {
        val entryArrayList = ArrayList<BarEntry>()
        for (i in tradeDashboardModel!!.payload!!.groupSalesChart!!.indices) {
            entryArrayList.add(
                BarEntry(
                    i.toFloat(),
                    UTILS.parseDouble(tradeDashboardModel!!.payload!!.groupSalesChart!![i].value!!)
                        .toFloat()
                )
            )


        }
        var set1: BarDataSet? = null

        if (rootView!!.chartBar.data != null &&
            rootView!!.chartBar.data.dataSetCount > 0
        ) {
            set1 = rootView!!.chartBar.data.getDataSetByIndex(0) as BarDataSet
            set1.values = entryArrayList
            rootView!!.chartBar.data.notifyDataChanged()
            rootView!!.chartBar.notifyDataSetChanged()
        } else {
            set1 = BarDataSet(entryArrayList, "Data Set")
            set1.setColors(
                ContextCompat.getColor(
                    context!!,
                    R.color.light_blue
                )
            )  //ColorTemplate.VORDIPLOM_COLORS
            set1.setDrawValues(true)
            val dataSets = java.util.ArrayList<IBarDataSet>()
            dataSets.add(set1)
            val data = BarData(dataSets)
            data.setValueFormatter(YValyeFormatter())

            data.setValueTextSize(10f)
            data.setDrawValues(true)
            data.setValueTextColor(Color.WHITE)
            //data.barWidth=0.5f
            //data.getGroupWidth(1f,2f)
            rootView!!.chartBar.data = data
            rootView!!.chartBar.setDrawValueAboveBar(true)
            rootView!!.chartBar.setFitBars(true)

        }
        rootView!!.chartBar.data = BarData(set1).apply {
            barWidth = 0.5f
        }
        rootView!!.chartBar.invalidate()


    }*/


    private fun initSalesLineChartDownFill() {
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

        rootView!!.lineChart.setDrawGridBackground(false)
        rootView!!.lineChart.description.isEnabled = false
        rootView!!.lineChart.setScaleEnabled(false)
        rootView!!.lineChart.setPinchZoom(false)
        rootView!!.lineChart.axisRight.isEnabled = false
        rootView!!.lineChart.xAxis.setDrawGridLines(false)
        rootView!!.lineChart.axisLeft.setDrawGridLines(false)
        rootView!!.lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        rootView!!.lineChart.xAxis.labelRotationAngle = -45f
        rootView!!.lineChart.xAxis.isGranularityEnabled = true
        rootView!!.lineChart.xAxis.granularity = 1F
        // rootView!!.lineChart.extraRightOffset = 30f
        rootView!!.lineChart.extraTopOffset = 30f

        rootView!!.lineChart.xAxis.labelCount =
            tradeDashboardModel!!.payload!!.groupSalesChart!!.size
        rootView!!.lineChart.xAxis.textColor =
            ContextCompat.getColor(activity!!, R.color.white)
        rootView!!.lineChart.axisLeft.textColor =
            ContextCompat.getColor(activity!!, R.color.white)
        val formatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                if (value < 0 || value >= tradeDashboardModel!!.payload!!.groupSalesChart!!.size) {
                    return ""
                }
                return UTILS.convertDate(
                    "yyyy-MM-dd HH:mm:ss",
                    "yyyy-MM-dd",
                    tradeDashboardModel!!.payload!!.groupSalesChart?.get(value.toInt())!!.date.toString()
                )
            }
        }
        rootView!!.lineChart.xAxis.valueFormatter = formatter
        //  rootView!!.lineChart.axisLeft.axisMinimum = 0f
        //   rootView!!.lineChart.xAxis.mAxisMaximum=60f
        //  rootView!!.lineChart.xAxis.axisMinimum = 0f
        rootView!!.lineChart.xAxis.mAxisMaximum =
            (tradeDashboardModel!!.payload!!.groupSalesChart!!.size).toFloat()
        // rootView!!.chart.setVisibleXRangeMaximum(5f)

        if (tradeDashboardModel!!.payload!!.groupSalesChart!!.size!! > 0)
            rootView!!.lineChart.axisLeft.axisMaximum =
                tradeDashboardModel!!.payload!!.groupSalesChart!!.maxBy { it!!.value!!.toFloat() }!!.value!!.toFloat()



        lineChartSalesDownFillWithData()
    }


    private fun lineChartSalesDownFillWithData() {

        val entryArrayList = ArrayList<Entry>()
        for (i in tradeDashboardModel!!.payload!!.groupSalesChart!!.indices) {
            entryArrayList.add(
                Entry(
                    i.toFloat(),
                    tradeDashboardModel!!.payload!!.groupSalesChart?.get(i)!!.value!!.toFloat()
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
        rootView!!.lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        rootView!!.lineChart.axisRight.isEnabled = false

        rootView!!.lineChart.data = lineData
        rootView!!.lineChart.invalidate()


    }


    private fun initLineChartDownOLDFill() {

        rootView!!.lineChart.setDrawGridBackground(false)
        rootView!!.lineChart.description.isEnabled = false
        rootView!!.lineChart.setScaleEnabled(false)
        rootView!!.lineChart.setPinchZoom(false)
        rootView!!.lineChart.axisRight.isEnabled = false
        rootView!!.lineChart.xAxis.setDrawGridLines(false)
        rootView!!.lineChart.axisLeft.setDrawGridLines(false)
        rootView!!.lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        rootView!!.lineChart.xAxis.labelRotationAngle = -45f
        rootView!!.lineChart.xAxis.isGranularityEnabled = true
        rootView!!.lineChart.xAxis.granularity = 1F
        // rootView!!.lineChart.extraRightOffset = 30f
        rootView!!.lineChart.extraTopOffset = 30f

        rootView!!.lineChart.xAxis.labelCount =
            homeController.oldDashboardModel!!.payload!!.groupSalesChart!!.size
        rootView!!.lineChart.xAxis.textColor =
            ContextCompat.getColor(activity!!, R.color.white)
        rootView!!.lineChart.axisLeft.textColor =
            ContextCompat.getColor(activity!!, R.color.white)
        val formatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                if (value < 0 || value >= homeController.oldDashboardModel!!.payload!!.groupSalesChart!!.size) {
                    return ""
                }
                return UTILS.convertDate(
                    "yyyy-MM-dd HH:mm:ss",
                    "yyyy-MM-dd",
                    homeController.oldDashboardModel!!.payload!!.groupSalesChart?.get(value.toInt())!!.date.toString()
                )
            }
        }
        rootView!!.lineChart.xAxis.valueFormatter = formatter
        //  rootView!!.lineChart.axisLeft.axisMinimum = 0f
        //   rootView!!.lineChart.xAxis.mAxisMaximum=60f
        //  rootView!!.lineChart.xAxis.axisMinimum = 0f
        rootView!!.lineChart.xAxis.mAxisMaximum =
            (homeController.oldDashboardModel!!.payload!!.groupSalesChart!!.size).toFloat()
        // rootView!!.chart.setVisibleXRangeMaximum(5f)

        if (homeController.oldDashboardModel!!.payload!!.groupSalesChart!!.size!! > 0)
            rootView!!.lineChart.axisLeft.axisMaximum =
                homeController.oldDashboardModel!!.payload!!.groupSalesChart!!.maxBy { it!!.value!!.toFloat() }!!.value!!.toFloat()



        lineChartDownFillWithOLDData()
    }

    private fun lineChartDownFillWithOLDData() {

        val entryArrayList = ArrayList<Entry>()
        for (i in homeController.oldDashboardModel!!.payload!!.groupSalesChart!!.indices) {
            entryArrayList.add(
                Entry(
                    i.toFloat(),
                    homeController.oldDashboardModel!!.payload!!.groupSalesChart?.get(i)!!.value!!.toFloat()
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
        rootView!!.lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        rootView!!.lineChart.axisRight.isEnabled = false

        rootView!!.lineChart.data = lineData
        rootView!!.lineChart.invalidate()
    }


    /*private fun initBarChartDownOLDFill() {
        Log.d(TAG, "initBarChartDownFill")
        homeController.oldDashboardModel!!.payload!!.groupSalesChart?.let {
            if (it.isNotEmpty()) {
                if (it[0].date == "0" && it[0].value == "0") {

                } else {
                    homeController.oldDashboardModel!!.payload!!.groupSalesChart!!.add(
                        0,
                        TradeDashboardModel.GroupSalesChart().apply {
                            this.date = "0"
                            this.value = "0"
                        })
                }
            }
        }
        rootView!!.chartBar.minimumWidth =
            UTILS.dpToPixel(
                activity!!,
                30 * homeController.oldDashboardModel!!.payload!!.groupSalesChart!!.size
            )
        //rootView!!.chartBar.setPadding(20,0,20,0)
        rootView!!.chartBar.setDrawGridBackground(false)
        rootView!!.chartBar.description.isEnabled = false
        rootView!!.chartBar.setScaleEnabled(false)
        rootView!!.chartBar.setPinchZoom(false)
        rootView!!.chartBar.axisRight.isEnabled = false
        rootView!!.chartBar.xAxis.setDrawGridLines(false)
        rootView!!.chartBar.axisLeft.setDrawGridLines(false)
        rootView!!.chartBar.xAxis.position = XAxis.XAxisPosition.BOTTOM
        rootView!!.chartBar.xAxis.isGranularityEnabled = true
        rootView!!.chartBar.xAxis.granularity = 1F
        rootView!!.chartBar.xAxis.labelRotationAngle = -42f
        rootView!!.chartBar.xAxis.spaceMin = -0.9f

        //rootView!!.chartBar.extraLeftOffset = 30f
        //rootView!!.chartBar.extraRightOffset = 30f
        //rootView!!.chartBar.extraTopOffset = 30f

        rootView!!.chartBar.xAxis.labelCount =
            homeController.oldDashboardModel!!.payload!!.groupSalesChart!!.size
        rootView!!.chartBar.xAxis.textColor =
            ContextCompat.getColor(context!!, R.color.gray)
        rootView!!.chartBar.axisLeft.textColor =
            ContextCompat.getColor(context!!, R.color.gray)
        val formatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                if (value < 0 || value >= homeController.oldDashboardModel!!.payload!!.groupSalesChart!!.size) {
                    return ""
                }
                return UTILS.convertDate(
                    "yyyy-MM-dd",
                    "dd-MM", //"dd-MM"
                    homeController.oldDashboardModel!!.payload!!.groupSalesChart!![value.toInt()].date.toString()
                )
            }
        }
        rootView!!.chartBar.xAxis.valueFormatter = formatter
        *//*val mv = XYMarkerView(context!!, formatter)
        mv.chartView = chart // For bounds control
        rootView!!.chartBar.marker = mv*//*

        rootView!!.chartBar.axisLeft.axisMinimum = 0f
        rootView!!.chartBar.axisLeft.axisMaximum =
            homeController.oldDashboardModel!!.payload!!.groupSalesChart?.maxBy { it.value!!.toFloat() }!!.value!!.toInt() + 800.toFloat()

        // add a nice and smooth animation
        rootView!!.chartBar.animateY(1500)

        rootView!!.chartBar.legend.isEnabled = false

        barChartDownFillWithOLDData()
    }*/

    /*private fun barChartDownFillWithOLDData() {

        val entryArrayList = ArrayList<BarEntry>()
        for (i in homeController.oldDashboardModel!!.payload!!.groupSalesChart!!.indices) {
                entryArrayList.add(
                    BarEntry(
                        i.toFloat(),
                        homeController.oldDashboardModel!!.payload!!.groupSalesChart!![i].value!!.toFloat()
                    )
                )

        }
        var set1: BarDataSet? = null

        if (rootView!!.chartBar.data != null &&
            rootView!!.chartBar.data.dataSetCount > 0
        ) {
            set1 = rootView!!.chartBar.data.getDataSetByIndex(0) as BarDataSet
            set1.values = entryArrayList
            rootView!!.chartBar.data.notifyDataChanged()
            rootView!!.chartBar.notifyDataSetChanged()
        } else {
            set1 = BarDataSet(entryArrayList, "Data Set")
            set1.setColors(
                ContextCompat.getColor(
                    context!!,
                    R.color.light_blue
                )
            )  //ColorTemplate.VORDIPLOM_COLORS
            set1.setDrawValues(true)
            set1.valueFormatter = YValyeFormatter()
            val dataSets = java.util.ArrayList<IBarDataSet>()

            dataSets.add(set1)
            val data = BarData(dataSets)
            data.setValueTextSize(12f)


            data.setValueTextColor(Color.WHITE)
            //data.getGroupWidth(1f,2f)

            rootView!!.chartBar.data = data

            rootView!!.chartBar.setDrawValueAboveBar(true);
            //  rootView!!.chartBar.setFitBars(true)
        }

        rootView!!.chartBar.data = BarData(set1).apply {
            barWidth = 0.5f
        }
        rootView!!.chartBar.invalidate()


    }*/


    private fun addObservers() {
        myMainViewModel!!.isLoading?.observe(this@HomeFragment, Observer {
            if (it) {
                homeController.showProgressDialog()
                Log.e(TAG, "model showing - $tradeDashboardModel")
            } else {
                homeController.dismissProgressDialog()
                Log.e(TAG, "model dismiss - $tradeDashboardModel")
            }
        })
        myMainViewModel!!.tradeDashboardModel?.observe(this@HomeFragment, Observer {

            /**first need to check kyc condition - if kyc status 0(not requested) need to show popup which redirects to kyc*/
            Log.e("zxczxc", "tradeDashboard observed")
            it.payload?.stock_faq_link?.let { it1 -> Pref.setValue(context!!, "stock_help", it1) };

            homeController.resideMenu!!.enableDirection(ResideMenu.DIRECTION_LEFT)
            homeController.iv_navigation!!.isEnabled = true

            /**if any investment ending in 7 days*/
            if (it.payload?.userInvestments == "1") {
                showInvestmentDialog()
            }
            /**now need to check if user opened app for first time so need to check with following flag*/
            else if (checkFingerPrintConditions && BiometricUtils.checkBiometricPossible(activity!!)) {
                /**if first time this screen executed then need to show biometric prompt for authentication*/
                checkFingerPrintConditions = false
                if (userModel.payload!!.user!!.fingerPrintSetInThisDevice) {
                    showBiometricPrompt()
                } else if (!userModel.payload!!.user!!.askedFingerPrint) {
                    showFingerPrintDialog()
                }
            }
            homeController.oldDashboardModel = it
            tradeDashboardModel = it
            updateUi()
        }
        )
        myMainViewModel!!.enableFingerprintResponse?.observe(this@HomeFragment, Observer {
            homeController.messageToast(it)
            userModel.apply {
                this.payload!!.user!!.fingerUUID = uuid
                this.payload!!.user!!.fingerPrintSetInThisDevice = true
            }
            Pref.setValue(activity!!, Constants.prefUserData, Gson().toJson(userModel))
        })
        myMainViewModel!!.enableFingerprintWithOTPResponse?.observe(
            this@HomeFragment,
            Observer {
                /*    if (homeController.shownMessage) {
                        return@Observer
                    }
                    homeController.shownMessage = true
                */
                dialogEnterOtpOptions!!.dismiss()
                homeController.messageToast(it)
                userModel.apply {
                    this.payload!!.user!!.fingerUUID = uuid
                    this.payload!!.user!!.fingerPrintSetInThisDevice = true
                }
                Pref.setValue(activity!!, Constants.prefUserData, Gson().toJson(userModel))

            })
        myMainViewModel!!.sendOTPResponse?.observe(this@HomeFragment, Observer {
            /**this gonna call only single time*/
            homeController.messageToast(it)
            showOTPDialog()
        })
        myMainViewModel!!.reSendOTPResponse?.observe(this@HomeFragment, Observer {
            /*   if (homeController.shownMessage) {
                   return@Observer
               }
               homeController.shownMessage = true
   */
            homeController.messageToast(it)
        })
        myMainViewModel?.responseError?.observe(this@HomeFragment, Observer {
            it?.let {
                /*              if (homeController.shownMessage) {
                                  return@Observer
                              }
                              homeController.shownMessage = true
                */              homeController.errorBody(it)
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun updateUi() {

        /**update balance in saved usermodel*/
        userModel.payload?.user?.apply {
            this.balance = tradeDashboardModel!!.payload!!.balance
            this.name = tradeDashboardModel!!.payload!!.name
            this.packageId = tradeDashboardModel!!.payload!!.packageId
            this.isConsultant =
                if (tradeDashboardModel!!.payload?.isConsultant != null) tradeDashboardModel!!.payload!!.isConsultant else "0"
        }
        tradeDashboardModel!!.payload!!.groupSalesChart?.let {
            if (it.isNotEmpty()) {
//                initBarChartDownFill()
                initSalesLineChartDownFill()
                //initBarChartDownOLDFill()
                //rootView!!.tv_chart_no_data.visibility = View.GONE
            } else {
                // rootView!!.tv_chart_no_data.visibility = View.VISIBLE
            }
            Pref.setValue(activity!!, Constants.prefUserData, Gson().toJson(userModel))
            Pref.setValue(
                activity!!,
                Constants.prefPromoAccount,
                "${tradeDashboardModel!!.payload!!.promoAccount.toString()}"
            )

            /**package id condition*/
            if (userModel.payload!!.user!!.packageId == "0") {

                /**(If Package id is 0 then show only balance and news
                till they not update there package).*/
                homeController.navSelfTrading!!.visibility = View.GONE
                homeController.navPackages!!.visibility = View.GONE
                homeController.navOryxHotel!!.visibility = View.GONE

                rootView!!.ll_all_details.visibility = View.GONE

            } else {
                rootView!!.ll_all_details.visibility = View.VISIBLE
                // homeController!!.navPackages!!.visibility = View.VISIBLE //new demo2 change

            }

            /** package id = 0 and fund wallet == 0 */
            if (userModel.payload!!.user!!.packageId == "0" && tradeDashboardModel!!.payload!!.fundWallet == "0") addFundRedirectDialog()
            if (userModel.payload!!.user!!.packageId == "0" && tradeDashboardModel!!.payload!!.fundWallet!!.toDouble() > 0) purchasePackageRedirectDialog()

            Log.e("zxczxc", "homefragment Updating ui ")
            homeController.tv_count.text = tradeDashboardModel!!.payload!!.unReadTicketCount
            rootView!!.tv_welcome.text =
                "${getString(R.string.welcome)}, ${tradeDashboardModel!!.payload!!.name}"

            rootView!!.tv_total_direct_downline.text = tradeDashboardModel!!.payload!!.totalDownline
//        if (tradeDashboardModel!!.payload!!.rankingCriteriaToGO!!.isEmpty()){
//            rootView!!.tv_total_investors_count.text = "0"
//        }else{
//            rootView!!.tv_total_investors_count.text = tradeDashboardModel!!.payload!!.rankingCriteriaToGO!!.size.toString()
//        }
            rootView!!.tv_total_investors_count.text =
                tradeDashboardModel!!.payload!!.total_investors

            rootView!!.tv_direct_downline_balance_amount.text =
                tradeDashboardModel!!.payload!!.totalDownline
            rootView!!.tv_total_direct_sales.text =
                "$${parseDouble(tradeDashboardModel!!.payload!!.totalDirectSales!!)}"
            rootView!!.tv_personal_sales_balance_amount.text =
                "$${parseDouble(tradeDashboardModel!!.payload!!.totalDirectSales!!)}"
            // rootView!!.tv_balance_amount.text =
            //      "$${parseDouble(tradeDashboardModel!!.payload!!.balance!!)}"
            rootView!!.tv_total_group_sales.text =
                "$${parseDouble(tradeDashboardModel!!.payload!!.totalGroupSales!!)}"
            rootView!!.tv_total_group_sales_amount.text =
                "$${parseDouble(tradeDashboardModel!!.payload!!.totalGroupSales!!)}"
            rootView!!.tv_group_sales_balance_amount.text =
                "$${parseDouble(tradeDashboardModel!!.payload!!.totalGroupSales!!)}"
            rootView!!.tv_monthly_group_sales.text =
                "$${tradeDashboardModel!!.payload!!.monthlyGroupSales?.let { it1 -> parseDouble(it1) }}"
            rootView!!.tv_current_month_sales_amount.text =
                "$${tradeDashboardModel!!.payload!!.monthlyGroupSales?.let { it1 -> parseDouble(it1) }}"
            rootView!!.tv_previous_month_sales_amount.text =
                "$${tradeDashboardModel!!.payload!!.previousMonthSales?.let { it1 -> parseDouble(it1) }}"
            //startCountAnimation(rootView!!.tv_previous_month_sales_amount,tradeDashboardModel!!.payload!!.previousMonthSales!!.toFloat())
            rootView!!.tv_current_rank_name.text =
                tradeDashboardModel!!.payload!!.rankDetail!!.rankName
            rootView!!.tv_your_package_amount.text = tradeDashboardModel!!.payload!!.currentPackage
            rootView!!.tvVexstocksInvestment.text = tradeDashboardModel!!.payload!!.stockInvestmentAmount
            rootView!!.tv_news_updates.visibility =
                if (tradeDashboardModel!!.payload!!.latestNews!!.isEmpty()) View.GONE else View.VISIBLE
            if (tradeDashboardModel!!.payload!!.latestDownlineSales != null) {
                rootView!!.rv_latest_network_sales.visibility =
                    if (tradeDashboardModel!!.payload!!.latestDownlineSales!!.isEmpty()) View.GONE else View.VISIBLE
                rootView!!.llProfileSalesText.visibility =
                    if (tradeDashboardModel!!.payload!!.latestDownlineSales!!.isEmpty()) View.GONE else View.VISIBLE
                rootView!!.tv_no_data.visibility =
                    if (tradeDashboardModel!!.payload!!.latestDownlineSales!!.isEmpty()) View.VISIBLE else View.GONE
            }

            if (tradeDashboardModel!!.payload!!.lastMonthCommission != null) {
                rootView!!.rvLastMonthCommission.visibility =
                    if (tradeDashboardModel!!.payload!!.lastMonthCommission!!.isEmpty()) View.GONE else View.VISIBLE
                rootView!!.llLastMonthCommissionText.visibility =
                    if (tradeDashboardModel!!.payload!!.lastMonthCommission!!.isEmpty()) View.GONE else View.VISIBLE
                rootView!!.tvNoDataLastMonthCommission.visibility =
                    if (tradeDashboardModel!!.payload!!.lastMonthCommission!!.isEmpty()) View.VISIBLE else View.GONE
            }

//        Glide.with(context!!)
//            .load(tradeDashboardModel!!.payload!!.rankDetail!!.image)
//            .apply(RequestOptions().placeholder(R.color.off_white))
//            .into(rootView!!.img_current_rank)

            /** latst network sales */
            tradeDashboardModel!!.payload!!.latestDownlineSales?.let {
                if (it.isNotEmpty()) {
                    //rootView!!.ll_news_updates.visibility = View.VISIBLE
                    rootView!!.rv_latest_network_sales!!.adapter?.notifyDataSetChanged() ?: run {
                        rootView!!.rv_latest_network_sales.adapter =
                            LatestNetworkSalesListAdapter(
                                activity!!,
                                tradeDashboardModel!!.payload!!.latestDownlineSales!!,
                                this@HomeFragment
                            )
                    }
                }
            }

            /** last month commission */
            tradeDashboardModel!!.payload!!.lastMonthCommission?.let {
                if (it.isNotEmpty()) {
                    //rootView!!.ll_news_updates.visibility = View.VISIBLE
                    rootView!!.rvLastMonthCommission!!.adapter?.notifyDataSetChanged() ?: run {
                        rootView!!.rvLastMonthCommission.adapter =
                            LastMonthCommissionListAdapter(
                                activity!!,
                                tradeDashboardModel!!.payload!!.lastMonthCommission!!
                            )
                    }
                }
            }

            /** ranking to go */
            tradeDashboardModel!!.payload!!.rankingCriteriaToGO?.let {
                if (it.isNotEmpty()) {
                    //rootView!!.ll_news_updates.visibility = View.VISIBLE
                    rootView!!.tv_ranking_to_go_label.text =
                        getString(R.string.criteria_tag) + " (" + tradeDashboardModel!!.payload!!.next_rank + ")"
                    rootView!!.rv_ranking_to_go_criteria!!.adapter?.notifyDataSetChanged() ?: run {
                        rootView!!.rv_ranking_to_go_criteria.adapter =
                            tradeDashboardModel!!.payload!!.rankingCriteriaToGO?.let { it1 ->
                                RankingToGoCriteriaAdapter(
                                    activity!!,
                                    it1,
                                    this@HomeFragment
                                )
                            }
                    }
                    rootView!!.rv_last_month_plan!!.adapter?.notifyDataSetChanged() ?: run {
                        rootView!!.rv_last_month_plan.adapter =
                            LastMonthPlanPerformanceAdapter(
                                activity!!,
                                tradeDashboardModel!!.payload!!.rankingCriteriaToGO!!,
                                this@HomeFragment
                            )
                    }
                }
            }
            /**setting news and updates*/
            tradeDashboardModel!!.payload!!.latestNews?.let {
                if (it.isNotEmpty()) {
                    rootView!!.ll_news_updates.visibility = View.VISIBLE
                    rootView!!.rv_news_updates!!.adapter?.notifyDataSetChanged() ?: run {
                        rootView!!.rv_news_updates.adapter =
                            HomeNewsAdapter(
                                activity!!,
                                tradeDashboardModel!!.payload!!.latestNews!!,
                                this@HomeFragment
                            )
                    }
                }
            }


            /**setting graph*/
            tradeDashboardModel!!.payload!!.groupSalesChart?.let {
                if (it.isNotEmpty()) {
                    initLineChartDownFill()
                    rootView!!.tv_chart_no_data.visibility = View.GONE
                } else {
                    rootView!!.tv_chart_no_data.visibility = View.VISIBLE
                }
            }


        }

        //TODO: Set New Announcement Adapter With New Data

        tradeDashboardModel!!.payload!!.announcement?.let {
            (rvNewAnnouncement.adapter as NewAnnouncementAdapter?)?.notifyDataSetChanged() ?: run {
                rvNewAnnouncement.adapter = NewAnnouncementAdapter(
                    activity!!,
                    tradeDashboardModel!!.payload!!.announcement!!
                )
                indicatorAnnouncement.attachToRecyclerView(rvNewAnnouncement)
                PagerSnapHelper().attachToRecyclerView(rvNewAnnouncement)
            }
        }

        consultantUser()  //user is consultant or not

        rootView!!.ll_main.visibility = View.VISIBLE

        if (!userModel.payload?.announcement?.isNullOrEmpty()!!) {
            if (userModel.payload?.announcement?.size!! > 0 && Pref.getValue(
                    activity!!,
                    Constants.ShowAnnouncement,
                    true
                )
            ) {
                Pref.setValue(activity!!, Constants.ShowAnnouncement, false)
                var dialog = Dialog(activity!!, R.style.dialogTheme)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.window!!.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        activity!!,
                        R.drawable.dialog_bg
                    )
                )
                dialog.setContentView(R.layout.dialog_announcement_layout)
                val window: Window? = dialog.window
                val wlp: WindowManager.LayoutParams = window!!.attributes
                wlp.gravity = Gravity.CENTER
                wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND.inv()
                window.attributes = wlp
                dialog.window!!.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
                )
                dialog.setCancelable(false)
                var announcement = userModel.payload?.announcement!!
                dialog.rvAnnouncement.adapter = AnnouncementAdapter(activity!!, announcement)
                dialog.indicator.attachToRecyclerView(dialog.rvAnnouncement)
                PagerSnapHelper().attachToRecyclerView(dialog.rvAnnouncement)
                dialog.imgClose.setOnClickListener {
                    dialog.cancel()
                }
                dialog.show()
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun updateUiWithOldData() {
        userModel.payload?.user?.apply {
            this.balance = homeController.oldDashboardModel!!.payload!!.balance
            this.name = homeController.oldDashboardModel!!.payload!!.name
            this.packageId = homeController.oldDashboardModel!!.payload!!.packageId
            this.isConsultant =
                if (homeController.oldDashboardModel!!.payload?.isConsultant != null) homeController.oldDashboardModel!!.payload!!.isConsultant else "0"
        }
        Pref.setValue(activity!!, Constants.prefUserData, Gson().toJson(userModel))

        /**package id condition*/
        if (userModel.payload!!.user!!.packageId == "0") {

            /**(If Package id is 0 then show only balance and news
            till they not update there package).*/
            homeController.navSelfTrading!!.visibility = View.GONE
            homeController.navPackages!!.visibility = View.GONE
            homeController.navOryxHotel!!.visibility = View.GONE

            rootView!!.ll_all_details.visibility = View.GONE

        } else {
            rootView!!.ll_all_details.visibility = View.VISIBLE
            // homeController!!.navPackages!!.visibility = View.VISIBLE //new demo2 change

        }

        /** package id = 0 and fund wallet == 0 */
        if (userModel.payload!!.user!!.packageId == "0" && homeController.oldDashboardModel!!.payload!!.fundWallet == "0") addFundRedirectDialog()
        if (userModel.payload!!.user!!.packageId == "0" && homeController.oldDashboardModel!!.payload!!.fundWallet!!.toDouble() > 0) purchasePackageRedirectDialog()

        Log.e("zxczxc", "homefragment Updating ui ")
        homeController.tv_count.text =
            homeController.oldDashboardModel!!.payload!!.unReadTicketCount
        rootView!!.tv_welcome.text =
            "${getString(R.string.welcome)}, ${homeController.oldDashboardModel!!.payload!!.name}"

        rootView!!.tv_total_direct_downline.text =
            homeController.oldDashboardModel!!.payload!!.totalDownline
//        if (homeController.oldDashboardModel!!.payload!!.rankingCriteriaToGO!!.isEmpty()){
//            rootView!!.tv_total_investors_count.text = "0"
//        }else{
//            rootView!!.tv_total_investors_count.text =
//                homeController.oldDashboardModel!!.payload!!.rankingCriteriaToGO!!.size.toString()
//        }
        "$${parseDouble(homeController.oldDashboardModel!!.payload!!.totalDirectSales!!)}"
        rootView!!.tv_personal_sales_balance_amount.text =
            "$${parseDouble(homeController.oldDashboardModel!!.payload!!.totalDirectSales!!)}"
        // rootView!!.tv_balance_amount.text =
        //      "$${parseDouble(homeController.oldDashboardModel!!.payload!!.balance!!)}"
        rootView!!.tv_total_group_sales.text =
            "$${homeController.oldDashboardModel!!.payload!!.totalGroupSales?.let { parseDouble(it) }}"
        rootView!!.tv_total_group_sales_amount.text =
            "$${homeController.oldDashboardModel!!.payload!!.totalGroupSales?.let { parseDouble(it) }}"
        rootView!!.tv_group_sales_balance_amount.text =
            "$${homeController.oldDashboardModel!!.payload!!.totalGroupSales?.let { parseDouble(it) }}"
//        rootView!!.tv_monthly_group_sales.text =
//            "$${parseDouble(homeController.oldDashboardModel!!.payload!!.monthlyGroupSales!!)}"
        rootView!!.tv_current_month_sales_amount.text =
            "$${parseDouble(homeController.oldDashboardModel!!.payload!!.monthlyGroupSales!!)}"
        rootView!!.tv_previous_month_sales_amount.text =
            "$${parseDouble(homeController.oldDashboardModel!!.payload!!.previousMonthSales!!)}"
        //startCountAnimation(rootView!!.tv_previous_month_sales_amount,homeController.oldDashboardModel!!.payload!!.previousMonthSales!!.toFloat())
        rootView!!.tv_current_rank_name.text =
            homeController.oldDashboardModel!!.payload!!.rankDetail!!.rankName
        rootView!!.tv_your_package_amount.text = homeController.oldDashboardModel!!.payload!!.currentPackage
        rootView!!.tvVexstocksInvestment.text = homeController.oldDashboardModel!!.payload!!.stockInvestmentAmount
        rootView!!.tv_news_updates.visibility =
            if (homeController.oldDashboardModel!!.payload!!.latestNews!!.isEmpty()) View.GONE else View.VISIBLE
        if (homeController.oldDashboardModel!!.payload!!.latestDownlineSales != null) {
            rootView!!.rv_latest_network_sales.visibility =
                if (homeController.oldDashboardModel!!.payload!!.latestDownlineSales!!.isEmpty()) View.GONE else View.VISIBLE
            rootView!!.tv_no_data.visibility =
                if (homeController.oldDashboardModel!!.payload!!.latestDownlineSales!!.isEmpty()) View.VISIBLE else View.GONE
        }
        if (homeController.oldDashboardModel!!.payload!!.lastMonthCommission != null) {
            rootView!!.rvLastMonthCommission.visibility =
                if (homeController.oldDashboardModel!!.payload!!.lastMonthCommission!!.isEmpty()) View.GONE else View.VISIBLE
            rootView!!.tvNoDataLastMonthCommission.visibility =
                if (homeController.oldDashboardModel!!.payload!!.latestDownlineSales!!.isEmpty()) View.VISIBLE else View.GONE
        }

//        Glide.with(context!!)
//            .load(homeController.oldDashboardModel!!.payload!!.rankDetail!!.image)
//            .apply(RequestOptions().placeholder(R.color.off_white))
//            .into(rootView!!.img_current_rank)


        /** latst network sales */
        homeController.oldDashboardModel!!.payload!!.latestDownlineSales?.let {
            if (it.isNotEmpty()) {
                //rootView!!.ll_news_updates.visibility = View.VISIBLE
                rootView!!.rv_latest_network_sales!!.adapter?.notifyDataSetChanged() ?: run {
                    rootView!!.rv_latest_network_sales.adapter =
                        LatestNetworkSalesListAdapter(
                            activity!!,
                            homeController.oldDashboardModel!!.payload!!.latestDownlineSales!!,
                            this@HomeFragment
                        )
                }
            }
        }

        /** last month commission */
        homeController.oldDashboardModel!!.payload!!.lastMonthCommission?.let {
            if (it.isNotEmpty()) {
                //rootView!!.ll_news_updates.visibility = View.VISIBLE
                rootView!!.rvLastMonthCommission!!.adapter?.notifyDataSetChanged() ?: run {
                    rootView!!.rvLastMonthCommission.adapter =
                        LastMonthCommissionListAdapter(
                            activity!!,
                            homeController.oldDashboardModel!!.payload!!.lastMonthCommission!!
                        )
                }
            }
        }

        /** ranking to go */
        homeController.oldDashboardModel!!.payload!!.rankingCriteriaToGO?.let {
            if (it.isNotEmpty()) {
                //rootView!!.ll_news_updates.visibility = View.VISIBLE
                rootView!!.rv_ranking_to_go_criteria!!.adapter?.notifyDataSetChanged() ?: run {
                    rootView!!.rv_ranking_to_go_criteria.adapter =
                        RankingToGoCriteriaAdapter(
                            activity!!,
                            homeController.oldDashboardModel!!.payload!!.rankingCriteriaToGO!!,
                            this@HomeFragment
                        )
                }
            }
        }

        /**setting news and updates*/
        rootView!!.rv_news_updates.layoutManager =
            LinearLayoutManager(activity!!, LinearLayoutManager.VERTICAL, false)
        homeController.oldDashboardModel!!.payload!!.latestNews?.let {
            if (it.isNotEmpty()) {
                rootView!!.ll_news_updates.visibility = View.VISIBLE
                rootView!!.rv_news_updates!!.adapter?.notifyDataSetChanged() ?: run {
                    rootView!!.rv_news_updates.adapter =
                        HomeNewsAdapter(
                            activity!!,
                            homeController.oldDashboardModel!!.payload!!.latestNews!!,
                            this@HomeFragment
                        )
                }
            }
        }


        /**setting graph*/
        homeController.oldDashboardModel!!.payload!!.groupSalesChart?.let {
            if (it.isNotEmpty()) {
                //initLineChartDownFill()
                oldInitLineChartDownFill()
                rootView!!.tv_chart_no_data.visibility = View.GONE
            } else {
                rootView!!.tv_chart_no_data.visibility = View.VISIBLE
            }
        }

        homeController.oldDashboardModel!!.payload!!.groupSalesChart?.let {
            if (it.isNotEmpty()) {
//                initBarChartDownOLDFill()
                initLineChartDownOLDFill()
                //   initBarChartDownFill()
                //rootView!!.tv_chart_no_data.visibility = View.GONE
            } else {
                // rootView!!.tv_chart_no_data.visibility = View.VISIBLE
            }
        }


        homeController.oldDashboardModel?.payload?.announcement?.let {
            (rootView!!.rvNewAnnouncement.adapter as NewAnnouncementAdapter?)?.notifyDataSetChanged()
                ?: run {
                    rootView!!.rvNewAnnouncement.adapter = NewAnnouncementAdapter(
                        activity!!,
                        homeController.oldDashboardModel!!.payload!!.announcement!!
                    )
                    rootView!!.indicatorAnnouncement.attachToRecyclerView(rootView!!.rvNewAnnouncement)
                    PagerSnapHelper().attachToRecyclerView(rootView!!.rvNewAnnouncement)
                }
        }

        consultantUser()  //user is consultant or not

        rootView!!.ll_main.visibility = View.VISIBLE

    }

    private fun consultantUser() {
        /**FOr consultant user if constuant  == 1 then only display Weekly Total income graph**/
        if (Pref.getUserModel(activity!!)!!.payload!!.user?.isConsultant == "1") {
            rootView!!.tv_sales_summary_lable.visibility = View.GONE
            rootView!!.tv_current_month_sales_lable.visibility = View.GONE
            rootView!!.tv_previous_month_sales_lable.visibility = View.GONE
            rootView!!.rl_barchart.visibility = View.GONE
            rootView!!.ll_amount_label.visibility = View.GONE
            rootView!!.tv_latest_network_sales_label.visibility = View.GONE
            rootView!!.tv_ranking_to_go_label.visibility = View.GONE
            rootView!!.rv_ranking_to_go_criteria.visibility = View.GONE
            rootView!!.ll_criteria_togo.makeGone()

            rootView!!.ll_news_updates.makeVisible()
            rootView!!.tv_welcome.text =
                "${getString(R.string.welcome)}, ${userModel.payload!!.user!!.name}"
            rootView!!.tv_welcome.makeVisible()
            rootView!!.llCurrentRank.makeGone()
            rootView!!.llSalesSummary.makeGone()
            rootView!!.llWeeklyTotalIncome.makeGone()
            rootView!!.ll_last_month_plane_performance.makeGone()
            rootView!!.ll_criteria_togo.makeGone()
        }
    }

    private fun addFundRedirectDialog() {
        val builder =
            AlertDialog.Builder(activity!!, R.style.MyDialogTheme)
        builder.setTitle(getString(R.string.notice))
        builder.setMessage(
            getString(R.string.to_unlock_the_features_of_your_member_login_kindly_proceed_funding_account)
        )
        builder.setCancelable(true)
        builder.setPositiveButton(getString(R.string.add_fund),
            DialogInterface.OnClickListener { dialog, which ->
                homeController.loadFragment(
                    FundWallets(),
                    "FundWallets_D",
                    activity!!.supportFragmentManager.findFragmentById(R.id.fragment_container)!!.javaClass.simpleName
                )
            })
        builder.setNegativeButton(getString(R.string.cancel),
            DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
            })
        if (dialogAddFundOptions == null || !dialogAddFundOptions!!.isShowing) {
            dialogAddFundOptions = builder.create()
                .apply {

                    this.setOnShowListener {
                        this.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                            .setTextColor(
                                ContextCompat.getColor(
                                    activity!!,
                                    R.color.red_dark_btn
                                )
                            )

                    }
                }
            dialogAddFundOptions!!.show()
        }
    }

    private fun purchasePackageRedirectDialog() {
        val builder =
            AlertDialog.Builder(activity!!, R.style.MyDialogTheme)
        builder.setTitle(getString(R.string.notice))
        builder.setMessage(
            getString(R.string.please_buy_a_package_to_activate_account_and_access_all_features)
        )
        builder.setCancelable(true)
        builder.setPositiveButton(getString(R.string.add_package),
            DialogInterface.OnClickListener { dialog, which ->
                homeController.loadFragment(
                    MyAccountFragment().apply {
                        this.viewTagSelected = "buy_package"
                        //homeController.my_profile_viewTagSelected = this.viewTagSelected
                    },
                    "MyAccountFragment_D",
                    activity!!.supportFragmentManager.findFragmentById(R.id.fragment_container)!!.javaClass.simpleName
                )
            })
        builder.setNegativeButton(getString(R.string.cancel),
            DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
            })
        if (dialogAddFundOptions == null || !dialogAddFundOptions!!.isShowing) {
            dialogAddFundOptions = builder.create()
                .apply {

                    this.setOnShowListener {
                        this.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                            .setTextColor(
                                ContextCompat.getColor(
                                    activity!!,
                                    R.color.red_dark_btn
                                )
                            )

                    }
                }
            dialogAddFundOptions!!.show()
        }
    }


    override fun onListShow(position: Int, obj: Any?) {
        super.onListShow(position, obj)
        var payload = obj as TradeDashboardModel.LatestNews
        var newsAndUpdateFragment = NewsAndUpdateFragment()
        var bundle = Bundle()
        bundle.putString("latestNewsData", Gson().toJson(payload))
        newsAndUpdateFragment.arguments = bundle
        homeController.addLoadFragment(
            newsAndUpdateFragment,
            "NewsAndUpdateFragment",
            this.javaClass.simpleName
        )
    }

    private fun fillTempChartData() {
        tradeDashboardModel!!.payload!!.groupSalesChart!!.add(
            TradeDashboardModel.GroupSalesChart().apply {
                this.date = "2019-11-08"
                this.value = "84051.04"
            })
        tradeDashboardModel!!.payload!!.groupSalesChart!!.add(
            TradeDashboardModel.GroupSalesChart().apply {
                this.date = "2019-11-12"
                this.value = "70051.04"
            })
        tradeDashboardModel!!.payload!!.groupSalesChart!!.add(
            TradeDashboardModel.GroupSalesChart().apply {
                this.date = "2019-11-18"
                this.value = "80051.04"
            })
        tradeDashboardModel!!.payload!!.groupSalesChart!!.add(
            TradeDashboardModel.GroupSalesChart().apply {
                this.date = "2019-11-22"
                this.value = "74051.04"
            })
        tradeDashboardModel!!.payload!!.groupSalesChart!!.add(
            TradeDashboardModel.GroupSalesChart().apply {
                this.date = "2019-11-28"
                this.value = "54051.04"
            })
    }

    override fun onPause() {
        super.onPause()
        homeController.llBottomNew.makeGone()
    }

}


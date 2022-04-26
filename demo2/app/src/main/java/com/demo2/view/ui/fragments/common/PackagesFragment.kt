package com.demo2.view.ui.fragments.common

//PackagesFragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.biometric.BiometricConstants
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.demo2.R
import com.demo2.utilities.Constants
import com.demo2.utilities.Pref
import com.demo2.view.interfaces.BiometricCallback
import com.demo2.view.service.MyViewModelFactory
import com.demo2.view.service.NetworkUtil
import com.demo2.view.ui.base.BaseFragment
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_packages.view.*
import kotlinx.android.synthetic.main.include_biometric_selection.view.*
import org.json.JSONObject


class PackagesFragment : BaseFragment() {
    private val TAG = this.javaClass.simpleName
    var rootView: View? = null

    var myMainViewModel: PackagesViewModel? = null
    var historyPackagesModel: HistoryPackagesModel? = null

    /**flags*/
    private var typeReceivedHistory = "typeReceivedHistory"
    private var currentSelectedHistoryTab = typeReceivedHistory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_packages, container, false)
        }
        return rootView
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Pref.setLocale(activity!!, Pref.getLocalization(activity!!))

        homeController.tv_title.visibility = View.VISIBLE
        homeController.tv_title.text = getString(R.string.packages_tag)
        homeController.iv_navigation.visibility = View.GONE
        homeController.ll_bottombar.visibility = View.GONE
        homeController.iv_back.visibility = View.VISIBLE
        homeController.resideMenu!!.addIgnoredView(rootView!!.horizontal_scroll_histories)
        homeController.resideMenu!!.addIgnoredView(rootView!!.horizontal_scroll_topups)
        init()
        setup()
        addListeners()
    }


    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun init() {
        myMainViewModel = ViewModelProviders.of(
            this@PackagesFragment,
            MyViewModelFactory(PackagesViewModel(activity!!))
        )[PackagesViewModel::class.java]
    }

    private fun setup() {
        /**condition if user enable fingerprint then give options*/
        // rootView!!.layout_method_selection.visibility = View.VISIBLE
        setupBiometricSelection(
            methodSelectionLayout = rootView!!.layout_method_selection,
            securityPasswordLayout = rootView!!.ll_security_password,
            radioBiometric = rootView!!.rb_biometric,
            radioSecurityPassword = rootView!!.rb_security_password,
            radioGroupSelection = rootView!!.rg_selection
        )
        addObservers()
        myMainViewModel!!.getPackagesHistory(0)
    }

    private fun addListeners() {
        rootView!!.cv_verify_username.setOnClickListener {
            if (rootView!!.edt_sponser_username.text.toString().trim().isEmpty()) {
                Toast.makeText(
                    activity!!,
                    getString(R.string.validation_username),
                    Toast.LENGTH_SHORT
                ).show()

                rootView!!.scroll.myRequestFocus(rootView!!.edt_sponser_username)
            } else {
                myMainViewModel!!.varifyDownlineSponserCall(name = rootView!!.edt_sponser_username.text.toString().trim())
            }
        }
        rootView!!.sp_point_wallet_percentage.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {
                }

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    rootView!!.sp_point_wallet_percentage.adapter.let {
                        var x: HighLightArrayAdapter = it as HighLightArrayAdapter
                        x.setSelection(p2)
                    }
                }
            }
        rootView!!.sp_upgrade_package.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {
                }

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    rootView!!.sp_upgrade_package.adapter.let {
                        var x: HighLightArrayAdapter = it as HighLightArrayAdapter
                        x.setSelection(p2)
                    }
                }
            }

        rootView!!.scroll.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (v.getChildAt(v.childCount - 1) != null) {
                if ((scrollY >= (v.getChildAt(v.childCount - 1).measuredHeight - v.measuredHeight))
                    && scrollY > oldScrollY
                ) {
                    if (currentSelectedHistoryTab == typeReceivedHistory) {
                        if (!historyPackagesModel!!.paginationEnded) {
                            myMainViewModel?.getPackagesHistory(
                                historyPackagesModel!!.payload!!.history?.size!!
                            )
                        }
                    }
                }
            }
        })

        rootView!!.cv_transfer_to_funds.setOnClickListener {
            if (rootView!!.edt_sponser_username.text.toString().trim().isEmpty()) {
                Toast.makeText(
                    activity!!,
                    getString(R.string.validation_username),
                    Toast.LENGTH_SHORT
                ).show()

                rootView!!.scroll.myRequestFocus(rootView!!.edt_sponser_username)
            } else if (rootView!!.edt_security_password.text.toString().trim().isEmpty() && rootView!!.rb_security_password.isChecked) {

                Toast.makeText(
                    activity!!,
                    getString(R.string.security_password_validation),
                    Toast.LENGTH_SHORT
                )
                    .show()
                rootView!!.scroll.myRequestFocus(rootView!!.edt_security_password)

            } else {
                /**implement here of transfer funds*/
                homeController.hideSoftKeyboard()
                if (rootView!!.rb_biometric.isChecked) {
                    /**biometric auth*/
                    showBiometric(biometricCallback = object : BiometricCallback {
                        override fun onSuccess(result: BiometricPrompt.AuthenticationResult) {
                            /**success*/

                            //commented as per new changes
                            myMainViewModel!!.buyPackageWithUUID(
                                sponsorName = rootView!!.edt_sponser_username.text.toString().trim(),
                                percent = historyPackagesModel!!.payload!!.percent!!.get(
                                    rootView!!.sp_point_wallet_percentage.selectedItemPosition
                                )!!.value!!.toString(),
                                packageId = historyPackagesModel!!.payload!!.packages!![rootView!!.sp_upgrade_package.selectedItemPosition]!!.id!!.toString(),
                                uuid = userModel.payload!!.user!!.fingerUUID.toString().trim()
                            )

                        }

                        override fun onFail() {}

                        override fun onError(errorCode: Int, errString: CharSequence) {
                            if (errorCode == BiometricConstants.ERROR_LOCKOUT || errorCode == BiometricConstants.ERROR_LOCKOUT_PERMANENT) {
                                Toast.makeText(
                                    activity!!,
                                    getString(R.string.too_many_attempts),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    })
                } else {

                    myMainViewModel!!.buyPackage(
                        sponsorName = rootView!!.edt_sponser_username.text.toString().trim(),
                        percent = historyPackagesModel!!.payload!!.percent!!.get(
                            rootView!!.sp_point_wallet_percentage.selectedItemPosition
                        )!!.value!!.toString(),
                        packageId = historyPackagesModel!!.payload!!.packages!![rootView!!.sp_upgrade_package.selectedItemPosition]!!.id!!.toString(),
                        securityPassword = rootView!!.edt_security_password.text.toString().trim()
                    )
                }
            }
        }

        /**managed historyModel clicks below*/
        rootView!!.cv_received_history.setOnClickListener {
            if (currentSelectedHistoryTab == typeReceivedHistory) {
                return@setOnClickListener
            }
            if (!NetworkUtil.isInternetAvailable(activity!!)) {
                Toast.makeText(
                    activity!!,
                    getString(R.string.no_internet_connection),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            currentSelectedHistoryTab = typeReceivedHistory
            unSelectHistoryOptions()
            selectTab(
                rootView!!.cv_received_history,
                rootView!!.tv_received_history
            )
            rootView!!.tv_history_total.visibility = View.GONE

            if (historyPackagesModel == null) {
                //temp commented as api is pending

                myMainViewModel!!.getPackagesHistory(0)
            } else {
                rootView!!.rv_history.adapter = PackageHistoryAdapter(
                    activity!!,
                    historyPackagesModel!!.payload!!.history,
                    this@PackagesFragment
                )
            }
        }

    }


    private fun addObservers() {
        myMainViewModel!!.isLoading?.observe(this@PackagesFragment, Observer {
            if (it) {
                homeController.showProgressDialog()
            } else {
                homeController.dismissProgressDialog()

            }
        })
        myMainViewModel!!.responseError?.observe(this@PackagesFragment, Observer {
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
                    userModel.apply {
                        this.payload!!.user!!.fingerPrintSetInThisDevice = false
                    }
                    Pref.setValue(activity!!, Constants.prefUserData, Gson().toJson(userModel))
                    setupBiometricSelection(
                        methodSelectionLayout = rootView!!.layout_method_selection,
                        securityPasswordLayout = rootView!!.ll_security_password,
                        radioBiometric = rootView!!.rb_biometric,
                        radioSecurityPassword = rootView!!.rb_security_password,
                        radioGroupSelection = rootView!!.rg_selection
                    )
                } else {
                    homeController.errorBodyFromJson(jsonObject)
                }
            } catch (e: java.lang.Exception) {
                Toast.makeText(
                    activity!!,
                    getString(R.string.something_wrong_message),
                    Toast.LENGTH_SHORT
                ).show()
            }

        })

        myMainViewModel!!.varifyResponseModel?.observe(this@PackagesFragment, Observer {
            homeController.messageToast(it)

        })


        myMainViewModel!!.buyPackageRequestResponseModel?.observe(
            this@PackagesFragment,
            Observer {
                rootView!!.edt_sponser_username.text.clear()
                rootView!!.edt_sponser_username.clearFocus()
                rootView!!.edt_security_password.text.clear()
                rootView!!.edt_security_password.clearFocus()
                /*
                 //commented as per new changes
                 rootView!!.sp_upgrade_package.setSelection(0)
                 rootView!!.sp_point_wallet_percentage.setSelection(0)
                 rootView!!.rb_self_fund_balance.isChecked = true

 */
                rootView!!.tv_funds_balance.text =
                    "$${parseDouble(it!!.payload!!.fundBalance!!)}"
                rootView!!.tv_points_balance_amount.text =
                    "$${parseDouble(it!!.payload!!.pointsBalance!!)}"
                if (historyPackagesModel != null) {
                    if (historyPackagesModel!!.payload!!.history == null) {
                        historyPackagesModel!!.payload!!.history = ArrayList()
                    }
                    historyPackagesModel!!.payload!!.history!!.add(
                        0,
                        it.payload!!.history
                    )
                    if (currentSelectedHistoryTab == typeReceivedHistory) {
                        (rootView!!.rv_history.adapter as PackageHistoryAdapter?)?.let { adapter ->
                            /**already adapter set just need to notify
                             */            //adapter.notifyDataSetChanged()
                             adapter.notifyDataSetChanged()
                            //adapter.notifyItemInserted(0)
                        } ?: run {
                            /**need to set new adapter
                             */
                            rootView!!.rv_history.adapter = PackageHistoryAdapter(
                                activity!!,
                                historyPackagesModel!!.payload!!.history,
                                this@PackagesFragment
                            )
                        }
                    }
                }
            })


        myMainViewModel!!.historyPackagesModel?.observe(
            this@PackagesFragment,
            Observer {
                if (historyPackagesModel == null) {
                    historyPackagesModel = it

                    /**api called for first time*/
                    /**as api called first time need to set new adapter*/
                    rootView!!.rv_history.adapter = PackageHistoryAdapter(
                        activity!!,
                        historyPackagesModel!!.payload!!.history,
                        this@PackagesFragment
                    )
                    rootView!!.tv_terms_conditions.setHtmlData(historyPackagesModel!!.payload!!.termsAndCondition!!)

                    rootView!!.sp_point_wallet_percentage.adapter = HighLightArrayAdapter(
                        activity!!, R.layout.row_spinner, ArrayList<String>().apply {
                            historyPackagesModel!!.payload!!.percent!!.forEach { percentModel ->
                                this.add("${percentModel!!.label.toString().trim()}")
                            }
                        })
                    rootView!!.sp_upgrade_package.adapter = HighLightArrayAdapter(
                        activity!!, R.layout.row_spinner, ArrayList<String>().apply {
                            historyPackagesModel!!.payload!!.packages!!.forEach { percentModel ->
                                this.add(percentModel!!.name.toString().trim())
                            }
                        })
                } else {
                    historyPackagesModel!!.payload!!.history?.let {

                        (rootView!!.rv_history.adapter as PackageHistoryAdapter?)?.let { adapter ->
                            /**already adapter set just need to notify*/
                            adapter.notifyItemRangeInserted(
                                adapter.lastPosition + 1,
                                it.size - (adapter.lastPosition + 1)
                            )
                        } ?: run {
                            /**need to set new adapter*/
                            rootView!!.rv_history.adapter = PackageHistoryAdapter(
                                activity!!,
                                historyPackagesModel!!.payload!!.history,
                                this@PackagesFragment
                            )
                        }

                    }
                }
                rootView!!.tv_funds_balance_amount.text =
                    "$${parseDouble(historyPackagesModel!!.payload!!.fundWallet!!)}"
                rootView!!.tv_points_balance_amount.text =
                    "$${parseDouble(historyPackagesModel!!.payload!!.pointsBalance!!)}"

                rootView!!.ll_main.visibility = View.VISIBLE

            })
    }

    private fun unSelectHistoryOptions() {

        rootView!!.cv_received_history.setCardBackgroundColor(
            ContextCompat.getColor(
                activity!!,
                R.color.colorPrimaryDark
            )
        )



        rootView!!.tv_received_history.setTextColor(
            ContextCompat.getColor(
                activity!!,
                R.color.purple_light
            )
        )
    }


}


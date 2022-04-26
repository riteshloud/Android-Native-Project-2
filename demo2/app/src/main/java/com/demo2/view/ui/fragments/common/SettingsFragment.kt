package com.demo2.view.ui.fragments.common


import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricConstants
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

import com.demo2.R
import com.demo2.utilities.BiometricUtils
import com.demo2.utilities.Constants
import com.demo2.utilities.Pref
import com.demo2.view.interfaces.BiometricCallback
import com.demo2.view.service.ApiClient
import com.demo2.view.service.MyViewModelFactory
import com.demo2.view.ui.base.BaseFragment
import com.google.gson.Gson
import com.special.ResideMenu.ResideMenu
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_settings.view.*
import kotlinx.android.synthetic.main.view_enter_otp.view.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class SettingsFragment : BaseFragment() {

    var rootview: View? = null
    var myMainViewModel: SettingsViewModel? = null
    var uuid: String? = null
    var dialogEnableOptions: AlertDialog? = null
    var dialogEnterOtpOptions: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        rootview = inflater.inflate(R.layout.fragment_settings, container, false)
        init()
        setup()
        onClickListeners()
        //}
        return rootview
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Pref.setLocale(activity!!, Pref.getLocalization(activity!!))

        homeController.tv_title.text = getString(R.string.settings)
        homeController.resideMenu!!.setSwipeDirectionDisable(ResideMenu.DIRECTION_LEFT)
        homeController.settings_toolbar.visibility = View.GONE
        homeController.iv_navigation.visibility = View.GONE
        homeController.ll_bottombar.visibility = View.GONE
        homeController.iv_back.visibility = View.VISIBLE
        homeController.ll_bottom.visibility = View.GONE

        homeController.viewVisibleDrawerBottomBar(2)
    }

    private fun init() {
        myMainViewModel =
            ViewModelProviders.of(
                this@SettingsFragment,
                MyViewModelFactory(
                    SettingsViewModel(
                        activity!!
                    )
                )
            )[SettingsViewModel::class.java]
    }

    private fun setup() {

        if (BiometricUtils.checkBiometricPossible(activity!!)) {
            rootview!!.switch_biometric.isChecked =
                userModel!!.payload!!.user!!.fingerPrintSetInThisDevice
            addObservers()

            rootview!!.tv_biometric_desc.visibility = View.GONE
        } else {
            rootview!!.switch_biometric.isEnabled = false
            rootview!!.tv_biometric_desc.visibility = View.VISIBLE
        }
    }


    private fun onClickListeners() {
        homeController.iv_back_settings.setOnClickListener {
            activity!!.onBackPressed()
        }

        rootview!!.rl_notification.setOnClickListener{
            homeController.loadFragment(
                NotificationListFragment(),
                "NotificationListFragment",
                this.javaClass.simpleName
            )
        }
        rootview!!.switch_biometric.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!buttonView.isPressed) {
                return@setOnCheckedChangeListener
            }
            if (isChecked) {
                /**enabled*/


                showBiometric(biometricCallback = object : BiometricCallback {
                    override fun onSuccess(result: BiometricPrompt.AuthenticationResult) {
                        Log.e("zxczxc","settings onSuccess")
                        uuid = UUID.randomUUID().toString()

                        myMainViewModel!!.enableFingerPrint(uuid!!)

                    }

                    override fun onFail() {
                        rootview!!.switch_biometric.isChecked =
                            userModel!!.payload!!.user!!.fingerPrintSetInThisDevice

                    }

                    override fun onError(errorCode: Int, errString: CharSequence) {
                        rootview!!.switch_biometric.isChecked =
                            userModel!!.payload!!.user!!.fingerPrintSetInThisDevice

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
                /**disabled*/
                showBiometric(biometricCallback = object : BiometricCallback {
                    override fun onSuccess(result: BiometricPrompt.AuthenticationResult) {
                        Log.e("zxczxc","settings onSuccess")
                        removeUUID()

                    }

                    override fun onFail() {
                        rootview!!.switch_biometric.isChecked =
                            userModel!!.payload!!.user!!.fingerPrintSetInThisDevice

                    }

                    override fun onError(errorCode: Int, errString: CharSequence) {
                        rootview!!.switch_biometric.isChecked =
                            userModel!!.payload!!.user!!.fingerPrintSetInThisDevice

                        if (errorCode == BiometricConstants.ERROR_LOCKOUT || errorCode == BiometricConstants.ERROR_LOCKOUT_PERMANENT) {
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
    }

    fun removeUUID() {
        homeController.showProgressDialog()
        ApiClient.getClient(activity!!).logout(
            localization = Pref.getLocalization(activity!!)
            , authorization = Pref.getprefAuthorizationToken(activity!!)
        )
            .enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    homeController!!.dismissProgressDialog()
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    homeController!!.dismissProgressDialog()
                    if (response.isSuccessful) {
                        userModel.apply {
                            this.payload!!.user!!.fingerPrintSetInThisDevice = false

                        }
                        Pref.setValue(activity!!, Constants.prefUserData, Gson().toJson(userModel))

                    } else {

                        homeController!!.errorBody(response.errorBody()!!)
                    }
                }
            })
    }

    private fun addObservers() {
        myMainViewModel!!.isLoading?.observe(this@SettingsFragment, Observer {
            if (it) {
                homeController.showProgressDialog()
            } else {
                homeController.dismissProgressDialog()
            }
        })
        myMainViewModel!!.enableFingerprintResponse?.observe(this@SettingsFragment, Observer {
            homeController.messageToast(it)

            userModel.apply {
                this.payload!!.user!!.fingerUUID = uuid
                this.payload!!.user!!.fingerPrintSetInThisDevice = true
            }
            Pref.setValue(activity!!, Constants.prefUserData, Gson().toJson(userModel))

        })
        myMainViewModel!!.enableFingerprintWithOTPResponse?.observe(
            this@SettingsFragment,
            Observer {
                dialogEnterOtpOptions!!.dismiss()
                homeController.messageToast(it)
                userModel.apply {
                    this.payload!!.user!!.fingerUUID = uuid
                    this.payload!!.user!!.fingerPrintSetInThisDevice = true
                }
                Pref.setValue(activity!!, Constants.prefUserData, Gson().toJson(userModel))

            })
        myMainViewModel!!.sendOTPResponse?.observe(this@SettingsFragment, Observer {
            homeController.messageToast(it)
            showOTPDialog()
        })
        myMainViewModel!!.reSendOTPResponse?.observe(this@SettingsFragment, Observer {
            homeController.messageToast(it)
        })
        myMainViewModel?.responseError?.observe(this@SettingsFragment, Observer {
            it?.let {
                val res = it.string()
                val jsonObject = JSONObject(res)
                if (jsonObject.optInt("code") == 305) {
                    Toast.makeText(
                        activity!!,
                        jsonObject.getString("message"),
                        Toast.LENGTH_SHORT
                    ).show()
                    showOTPDialog()


                } else {
                    homeController.errorBodyFromJson(jsonObject)
                }
            }
        })

    }

    override fun onDestroyView() {
        super.onDestroyView()
        //homeController.visibleBottomBar(0)
        homeController.ll_bottom.visibility = View.VISIBLE
        homeController.resideMenu!!.enableDirection(ResideMenu.DIRECTION_LEFT)
        homeController.settings_toolbar.visibility = View.GONE

    }
/*

    private fun showFingerPrintDialog() {
        */
    /**only need to continue if following conditions true*//*
        if (!BiometricUtils.checkBiometricPossible(activity!!)) return


        if (userModel.payload!!.user!!.askedFingerPrint!!) {
            return
        }
        uuid = UUID.randomUUID().toString()

        dialogEnableOptions = AlertDialog.Builder(activity!!, R.style.MyDialogTheme).apply {
            this.setTitle(getString(R.string.fingerprint_title))
            this.setMessage(getString(R.string.fingerprint_description))

            this.setPositiveButton(getString(R.string.enable_button)) { dialog, which ->
                */
    /**on enable*//*
                if (userModel!!.payload!!.user!!.alreadyFingerPrintAdded == "1") {
                    myMainViewModel!!.sendOTPFingerprint()
                    //  showOTPDialog()
                } else {
                    showBiometric(biometricCallback = object : BiometricCallback {
                        override fun onSuccess(result: BiometricPrompt.AuthenticationResult) {
                            myMainViewModel!!.enableFingerPrint(uuid!!)

                        }

                        override fun onFail() {}
                        override fun onError(errorCode: Int, errString: CharSequence) {
                            rootview!!.switch_biometric.isChecked =
                                userModel!!.payload!!.user!!.fingerPrintSetInThisDevice

                            if (errorCode == BiometricConstants.ERROR_LOCKOUT || errorCode == BiometricConstants.ERROR_LOCKOUT_PERMANENT) {
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
                */
    /**on cancel*//*

            }
        }.create()
        dialogEnableOptions!!.setOnShowListener {
            dialogEnableOptions!!.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(activity!!, R.color.black))
            dialogEnableOptions!!.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(activity!!, R.color.black))
        }
        dialogEnableOptions!!.show()


        */
    /**updating asked as it should ask only first time*//*
        //temporary commented
        userModel.apply {
            this.payload!!.user!!.askedFingerPrint = true
        }

        Pref.setValue(activity!!, Constants.prefUserData, Gson().toJson(userModel))
    }

    */
    private fun showOTPDialog() {
        var inflater =
            activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var v = inflater.inflate(R.layout.view_enter_otp, null)

        var alertDialog = AlertDialog.Builder(activity!!, R.style.MyDialogTheme).apply {
            this.setCancelable(false)
            this.setTitle(getString(R.string.confitm_otp))
            this.setMessage(getString(R.string.confitm_otp_description))
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
                            Log.e("zxczxc","settings onSuccess")
                            myMainViewModel!!.enableFingerPrintWithOTP(
                                uuid = uuid!!,
                                otp = v.edt_otp.text.toString().trim()
                            )

                        }

                        override fun onFail() {
                            rootview!!.switch_biometric.isChecked =
                                userModel!!.payload!!.user!!.fingerPrintSetInThisDevice

                        }

                        override fun onError(errorCode: Int, errString: CharSequence) {
                            rootview!!.switch_biometric.isChecked =
                                userModel!!.payload!!.user!!.fingerPrintSetInThisDevice

                            if (errorCode == BiometricConstants.ERROR_LOCKOUT || errorCode == BiometricConstants.ERROR_LOCKOUT_PERMANENT) {
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
                rootview!!.switch_biometric.isChecked =
                    userModel!!.payload!!.user!!.fingerPrintSetInThisDevice

                dialogEnterOtpOptions!!.dismiss()
            }
            v.requestFocus()
        }
        showSoftKeyboard()
        dialogEnterOtpOptions = alertDialog.create()
        dialogEnterOtpOptions!!.show()
    }

}

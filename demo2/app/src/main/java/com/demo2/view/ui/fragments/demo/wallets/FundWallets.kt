package com.demo2.view.ui.fragments.demo.wallets


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.provider.MediaStore
import android.provider.Settings
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.Toast
import androidx.biometric.BiometricConstants
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.demo2.R
import com.demo2.utilities.*
import com.demo2.utilities.Constants.Companion.codeCameraRequest
import com.demo2.utilities.Constants.Companion.codePickImageRequest
import com.demo2.utilities.Constants.Companion.codePickPdfRequest
import com.demo2.utilities.Constants.Companion.codeSettings
import com.demo2.view.adapters.*
import com.demo2.view.interfaces.BiometricCallback
import com.demo2.view.service.MyViewModelFactory
import com.demo2.view.service.NetworkUtil
import com.demo2.view.ui.base.BaseFragment
import com.demo2.view.ui.fragments.common.FundWebviewFragment
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.dialog_upload_file.view.*
import kotlinx.android.synthetic.main.fragment_wallet_fund.*
import kotlinx.android.synthetic.main.fragment_wallet_fund.view.*
import kotlinx.android.synthetic.main.include_biometric_selection.view.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class FundWallets : BaseFragment() {
    var rootView: View? = null
    private var myMainViewModel: WalletFundsViewModel? = null
    private var fundTopupHistoryModel: HistoryFundTopupModel? = null
    private var walletHistoryModel: HistoryFundWallet? = null
    private var mt4TopupHistoryModel: HistoryMt4TopupModel? = null

    /**declarations for image/pdf selection*/
    private var currentPhotoPath: String? = null
    private var mImageBitmap: Bitmap? = null
    private var mSelectionPath: String? = null
    private var mSelectionPathThirdParty: String? = null
    private var photoFile: File? = null
    private var selectedType: Int = 0
    private var mBottomSheetDialog: BottomSheetDialog? = null
    private var permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )

    /**flags*/
    private var sponserUserVerified: Boolean = false
    private var typeFundTopupHistory = "typeFundTopupHistory"
    private var typeWalletHistory = "typeWalletHistory"
    private var typeMT4TopupHistory = "typeMT4TopupHistory"
    private var currentSelectedTab = typeFundTopupHistory

    private var typeTopupFunds = "typeTopupFunds"
    private var typeTransferDownlinesUplines = "typeTransferDownlinesUplines"
    private var typeTransferDownlineOTM = "typeTransferDownlineOTM"
    private var currentSelectedTransferTab = typeTopupFunds

    //    private val typeUSDT = "usdt"
//    private val typeOnlinePayment = "online"
//    private val thirdPartyPayment = "bank3rdParty"
//    private val typeDirectTransfer = "direct_transfer"
    private var currentSelectedBankUSDTTab = Constants.TYPE_USDT

    private var selectedImage = 0
    private var usdtQrCodeUrl: String? = null
    private var usdtAddressValue: String? = null
    private var bankDetail =
        "0"  //0 - Only bank name and 1 = bank list, bank holder name and bank account number

//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//       // Pref.setLocale(activity!!, Pref.getLocalization(activity!!))
//    }

    //TODO: TopUp Methods
    var topUpFundsMethodAdapter: TopUpFundsMethodsAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_wallet_fund, container, false)
            init()
            setup()
        }
        return rootView
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Pref.setLocale(activity!!, Pref.getLocalization(activity!!))
        homeController.tv_title.text = getString(R.string.topup_fund_nav)
        homeController.tv_title.visibility = View.VISIBLE
        homeController.iv_navigation.visibility = View.GONE
        homeController.ll_bottombar.visibility = View.GONE
        homeController.ll_bottomBar_selfTrad.visibility = View.GONE
        homeController.iv_back.visibility = View.VISIBLE

        homeController.viewVisibleDrawerBottomBar(1)

        homeController.resideMenu!!.addIgnoredView(rootView!!.horizontal_scroll)


        addListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        homeController.dismissSnackbar()
    }

    private fun init() {
        myMainViewModel = ViewModelProviders
            .of(
                this@FundWallets,
                MyViewModelFactory(WalletFundsViewModel(activity!!))
            )[WalletFundsViewModel::class.java]
    }

    private fun setup() {
        if (userModel!!.payload!!.user?.isConsultant == "1") {
//            rootView!!.ll_balance_section.visibility = View.GONE
            rootView!!.ll_topup_section.visibility = View.GONE
            rootView!!.cv_usdt_terms_conditions.visibility = View.GONE
            rootView!!.ll_usdt_section.visibility = View.GONE
            rootView!!.ll_online_payment_gateway_section.visibility = View.GONE
            rootView!!.cv_topup_funds.visibility = View.GONE
        }
        val webSettings = rootView!!.webviewUsdtCondition.settings
        webSettings.defaultFontSize = 12
        if (homeController.oldDashboardModel == null) {
            /**in this screen we are showing on condition based which coming
             *  from dashboard api so if this is null then need to hide main layout*/
            rootView!!.ll_main.visibility = View.GONE
            return
        }

        /**if kyc not done need to give no access to wallets*/  //commenting due to demo2 no include KYC process
        /*if (homeController!!.oldDashboardModel!!.payload!!.kycStatus != "2") {
            rootView!!.ll_main.visibility = View.GONE
            val builder =
                android.app.AlertDialog.Builder(activity!!, R.style.MyDialogTheme)
            builder.setMessage(
                HtmlFormatter.formatHtml(
                    HtmlFormatterBuilder().setHtml(homeController!!.oldDashboardModel!!.payload!!.kycStatusMsg).setImageGetter(
                        HtmlResImageGetter(activity!!)
                    )
                )
            )
            builder.setCancelable(false)

            builder.setPositiveButton(getString(R.string.ok),
                DialogInterface.OnClickListener { dialog, which ->
                    homeController.loadFragment(
                        KycFragment(),
                        "KycFragment",
                        this.javaClass.simpleName
                    )
                })
            builder.create().apply {

                this.setOnShowListener {
                    this.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(ContextCompat.getColor(activity!!, R.color.black))

                }
            }.show()
        } else {*/
        rootView!!.edt_transfer_downline_amount_usd.filters = arrayOf<InputFilter>(
            DigitsInputFilter(
                Constants.maxIntCount,
                Constants.amountDecimal,
                Constants.maxAmount
            )
        )
        rootView!!.edt_usdt_amount_usd.filters = arrayOf<InputFilter>(
            DigitsInputFilter(
                Constants.maxIntCount,
                Constants.amountDecimal,
                Constants.maxAmount
            )
        )
        rootView!!.edt_amount_usd.filters = arrayOf<InputFilter>(
            DigitsInputFilter(
                Constants.maxIntCount,
                Constants.amountDecimal,
                Constants.maxAmount
            )
        )
        setupBiometricSelection(
            methodSelectionLayout = rootView!!.layout_method_selection,
            securityPasswordLayout = rootView!!.ll_security_password,
            radioBiometric = rootView!!.rb_biometric,
            radioSecurityPassword = rootView!!.rb_security_password,
            radioGroupSelection = rootView!!.rg_selection
        )
        setupBiometricSelection(
            methodSelectionLayout = rootView!!.layout_method_selection_online,
            securityPasswordLayout = rootView!!.ll_security_password_online,
            radioBiometric = rootView!!.rb_biometric_online,
            radioSecurityPassword = rootView!!.rb_security_password_online,
            radioGroupSelection = rootView!!.rg_selection_online
        )

        setupBiometricSelection(
            methodSelectionLayout = rootView!!.layout_method_selection_dt,
            securityPasswordLayout = rootView!!.ll_security_password_dt,
            radioBiometric = rootView!!.rb_biometric_dt,
            radioSecurityPassword = rootView!!.rb_security_password_dt,
            radioGroupSelection = rootView!!.rg_selection_dt
        )

        setupBiometricSelection(
            methodSelectionLayout = rootView!!.layout_method_selection_transfer_downline,
            securityPasswordLayout = rootView!!.ll_security_password_transfer_downline,
            radioBiometric = rootView!!.rb_biometric_transfer_downline,
            radioSecurityPassword = rootView!!.rb_security_password_transfer_downline,
            radioGroupSelection = rootView!!.rg_selection_transfer_downline
        )


        addObservers()
        myMainViewModel?.getFundTopupHistory(0)
        //  }


    }

    private fun addListeners() {
        rootView!!.cvVarifyUserName.setOnClickListener {
            if (rootView!!.edt_sponser_username.text.toString().trim().isEmpty()) {
                Toast.makeText(
                    activity!!,
                    getString(R.string.validation_sponser_username),
                    Toast.LENGTH_SHORT
                ).show()

                rootView!!.scroll.myRequestFocus(rootView!!.edt_sponser_username)
            } else {
                myMainViewModel!!.varifyDownlineSponserCall(
                    name = rootView!!.edt_sponser_username.text.toString().trim()
                )
            }
        }
        rootView!!.edt_sponser_username.addTextChangedListener {
            sponserUserVerified = false
        }

        //online
        rootView!!.edt_usdt_amount_usd.addTextChangedListener {
            if (rootView!!.edt_usdt_amount_usd.text.toString().trim().isNotEmpty()) {

                rootView!!.edt_myr_amount_usd.text = "${
                    parseDouble(
                        (parseDouble(fundTopupHistoryModel!!.payload!!.conversionRate!!).removeComma()
                            .toDouble()
                                * parseDouble(
                            rootView!!.edt_usdt_amount_usd.text.toString().trim()
                        ).removeComma().toDouble()
                                ).toString()
                    )
                }"
            } else {
                rootView!!.edt_myr_amount_usd.text = "0.00"
            }

        }

        //Direct transfer
        rootView!!.edt_dt_amount_usd.addTextChangedListener {
            if (rootView!!.edt_dt_amount_usd.text.toString().trim().isNotEmpty()) {

                rootView!!.edt_myr_amount_usd_dt.text = "${
                    parseDouble(
                        (parseDouble(fundTopupHistoryModel!!.payload!!.conversionRate!!).removeComma()
                            .toDouble()
                                * parseDouble(
                            rootView!!.edt_dt_amount_usd.text.toString().trim()
                        ).removeComma().toDouble()
                                ).toString()
                    )
                }"
            } else {
                rootView!!.edt_myr_amount_usd_dt.text = "0.00"
            }

        }

        //3rd party
        rootView!!.edt_amount_usd_thirdparty.addTextChangedListener {
            if (rootView!!.edt_amount_usd_thirdparty.text.toString().trim().isNotEmpty()) {

                rootView!!.edt_myr_amount_usd_thirdparty.text = "${
                    parseDouble(
                        (parseDouble(fundTopupHistoryModel!!.payload!!.conversionRate!!).removeComma()
                            .toDouble()
                                * parseDouble(
                            rootView!!.edt_amount_usd_thirdparty.text.toString().trim()
                        ).removeComma().toDouble()
                                ).toString()
                    )
                }"
            } else {
                rootView!!.edt_myr_amount_usd_thirdparty.text = "0.00"
            }

        }

        rootView!!.rl_upload.setOnClickListener {
            selectedImage = 0
            callPermissions()
        }

        rootView!!.rl_upload_thirdparty.setOnClickListener {
            selectedImage = 1
            callPermissions()
        }

        /**new pagination handling with offset*/
        rootView!!.scroll.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (v.getChildAt(v.childCount - 1) != null) {
                if ((scrollY >= (v.getChildAt(v.childCount - 1).measuredHeight - v.measuredHeight))
                    && scrollY > oldScrollY
                ) {
                    if (!fundTopupHistoryModel!!.paginationEnded) {
                        myMainViewModel?.getFundTopupHistory(fundTopupHistoryModel!!.payload!!.history!!.size)

                    }

                }
            }
        })

        rootView!!.cv_topup_funds.setOnClickListener {

            if (currentSelectedBankUSDTTab == Constants.TYPE_USDT) {

                if (rootView!!.edt_amount_usd.text.toString().trim().isEmpty()) {

                    Toast.makeText(
                        activity!!,
                        getString(R.string.amount_validation),
                        Toast.LENGTH_SHORT
                    ).show()

                    rootView!!.scroll.myRequestFocus(rootView!!.edt_amount_usd)
                } else if (rootView!!.edt_amount_usd.text.toString().trim() == ".") {
                    Toast.makeText(
                        activity!!,
                        getString(R.string.valid_amount_validation),
                        Toast.LENGTH_SHORT
                    ).show()

                    rootView!!.scroll.myRequestFocus(rootView!!.edt_amount_usd)
                } else if (rootView!!.edt_amount_usd.text.toString().trim().removeComma()
                        .toDouble() <= 0
                ) {

                    Toast.makeText(
                        activity!!,
                        getString(R.string.valid_amount_validation),
                        Toast.LENGTH_SHORT
                    ).show()

                    rootView!!.scroll.myRequestFocus(rootView!!.edt_amount_usd)
                } else if (mSelectionPath == null || mSelectionPath!!.isEmpty()) {

                    Toast.makeText(
                        activity!!,
                        getString(R.string.bank_proof_validation),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } else if (rootView!!.edt_security_password.text.toString().trim()
                        .isEmpty() && rootView!!.rb_security_password.isChecked
                ) {

                    Toast.makeText(
                        activity!!,
                        getString(R.string.security_password_validation),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    rootView!!.scroll.myRequestFocus(rootView!!.edt_security_password)

                } else { //change type when update from api like usdt and online payment gateway
                    /**implement here of transfer funds*/
                    homeController.hideSoftKeyboard()
                    if (rootView!!.rb_biometric.isChecked) {
                        /**biometric auth*/
                        showBiometric(biometricCallback = object : BiometricCallback {
                            override fun onSuccess(result: BiometricPrompt.AuthenticationResult) {
                                /**success*/
                                var receipt: MultipartBody.Part? = null
                                receipt =
                                    if (selectedType == codeCameraRequest || selectedType == codePickImageRequest) {
                                        prepareImageFilePart("bank_proof", mSelectionPath!!)
                                    } else {
                                        preparePdfFilePart("bank_proof", mSelectionPath!!)
                                    }

                                homeController.hideSoftKeyboard()

                                myMainViewModel!!.topupFundUSDTWithUUID(
                                    amount = edt_amount_usd.text.toString().trim()
                                        .toRequestBody("text/plain".toMediaTypeOrNull()),
                                    securityPassword = userModel.payload!!.user!!.fingerUUID.toString()
                                        .trim()
                                        .toRequestBody(
                                            "text/plain".toMediaTypeOrNull()
                                        ),
                                    type = currentSelectedBankUSDTTab.toRequestBody("text/plain".toMediaTypeOrNull()),
                                    authType = Constants.TransactionUUID.toRequestBody("text/plain".toMediaTypeOrNull()),
                                    usdtAddress = usdtAddressValue!!.toRequestBody("text/plain".toMediaTypeOrNull()),
                                    receipt = receipt!!
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
                        var receipt: MultipartBody.Part? = null
                        receipt =
                            if (selectedType == codeCameraRequest || selectedType == codePickImageRequest) {
                                prepareImageFilePart("bank_proof", mSelectionPath!!)
                            } else {
                                preparePdfFilePart("bank_proof", mSelectionPath!!)
                            }

                        homeController.hideSoftKeyboard()
                        myMainViewModel!!.topupFundUSDT(
                            amount = edt_amount_usd.text.toString().trim()
                                .toRequestBody("text/plain".toMediaTypeOrNull()),
                            securityPassword = edt_security_password.text.toString().trim()
                                .toRequestBody(
                                    "text/plain".toMediaTypeOrNull()
                                ),
                            type = currentSelectedBankUSDTTab.toRequestBody("text/plain".toMediaTypeOrNull()),
                            usdtAddress = usdtAddressValue!!.toRequestBody("text/plain".toMediaTypeOrNull()),
                            receipt = receipt!!
                        )
                    }
                }
            }
            if (currentSelectedBankUSDTTab == Constants.TYPE_ONLINE) {

                if (rootView!!.edt_usdt_amount_usd.text.toString().trim().isEmpty()) {

                    Toast.makeText(
                        activity!!,
                        getString(R.string.amount_validation),
                        Toast.LENGTH_SHORT
                    ).show()

                    rootView!!.scroll.myRequestFocus(rootView!!.edt_usdt_amount_usd)
                } else if (rootView!!.edt_usdt_amount_usd.text.toString().trim().toDouble() <= 0) {

                    Toast.makeText(
                        activity!!,
                        getString(R.string.valid_amount_validation),
                        Toast.LENGTH_SHORT
                    ).show()

                    rootView!!.scroll.myRequestFocus(rootView!!.edt_usdt_amount_usd)
                } else if (fundTopupHistoryModel!!.payload!!.bankList!!.isEmpty()) {
                    Toast.makeText(
                        activity!!,
                        getString(R.string.validation_bank),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
//                else if (bankDetail == "1" && rootView!!.edt_bank_account_number!!.text.toString()
//                        .trim().isEmpty()
//                ) {
//                    Toast.makeText(
//                        activity!!,
//                        getString(R.string.validation_account_no),
//                        Toast.LENGTH_SHORT
//                    )
//                        .show()
//                    rootView!!.scroll.myRequestFocus(rootView!!.edt_bank_account_number)
//                }
                /*else if (bankDetail == "1" && rootView!!.edt_bank_account_holder_name!!.text.toString()
                        .trim().isEmpty()
                ) {
                    Toast.makeText(
                        activity!!,
                        getString(R.string.validation_bank_holder),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    rootView!!.scroll.myRequestFocus(rootView!!.edt_bank_account_holder_name)
                } */
                else if (rootView!!.edt_usdt_security_password.text.toString().trim()
                        .isEmpty() && rootView!!.rb_security_password_online.isChecked
                ) {

                    Toast.makeText(
                        activity!!,
                        getString(R.string.security_password_validation),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    rootView!!.scroll.myRequestFocus(rootView!!.edt_usdt_security_password)

                }/* else if (rootView!!.edt_security_password.text.toString().trim().length < 8) {

                Toast.makeText(
                    activity!!,
                    getString(R.string.validation_min_security_password),
                    Toast.LENGTH_SHORT
                )
                    .show()
                rootView!!.scroll.myRequestFocus(rootView!!.edt_security_password)
            }*/ else {
                    /**implement here of transfer funds*/
                    homeController.hideSoftKeyboard()
                    if (rootView!!.rb_biometric_online.isChecked) {
                        /**biometric auth*/
                        showBiometric(biometricCallback = object : BiometricCallback {
                            override fun onSuccess(result: BiometricPrompt.AuthenticationResult) {
                                /**success*/
                                myMainViewModel!!.topupFundOnlineUUID(
                                    amount = edt_usdt_amount_usd.text.toString().trim(),
                                    securityPassword = userModel.payload!!.user!!.fingerUUID.toString()
                                        .trim(),
                                    type = currentSelectedBankUSDTTab,
                                    bank_id = fundTopupHistoryModel!!.payload!!.bankList!![rootView!!.sp_your_bank_list!!.selectedItemPosition].code!!,
                                    bank_amount = edt_myr_amount_usd.text.toString(),
                                    authType = Constants.TransactionUUID,
                                    bankDetail = bankDetail
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
                        myMainViewModel!!.topupFundOnline(
                            amount = edt_usdt_amount_usd.text.toString().trim(),
                            securityPassword = edt_usdt_security_password.text.toString().trim(),
                            type = currentSelectedBankUSDTTab,
                            bank_id = fundTopupHistoryModel!!.payload!!.bankList!![rootView!!.sp_your_bank_list!!.selectedItemPosition].code!!,
                            bank_amount = edt_myr_amount_usd.text.toString(),
                            bankDetail = bankDetail
                        )
                    }
                }
            }

            if (currentSelectedBankUSDTTab == Constants.TYPE_DIRECT_TRANSFER) {

                if (rootView!!.edt_dt_amount_usd.text.toString().trim().isEmpty()) {
                    Toast.makeText(
                        activity!!,
                        getString(R.string.amount_validation),
                        Toast.LENGTH_SHORT
                    ).show()

                    rootView!!.scroll.myRequestFocus(rootView!!.edt_dt_amount_usd)

                } else if (rootView!!.edt_dt_amount_usd.text.toString().trim().toDouble() <= 0) {
                    Toast.makeText(
                        activity!!,
                        getString(R.string.valid_amount_validation),
                        Toast.LENGTH_SHORT
                    ).show()

                    rootView!!.scroll.myRequestFocus(rootView!!.edt_dt_amount_usd)

                } else if (rootView!!.edt_dt_security_password.text.toString().trim()
                        .isEmpty() && rootView!!.rb_security_password_dt.isChecked
                ) {

                    Toast.makeText(
                        activity!!,
                        getString(R.string.security_password_validation),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    rootView!!.scroll.myRequestFocus(rootView!!.edt_dt_security_password)

                } else {
                    /**implement here of transfer funds*/
                    homeController.hideSoftKeyboard()
                    if (rootView!!.rb_biometric_dt.isChecked) {
                        /**biometric auth*/
                        showBiometric(biometricCallback = object : BiometricCallback {
                            override fun onSuccess(result: BiometricPrompt.AuthenticationResult) {
                                /**success*/
                                myMainViewModel!!.topupFundDirectTransfer(
                                    amount = edt_dt_amount_usd.text.toString().trim(),
                                    securityPassword = userModel.payload!!.user!!.fingerUUID.toString()
                                        .trim(),
                                    type = currentSelectedBankUSDTTab,
                                    bank_amount = edt_myr_amount_usd_dt.text.toString(),
                                    authType = Constants.TransactionUUID
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
                        myMainViewModel!!.topupFundDirectTransfer(
                            amount = edt_dt_amount_usd.text.toString().trim(),
                            securityPassword = edt_dt_security_password.text.toString().trim(),
                            type = currentSelectedBankUSDTTab,
                            bank_amount = edt_myr_amount_usd_dt.text.toString(),
                            authType = Constants.TransactionNormal
                        )
                    }
                }
            }

            if (currentSelectedBankUSDTTab == Constants.TYPE_BANK_3RD_PARTY) {
                if (rootView!!.edt_amount_usd_thirdparty.text.toString().trim().isEmpty()) {
                    Toast.makeText(
                        activity!!,

                        getString(R.string.amount_validation),
                        Toast.LENGTH_SHORT
                    ).show()

                    rootView!!.scroll.myRequestFocus(rootView!!.edt_amount_usd_thirdparty)
                } else if (rootView!!.edt_amount_usd_thirdparty.text.toString().trim()
                        .toDouble() <= 0
                ) {

                    Toast.makeText(
                        activity!!,
                        getString(R.string.valid_amount_validation),
                        Toast.LENGTH_SHORT
                    ).show()

                    rootView!!.scroll.myRequestFocus(rootView!!.edt_amount_usd_thirdparty)
                } else if (rootView!!.edt_security_password_thirdparty.text.toString().trim()
                        .isEmpty() && rootView!!.rb_security_password_online_thirdparty.isChecked
                ) {
                    Toast.makeText(
                        activity!!,
                        getString(R.string.security_password_validation),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    rootView!!.scroll.myRequestFocus(rootView!!.edt_security_password_thirdparty)

                } else if (mSelectionPathThirdParty == null || mSelectionPathThirdParty!!.isEmpty()) {

                    Toast.makeText(
                        activity!!,
                        getString(R.string.bank_proof_validation),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } else {
                    homeController.hideSoftKeyboard()
                    if (rootView!!.rb_biometric_online_thirdparty.isChecked) {
                        showBiometric(biometricCallback = object : BiometricCallback {
                            override fun onSuccess(result: BiometricPrompt.AuthenticationResult) {
                                /**success*/
                                var receipt: MultipartBody.Part? = null
                                receipt =
                                    if (selectedType == codeCameraRequest || selectedType == codePickImageRequest) {
                                        prepareImageFilePart(
                                            "bank_proof",
                                            mSelectionPathThirdParty!!
                                        )
                                    } else {
                                        preparePdfFilePart("bank_proof", mSelectionPathThirdParty!!)
                                    }

                                homeController.hideSoftKeyboard()

                                myMainViewModel!!.topupFundUSDTWithUUID(
                                    amount = edt_amount_usd_thirdparty.text.toString().trim()
                                        .toRequestBody("text/plain".toMediaTypeOrNull()),
                                    securityPassword = userModel.payload!!.user!!.fingerUUID.toString()
                                        .trim()
                                        .toRequestBody(
                                            "text/plain".toMediaTypeOrNull()
                                        ),
                                    type = currentSelectedBankUSDTTab.toRequestBody("text/plain".toMediaTypeOrNull()),
                                    authType = Constants.TransactionUUID.toRequestBody("text/plain".toMediaTypeOrNull()),
                                    usdtAddress = "".toRequestBody("text/plain".toMediaTypeOrNull()),
                                    receipt = receipt!!
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
                        var receipt: MultipartBody.Part? = null
                        receipt =
                            if (selectedType == codeCameraRequest || selectedType == codePickImageRequest) {
                                prepareImageFilePart("bank_proof", mSelectionPathThirdParty!!)
                            } else {
                                preparePdfFilePart("bank_proof", mSelectionPathThirdParty!!)
                            }

                        homeController.hideSoftKeyboard()
                        myMainViewModel!!.topupFundUSDT(
                            amount = edt_amount_usd_thirdparty.text.toString().trim()
                                .toRequestBody("text/plain".toMediaTypeOrNull()),
                            securityPassword = edt_security_password_thirdparty.text.toString()
                                .trim()
                                .toRequestBody(
                                    "text/plain".toMediaTypeOrNull()
                                ),
                            type = currentSelectedBankUSDTTab.toRequestBody("text/plain".toMediaTypeOrNull()),
                            usdtAddress = "".toRequestBody("text/plain".toMediaTypeOrNull()),
                            receipt = receipt!!
                        )
                    }
                }

            }
        }

        rootView!!.cv_transfer_downline.setOnClickListener {
            if (rootView!!.edt_sponser_username.text.toString().trim().isEmpty()) {
                Toast.makeText(
                    activity!!,
                    getString(R.string.validation_sponser_username),
                    Toast.LENGTH_SHORT
                ).show()

                rootView!!.scroll.myRequestFocus(rootView!!.edt_sponser_username)
            } else if (rootView!!.edt_transfer_downline_amount_usd.text.toString().trim()
                    .isEmpty()
            ) {

                Toast.makeText(
                    activity!!,
                    getString(R.string.amount_validation),
                    Toast.LENGTH_SHORT
                ).show()

                rootView!!.scroll.myRequestFocus(rootView!!.edt_transfer_downline_amount_usd)
            } else if (rootView!!.edt_transfer_downline_amount_usd.text.toString().trim()
                    .toDouble() <= 0
            ) {

                Toast.makeText(
                    activity!!,
                    getString(R.string.valid_amount_validation),
                    Toast.LENGTH_SHORT
                ).show()

                rootView!!.scroll.myRequestFocus(rootView!!.edt_transfer_downline_amount_usd)
            } else if (rootView!!.edt_security_password_transfer_downline.text.toString().trim()
                    .isEmpty() && rootView!!.rb_security_password_transfer_downline.isChecked
            ) {

                Toast.makeText(
                    activity!!,
                    getString(R.string.security_password_validation),
                    Toast.LENGTH_SHORT
                )
                    .show()
                rootView!!.scroll.myRequestFocus(rootView!!.edt_security_password_transfer_downline)

            }/* else if (rootView!!.edt_security_password.text.toString().trim().length < 8) {

                Toast.makeText(
                    activity!!,
                    getString(R.string.validation_min_security_password),
                    Toast.LENGTH_SHORT
                )
                    .show()
                rootView!!.scroll.myRequestFocus(rootView!!.edt_security_password)
            }*/ else {
                /**implement here of transfer funds*/
                homeController.hideSoftKeyboard()
                if (rootView!!.rb_biometric_transfer_downline.isChecked) {
                    /**biometric auth*/
                    showBiometric(biometricCallback = object : BiometricCallback {
                        override fun onSuccess(result: BiometricPrompt.AuthenticationResult) {
                            /**success*/
                            if (currentSelectedTransferTab == typeTransferDownlineOTM) {
                                /**implement here of typeTransferDownlineOTM*/
                                myMainViewModel!!.transferToDownlineOTMWithUUID(
                                    amount = rootView!!.edt_transfer_downline_amount_usd.text.toString()
                                        .trim(),
                                    sponserName = rootView!!.edt_sponser_username.text.toString()
                                        .trim(),
                                    uuid = userModel.payload!!.user!!.fingerUUID.toString().trim(),
                                    isVerified = sponserUserVerified
                                )
                            }
                            if (currentSelectedTransferTab == typeTransferDownlinesUplines) {
                                /**implement here of typeTransferDownlinesUplines*/
                                myMainViewModel!!.transferToDownlineUplineWithUUID(
                                    amount = rootView!!.edt_transfer_downline_amount_usd.text.toString()
                                        .trim(),
                                    sponserName = rootView!!.edt_sponser_username.text.toString()
                                        .trim(),
                                    uuid = userModel.payload!!.user!!.fingerUUID.toString().trim(),
                                    type = rootView!!.sp_transfer_for.selectedItemPosition.toString(),
                                    isVerified = sponserUserVerified
                                )
                            }
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
                    if (currentSelectedTransferTab == typeTransferDownlineOTM) {
                        /**implement here of typeTransferDownlineOTM*/
                        myMainViewModel!!.transferToDownlineOTM(
                            amount = rootView!!.edt_transfer_downline_amount_usd.text.toString()
                                .trim(),
                            sponserName = rootView!!.edt_sponser_username.text.toString().trim(),
                            securityPassword = rootView!!.edt_security_password_transfer_downline.text.toString()
                                .trim(),
                            isVerified = sponserUserVerified
                        )
                    }
                    if (currentSelectedTransferTab == typeTransferDownlinesUplines) {
                        /**implement here of typeTransferDownlinesUplines*/
                        myMainViewModel!!.transferToDownlineUpline(
                            amount = rootView!!.edt_transfer_downline_amount_usd.text.toString()
                                .trim(),
                            sponserName = rootView!!.edt_sponser_username.text.toString().trim(),
                            securityPassword = rootView!!.edt_security_password_transfer_downline.text.toString()
                                .trim(),
                            type = rootView!!.sp_transfer_for.selectedItemPosition.toString(),
                            isVerified = sponserUserVerified
                        )
                    }
                }
            }
        }

        /**clicks of bank usdt tabs*/

        /*rootView!!.sp_topup_funds.setOnTouchListener { v, event ->
            hideSoftKeyboard()
            activity!!.currentFocus?.clearFocus()
            false
        }*/
        /*rootView!!.sp_topup_funds.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {
                }

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    rootView!!.sp_topup_funds.adapter.let {
                        var x: HighLightArrayAdapterV2 = it as HighLightArrayAdapterV2
                        x.setSelection(p2)
                        var name = rootView!!.sp_topup_funds.selectedItem.toString()
                        Log.e("TestPositionEntry", "***  " + p2 + "n==" + name)
                        rootView!!.cv_usdt_terms_conditions.visibility = View.GONE
                        if (name == getString(R.string.usdt)) {
                            rootView!!.ll_usdt_section.visibility = View.VISIBLE
                            rootView!!.ll_online_payment_gateway_section.visibility = View.GONE
                            rootView!!.ll_thirdparty_section.visibility = View.GONE
                            if (fundTopupHistoryModel != null) {

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) Pref.setLocale(
                                    activity!!,
                                    Pref.getLocalization(activity!!)
                                )
                                rootView!!.webviewUsdtCondition.loadDataWithBaseURL(
                                    null,
                                    fundTopupHistoryModel!!.payload!!.usdtTerms!!,
                                    "text/html",
                                    "UTF-8",
                                    null
                                );

                                *//*rootView!!.webviewUsdtCondition.setWebViewClient(object : WebViewClient() {
                                    override fun onPageStarted(
                                        view: WebView?,
                                        url: String?,
                                        favicon: Bitmap?
                                    ) {
                                        super.onPageStarted(view, url, favicon)
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            Pref.setLocale(activity!!, Pref.getLocalization(activity!!))
                                        }
                                    }

                                    override fun onPageFinished(
                                        view: WebView,
                                        url: String
                                    ) {
                                        super.onPageFinished(view, url)
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            Pref.setLocale(activity!!, Pref.getLocalization(activity!!))
                                        }
                                    }
                                })*//*

                                rootView!!.webviewUsdtCondition.webViewClient =
                                    object : WebViewClient() {
                                        override fun shouldOverrideUrlLoading(
                                            view: WebView?,
                                            url: String?
                                        ): Boolean {
                                            url?.let { it1 -> view?.loadUrl(it1) }
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) Pref.setLocale(
                                                activity!!,
                                                Pref.getLocalization(activity!!)
                                            )
                                            return true
                                        }
                                    }

                            }

                            //rootView!!.tv_usdt_terms_conditions.setHtmlData(fundTopupHistoryModel!!.payload!!.usdtTerms!!)
                            setupBiometricSelection(
                                methodSelectionLayout = rootView!!.layout_method_selection,
                                securityPasswordLayout = rootView!!.ll_security_password,
                                radioBiometric = rootView!!.rb_biometric,
                                radioSecurityPassword = rootView!!.rb_security_password,
                                radioGroupSelection = rootView!!.rg_selection
                            )
                            rootView!!.cv_usdt_terms_conditions.visibility = View.VISIBLE
                            rootView!!.tv_tand_C.visibility = View.VISIBLE
                            rootView!!.tv_terms_conditions.visibility = View.VISIBLE

                            currentSelectedBankUSDTTab = typeUSDT
                        } else if (name == getString(R.string.direct_transfer)) {
                            bankDetail = "0"
                            rootView!!.ll_usdt_section.visibility = View.GONE
                            rootView!!.ll_online_payment_gateway_section.visibility = View.VISIBLE
                            rootView!!.ll_thirdparty_section.visibility = View.GONE
                            rootView!!.tv_tand_C.visibility = View.VISIBLE
                            rootView!!.tv_terms_conditions.visibility = View.VISIBLE
                            for (i in fundTopupHistoryModel!!.payload!!.fundOption!!) {
                                if (i!!.text == rootView!!.sp_topup_funds.selectedItem) {
                                    if (i!!.bankDetail == "0") {
                                        rootView!!.ll_bank_account_number!!.visibility = View.GONE
                                        rootView!!.ll_bank_account_holder_name!!.visibility = View.GONE
                                    } else {
                                        bankDetail = "1"
                                        rootView!!.ll_Bank_detail.visibility = View.GONE
                                        rootView!!.ll_bank_account_number!!.visibility = View.GONE
                                        rootView!!.ll_bank_account_holder_name!!.visibility = View.GONE

//                                        rootView!!.ll_bank_account_number!!.visibility = View.VISIBLE
//                                        rootView!!.ll_bank_account_holder_name!!.visibility = View.VISIBLE
                                    }
                                }
                            }
                            setupBiometricSelection(
                                methodSelectionLayout = rootView!!.layout_method_selection_online,
                                securityPasswordLayout = rootView!!.ll_security_password_online,
                                radioBiometric = rootView!!.rb_biometric_online,
                                radioSecurityPassword = rootView!!.rb_security_password_online,
                                radioGroupSelection = rootView!!.rg_selection_online
                            )
                            currentSelectedBankUSDTTab = typeOnlinePayment
                        } else if (name == getString(R.string.third_party_payment)) {
                            rootView!!.ll_usdt_section.visibility = View.GONE
                            rootView!!.ll_online_payment_gateway_section.visibility = View.GONE
                            rootView!!.ll_thirdparty_section.visibility = View.VISIBLE

                            rootView!!.tv_tand_C.visibility = View.VISIBLE
                            rootView!!.tv_terms_conditions.visibility = View.VISIBLE
                            rootView!!.cv_usdt_terms_conditions.visibility = View.VISIBLE

                            setupBiometricSelection(
                                methodSelectionLayout = rootView!!.layout_method_selection_online_thirdparty,
                                securityPasswordLayout = rootView!!.ll_security_password_thirdparty,
                                radioBiometric = rootView!!.rb_biometric_online_thirdparty,
                                radioSecurityPassword = rootView!!.rb_security_password_online_thirdparty,
                                radioGroupSelection = rootView!!.rg_selection_online_thirdparty
                            )
                            currentSelectedBankUSDTTab = thirdPartyPayment
                        }
                    }
                }
            }*/

        rootView!!.sp_your_bank_list.setOnTouchListener { v, event ->
            hideSoftKeyboard()
            activity!!.currentFocus?.clearFocus()
            false
        }


        /**clicks of history tabs*/
        rootView!!.ln_fund_topup_history.setOnClickListener {
            if (currentSelectedTab == typeFundTopupHistory) {
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
            unSelectHistoryOptions()
            selectViewTab(rootView!!.view_fund_topup_history, rootView!!.tv_fund_topup_history)
            currentSelectedTab = typeFundTopupHistory

            if (fundTopupHistoryModel != null) {
                rootView!!.rv_history.adapter = FundTopupHistoryAdapter(
                    activity!!,
                    fundTopupHistoryModel!!.payload!!.history,
                    this@FundWallets
                )
            }

        }

        /*rootView!!.ivUDSTQRCode.setOnClickListener {
              homeController.showQRCodeDialog(usdtQrCodeUrl)
            }*/

        rootView!!.tvClickToCopy.setOnClickListener {
            val clipboard: ClipboardManager =
                activity!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            if (clipboard != null) {
                val clip = ClipData.newPlainText(
                    "UsdtAddress",
                    rootView!!.etUSDTAdress.text.toString().trim()
//                        userModel!!.payload!!.user!!.shareLink
                )
                clipboard.setPrimaryClip(clip)
                Toast.makeText(
                    activity!!,
                    getString(R.string.copied_to_clipboard),
                    Toast.LENGTH_SHORT
                ).show();
            }
        }

        rootView!!.spUsdtAddress.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {
                }

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    rootView!!.spUsdtAddress.adapter.let {
                        var x: HighLightArrayAdapterV2 = it as HighLightArrayAdapterV2
                        x.setSelection(p2)
                    }

                    var usdtAddressh = fundTopupHistoryModel?.payload?.usdtaddress?.get(p2)!!
                    if (usdtAddressh != null) {
                        usdtAddressValue = "" + usdtAddressh?.value
                        usdtQrCodeUrl = usdtAddressh.image
                        rootView!!.etUSDTAdress.setText(usdtAddressValue)
                        Glide.with(activity!!).load(usdtQrCodeUrl).into(rootView!!.ivUDSTQRCode)

                    } else {
                    }
//                    setUSDTAddressdata(usdtAddressh);
                }
            }
    }

    private fun addObservers() {
        myMainViewModel!!.isLoading?.observe(this@FundWallets, Observer {
            if (it) {
                homeController.dismissSnackbar()
                homeController.showProgressDialog()
            } else {
                homeController.dismissProgressDialog()

            }
        })
        myMainViewModel!!.responseError?.observe(this@FundWallets, Observer {
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
                    setupBiometricSelection(
                        methodSelectionLayout = rootView!!.layout_method_selection_online,
                        securityPasswordLayout = rootView!!.ll_security_password_online,
                        radioBiometric = rootView!!.rb_biometric_online,
                        radioSecurityPassword = rootView!!.rb_security_password_online,
                        radioGroupSelection = rootView!!.rg_selection_online
                    )

                    setupBiometricSelection(
                        methodSelectionLayout = rootView!!.layout_method_selection_dt,
                        securityPasswordLayout = rootView!!.ll_security_password_dt,
                        radioBiometric = rootView!!.rb_biometric_dt,
                        radioSecurityPassword = rootView!!.rb_security_password_dt,
                        radioGroupSelection = rootView!!.rg_selection_dt
                    )

                    setupBiometricSelection(
                        methodSelectionLayout = rootView!!.layout_method_selection_transfer_downline,
                        securityPasswordLayout = rootView!!.ll_security_password_transfer_downline,
                        radioBiometric = rootView!!.rb_biometric_transfer_downline,
                        radioSecurityPassword = rootView!!.rb_security_password_transfer_downline,
                        radioGroupSelection = rootView!!.rg_selection_transfer_downline
                    )


                } else if (jsonObject.optInt("code") == 406) {
                    homeController.showSnackbar(
                        homeController.llBottomNew,
                        jsonObject.getString("message"),
                        30000
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
        myMainViewModel!!.varifyResponseModel?.observe(this@FundWallets, Observer {
            sponserUserVerified = true
            homeController.messageToast(it)

        })

        myMainViewModel!!.cancelFundRequestModel?.observe(this@FundWallets, Observer {
            homeController.message("" + it.message!!)
            if (it.success!!) {
                fundTopupHistoryModel?.payload?.history?.clear()
                myMainViewModel?.fundTopupHistoryModel?.value?.payload?.history?.clear()
                /*for (i in fundTopupHistoryModel!!.payload!!.fundOption!!.indices) {
                    fundTopupHistoryModel!!.payload!!.fundOption!![i].selected = false
                }*/
                myMainViewModel?.getFundTopupHistory(0)
            }
        })

        myMainViewModel!!.fundTopupHistoryModel?.observe(this@FundWallets, Observer {
            fundTopupHistoryModel = it
            updateUi()
        })
        myMainViewModel!!.walletHistoryModel?.observe(this@FundWallets, Observer {

            if (walletHistoryModel == null) {
                walletHistoryModel = it

                /**as api called first time need to set new adapter*/
                walletHistoryModel!!.payload!!.history?.let {
                    rootView!!.rv_history.adapter = FundWalletHistoryAdapter(
                        activity!!,
                        walletHistoryModel!!.payload!!.history,
                        this@FundWallets
                    )
                }
            } else {

                walletHistoryModel = it

                walletHistoryModel!!.payload!!.history?.let {

                    (rootView!!.rv_history.adapter as FundWalletHistoryAdapter?)?.let { adapter ->
                        /**already adapter set just need to notify*/
                        adapter.notifyItemRangeInserted(
                            adapter.lastPosition + 1,
                            it.size - (adapter.lastPosition + 1)
                        )
                    } ?: run {
                        /**need to set new adapter*/
                        rootView!!.rv_history.adapter = FundWalletHistoryAdapter(
                            activity!!,
                            walletHistoryModel!!.payload!!.history,
                            this@FundWallets
                        )
                    }
                }


            }


        })
        myMainViewModel!!.mt4TopupHistoryModel?.observe(this@FundWallets, Observer {

            if (mt4TopupHistoryModel == null) {
                mt4TopupHistoryModel = it

                /**as api called first time need to set new adapter*/
                mt4TopupHistoryModel!!.payload!!.history?.let {
                    rootView!!.rv_history.adapter = MT4TopupHistoryAdapter(
                        activity!!,
                        mt4TopupHistoryModel!!.payload!!.history,
                        this@FundWallets
                    )
                }
            } else {

                mt4TopupHistoryModel = it

                mt4TopupHistoryModel!!.payload!!.history?.let {

                    (rootView!!.rv_history.adapter as MT4TopupHistoryAdapter?)?.let { adapter ->
                        /**already adapter set just need to notify*/
                        adapter.notifyItemRangeInserted(
                            adapter.lastPosition + 1,
                            it.size - (adapter.lastPosition + 1)
                        )
                    } ?: run {
                        /**need to set new adapter*/
                        rootView!!.rv_history.adapter = MT4TopupHistoryAdapter(
                            activity!!,
                            mt4TopupHistoryModel!!.payload!!.history,
                            this@FundWallets
                        )
                    }

                }


            }

        })

        myMainViewModel!!.topupFundResponseModel?.observe(this@FundWallets, Observer {

            rootView!!.edt_sponser_username.setText("")
            rootView!!.edt_sponser_username.clearFocus()
            rootView!!.edt_transfer_downline_amount_usd.setText("")
            rootView!!.edt_transfer_downline_amount_usd.clearFocus()
            rootView!!.edt_security_password_transfer_downline.setText("")
            rootView!!.edt_security_password_transfer_downline.clearFocus()
            rootView!!.sp_transfer_for.setSelection(0)

            if (currentSelectedBankUSDTTab == Constants.TYPE_USDT) {
                rootView!!.edt_amount_usd.setText("")
                rootView!!.edt_amount_usd.clearFocus()
                rootView!!.edt_security_password.setText("")
                rootView!!.edt_security_password.clearFocus()
                currentPhotoPath = null
                mImageBitmap = null
                mSelectionPath = null
                photoFile = null
                selectedType = 0
                rootView!!.image.visibility = View.GONE
                rootView!!.pdfLayout.visibility = View.GONE
            }
            if (currentSelectedBankUSDTTab == Constants.TYPE_BANK_3RD_PARTY) {
                rootView!!.edt_amount_usd_thirdparty.setText("")
                rootView!!.edt_amount_usd_thirdparty.clearFocus()
                rootView!!.edt_security_password_thirdparty.setText("")
                rootView!!.edt_security_password_thirdparty.clearFocus()
                currentPhotoPath = null
                mImageBitmap = null
                mSelectionPathThirdParty = null
                photoFile = null
                selectedType = 0
                rootView!!.imageThirdparty.visibility = View.GONE
                rootView!!.pdfLayoutthirdparty.visibility = View.GONE
            }

            if (currentSelectedBankUSDTTab == Constants.TYPE_ONLINE) {
                rootView!!.edt_usdt_amount_usd.setText("")
                rootView!!.edt_usdt_amount_usd.clearFocus()
                rootView!!.edt_usdt_security_password.setText("")
                rootView!!.edt_usdt_security_password.clearFocus()
            }

            if (currentSelectedBankUSDTTab == Constants.TYPE_DIRECT_TRANSFER) {
                rootView!!.edt_dt_amount_usd.setText("")
                rootView!!.edt_dt_amount_usd.clearFocus()
                rootView!!.edt_dt_security_password.setText("")
                rootView!!.edt_dt_security_password.clearFocus()
            }




            if (it.payload!!.fundWalletBalance!! != null && !it.payload!!.fundWalletBalance!!.isNullOrEmpty()) {
                fundTopupHistoryModel!!.payload!!.fundWalletBalance = it.payload!!.fundWalletBalance

                rootView!!.tv_balance_amount.text =
                    "$${parseDouble(fundTopupHistoryModel!!.payload!!.fundWalletBalance!!)}"
            }
            if (fundTopupHistoryModel!!.payload!!.history == null) {
                fundTopupHistoryModel!!.payload!!.history = ArrayList()
            }
            fundTopupHistoryModel!!.payload!!.history!!.add(0, it.payload!!.history)
            //if (currentSelectedTab == typeFundTopupHistory) {
            (rootView!!.rv_history.adapter as FundTopupHistoryAdapter?)?.let { adapter ->
                /**already adapter set just need to notify*/
                adapter.notifyDataSetChanged()
            } ?: run {
                /**need to set new adapter*/
                rootView!!.rv_history.adapter = FundTopupHistoryAdapter(
                    activity!!,
                    fundTopupHistoryModel!!.payload!!.history,
                    this@FundWallets
                )
            }

            rootView!!.rv_history.visibility =
                if (fundTopupHistoryModel!!.payload!!.history!!.size > 0) View.VISIBLE else View.GONE
            rootView!!.tv_no_data.visibility =
                if (fundTopupHistoryModel!!.payload!!.history!!.size > 0) View.GONE else View.VISIBLE

            // }
            Log.v("===webview===","-"+it.payload!!.webviewUrl.toString());
            if (!it.payload!!.webviewUrl.isNullOrEmpty()) {
                homeController.loadFragment(
                    FundWebviewFragment().apply {
                        this.url = it!!.payload!!.webviewUrl!!
                        this.successURL = it!!.payload!!.successUrl!!
                        this.cancelURL = it!!.payload!!.cancelUrl!!

                    },
                    "FundWebviewFragment",
                    this.javaClass.simpleName
                )
            }

        })
        myMainViewModel!!.transferFundResponseModel?.observe(this@FundWallets, Observer {

            rootView!!.edt_sponser_username.setText("")
            rootView!!.edt_sponser_username.clearFocus()
            rootView!!.edt_transfer_downline_amount_usd.setText("")
            rootView!!.edt_transfer_downline_amount_usd.clearFocus()
            rootView!!.edt_security_password_transfer_downline.setText("")
            rootView!!.edt_security_password_transfer_downline.clearFocus()
            rootView!!.sp_transfer_for.setSelection(0)


            rootView!!.edt_amount_usd.setText("")
            rootView!!.edt_amount_usd.clearFocus()
            rootView!!.edt_security_password.setText("")
            rootView!!.edt_security_password.clearFocus()
            fundTopupHistoryModel!!.payload!!.fundWalletBalance = it.payload!!.fundWalletBalance
            rootView!!.tv_balance_amount.text =
                "$${parseDouble(fundTopupHistoryModel!!.payload!!.fundWalletBalance!!)}"
            if (walletHistoryModel != null) {
                if (walletHistoryModel!!.payload!!.history == null) {
                    walletHistoryModel!!.payload!!.history = ArrayList()
                }
                walletHistoryModel!!.payload!!.history!!.add(0, it.payload!!.history)
                if (currentSelectedTab == typeWalletHistory) {
                    (rootView!!.rv_history.adapter as FundWalletHistoryAdapter?)?.let { adapter ->
                        /**already adapter set just need to notify*/
                        adapter.notifyDataSetChanged()
                    } ?: run {
                        /**need to set new adapter*/
                        rootView!!.rv_history.adapter = FundWalletHistoryAdapter(
                            activity!!,
                            walletHistoryModel!!.payload!!.history,
                            this@FundWallets
                        )
                    }


                }

            }
        })
        myMainViewModel!!.usdtTopupFundResponseModel?.observe(this@FundWallets, Observer {


            rootView!!.edt_usdt_amount_usd.setText("")
            rootView!!.edt_usdt_amount_usd.clearFocus()
            rootView!!.edt_usdt_security_password.setText("")
            rootView!!.edt_usdt_security_password.clearFocus()

            fundTopupHistoryModel!!.payload!!.fundWalletBalance = it.payload!!.fundWalletBalance
            rootView!!.tv_balance_amount.text =
                "$${parseDouble(fundTopupHistoryModel!!.payload!!.fundWalletBalance!!)}"

            if (fundTopupHistoryModel!!.payload!!.history == null) {
                fundTopupHistoryModel!!.payload!!.history = ArrayList()
            }
            fundTopupHistoryModel!!.payload!!.history!!.add(0, it.payload!!.history)
            if (currentSelectedTab == typeFundTopupHistory) {
                (rootView!!.rv_history.adapter as FundTopupHistoryAdapter?)?.let { adapter ->
                    /**already adapter set just need to notify*/
                    adapter.notifyDataSetChanged()
                } ?: run {
                    /**need to set new adapter*/
                    rootView!!.rv_history.adapter = FundTopupHistoryAdapter(
                        activity!!,
                        fundTopupHistoryModel!!.payload!!.history,
                        this@FundWallets
                    )
                }
            }

            activity!!.startActivity(
                Intent(activity!!, PdfViewActivity::class.java).putExtra(
                    "url", it!!.payload!!.webviewUrl!!
                ).putExtra(
                    PdfViewActivity.successURL, it!!.payload!!.successUrl!!
                ).putExtra(
                    PdfViewActivity.cancelURL, it!!.payload!!.cancelUrl!!
                )
            )
        })

    }

    @SuppressLint("SetTextI18n")
    private fun updateUi() {
        Log.e(tag, "Updating Ui")

        rootView!!.rv_history.visibility =
            if (fundTopupHistoryModel!!.payload!!.history!!.size > 0) View.VISIBLE else View.GONE
        rootView!!.tv_no_data.visibility =
            if (fundTopupHistoryModel!!.payload!!.history!!.size > 0) View.GONE else View.VISIBLE

//        rootView!!.sp_topup_funds.adapter = HighLightArrayAdapterV2(
//            context = activity!!,
//            dropdownResource = R.layout.row_spinner_wallet_option_dropdown,
//            viewResource = R.layout.row_spinner_wallet_option,
//            objects = ArrayList<String>().apply {
//                for (i in fundTopupHistoryModel!!.payload!!.fundOption!!) {
//                    this.add(i!!.text!!)
//                }
//            })

        //TODO: Set TopUp Funds Methods setup

        fundTopupHistoryModel!!.payload!!.fundOption?.let {
            for (i in fundTopupHistoryModel!!.payload!!.fundOption!!.indices) {
                fundTopupHistoryModel!!.payload!!.fundOption!![i].selected = false
            }
            fundTopupHistoryModel!!.payload!!.fundOption!![0].selected = true
            topUpFundsMethodAdapter = TopUpFundsMethodsAdapter(
                context!!,
                fundTopupHistoryModel!!.payload!!.fundOption!!,
                this
            )
            rootView!!.rvFundAvailableMethods.adapter = topUpFundsMethodAdapter
            setFundMethodViewByType(fundTopupHistoryModel!!.payload!!.fundOption!![0].value)
        }


        rootView!!.sp_your_bank_list.adapter = HighLightArrayAdapterV2(
            context = activity!!,
            dropdownResource = R.layout.row_spinner_wallet_option_dropdown,
            viewResource = R.layout.row_spinner_bank_list_option,
            objects = ArrayList<String>().apply {
                for (i in fundTopupHistoryModel!!.payload!!.bankList!!) {
                    this.add(i!!.name!!)
                }
            })
        // applyUiConditions()
        //rootView!!.tv_usdt_transfer_type.text = fundTopupHistoryModel!!.payload!!.usdtText
        rootView!!.tv_myr_amount_usd_label.text = fundTopupHistoryModel!!.payload!!.currency
        rootView!!.tv_myr_amount_usd_label_thirdparty.text =
            fundTopupHistoryModel!!.payload!!.currency
        rootView!!.tv_terms_conditions.setHtmlData(fundTopupHistoryModel!!.payload!!.termsConditions!!)
        rootView!!.tv_thirdpart_bank_detail.setHtmlData(fundTopupHistoryModel!!.payload!!.bank_detail!!)

        fundTopupHistoryModel!!.payload!!.termsConditionsDownline?.let {
            rootView!!.tv_transfer_downline_terms_conditions.setHtmlData(it)
        }

        rootView!!.tv_balance_amount.text =
            "$${parseDouble(fundTopupHistoryModel!!.payload!!.fundWalletBalance!!)}"
        fundTopupHistoryModel!!.payload!!.history?.let {
            if (it.isNotEmpty()) {
                (rootView!!.rv_history.adapter as FundTopupHistoryAdapter?)?.let { adapter ->
                    /**already adapter set just need to notify*/
                    adapter.notifyDataSetChanged()
//                    adapter.notifyItemRangeInserted(
//                        adapter.lastPosition + 1,
//                        it.size - (adapter.lastPosition + 1)
//                    )
                } ?: run {
                    /**need to set new adapter*/
                    rootView!!.rv_history.adapter = FundTopupHistoryAdapter(
                        activity!!,
                        fundTopupHistoryModel!!.payload!!.history,
                        this@FundWallets
                    )
                }
            }
        }
        Pref.setLocale(activity!!, Pref.getLocalization(activity!!))
        rootView!!.ll_main.visibility = View.VISIBLE
        rootView!!.txt_upload_bankproof.setText(getString(R.string.upload_bank_proof_tag))
        rootView!!.tvUpload.setText(getString(R.string.upload_text))
        rootView!!.tv_tand_C.setText(getString(R.string.terms_conditions_tag))
        rootView!!.txtTopupfound.setText(getString(R.string.topup_funds_tag))
        rootView!!.tv_fund_topup_history.setText(getString(R.string.fund_topup_history_tag))
        rootView!!.txtAmountThird.setText(getString(R.string.amount_usd_tag))
        rootView!!.txt_upload_bankproofThird.setText(getString(R.string.upload_bank_proof_tag))
        rootView!!.txtSecurityTagThird.setText(getString(R.string.security_password_tag))
        rootView!!.tvUploadthirdparty.setText(getString(R.string.upload_text))
        rootView!!.txtAmountUSDOnline.setText(getString(R.string.amount_usd_tag))
        rootView!!.txtYourBank.setText(getString(R.string.your_bank))
        rootView!!.txtHolderName.setText(getString(R.string.bank_account_holder_name))
        rootView!!.txtSecurityPassword.setText(getString(R.string.security_password_tag))
        rootView!!.edt_amount_usd_thirdparty.setHint(getString(R.string.amount_usd_tag))
        rootView!!.edt_amount_usd.setHint(getString(R.string.amount_usd_tag))
        rootView!!.edt_usdt_amount_usd.setHint(getString(R.string.amount_usd_tag))
        rootView!!.edt_dt_amount_usd.setHint(getString(R.string.amount_usd_tag))
        rootView!!.edt_usdt_security_password.setHint(getString(R.string.security_password_tag))
        rootView!!.edt_dt_security_password.setHint(getString(R.string.security_password_tag))
        rootView!!.edt_security_password_transfer_downline.setHint(getString(R.string.security_password_tag))
        rootView!!.edt_security_password.setHint(getString(R.string.security_password_tag))

        rootView!!.txthistory.setText(getString(R.string.fund_topup_history_tag))

        rootView!!.spUsdtAddress.adapter = HighLightArrayAdapterV2(
            context = activity!!,
            dropdownResource = R.layout.row_spinner_wallet_option_dropdown,
            viewResource = R.layout.row_spinner_bank_list_option,
            objects = ArrayList<String>().apply {
                for (i in fundTopupHistoryModel!!.payload!!.usdtaddress!!) {
                    this.add(i!!.name!!)
                }
            })
    }

    private fun unSelectHistoryOptions() {
        rootView!!.view_fund_topup_history.visibility = View.INVISIBLE

        rootView!!.ln_fund_topup_history.setBackgroundColor(
            ContextCompat.getColor(
                activity!!,
                android.R.color.transparent
            )
        )

        rootView!!.tv_fund_topup_history.setTextColor(
            ContextCompat.getColor(
                activity!!,
                R.color.gray
            )
        )
    }

    private fun callPermissions() {
        ActivityCompat.requestPermissions(activity!!, permissions, Constants.codePermissions)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var denied = 0
        var neverask = 0
        var allowed = 0
        for (permission in permissions) {
            when {
                ActivityCompat.checkSelfPermission(
                    activity!!,
                    permission
                ).toString() === PackageManager.PERMISSION_GRANTED.toString() -> allowed++
                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity!!,
                    permission
                ) -> denied++
                else -> neverask++
            }
        }
        if (neverask > 0) {
            showSettingsDialog()
        } else if (denied > 0) {
            //  callPermissions();
        } else {
            showSelectionDailog()
        }
    }

    private fun showSelectionDailog() {
        mBottomSheetDialog = BottomSheetDialog(activity!!)
        val sheetView = activity!!.layoutInflater.inflate(R.layout.dialog_upload_file, null)
        mBottomSheetDialog!!.setContentView(sheetView)
        mBottomSheetDialog!!.show()
        sheetView.cancel
            .setOnClickListener { mBottomSheetDialog!!.dismiss() }
        sheetView.capturePicture.setOnClickListener {
            sendTakePictureIntent()
            mBottomSheetDialog!!.dismiss()
        }
        sheetView.choosePicture.setOnClickListener {
            pickImageIntent()
            mBottomSheetDialog!!.dismiss()
        }
        sheetView.choosePdf.setOnClickListener {
            pickPdfIntent()
            mBottomSheetDialog!!.dismiss()
        }
    }

    private fun sendTakePictureIntent() {
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(activity!!.packageManager) != null) {
            // Create the File where the photo should go
            photoFile = myCreateImageFile()

            // Continue only if the File was successfully created
            if (photoFile != null) {
                Log.e(tag, " path " + photoFile!!.absolutePath)
                val photoURI: Uri = FileUtils.getFileProviderUri(homeController, photoFile!!)
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(cameraIntent, codeCameraRequest)
            } else {
                Log.e(tag, "Photofile is null")

            }
        }
    }

    private fun pickImageIntent() {
        val intent = Intent()
        // Show only images, no videos or anything else
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        // Always show the chooser (if there are multiple options available)
        startActivityForResult(
            Intent.createChooser(intent, getString(R.string.select_picture_tag)),
            codePickImageRequest
        )
    }

    private fun pickPdfIntent() {
        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(
            Intent.createChooser(intent, getString(R.string.select_pdf_tag)),
            codePickPdfRequest
        )
    }

    private fun myCreateImageFile(): File? {
        var image = FileUtils.getNewImageFile(activity!!)
        // Save a file: path for use with ACTION_VIEW intents
        if (image == null) {
            return null
        } else {
            currentPhotoPath = "file:" + image.absolutePath
            return image

        }
    }

    private fun prepareImageFilePart(partName: String, sfile: String): MultipartBody.Part {
        val file = File(sfile)

        try {
            var bitmap = BitmapFactory.decodeFile(file.path)
            bitmap = myRotateImageIfRequired(bitmap, file.path)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, FileOutputStream(file))
        } catch (t: Throwable) {
            Log.e("ERROR", "Error compressing file.$t")
            t.printStackTrace()
        }

        val reqFile = file.asRequestBody("image/*".toMediaTypeOrNull())


        return MultipartBody.Part.createFormData(partName, file.name, reqFile)

    }

    private fun preparePdfFilePart(partName: String, sfile: String): MultipartBody.Part {
        val file = File(sfile)
        val reqFile = file.asRequestBody("pdf/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, file.name, reqFile)

    }

    @Throws(IOException::class)
    private fun myRotateImageIfRequired(img: Bitmap, selectedImage: String?): Bitmap {

        //  ExifInterface ei = new ExifInterface(selectedImage.getPath());
        val ei = selectedImage?.let { ExifInterface(it) }
        val orientation =
            ei?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> return myRotateImage(img, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> return myRotateImage(img, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> return myRotateImage(img, 270)
            else -> return img
        }
    }

    private fun myRotateImage(img: Bitmap, degree: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        img.recycle()
        return rotatedImg
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == codeSettings) {
            callPermissions()
        }
        if (resultCode != Activity.RESULT_OK) {
            if (mSelectionPath == null) {
                //    rootView!!.imageCard.setVisibility(View.GONE);
            }
            if (requestCode == codeCameraRequest) {
                if (photoFile!!.length() == 0L) {
                    Log.e("zxczxc", " file exists before delete - " + photoFile!!.exists())

                    Log.e("zxczxc", " file is empty ")
                    if (photoFile!!.delete()) {
                        Log.e("zxczxc", " deleted successfully ")

                    } else {
                        Log.e("zxczxc", " deleted not successfully ")

                    }
                    Log.e("zxczxc", " file exists after delete - " + photoFile!!.exists())
                }

            }
            return

        }
        if (requestCode == codeCameraRequest) {
            try {

                if (selectedImage == 0) {
                    mImageBitmap = MediaStore.Images.Media.getBitmap(
                        activity!!.contentResolver,
                        Uri.parse(currentPhotoPath)
                    )
                    //  rootView!!.imageCard.setVisibility(View.VISIBLE);
                    try {
                        mImageBitmap =
                            myRotateImageIfRequired(
                                mImageBitmap!!,
                                Uri.parse(currentPhotoPath).path
                            )
                    } catch (e: Exception) {

                    }

                    rootView!!.image.visibility = View.VISIBLE
                    rootView!!.pdfLayout.visibility = View.GONE

                    rootView!!.image.setImageBitmap(mImageBitmap)
                    mSelectionPath = photoFile!!.absolutePath
                } else if (selectedImage == 1) {
                    mImageBitmap = MediaStore.Images.Media.getBitmap(
                        activity!!.contentResolver,
                        Uri.parse(currentPhotoPath)
                    )
                    //  rootView!!.imageCard.setVisibility(View.VISIBLE);
                    try {
                        mImageBitmap =
                            myRotateImageIfRequired(
                                mImageBitmap!!,
                                Uri.parse(currentPhotoPath).path
                            )
                    } catch (e: Exception) {

                    }
                    rootView!!.imageThirdparty.visibility = View.VISIBLE
                    rootView!!.pdfLayoutthirdparty.visibility = View.GONE

                    rootView!!.imageThirdparty.setImageBitmap(mImageBitmap)
                    mSelectionPathThirdParty = photoFile!!.absolutePath
                }

                selectedType = codeCameraRequest
            } catch (e: IOException) {
                e.printStackTrace()

                Log.e("zxczxc", " exception - $e")
            }

        }

        if (data == null) {
            //if()


            if (mSelectionPath == null) {
                //     rootView!!.tvUpload.setText(getString(R.string.upload_your_receipt_here));
                rootView!!.imageCard.setVisibility(View.GONE)
            }

            return
        }

        if (requestCode == codePickImageRequest) {
            val uri = data.data
            try {
                Log.e("zxczxc", " selected image path - " + uri!!.path!!)
                if (selectedImage == 0) {
                    var bitmap =
                        MediaStore.Images.Media.getBitmap(activity!!.getContentResolver(), uri)
                    //    rootView!!.imageCard.setVisibility(View.VISIBLE);
                    mSelectionPath = FileUtils.getRealPath(activity!!, data.data!!)
                    if (mSelectionPath == null) {
                        Toast.makeText(
                            activity!!,
                            getString(R.string.provide_valid_receipt),
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                    try {
                        bitmap = myRotateImageIfRequired(bitmap, mSelectionPath!!)

                    } catch (e: Exception) {
                        Log.e("zxczxc", " exception e $e")
                    }

                    rootView!!.image.setVisibility(View.VISIBLE)
                    rootView!!.pdfLayout.setVisibility(View.GONE)
                    rootView!!.image.setImageBitmap(bitmap)
                    // mSelectionPath = FileUtils.getPath(getActivity(), data.getData());

                    // mSelectionPath = file.getAbsolutePath();
                    Log.e("zxczxc", " real path - $mSelectionPath")
                } else if (selectedImage == 1) {
                    var bitmap =
                        MediaStore.Images.Media.getBitmap(activity!!.getContentResolver(), uri)
                    //    rootView!!.imageCard.setVisibility(View.VISIBLE);
                    mSelectionPathThirdParty = FileUtils.getRealPath(activity!!, data.data!!)
                    if (mSelectionPathThirdParty == null) {
                        Toast.makeText(
                            activity!!,
                            getString(R.string.provide_valid_receipt),
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                    try {
                        bitmap = myRotateImageIfRequired(bitmap, mSelectionPathThirdParty!!)

                    } catch (e: Exception) {
                        Log.e("zxczxc", " exception e $e")
                    }

                    rootView!!.imageThirdparty.setVisibility(View.VISIBLE)
                    rootView!!.pdfLayoutthirdparty.setVisibility(View.GONE)
                    rootView!!.imageThirdparty.setImageBitmap(bitmap)
                }
                selectedType = codePickImageRequest

            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        if (requestCode == codePickPdfRequest) {
            val uri = data.data
            Log.e("zxczxc", " selected pdf path - " + uri!!.path!!)
            //   mSelectionPath = getPathFromURI(getActivity(), uri);

            //  mSelectionPath = FileUtils.getPath(getActivity(), uri);
            if (selectedImage == 0) {
                mSelectionPath = FileUtils.getRealPath(activity!!, data.data!!)
                if (mSelectionPath == null) {
                    Toast.makeText(
                        activity!!,
                        getString(R.string.provide_valid_receipt),
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
                if (mSelectionPath != null) {
                    rootView!!.image.setVisibility(View.GONE)
                    rootView!!.pdfLayout.setVisibility(View.VISIBLE)

                    Log.e("zxczxc", " real path - " + mSelectionPath + "    ----   " + uri.path)
                } else {
                    Toast.makeText(
                        activity,
                        getString(R.string.provide_valid_receipt),
                        Toast.LENGTH_SHORT
                    ).show()

                }
            } else if (selectedImage == 1) {
                mSelectionPathThirdParty = FileUtils.getRealPath(activity!!, data.data!!)
                if (mSelectionPathThirdParty == null) {
                    Toast.makeText(
                        activity!!,
                        getString(R.string.provide_valid_receipt),
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
                if (mSelectionPathThirdParty != null) {
                    rootView!!.imageThirdparty.setVisibility(View.GONE)
                    rootView!!.pdfLayoutthirdparty.setVisibility(View.VISIBLE)

                    Log.e(
                        "zxczxc",
                        " real path - " + mSelectionPathThirdParty + "    ----   " + uri.path
                    )
                } else {
                    Toast.makeText(
                        activity,
                        getString(R.string.provide_valid_receipt),
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }
            selectedType = codePickPdfRequest

        }
    }

    private fun showSettingsDialog() {
        val builder = AlertDialog.Builder(activity, R.style.MyDialogTheme)
        builder.setTitle(getString(R.string.need_permission_msg))
        builder.setMessage(getString(R.string.open_settings_msg))
        builder.setPositiveButton(
            getString(R.string.go_to_settings_tag)
        ) { dialog, _ ->
            dialog.cancel()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", activity!!.packageName, null)
            intent.data = uri
            startActivityForResult(intent, 101)
        }
        builder.setNegativeButton(
            getString(R.string.cancel_tag)
        ) { dialog, _ ->
            dialog.cancel()
            //finishAffinity();
            //  requestStoragePermission();
        }
        builder.create().apply {

            this.setOnShowListener {
                this.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(ContextCompat.getColor(activity!!, R.color.black))
                this.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(ContextCompat.getColor(activity!!, R.color.black))

            }
        }.show()
    }

    override fun onListShow(position: Int, obj: Any?) {
        super.onListShow(position, obj)

        if (!NetworkUtil.isInternetAvailable(activity!!)) {
            Toast.makeText(
                activity!!,
                getString(R.string.no_internet_connection),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        showImagePdfDialog(obj.toString())
    }

    override fun onListClick(position: Int, obj: Any?) {
        super.onListClick(position, obj)

        if (!NetworkUtil.isInternetAvailable(activity!!)) {
            Toast.makeText(
                activity!!,
                getString(R.string.no_internet_connection),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        showCancelRequestDialog(obj.toString())
    }

    private fun showCancelRequestDialog(transaction_id: String) {

        var mTitleMsg =
            getString(R.string.cancel_transaction_title_msg) + "\n\n" + getString(R.string.cancel_transaction_msg)

        androidx.appcompat.app.AlertDialog.Builder(activity!!, R.style.MyDialogTheme).let {
            it.setMessage(mTitleMsg)
            it.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                myMainViewModel?.cancelFundRequest(transaction_id)
                dialog.dismiss()
            }
            it.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            it.create().apply {

                this.setOnShowListener {
                    this.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(ContextCompat.getColor(activity!!, R.color.black))
                    this.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(ContextCompat.getColor(activity!!, R.color.black))

                }
            }.show()
        }
    }

    override fun onListClickSimple(position: Int, string: String?) {
        super.onListClickSimple(position, string)

        for (i in fundTopupHistoryModel!!.payload!!.fundOption!!.indices) {
            fundTopupHistoryModel!!.payload!!.fundOption!![i].selected = false
        }
        fundTopupHistoryModel!!.payload!!.fundOption!![position].selected = true

        topUpFundsMethodAdapter?.notifyDataSetChanged()

        setFundMethodViewByType(string)
    }

    private fun setFundMethodViewByType(name: String?) {

        rootView!!.cv_usdt_terms_conditions.visibility = View.GONE
        if (name == Constants.TYPE_USDT) {
            rootView!!.ll_usdt_section.visibility = View.VISIBLE
            rootView!!.ll_online_payment_gateway_section.visibility = View.GONE
            rootView!!.llDirectTransferSelection.makeGone()
            rootView!!.ll_thirdparty_section.visibility = View.GONE
            if (fundTopupHistoryModel != null) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) Pref.setLocale(
                    activity!!,
                    Pref.getLocalization(activity!!)
                )
                rootView!!.webviewUsdtCondition.loadDataWithBaseURL(
                    null,
                    fundTopupHistoryModel!!.payload!!.usdtTerms!!,
                    "text/html",
                    "UTF-8",
                    null
                )
                rootView!!.webviewUsdtCondition.makeVisible()
                rootView!!.webviewUsdtCondition?.setBackgroundColor(Color.TRANSPARENT)

                /*rootView!!.webviewUsdtCondition.setWebViewClient(object : WebViewClient() {
                    override fun onPageStarted(
                        view: WebView?,
                        url: String?,
                        favicon: Bitmap?
                    ) {
                        super.onPageStarted(view, url, favicon)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            Pref.setLocale(activity!!, Pref.getLocalization(activity!!))
                        }
                    }

                    override fun onPageFinished(
                        view: WebView,
                        url: String
                    ) {
                        super.onPageFinished(view, url)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            Pref.setLocale(activity!!, Pref.getLocalization(activity!!))
                        }
                    }
                })*/

                rootView!!.webviewUsdtCondition.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        url?.let { view?.loadUrl(it) }
                        view?.setBackgroundColor(Color.TRANSPARENT)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) Pref.setLocale(
                            activity!!,
                            Pref.getLocalization(activity!!)
                        )
                        return true
                    }
                }

            }

            setupBiometricSelection(
                methodSelectionLayout = rootView!!.layout_method_selection,
                securityPasswordLayout = rootView!!.ll_security_password,
                radioBiometric = rootView!!.rb_biometric,
                radioSecurityPassword = rootView!!.rb_security_password,
                radioGroupSelection = rootView!!.rg_selection
            )
            rootView!!.cv_usdt_terms_conditions.visibility = View.VISIBLE
            rootView!!.tv_tand_C.visibility = View.VISIBLE
            rootView!!.tv_terms_conditions.visibility = View.VISIBLE

            currentSelectedBankUSDTTab = Constants.TYPE_USDT
        } else if (name == Constants.TYPE_ONLINE) {
//            bankDetail = "0"
            rootView!!.ll_usdt_section.visibility = View.GONE
            rootView!!.ll_online_payment_gateway_section.visibility = View.VISIBLE
            rootView!!.llDirectTransferSelection.makeGone()
            rootView!!.ll_thirdparty_section.visibility = View.GONE
            rootView!!.tv_tand_C.visibility = View.VISIBLE
            rootView!!.tv_terms_conditions.visibility = View.VISIBLE
            rootView!!.webviewUsdtCondition.makeGone()

            /*for (i in fundTopupHistoryModel!!.payload!!.fundOption!!) {
                if (i!!.value == Constants.TYPE_ONLINE) {
                    if (i!!.bankDetail == "0") {
                        rootView!!.ll_bank_account_number!!.visibility = View.GONE
                        rootView!!.ll_bank_account_holder_name!!.visibility = View.GONE
                    } else {
                        bankDetail = "1"
                        rootView!!.ll_Bank_detail.visibility = View.GONE
                        rootView!!.ll_bank_account_number!!.visibility = View.GONE
                        rootView!!.ll_bank_account_holder_name!!.visibility = View.GONE
                    }
                }
            }*/

            setupBiometricSelection(
                methodSelectionLayout = rootView!!.layout_method_selection_online,
                securityPasswordLayout = rootView!!.ll_security_password_online,
                radioBiometric = rootView!!.rb_biometric_online,
                radioSecurityPassword = rootView!!.rb_security_password_online,
                radioGroupSelection = rootView!!.rg_selection_online
            )
            currentSelectedBankUSDTTab = Constants.TYPE_ONLINE
        } else if (name == Constants.TYPE_DIRECT_TRANSFER) {
//            bankDetail = "0"
            rootView!!.ll_usdt_section.makeGone()
            rootView!!.ll_online_payment_gateway_section.makeGone()
            rootView!!.ll_thirdparty_section.makeGone()
            rootView!!.tv_tand_C.makeVisible()
            rootView!!.tv_terms_conditions.makeVisible()
            rootView!!.webviewUsdtCondition.makeGone()

            rootView!!.llDirectTransferSelection.makeVisible()

            setupBiometricSelection(
                methodSelectionLayout = rootView!!.layout_method_selection_dt,
                securityPasswordLayout = rootView!!.ll_security_password_dt,
                radioBiometric = rootView!!.rb_biometric_dt,
                radioSecurityPassword = rootView!!.rb_security_password_dt,
                radioGroupSelection = rootView!!.rg_selection_dt
            )
            currentSelectedBankUSDTTab = Constants.TYPE_DIRECT_TRANSFER

        } else if (name == Constants.TYPE_BANK_3RD_PARTY) {
            rootView!!.ll_usdt_section.visibility = View.GONE
            rootView!!.ll_online_payment_gateway_section.visibility = View.GONE
            rootView!!.llDirectTransferSelection.makeGone()
            rootView!!.ll_thirdparty_section.visibility = View.VISIBLE
            rootView!!.webviewUsdtCondition.makeGone()
            rootView!!.tv_tand_C.visibility = View.VISIBLE
            rootView!!.tv_terms_conditions.visibility = View.VISIBLE
            rootView!!.cv_usdt_terms_conditions.visibility = View.VISIBLE
            fundTopupHistoryModel?.let {
                it.payload?.let {
                    if (!it.third_party_payment_noteval.equals("")) {
                        rootView!!.ivdrt_remarks.makeVisible()
                        rootView!!.ivdrt_remarks.text=it.third_party_payment_noteval

                    } else {
                        rootView!!.ivdrt_remarks.makeGone()

                    }
                }
            }

            setupBiometricSelection(
                methodSelectionLayout = rootView!!.layout_method_selection_online_thirdparty,
                securityPasswordLayout = rootView!!.ll_security_password_thirdparty,
                radioBiometric = rootView!!.rb_biometric_online_thirdparty,
                radioSecurityPassword = rootView!!.rb_security_password_online_thirdparty,
                radioGroupSelection = rootView!!.rg_selection_online_thirdparty
            )
            currentSelectedBankUSDTTab = Constants.TYPE_BANK_3RD_PARTY
        }
    }

    override fun onResume() {
        super.onResume()
        homeController.llBottomNew.makeVisible()
    }

    override fun onPause() {
        super.onPause()
        homeController.llBottomNew.makeGone()
    }

}


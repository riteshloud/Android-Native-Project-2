package com.demo2.view.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.biometric.BiometricConstants
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.demo.navigationdemo.WebViewActivity
import com.google.gson.Gson
import com.demo2.R
import com.demo2.utilities.BiometricUtils
import com.demo2.utilities.Constants
import com.demo2.utilities.Pref
import com.demo2.view.interfaces.BiometricCallback
import com.demo2.view.service.MyViewModelFactory
import com.demo2.view.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_auth_options.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.sp_selectLanguage
import org.json.JSONObject


/**master password - Oryx135!@#*/
class LoginActivity : BaseActivity() {
    private val TAG = this.javaClass.simpleName
    private var loginViewModel: LoginViewModel? = null

    var myBiometricCallback: BiometricCallback? = null

    var myBioMetric: BiometricPrompt? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Pref.setLocale(this@LoginActivity, Pref.getLocalization(this@LoginActivity))
        setContentView(R.layout.activity_login)
        init()
        setup()
        addListeners()
    }

    private fun init() {
        loginViewModel = ViewModelProviders.of(
            this@LoginActivity,
            MyViewModelFactory(LoginViewModel(this@LoginActivity))
        )[LoginViewModel::class.java]
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        tvCopyRight.setCopyright()
    }

    private fun setup() {
        ln_fingerprint!!.visibility = if (Pref.getValue(
                this@LoginActivity,
                Constants.prefFingerPrintSetInThisDevice,
                false
            )
        ) View.VISIBLE else View.GONE

        sp_selectLanguage.adapter = HighLightArrayAdapterV2(
            context = this@LoginActivity,
            dropdownResource = R.layout.row_spinner_login_dropdown,
            viewResource = R.layout.row_spinner_login,
            objects = ArrayList<String>().apply {
                this.add("English")
                this.add("Chinese")
                this.add("Korean")
                this.add("Thai")
                this.add("Vietnam")
            })

        sp_selectLanguage?.let {
            if (Pref.getValue(
                    this@LoginActivity,
                    Constants.Localization,
                    ""
                ).equals("en")
            ) {
                it?.setSelection(0)
            } else if (Pref.getValue(
                    this@LoginActivity,
                    Constants.Localization,
                    ""
                ).equals("cn")
            ){
                it?.setSelection(1)
            }else if (Pref.getValue(
                    this@LoginActivity,
                    Constants.Localization,
                    ""
                ).equals("ko")
            ){
                it?.setSelection(2)
            }else if (Pref.getValue(
                    this@LoginActivity,
                    Constants.Localization,
                    ""
                ).equals("th")
            ){
                it?.setSelection(3)
            }else if (Pref.getValue(
                    this@LoginActivity,
                    Constants.Localization,
                    ""
                ).equals("vi")
            ){
                it?.setSelection(4)
            }
        }

        addObservers()
        if (Pref.getValue(this@LoginActivity, Constants.prefIsRemember, false)) {
            edt_username.setText(Pref.getValue(this@LoginActivity, Constants.prefLoginUsername, ""))
            edt_password.setText(Pref.getValue(this@LoginActivity, Constants.prefLoginPassword, ""))
            cb_rememeber.isChecked = true
        }

        //fillTestData()
        tv_login.makeLinks(
            Pair("terms", View.OnClickListener {
                startActivity(
                    Intent(this@LoginActivity, WebViewActivity::class.java).putExtra(
                        "url",
                        Pref.getCommonDataModel(this)!!.payload!!.termsConditions
                    ).putExtra("isPdf", true)
                )

            }),
            Pair("privacy policy", View.OnClickListener {
                startActivity(
                    Intent(this@LoginActivity, WebViewActivity::class.java).putExtra(
                        "url",
                        Pref.getCommonDataModel(this)!!.payload!!.privacyPolicy
                    ).putExtra("isPdf", true)
                )
            }))

    }

    private fun addListeners() {
        img_back.setOnClickListener {
            finish()
        }


        edt_username.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (!p0.isNullOrEmpty() && p0 != Pref.getValue(
                        this@LoginActivity,
                        Constants.prefFingerUsername,
                        ""
                    )
                ) {
                    ln_fingerprint!!.visibility = View.GONE
                    Pref.setValue(this@LoginActivity, Constants.prefFingerUsername, "")
                    Pref.setValue(this@LoginActivity, Constants.prefFingerPassword, "")
                    Pref.setValue(
                        this@LoginActivity,
                        Constants.prefFingerPrintSetInThisDevice,
                        false
                    )
                }
            }
        })
        var isUserAction = false
        sp_selectLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                sp_selectLanguage.adapter.let {
                    var x: HighLightArrayAdapterV2 = it as HighLightArrayAdapterV2
                    x.setSelection(p2)
                    if (sp_selectLanguage.selectedItem.toString() == "English") {
                        Pref.setLocale(this@LoginActivity,"en")
                    } else if (sp_selectLanguage.selectedItem.toString() == "Chinese") {
                        Pref.setLocale(this@LoginActivity,"cn")
                    } else if (sp_selectLanguage.selectedItem.toString() == "Korean") {
                        Pref.setLocale(this@LoginActivity,"ko")
                    } else if (sp_selectLanguage.selectedItem.toString() == "Thai") {
                        Pref.setLocale(this@LoginActivity,"th")
                    } else if (sp_selectLanguage.selectedItem.toString() == "Vietnam") {
                        Pref.setLocale(this@LoginActivity,"vi")
                    }
                    if (isUserAction){
                        changeText()
                        loginViewModel!!.loadCommonData()
                    }else{
                        isUserAction = true
                    }
                }
            }
        }
        cv_signin.setOnClickListener {

            when {
                edt_username.text.toString().trim().isEmpty() -> {
                    Toast.makeText(
                        this@LoginActivity,
                        getString(R.string.validation_username),
                        Toast.LENGTH_SHORT
                    ).show()

                    //scroll.myRequestFocus(edt_username)
                }
                edt_password.text.toString().trim().isEmpty() -> {

                    Toast.makeText(
                        this@LoginActivity,
                        getString(R.string.validation_password),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    //  scroll.myRequestFocus(edt_password)

                }
                else -> {
                    Pref.setValue(
                        this@LoginActivity,
                        Constants.prefFingerUsername,
                        edt_username.text.toString().trim()
                    )
                    Pref.setValue(
                        this@LoginActivity,
                        Constants.prefFingerPassword,
                        edt_password.text.toString().trim()
                    )
                    loginViewModel?.loginCall(
                        currentLocalization, edt_username.text.toString().trim()
                        , edt_password.text.toString().trim()
                        , Constants.deviceType
                        , "test"
                    )

                }
            }


        }

        rl_finger!!.setOnClickListener {
            //Log.e("TestClerea","###   " +  Pref.getValue(this@LoginActivity, Constants.prefFingerUUID, "")!!)
            showBiometric(biometricCallback = object : BiometricCallback {
                override fun onSuccess(result: BiometricPrompt.AuthenticationResult) {
                    /*Log.e("TestPasword","decrypt " + UTILS.decrypt(
                       Constants.passwordKeyAES,
                       Base64.decode(
                           Pref.getValue(this@LoginActivity, Constants.prefFingerPassword, "")!!
                               .toByteArray(
                                   charset("UTF-16LE")
                               ), Base64.DEFAULT
                       )
                   )!!)*/
                    loginViewModel?.loginCallWithUUID(
                        currentLocalization,
                        Pref.getValue(this@LoginActivity, Constants.prefFingerUsername, "")!!
                        , Pref.getValue(this@LoginActivity, Constants.prefFingerPassword, "")!!
                        , Constants.deviceType
                        , "test", Pref.getValue(this@LoginActivity, Constants.prefFingerUUID, "")!!
                    )
                }

                override fun onFail() {}

                override fun onError(errorCode: Int, errString: CharSequence) {
                    if (errorCode == BiometricConstants.ERROR_LOCKOUT || errorCode == BiometricConstants.ERROR_LOCKOUT_PERMANENT) {
                        Toast.makeText(
                            this@LoginActivity!!,
                            getString(R.string.too_many_attempts),
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                }
            })
        }
        tvSignup.setOnClickListener {
            startActivity(Intent(this@LoginActivity, SignupActivity::class.java))
        }
        tv_forgot.setOnClickListener {
            startActivity(Intent(this@LoginActivity, ForgetPasswordActivity::class.java))
        }
    }

    private fun addObservers() {
        loginViewModel?.responseError?.observe(this@LoginActivity, Observer {
            it?.let {
                try {
                    val res = it.string()
                    val jsonObject = JSONObject(res)
                    if (jsonObject.optInt("code") == 305) {
                        ln_fingerprint!!.visibility = View.GONE
                        Pref.setValue(this@LoginActivity, Constants.prefFingerUsername, "")
                        Pref.setValue(this@LoginActivity, Constants.prefFingerPassword, "")
                        Pref.setValue(
                            this@LoginActivity,
                            Constants.prefFingerPrintSetInThisDevice,
                            false
                        )
                        Toast.makeText(
                            this@LoginActivity!!,
                            jsonObject.getString("message"),
                            Toast.LENGTH_LONG
                        ).show()


                    } else {
                        //errorBody(it)
                        errorBodyFromJson(jsonObject)
                    }
                } catch (e: java.lang.Exception) {
                    Toast.makeText(
                        this@LoginActivity!!,
                        getString(R.string.something_wrong_message),
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
        })

        loginViewModel?.isLoading?.observe(this@LoginActivity, Observer {

            if (it) {
                showProgressDialog()
            } else {
                dismissProgressDialog()
            }
        })

        loginViewModel?.userModel?.observe(this@LoginActivity, Observer {
            var userModel = it
            if(!userModel!!.payload!!.user!!.fingerUUID.isNullOrEmpty()){
                var previousUserUUId = Pref.getValue(this@LoginActivity,Constants.prefFingerUUID,"")!!.toLowerCase().trim()
                if(userModel!!.payload!!.user!!.fingerUUID!!.toLowerCase().trim() == previousUserUUId)
                    userModel!!.payload!!.user!!.fingerPrintSetInThisDevice = true
            }
            Pref.setValue(this@LoginActivity, Constants.prefUserData, Gson().toJson(userModel))
            Pref.setValue(this@LoginActivity, Constants.prefProfile, userModel!!.payload!!.user!!.profile_image!!)
            Pref.setValue(this@LoginActivity, Constants.prefRank, userModel.payload!!.user!!.rank_detail!!.name!!)


            Pref.setValue(
                this@LoginActivity,
                Constants.prefAuthorizationToken,
                "Bearer ${it.payload!!.token!!}"
            )

            if (sp_selectLanguage.selectedItem.toString() == "English") {
                Pref.setValue(this@LoginActivity, Constants.Localization, "en")
            } else if (sp_selectLanguage.selectedItem.toString() == "Chinese") {
                Pref.setValue(this@LoginActivity, Constants.Localization, "cn") //chi
            } else if (sp_selectLanguage.selectedItem.toString() == "Korean") {
                Pref.setValue(this@LoginActivity, Constants.Localization, "ko")
            } else if (sp_selectLanguage.selectedItem.toString() == "Thai") {
                Pref.setValue(this@LoginActivity, Constants.Localization, "th")
            } else if (sp_selectLanguage.selectedItem.toString() == "Vietnam") {
                Pref.setValue(this@LoginActivity, Constants.Localization, "vi")
            }

            if (cb_rememeber.isChecked) {
                Pref.setValue(this@LoginActivity, Constants.prefIsRemember, true)
                Pref.setValue(
                    this@LoginActivity,
                    Constants.prefLoginUsername,
                    edt_username!!.text.toString().trim()
                )
                Pref.setValue(
                    this@LoginActivity,
                    Constants.prefLoginPassword,
                    edt_password!!.text.toString().trim()
                )

            }else{
                Pref.setValue(this@LoginActivity, Constants.prefIsRemember, false)
            }
            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                finishAffinity()
            } else {
                finish()
            }
        })
    }

    fun showBiometric(
        title: String = getString(R.string.biometric_authentication_msg)
        , description: String = getString(R.string.biometric_authentication_desc_msg)
        , biometricCallback: BiometricCallback
    ) {
        if (!BiometricUtils.checkBiometricPossible(this@LoginActivity!!!!)) {
            Toast.makeText(
                this@LoginActivity!!,
                getString(R.string.biometric_not_supported_msg),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (myBioMetric == null) {
            generateBasicBioMetric()
        }
        myBiometricCallback = biometricCallback
        myBioMetric!!.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setDescription(description)
                //.setNegativeButtonText("Use test account password")
                .setDeviceCredentialAllowed(true)
                .build()
        )
    }

    fun generateBasicBioMetric() {
        myBioMetric =
            BiometricPrompt(this@LoginActivity, ContextCompat.getMainExecutor(this@LoginActivity)!!,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence
                    ) {
                        super.onAuthenticationError(errorCode, errString)
                        Log.e("zxcv", "Authentication error: $errString")

                        myBiometricCallback?.onError(errorCode, errString)
                    }

                    //
                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult
                    ) {
                        super.onAuthenticationSucceeded(result)
                        /**success*/
                        myBiometricCallback?.onSuccess(result)
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        /*   Toast.makeText(
                               activity!!, getString(R.string.auth_failed),
                               Toast.LENGTH_SHORT
                           )
                               .show()*/
                        myBiometricCallback?.onFail()

                    }
                })
    }

    //TODO: Change All Text When Language Change
    private fun changeText(){
        cb_rememeber.setText(R.string.remember_me_tag)
        txtLogin.setText(R.string.login_tag)
        txtDontHaveAccount.setText(R.string.dont_have_acc_tag)
        tvSignup.setText(R.string.signup_now)
        tv_forgot.setText(R.string.forgot_password_tag)
        edt_username.setHint(getString(R.string.username_tag))
        edt_password.setHint(getString(R.string.password_tag))
        tv_login.setText(getString(R.string.read_tag)+" "+getString(R.string.terms_tag)+" "+getString(R.string.and_tag)+" "+getString(R.string.privacy_policy_tag))
        tv_login.makeLinks(
            Pair(getString(R.string.terms_tag), View.OnClickListener {
                startActivity(
                    Intent(this@LoginActivity, WebViewActivity::class.java).putExtra(
                        "url",
                        Pref.getCommonDataModel(this)!!.payload!!.termsConditions
                    ).putExtra("isPdf", true)
                )

            }),
            Pair(getString(R.string.privacy_policy_tag), View.OnClickListener {
                startActivity(
                    Intent(this@LoginActivity, WebViewActivity::class.java).putExtra(
                        "url",
                        Pref.getCommonDataModel(this)!!.payload!!.privacyPolicy
                    ).putExtra("isPdf", true)
                )
            }))
        tvCopyRight.setCopyright()
    }

    private fun fillTestData() {
        //edt_username.setText("Testusername")
        edt_password.setText("W-,r*kBG/nmE5b:<")
    }

}
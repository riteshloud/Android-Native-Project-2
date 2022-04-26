package com.demo2.view.ui.base


import android.animation.ValueAnimator
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.Nullable
import androidx.biometric.BiometricPrompt
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.demo2.R
import com.demo2.utilities.BiometricUtils
import com.demo2.utilities.Pref
import com.demo2.utilities.UTILS
import com.demo2.view.interfaces.BiometricCallback
import com.demo2.view.interfaces.OnListClickListener
import com.demo2.view.ui.activities.HomeActivity
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.dialog_image_view.*
import org.sufficientlysecure.htmltextview.HtmlFormatter
import org.sufficientlysecure.htmltextview.HtmlFormatterBuilder
import org.sufficientlysecure.htmltextview.HtmlResImageGetter
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*


abstract class BaseFragment : OnListClickListener, Fragment() {

    private var TAG = this.javaClass.simpleName
    private var lifecycleCheck = "zxczxc"
    lateinit var homeController: HomeActivity
    lateinit var userModel: UserModel
    lateinit var commonDataModel: CommonDataModel
    //  companion object{
    var myBiometricCallback: BiometricCallback? = null

    var myBioMetric: BiometricPrompt? = null
    // }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        homeController = context as HomeActivity
        userModel = Pref.getUserModel(context)!!
        commonDataModel = Pref.getCommonDataModel(context)!!
        Log.e(lifecycleCheck, "lifecycle check - $TAG onAttach")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.e(lifecycleCheck, "lifecycle check - $TAG onCreateView")
        Pref.setLocale(activity!!, Pref.getLocalization(activity!!))
        return super.onCreateView(inflater, container, savedInstanceState)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.e(lifecycleCheck, "lifecycle check - $TAG onActivityCreated")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e(lifecycleCheck, "lifecycle check - $TAG onViewCreated")
    }

    override fun onStart() {
        super.onStart()
        Log.e(lifecycleCheck, "lifecycle check - $TAG onStart")
    }

    override fun onResume() {
        super.onResume()

       // setFullAppBackGround() //for comman background after dashboard in fragment
        Log.e(lifecycleCheck, "lifecycle check - $TAG onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.e(lifecycleCheck, "lifecycle check - $TAG onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.e(lifecycleCheck, "lifecycle check - $TAG onStop")
    }

    override fun onDetach() {
        super.onDetach()
        Log.e(lifecycleCheck, "lifecycle check - $TAG onDetach")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.e(lifecycleCheck, "lifecycle check - $TAG onDestroyView")
        try {
            homeController!!.lastDestroyedFragment = this.javaClass.simpleName
            hideSoftKeyboard()
        } catch (e: Exception) {

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(lifecycleCheck, "lifecycle check - $TAG onDestroy")

    }

    private fun setFullAppBackGround() {
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        params.setMargins(0, 0, 0, UTILS.dpToPixel(context!!,35))
        homeController.ln_all_page_bg.background = ContextCompat.getDrawable(context!!, R.mipmap.dashboard_app_bg)
        homeController.ln_all_page_bg.layoutParams = params
    }


    fun NestedScrollView.myRequestFocus(view: View) {

        view.requestFocus()
        //this.smoothScrollTo(0, view.bottom)
    }

    fun ScrollView.myRequestFocus(view: View) {
        view.requestFocus()
        //  this.smoothScrollTo(0, view.bottom)
    }


    fun hideSoftKeyboard() {
        if (activity!!.window.decorView.rootView != null) {
            val inputMethodManager =
                activity!!.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager!!.hideSoftInputFromWindow(
                activity!!.window.decorView.rootView.windowToken,
                0
            )
        } else {

        }
    }

    fun showSoftKeyboard() {

        val inputMethodManager =
            activity!!.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager

        if (inputMethodManager != null) {
            val currentFocusView = activity!!.window.decorView.rootView

            if (currentFocusView != null) {
                val iBinderToken = currentFocusView.windowToken
                if (iBinderToken != null) {
                    inputMethodManager.toggleSoftInputFromWindow(
                        iBinderToken,
                        InputMethodManager.SHOW_FORCED, 0
                    )
                }
            } else {
            }
        }
    }

    fun parseDouble(str: String): String {
        val nf = NumberFormat.getNumberInstance(Locale.US)
        val df = nf as DecimalFormat
        // df.applyPattern("##,##,##,##,##,##,##0.00")
        df.applyPattern("#0.00")
        val wallet = java.lang.Double.parseDouble(str)
        return df.format(wallet)
    }

    fun showImagePdfDialog(path: String) {
        if (path.endsWith("pdf")) {
            val browserIntent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(path)
                )
            activity!!.startActivity(browserIntent)
            return
        }

        var imgPreviewDialog = Dialog(activity!!)
        imgPreviewDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        imgPreviewDialog!!.setContentView(R.layout.dialog_image_view)
        imgPreviewDialog!!.getWindow()!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        imgPreviewDialog!!.progressBar.visibility = View.VISIBLE

        imgPreviewDialog!!.img.setOnClickListener {
            imgPreviewDialog!!.dismiss()
        }
/*
 imgPreviewDialog!!.img_close.setOnClickListener {
            imgPreviewDialog!!.dismiss()
        }
*/

        Glide.with(activity!!)
            .load(path)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    @Nullable e: GlideException?, model: Any,
                    target: com.bumptech.glide.request.target.Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    imgPreviewDialog.progressBar.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: com.bumptech.glide.request.target.Target<Drawable>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    imgPreviewDialog.progressBar.visibility = View.GONE
                    return false
                }
            }).into(imgPreviewDialog.img)

        //imgPreviewDialog.img.setOnClickListener { imgPreviewDialog.dismiss() }
        imgPreviewDialog.show()
    }

    fun selectTab(cvOtmWalletType: CardView, tvOtmWalletType: TextView) {
        cvOtmWalletType.setCardBackgroundColor(
            ContextCompat.getColor(
                activity!!,
                R.color.purple_selector
            )
        )

        tvOtmWalletType.setTextColor(
            ContextCompat.getColor(
                activity!!,
                android.R.color.white
            )
        )
    }

    fun selectViewTab(view: View, tvOtmWalletType: TextView) {
        view.visibility = View.VISIBLE

        tvOtmWalletType.setTextColor(
            ContextCompat.getColor(
                activity!!,
                R.color.blue_ribbon
            )
        )
    }

    fun changeSelfTradingSelectTab(cvOtmWalletType: CardView, tvOtmWalletType: TextView) {
        cvOtmWalletType.setCardBackgroundColor(
            ContextCompat.getColor(
                activity!!,
                R.color.light_blue
            )
        )

        tvOtmWalletType.setTextColor(
            ContextCompat.getColor(
                activity!!,
                android.R.color.white
            )
        )
    }


    fun TextView.setHtmlData(htmlData: String) {
        this.text = HtmlFormatter.formatHtml(
            HtmlFormatterBuilder().setHtml(htmlData).setImageGetter(
                HtmlResImageGetter(this.context)
            )
        )
    }

    fun TextView.underline() {
        paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    fun String.removeComma(): String {
        return this.replace(",", "")
    }

    fun setupBiometricSelection(
        methodSelectionLayout: View,
        securityPasswordLayout: View,
        radioGroupSelection: RadioGroup,
        radioSecurityPassword: RadioButton,
        radioBiometric: RadioButton
    ) {

        if (BiometricUtils.checkBiometricPossible(activity!!) && userModel.payload!!.user!!.fingerPrintSetInThisDevice) {
            methodSelectionLayout.visibility = View.VISIBLE
        } else {
            methodSelectionLayout.visibility = View.GONE
            securityPasswordLayout.visibility = View.VISIBLE
            radioSecurityPassword.isChecked = true
        }

        radioGroupSelection.setOnCheckedChangeListener { group, checkedId ->
            if (radioSecurityPassword.isChecked) {
                securityPasswordLayout.visibility = View.VISIBLE
            } else {
                hideSoftKeyboard()
                securityPasswordLayout.visibility = View.GONE
            }
        }
    }

    fun setupBiometricSelectionMT4(
        methodSelectionLayout: View,
        securityPasswordLayout: View,
        radioGroupSelection: RadioGroup,
        radioSecurityPassword: RadioButton,
        radioBiometric: RadioButton
    ) {

        if (BiometricUtils.checkBiometricPossible(activity!!) && userModel.payload!!.user!!.fingerPrintSetInThisDevice) {
            methodSelectionLayout.visibility = View.VISIBLE
        } else {
            methodSelectionLayout.visibility = View.GONE
            securityPasswordLayout.visibility = View.VISIBLE
            radioSecurityPassword.isChecked = true
        }

        radioGroupSelection.setOnCheckedChangeListener { group, checkedId ->
            if (radioSecurityPassword.isChecked) {
                securityPasswordLayout.visibility = View.VISIBLE
            } else {
                securityPasswordLayout.visibility = View.GONE
            }
        }
    }

    fun showBiometricOld(
        title: String = getString(R.string.biometric_authentication_msg)
        , description: String = getString(R.string.biometric_authentication_desc_msg)
        , biometricCallback: BiometricCallback
    ) {
        var tempCallback: BiometricCallback? = biometricCallback
        BiometricPrompt(this, ContextCompat.getMainExecutor(activity)!!,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    Log.e(tag, "Authentication error: $errString")

                    tempCallback?.onError(errorCode, errString)
                }

                //
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    /**success*/
                    tempCallback?.onSuccess(result)


                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    /*  Toast.makeText(
                          activity!!, getString(R.string.auth_failed),
                          Toast.LENGTH_SHORT
                      )
                          .show()*/
                    tempCallback?.onFail()

                }
            }).let {
            it.authenticate(
                BiometricPrompt.PromptInfo.Builder()
                    .setTitle(title)
                    .setDescription(description)
                    //.setNegativeButtonText("Use test account password")
                    .setDeviceCredentialAllowed(true)
                    .build()
            )
        }
    }

    fun generateBasicBioMetric() {
        myBioMetric = BiometricPrompt(this@BaseFragment, ContextCompat.getMainExecutor(activity)!!,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    Log.e(tag, "Authentication error: $errString")

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
//
    fun showBiometric(
        title: String = getString(R.string.biometric_authentication_msg)
        , description: String = getString(R.string.biometric_authentication_desc_msg)
        , biometricCallback: BiometricCallback
    ) {
        if (!BiometricUtils.checkBiometricPossible(activity!!)) {
            Toast.makeText(
                activity!!,
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
    fun startCountAnimation(tv : TextView, amount : Float) {
        val animator =
            ValueAnimator.ofInt(0, amount.toInt())
        animator.duration = 5000

        animator.addUpdateListener { animation -> tv.text = "$${animation.animatedValue.toString()}" }
        animator.duration = 1000 // here you set the duration of the anim

        animator.start()
    }

    /**override methods from implemented interfaces are implemented below*/
    override fun onListClick(position: Int, obj: Any?) {}

    override fun onListClickSimple(position: Int, string: String?) {
    }

    override fun onListShow(position: Int, obj: Any?) {}


}

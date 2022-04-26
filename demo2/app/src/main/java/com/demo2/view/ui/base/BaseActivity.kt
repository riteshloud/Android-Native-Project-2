package com.demo2.view.ui.base

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Selection
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.demo2.R
import com.demo2.utilities.Constants
import com.demo2.utilities.Pref
import com.demo2.view.ui.activities.LoginActivity
import kotlinx.android.synthetic.main.dialog_progress.*
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


open class BaseActivity : AppCompatActivity() {
    var mProgressDialog: Dialog? = null
    var currentLocalization: String = "en"
    var userModel: UserModel? = null
    var commonDataModel: CommonDataModel? = null
    var lastDestroyedFragment: String = ""
    var mSnackBar:Snackbar? = null

    private val TAG = BaseActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }*/
    }
 /*   override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(Pref.updateResources(base!!, Pref.getLocalization(base!!)))
    }*/
    var faqModel: FaqModel? = null

    private fun init() {
        if (commonDataModel == null) {
            commonDataModel = Pref.getCommonDataModel(this@BaseActivity)
        }
        if (userModel == null) {
            userModel = Pref.getUserModel(this@BaseActivity)
        }
    }

    fun hideSoftKeyboard() {
        if (window.decorView.rootView != null) {
            val inputMethodManager =
                getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager!!.hideSoftInputFromWindow(
                    window.decorView.rootView.windowToken,
                    0
            )
        } else {
        }
    }

    fun showSoftKeyboard() {

        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputMethodManager != null) {
            val currentFocusView = window.decorView.rootView

            if (currentFocusView != null) {
                val iBinderToken = currentFocusView.windowToken
                if (iBinderToken != null) {
                    inputMethodManager!!.toggleSoftInputFromWindow(
                            iBinderToken,
                            InputMethodManager.SHOW_FORCED, 0
                    )
                }
            } else {
            }
        }
    }

    fun loadFragment(fragment: Fragment, tag: String) {

        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment, tag)
            .addToBackStack(null)
            .commit()
    }


    fun loadFragmentWithClearedStack(fragment: Fragment, tag: String, fragName: String?) {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, tag)
            .addToBackStack(null)
            .commit()

    }

    fun clearFragment(fragName: String) {
        supportFragmentManager.popBackStack(fragName, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    fun loadFragment(fragment: Fragment, tag: String, backstack: String) {

        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment, tag)
            .addToBackStack(backstack).commit()
    }

    fun addLoadFragment(fragment: Fragment, tag: String, backstack: String) {

        supportFragmentManager.beginTransaction().add(R.id.fragment_container, fragment, tag)
            .addToBackStack(backstack).commit()
    }


    fun NestedScrollView.myRequestFocus(view: View) {
        Log.e(
                "zxczxc",
                "NestedScrollView.myRequestFocus to ${resources.getResourceEntryName(view.id)}"
        )
        view.requestFocus()
        // this.smoothScrollTo(0, view.bottom)
    }

    fun ScrollView.myRequestFocus(view: View) {
        Log.e("zxczxc", "ScrollView.myRequestFocus to ${resources.getResourceEntryName(view.id)}")

        view.requestFocus()
        this.smoothScrollTo(0, view.bottom)
    }

    fun showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = Dialog(this@BaseActivity)
        }
        try {
            mProgressDialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            mProgressDialog!!.show()
            //  mProgressDialog!!.setIndeterminate(true)
            mProgressDialog!!.setCancelable(false)
            mProgressDialog!!.setCanceledOnTouchOutside(false)
            mProgressDialog!!.setContentView(R.layout.dialog_progress)
            mProgressDialog!!.progressbar.setVisibility(View.VISIBLE)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    public fun dismissProgressDialog() {
        if (mProgressDialog != null) {
            try {
                mProgressDialog!!.dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun dismissSnackbar() {
        if (mSnackBar!= null && mSnackBar!!.isShown){
            mSnackBar!!.dismiss()
        }
    }

    fun showSnackbar(mView: View, mMessage: String, mDuration: Int) {
        try {
            dismissSnackbar()
            mSnackBar = Snackbar.make(mView, mMessage, Snackbar.LENGTH_INDEFINITE).apply {
                view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).maxLines = 20
            }
            mSnackBar!!.view.background = ContextCompat.getDrawable(applicationContext,R.drawable.container_snackbar);
            mSnackBar!!.duration = mDuration
            mSnackBar!!.show()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    fun messageToast(responseBody: Response<ResponseBody>) {
        try {
            val res = responseBody.body()?.string()
            val jsonObject = JSONObject(res)
            Toast.makeText(this@BaseActivity, jsonObject.getString("message"), Toast.LENGTH_SHORT)
                .show()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    fun message(message: String) {
        Toast.makeText(this@BaseActivity, message, Toast.LENGTH_SHORT)
            .show()
    }

    fun errorBodyFromJson(jsonObject: JSONObject) {
        try {

            // Log.d(TAG, "errorBody: "+responseErrorBody.string())

            if (jsonObject.optInt("code") == 302 || jsonObject.optInt("code") == 301 || jsonObject.optInt(
                            "code"
                    ) == 300 || jsonObject.optInt("code") == 401
            ) {
                val builder = AlertDialog.Builder(this@BaseActivity, R.style.MyDialogTheme)
                builder.setMessage(jsonObject.getString("message"))
                builder.setCancelable(false)
                builder.setPositiveButton(getString(R.string.ok),
                        DialogInterface.OnClickListener { dialog, which ->

                            val gson = Gson()
                            var CommonDataModel =
                                    gson.fromJson(
                                            Pref.getValue(
                                                    this@BaseActivity,
                                                    Constants.prefCommonData,
                                                    ""
                                            ), CommonDataModel::class.java
                                    )
                            var is_remember =
                                    Pref.getValue(this@BaseActivity, Constants.prefIsRemember, false)
                            var email =
                                    Pref.getValue(this@BaseActivity, Constants.prefLoginUsername, "")
                            var password =
                                    Pref.getValue(this@BaseActivity, Constants.prefLoginPassword, "")

                            Pref.deleteAll(this@BaseActivity)
                            Pref.setLocale(this@BaseActivity, "en")
                            if (is_remember) {
                                Pref.setValue(this@BaseActivity, Constants.prefIsRemember, is_remember)
                                Pref.setValue(this@BaseActivity, Constants.prefLoginUsername, email!!)
                                Pref.setValue(
                                        this@BaseActivity,
                                        Constants.prefLoginPassword,
                                        password!!
                                )
                            }
                            Pref.setValue(
                                    this@BaseActivity,
                                    Constants.prefCommonData,
                                    gson.toJson(CommonDataModel)
                            )

                            startActivity(Intent(this@BaseActivity, LoginActivity::class.java))
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                finishAffinity()
                            } else {
                                finish()
                            }
                        })
                builder.create().apply {

                    this.setOnShowListener {
                        this.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                            .setTextColor(
                                    ContextCompat.getColor(
                                            this@BaseActivity!!,
                                            R.color.black
                                    )
                            )

                    }
                }.show()
            } else {
                Toast.makeText(
                        this@BaseActivity,
                        jsonObject.getString("message"),
                        Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: IOException) {
            Toast.makeText(
                    this@BaseActivity,
                    getString(R.string.something_wrong_message),
                    Toast.LENGTH_SHORT
            ).show()
            Log.e("zxczxc", "exception errorBody $e")
            e.printStackTrace()
        } catch (e: JSONException) {
            Toast.makeText(
                    this@BaseActivity,
                    getString(R.string.something_wrong_message),
                    Toast.LENGTH_SHORT
            ).show()
            Log.e("zxczxc", "exception errorBody $e")
            e.printStackTrace()
        }

    }

    fun errorBody(responseErrorBody: ResponseBody) {
        try {

            // Log.d(TAG, "errorBody: "+responseErrorBody.string())
            val res = responseErrorBody.string()
            val jsonObject = JSONObject(res)
            if (jsonObject.optInt("code") == 302 || jsonObject.optInt("code") == 301 || jsonObject.optInt(
                            "code"
                    ) == 300 || jsonObject.optInt("code") == 401
            ) {
                val builder = AlertDialog.Builder(this@BaseActivity, R.style.MyDialogTheme)
                builder.setMessage(jsonObject.getString("message"))
                builder.setCancelable(false)
                builder.setPositiveButton(getString(R.string.ok),
                        DialogInterface.OnClickListener { dialog, which ->

                            val gson = Gson()
                            var CommonDataModel =
                                    gson.fromJson(
                                            Pref.getValue(
                                                    this@BaseActivity,
                                                    Constants.prefCommonData,
                                                    ""
                                            ), CommonDataModel::class.java
                                    )
                            var is_remember =
                                    Pref.getValue(this@BaseActivity, Constants.prefIsRemember, false)
                            var email =
                                    Pref.getValue(this@BaseActivity, Constants.prefLoginUsername, "")
                            var password =
                                    Pref.getValue(this@BaseActivity, Constants.prefLoginPassword, "")

                            Pref.deleteAll(this@BaseActivity)
                            // Pref.setLocale(this@BaseActivity, "en")
                            if (is_remember) {
                                Pref.setValue(this@BaseActivity, Constants.prefIsRemember, is_remember)
                                Pref.setValue(this@BaseActivity, Constants.prefLoginUsername, email!!)
                                Pref.setValue(
                                        this@BaseActivity,
                                        Constants.prefLoginPassword,
                                        password!!
                                )
                            }
                            Pref.setValue(
                                    this@BaseActivity,
                                    Constants.prefCommonData,
                                    gson.toJson(CommonDataModel)
                            )

                            startActivity(Intent(this@BaseActivity, AuthOptionsActivity::class.java)) //LoginActivity
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                finishAffinity()
                            } else {
                                finish()
                            }
                        })
                builder.create().apply {

                    this.setOnShowListener {
                        this.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                            .setTextColor(
                                    ContextCompat.getColor(
                                            this@BaseActivity!!,
                                            R.color.black
                                    )
                            )

                    }
                }.show()
            } else {
                Toast.makeText(
                        this@BaseActivity,
                        jsonObject.getString("message"),
                        Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: IOException) {
            Toast.makeText(
                    this@BaseActivity,
                    getString(R.string.something_wrong_message),
                    Toast.LENGTH_SHORT
            ).show()
            Log.e("zxczxc", "exception errorBody $e")
            e.printStackTrace()
        } catch (e: JSONException) {
            Toast.makeText(
                    this@BaseActivity,
                    getString(R.string.something_wrong_message),
                    Toast.LENGTH_SHORT
            ).show()
            Log.e("zxczxc", "exception errorBody $e")
            e.printStackTrace()
        }

    }

    fun errorBodyForModel(res: String) {
        try {

            val jsonObject = JSONObject(res)
            val code = jsonObject.getInt("code")
            val message = jsonObject.getString("message")
            if (code == 500) {
                val builder = AlertDialog.Builder(this@BaseActivity, R.style.MyDialogTheme)
                builder.setMessage(message)
                builder.setCancelable(false)
                builder.setPositiveButton(getString(R.string.ok),
                        DialogInterface.OnClickListener { dialog, which ->


                            Pref.deleteAll(this@BaseActivity)


                            startActivity(Intent(this@BaseActivity, AuthOptionsActivity::class.java))
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                finishAffinity()
                            } else {
                                finish()
                            }
                        })
                builder.create().apply {

                    this.setOnShowListener {
                        this.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                            .setTextColor(
                                    ContextCompat.getColor(
                                            this@BaseActivity!!,
                                            R.color.black
                                    )
                            )

                    }
                }.show()
            } else {
                Toast.makeText(this@BaseActivity, message, Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                    this@BaseActivity,
                    getString(R.string.something_wrong_message),
                    Toast.LENGTH_SHORT
            ).show()

            Log.e("zxczxc", " exception - " + e.message)
            e.printStackTrace()
        }

    }

    open fun showQRCodeDialog(qrstring: String?) {
        val listDialog = Dialog(this@BaseActivity)
        val li =
            this@BaseActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        listDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        listDialog.setContentView(R.layout.dialog_qr_code)
        listDialog.setCanceledOnTouchOutside(false)
        val lp = WindowManager.LayoutParams()
        val window = listDialog.window
        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        lp.copyFrom(window.attributes)
        listDialog.window!!.setGravity(Gravity.CENTER)
        listDialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        val cvClose =
            listDialog.findViewById<View>(R.id.cvClose) as CardView
        val imgQr =
            listDialog.findViewById<View>(R.id.imgQr) as ImageView
        val cvShareCode: CardView =
            listDialog.findViewById<View>(R.id.cvShareCode) as CardView
        cvClose.setOnClickListener { listDialog.dismiss() }

        //generate qr code function
        val writer = QRCodeWriter()
        try {
            val bitMatrix: BitMatrix = writer.encode(qrstring, BarcodeFormat.QR_CODE, 512, 512)
            val width: Int = bitMatrix.getWidth()
            val height: Int = bitMatrix.getHeight()
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(
                            x,
                            y,
                            if (bitMatrix.get(
                                            x,
                                            y
                                    )
                            ) Color.BLACK else Color.WHITE
                    )
                }
            }
            imgQr.setImageBitmap(bmp)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
        cvShareCode.setOnClickListener(View.OnClickListener {
            showProgressDialog()
            shareBitmap(this!!.screenShot(imgQr)!!, "qrCode")
        })
        listDialog.show()
    }

    open fun screenShot(view: View): Bitmap? {
        val bitmap = Bitmap.createBitmap(
                view.width,
                view.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    //////// this method share your image
    open fun shareBitmap(bitmap: Bitmap, fileName: String) {
        try {
            val file = File(cacheDir, "$fileName.png")
            val fOut = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut)
            fOut.flush()
            fOut.close()
            file.setReadable(true, false)
            val intent = Intent(Intent.ACTION_SEND)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(
                    Intent.EXTRA_STREAM,
                    FileProvider.getUriForFile(
                            applicationContext,
                            "$packageName.fileprovider",
                            file
                    )
            )
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.demo2_referral_qr_code))
            intent.type = "image/png"
            Handler().postDelayed({
                startActivity(Intent.createChooser(intent, getString(R.string.share_qr_code)))
                dismissProgressDialog()
            }, 600)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /*fun languageDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this@BaseActivity, R.style.MyDialogTheme).let {
            it.setMessage(getString(R.string.language_message))
            it.setPositiveButton(
                getString(R.string.ok)

            ) { dialog, _ ->
                dialog.dismiss()

            }
            it.show()
        }
    }*/


    fun TextView.makeLinks(vararg links: Pair<String, View.OnClickListener>) {
        val spannableString = SpannableString(this.text)
        for (link in links) {
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(view: View) {
                    Selection.setSelection((view as TextView).text as Spannable, 0)
                    view.invalidate()
                    link.second.onClick(view)
                }
            }
            val startIndexOfLink = this.text.toString().indexOf(link.first)
            spannableString.setSpan(clickableSpan, startIndexOfLink, startIndexOfLink + link.first.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        this.movementMethod = LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not click
        this.setText(spannableString, TextView.BufferType.SPANNABLE)
    }


}

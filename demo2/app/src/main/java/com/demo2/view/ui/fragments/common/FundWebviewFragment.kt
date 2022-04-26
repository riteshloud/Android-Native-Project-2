package com.demo2.view.ui.fragments.common


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.special.ResideMenu.ResideMenu
import com.demo2.R
import com.demo2.utilities.Constants
import com.demo2.utilities.FileUtils
import com.demo2.utilities.Pref
import com.demo2.view.ui.base.BaseFragment
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_fund_webview.view.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class FundWebviewFragment : BaseFragment() {

    var successURL = ""
    var cancelURL = ""
    var url = ""
    var rootview: View? = null
    private var TAG = FundWebviewFragment::class.java.simpleName
    private var permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    )

    companion object {
        private const val FCR = 112
    }

    private var mCM: String? = null
    private var mUM: ValueCallback<Uri>? = null
    private var mUMA: ValueCallback<Array<Uri>>? = null

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        //if (rootview == null) {
        rootview = inflater.inflate(R.layout.fragment_fund_webview, container, false)
        init()
        setup()
        onClickListeners()
        //}
        return rootview
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Pref.setLocale(activity!!, Pref.getLocalization(activity!!))

        homeController.tv_title.text = getString(R.string.online_payment_gateway)
        homeController.tv_title.visibility = View.VISIBLE
        homeController.iv_navigation.visibility = View.GONE
        homeController.ll_bottombar.visibility = View.GONE
        homeController.iv_back.visibility = View.VISIBLE
        homeController.rl_message.visibility = View.GONE
        homeController.viewVisibleDrawerBottomBar(2)
        homeController.resideMenu!!.setSwipeDirectionDisable(ResideMenu.DIRECTION_LEFT)

    }

    private fun init() {
        Log.e("webview", "url  - ${url} $successURL ${cancelURL}")
    }

    private fun setup() {
        homeController.showProgressDialog()

//        val webSettings = rootview!!.webview.settings
        rootview!!.webview.settings.javaScriptEnabled = true
        rootview!!.webview.settings.loadWithOverviewMode = false
        rootview!!.webview.settings.useWideViewPort = false
        rootview!!.webview.settings.domStorageEnabled = true
        rootview!!.webview.settings.allowFileAccess=true
        rootview!!.webview.settings.allowContentAccess=true
        rootview!!.webview.settings.allowUniversalAccessFromFileURLs=true
        rootview!!.webview.settings.allowFileAccessFromFileURLs=true
        rootview!!.webview.settings.javaScriptCanOpenWindowsAutomatically=true

        setWebviewClient()

        rootview!!.webview.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {

                Log.e("webview", "shouldOverrideUrlLoading - $url")
                view.loadUrl(url)

                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Log.e("webview", "onPageStarted - $url")
                /*if (successURL != null) {
                    Log.e(
                        "webview",
                        "onPageStarted checking \nsuccess ${successURL}\ncancel - ${cancelURL}"
                    )

                    if (url == successURL || url == cancelURL
                    ) {
                        activity!!.supportFragmentManager.popBackStack()
                    }
                }*/
            }

            override fun onPageFinished(view: WebView, url: String) {
                Log.e("webview", "onPageFinished - $url")
                homeController.dismissProgressDialog()
                if (successURL != null) {
                    Log.e(
                            "webview",
                            "onPageStarted checking \nsuccess ${successURL}\ncancel - ${cancelURL}"
                    )

                    if (url == successURL || url == cancelURL
                    ) {
                        activity!!.supportFragmentManager.popBackStack()
                    }
                }

            }

        }
        rootview!!.webview.loadUrl(url)
    }

    private fun setWebviewClient() {
        rootview!!.webview.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(webView: WebView, filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: FileChooserParams): Boolean {
                if (mUMA != null) {
                    mUMA!!.onReceiveValue(null)
                }
                mUMA = filePathCallback
                var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePictureIntent!!.resolveActivity(activity!!.packageManager) != null) {
                    var photoFile: File? = null
                    try {
                        photoFile = createImageFile()
//                        photoFile =FileUtils.getNewImageFile(activity!!)
                        takePictureIntent.putExtra("PhotoPath", mCM)
                    } catch (ex: IOException) {
                        Log.e("Webview", "Image file creation failed", ex)
                    }
                    if (photoFile != null) {
                        mCM = "file:" + photoFile.getAbsolutePath()
                        takePictureIntent.putExtra(
                                MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(photoFile)
                        )
                    } else {
                        takePictureIntent = null
                    }
                }
                val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
                val mimeTypes = arrayOf("image/*", "application/pdf")
//                contentSelectionIntent.type = "*/*"
                contentSelectionIntent.type = "image/*|application/pdf"
                contentSelectionIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                val intentArray: Array<Intent>
                intentArray = takePictureIntent?.let { arrayOf(it) } ?: arrayOf<Intent>()
                val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser")
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
                startActivityForResult(chooserIntent, Companion.FCR)
                return true
            }
        }
    }


    /*private fun myCreateImageFile(): File? {
        var image = FileUtils.getNewImageFile(activity!!)
        // Save a file: path for use with ACTION_VIEW intents
        if (image == null) {
            return null
        } else {
//            currentPhotoPath = "file:" + image.absolutePath
            return image

        }
    }*/

    // Create an image file
    @Throws(IOException::class)
    private fun createImageFile(): File? {
        @SuppressLint("SimpleDateFormat") val timeStamp: String =
                SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "img_" + timeStamp + "_"
        val storageDir: File =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }


    fun openFileChooser(uploadMsg: ValueCallback<Uri?>?) {
        this.openFileChooser(uploadMsg, "*/*")
    }

    fun openFileChooser(uploadMsg: ValueCallback<Uri?>?, acceptType: String?) {
        this.openFileChooser(uploadMsg, acceptType, null)
    }

    fun openFileChooser(uploadMsg: ValueCallback<Uri?>?, acceptType: String?, capture: String?) {
        val i = Intent(Intent.ACTION_GET_CONTENT)
        i.addCategory(Intent.CATEGORY_OPENABLE)
        i.type = "*/*"
        activity!!.startActivityForResult(Intent.createChooser(i, "File Browser"), FCR)
    }

    /*private class MyWebChromeClient : WebChromeClient() {

    }*/


    private fun onClickListeners() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        homeController.rl_message.visibility = View.VISIBLE
        //homeController.visibleBottomBar(0)
        homeController.resideMenu!!.enableDirection(ResideMenu.DIRECTION_LEFT)

        homeController.tv_title.text = getString(R.string.topup_fund_nav)

        homeController.iv_navigation.visibility = View.VISIBLE
        homeController.iv_back.visibility = View.GONE


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        Log.e("WbeviewFragment"," requestCode: $requestCode resultCode: $resultCode ")
        if (requestCode != Companion.FCR){
            return
        }
       /* if (requestCode == Companion.FCR  && resultCode == Activity.RESULT_CANCELED ){

        }*/

        if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.READ_EXTERNAL_STORAGE) + ContextCompat.checkSelfPermission(activity!!, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            callPermissions()
            if (Build.VERSION.SDK_INT >= 21){
                mUMA!!.onReceiveValue(null)
                mUMA = null
            }else{
                mUM!!.onReceiveValue(null)
                mUM = null
            }
            setWebviewClient()
        }else if (Build.VERSION.SDK_INT >= 21) {
            var results: Array<Uri>? = null
            //Check if response is positive
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == Companion.FCR) {
                    if (null == mUMA) {
                        return
                    }
                    if (intent == null) { //Capture Photo if no image available
                        if (mCM != null) {
                            results = arrayOf(Uri.parse(mCM))
                        }
                    } else {
                        var dataString = intent.dataString
                        Log.e("WbeviewFragment"," dataString: $dataString ")
//                        val  mSelectionPath = FileUtils.getRealPath(activity!!, intent.data!!)
//                        Log.e("WbeviewFragment"," mSelectionPath: $mSelectionPath ")
//                        val dataString = intent.dataString
//                        dataString = mSelectionPath
                        if (dataString != null) {
                            results = arrayOf(Uri.parse(dataString))
                        }
                    }
                }
            }

            if (results == null){
                Toast.makeText(activity!!, getString(R.string.provide_valid_receipt), Toast.LENGTH_SHORT).show()
            }
            mUMA!!.onReceiveValue(results)
            mUMA = null
            setWebviewClient()
        } else {
            if (requestCode == Companion.FCR) {
                if (null == mUM) return
                val result = if (intent == null || resultCode != Activity.RESULT_OK) null else intent.data
                if (result == null){
                    Toast.makeText(activity!!, getString(R.string.provide_valid_receipt), Toast.LENGTH_SHORT).show()
                }
                mUM!!.onReceiveValue(result)
                mUM = null
                setWebviewClient()
            }
        }
//        setWebviewClient()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
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
            //all permission granted
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

    private fun callPermissions() {
        ActivityCompat.requestPermissions(activity!!, permissions, Constants.codePermissions)
    }

}

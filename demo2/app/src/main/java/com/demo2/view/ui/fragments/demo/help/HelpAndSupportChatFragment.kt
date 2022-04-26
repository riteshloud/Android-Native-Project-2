package com.demo2.view.ui.fragments.demo.help


import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.StrictMode
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.demo2.R
import com.demo2.utilities.Constants
import com.demo2.utilities.FileUtils
import com.demo2.view.service.MyViewModelFactory
import com.demo2.view.service.NetworkUtil
import com.demo2.view.ui.base.BaseFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.demo2.utilities.Pref
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.dialog_upload_file.view.*
import kotlinx.android.synthetic.main.fragment_help_and_support_chat.view.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * A simple [Fragment] subclass.
 */
class HelpAndSupportChatFragment : BaseFragment() {

    var rootView: View? = null
    var mContext: Context? = null

    var helpAndSupportChatViewModel: HelpAndSupportChatViewModel? = null

    var helpAndSupportChatModel: HelpAndSupportChatModel? = null

    var supportId: Int? = null
    var supportTitle: String? = null

    var bottomBarVisibility: Int ?= null

    /**declarations for image/pdf selection*/
    private var currentPhotoPath: String? = null
    private var mImageBitmap: Bitmap? = null
    private var mSelectionPath: String? = null
    private var photoFile: File? = null
    private var selectedType: Int = 0
    private var mBottomSheetDialog: BottomSheetDialog? = null
    private var permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        if (rootView == null) {
            rootView =
                inflater.inflate(R.layout.fragment_help_and_support_chat, container, false)

        }

        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Pref.setLocale(activity!!, Pref.getLocalization(activity!!))

        homeController.ll_bottombar.visibility = View.GONE

        init()
        setUp()
        onClickListeners()
    }

    private fun init() {
        rootView!!.tv_title.text=supportTitle
        activity!!.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        activity!!.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        helpAndSupportChatViewModel = ViewModelProviders
            .of(
                this@HelpAndSupportChatFragment,
                MyViewModelFactory(HelpAndSupportChatViewModel(activity!!))
            )[HelpAndSupportChatViewModel::class.java]

    }

    private fun setUp() {
        rootView!!.rvChatList.layoutManager =
            LinearLayoutManager(activity!!, LinearLayoutManager.VERTICAL, false)

        addObservers()

        helpAndSupportChatViewModel!!.getChatList(supportId!!)
    }

    private fun addObservers() {
        helpAndSupportChatViewModel!!.isLoading?.observe(this@HelpAndSupportChatFragment, Observer {
            if (it) {
                homeController.showProgressDialog()
            } else {
                homeController.dismissProgressDialog()

            }
        })
        helpAndSupportChatViewModel!!.responseError?.observe(
            this@HelpAndSupportChatFragment,
            Observer {
                homeController.errorBody(it)
            })

        helpAndSupportChatViewModel!!.helpAndSupportChatModel?.observe(
            this@HelpAndSupportChatFragment,
            Observer {

                if (helpAndSupportChatModel == null) {
                    helpAndSupportChatModel = it

                    if (it.payload!!.supportTicket!!.status == "0") {
                        rootView!!.llBottom.visibility = View.VISIBLE
                    } else if (it.payload!!.supportTicket!!.status == "1") {
                        rootView!!.llBottom.visibility = View.GONE
                    }

                    /**api called first time need to set new adapter*/
                    helpAndSupportChatModel!!.payload!!.messages?.let {
                        rootView!!.rvChatList.adapter = HelpAndSupportChatAdapter(
                            activity!!,
                            helpAndSupportChatModel!!.payload!!.messages,
                            this@HelpAndSupportChatFragment,
                            homeController
                        )

                        rootView!!.rvChatList.scrollToPosition(rootView!!.rvChatList.adapter!!.itemCount - 1)

                    }
                } else {

                    helpAndSupportChatModel = it

                    if (it.payload!!.supportTicket!!.status == "0") {
                        rootView!!.llBottom.visibility = View.VISIBLE
                    } else if (it.payload!!.supportTicket!!.status == "1") {
                        rootView!!.llBottom.visibility = View.GONE
                    }
                }

            })


        helpAndSupportChatViewModel!!.helpAndSupportSendMessage?.observe(this@HelpAndSupportChatFragment,
            Observer {

                val res = it.body()?.string()
                val jsonObject = JSONObject(res)

                val jsonPayload = jsonObject.optJSONObject("payload")
                val jsonMessage = jsonPayload.optJSONObject("messages")

                val jsonParser = JsonParser()
                val gson = Gson()

                var jsonElement: JsonElement = jsonParser.parse(jsonMessage.toString())

                if (helpAndSupportChatModel != null) {
                    helpAndSupportChatModel!!.payload!!.messages?.add(
                        gson.fromJson(
                            jsonElement,
                            HelpAndSupportChatModel.Payload.Message::class.java
                        )
                    )

                    updateUi()
                }

            })
    }

    private fun updateUi() {

        mSelectionPath = null
        rootView!!.edtSendBox.text.clear()
        rootView!!.edtSendBox.clearFocus()

        helpAndSupportChatModel!!.payload!!.messages?.let {

            (rootView!!.rvChatList.adapter as HelpAndSupportChatAdapter?)?.let { adapter ->
                /**already adapter set just need to notify*/
                adapter.notifyDataSetChanged()
                rootView!!.rvChatList.scrollToPosition(rootView!!.rvChatList.adapter!!.itemCount - 1)
                rootView!!.iv_attach.setImageResource(R.mipmap.ic_attach)
            } ?: run {
                /**need to set new adapter*/
                rootView!!.rvChatList.adapter = HelpAndSupportChatAdapter(
                    activity!!,
                    helpAndSupportChatModel!!.payload!!.messages,
                    this@HelpAndSupportChatFragment,
                    homeController
                )
                rootView!!.rvChatList.scrollToPosition(rootView!!.rvChatList.adapter!!.itemCount - 1)
            }

        }

    }

    private fun onClickListeners() {

        rootView!!.cv_send.setOnClickListener {

            if (!NetworkUtil.isInternetAvailable(activity!!)) {
                Toast.makeText(
                    activity!!,
                    getString(R.string.no_internet_connection),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            } else if (rootView!!.edtSendBox.text.trim().toString().isEmpty()) {

                Toast.makeText(
                    activity!!,
                    getString(R.string.message_empty_validation),
                    Toast.LENGTH_SHORT
                ).show()
            } /*else if (mSelectionPath == null || mSelectionPath!!.isEmpty()) {

                Toast.makeText(
                    activity!!,
                    getString(R.string.upload_attachment),
                    Toast.LENGTH_SHORT
                )
                    .show()
            } */ else {
                if (mSelectionPath == null || mSelectionPath!!.isEmpty()) {
                    helpAndSupportChatViewModel?.sendMessage(
                        supportId!!,
                        rootView!!.edtSendBox.text.trim().toString()
                    )
                } else {
                    var attachment: MultipartBody.Part? = null

                    attachment =
                        if (selectedType == Constants.codeCameraRequest || selectedType == Constants.codePickImageRequest) {
                            prepareImageFilePart("attachment[]", mSelectionPath!!)
                        } else {
                            preparePdfFilePart("attachment[]", mSelectionPath!!)
                        }
                    helpAndSupportChatViewModel?.sendMessage(
                        supportId!!,
                        rootView!!.edtSendBox.text.trim().toString(),
                        attachment
                    )
                }
            }
        }

        rootView!!.iv_back.setOnClickListener {
            fragmentManager?.popBackStack()
        }


        rootView!!.iv_attach.setOnClickListener {
            callPermissions()
        }
    }

    override fun onResume() {
        super.onResume()
        homeController.toolbar.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        homeController.toolbar.visibility = View.VISIBLE
        if(bottomBarVisibility == View.VISIBLE) homeController.visibleBottomBar(0)
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
                startActivityForResult(cameraIntent, Constants.codeCameraRequest)
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
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        // Always show the chooser (if there are multiple options available)
        startActivityForResult(
            Intent.createChooser(intent, getString(R.string.select_picture_tag)),
            Constants.codePickImageRequest
        )
    }

    private fun pickPdfIntent() {
        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(
            Intent.createChooser(intent, getString(R.string.select_pdf_tag)),
            Constants.codePickPdfRequest
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
        if (requestCode == Constants.codeSettings) {
            callPermissions()
        }
        if (resultCode != Activity.RESULT_OK) {
            if (mSelectionPath == null) {
                //    rootView!!.imageCard.setVisibility(View.GONE);
            }
            if (requestCode == Constants.codeCameraRequest) {
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
        if (requestCode == Constants.codeCameraRequest) {
            try {
                mImageBitmap = FileUtils.getBitmapFromUri(
                    activity!!,
                    Uri.parse(currentPhotoPath)
                )
                //  rootView!!.imageCard.setVisibility(View.VISIBLE);
                try {
                    mImageBitmap =
                        myRotateImageIfRequired(mImageBitmap!!, Uri.parse(currentPhotoPath).path)
                } catch (e: Exception) {

                }

                mSelectionPath = photoFile!!.absolutePath

                selectedType = Constants.codeCameraRequest


                rootView!!.iv_attach.setImageResource(R.mipmap.ic_select_file)
                rootView!!.edtSendBox.requestFocus()
                Handler().postDelayed({ showSoftKeyboard() }, 100)


            } catch (e: IOException) {
                e.printStackTrace()

                Log.e("zxczxc", " exception - $e")
            }

        }

        if (data == null) {
            //if()


            if (mSelectionPath == null) {
            }

            return
        }

        if (requestCode == Constants.codePickImageRequest) {
            val uri = data.data
            try {
                if (data.clipData != null) {
                    var count: Int = data.clipData!!.itemCount

                    for (i in 0 until count) {
                        var imageUri = data.clipData!!.getItemAt(i).uri
                    }
                } else if (data.data != null) {
                    var bitmap=FileUtils.getBitmapFromUri(activity!!,uri)
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
                    // mSelectionPath = FileUtils.getPath(getActivity(), data.getData());

                    // mSelectionPath = file.getAbsolutePath();
                    Log.e("zxczxc", " real path - $mSelectionPath")
                    selectedType = Constants.codePickImageRequest
                    rootView!!.edtSendBox.requestFocus()

                    Handler().postDelayed({ showSoftKeyboard() }, 100)
                    rootView!!.iv_attach.setImageResource(R.mipmap.ic_select_file)

                }


            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        if (requestCode == Constants.codePickPdfRequest) {

            if (data.clipData != null) {

                var count: Int = data.clipData!!.itemCount

                for (i in 0 until count) {

                    var pdfUri = data.clipData!!.getItemAt(i).uri
                     mSelectionPath = FileUtils.getRealPath(activity!!, pdfUri)

                    if (mSelectionPath != null) {
                        selectedType = Constants.codePickPdfRequest
                        rootView!!.edtSendBox.requestFocus()

                        Handler().postDelayed({ showSoftKeyboard() }, 100)
                        rootView!!.iv_attach.setImageResource(R.mipmap.ic_select_file)
                        Log.e("zxczxc", " real path - " + mSelectionPath + "    ----   " + pdfUri.path)

                    } else {
                        Toast.makeText(
                            activity,
                            getString(R.string.provide_valid_receipt),
                            Toast.LENGTH_SHORT
                        ).show()

                    }

                }

            } else if (data.data != null) {
                val uri = data.data
                Log.e("zxczxc", " selected pdf path - " + uri!!.path!!)
                //   mSelectionPath = getPathFromURI(getActivity(), uri);

                //  mSelectionPath = FileUtils.getPath(getActivity(), uri);
                mSelectionPath = FileUtils.getRealPath(activity!!, data.data!!)

                if (mSelectionPath != null) {
                    selectedType = Constants.codePickPdfRequest
                    rootView!!.edtSendBox.requestFocus()

                    Handler().postDelayed({ showSoftKeyboard() }, 100)
                    rootView!!.iv_attach.setImageResource(R.mipmap.ic_select_file)
                    Log.e("zxczxc", " real path - " + mSelectionPath + "    ----   " + uri.path)

                } else {
                    Toast.makeText(
                        activity,
                        getString(R.string.provide_valid_receipt),
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }


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

    override fun onListClickSimple(position: Int, string: String?) {
        super.onListClickSimple(position, string)

        if (!NetworkUtil.isInternetAvailable(activity!!)) {
            Toast.makeText(
                activity!!,
                getString(R.string.no_internet_connection),
                Toast.LENGTH_SHORT
            ).show()
            return
        }


        if (position == 0) {
            showImagePdfDialog(string!!)
        } else {
            val browserIntent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(string)
                )
            activity!!.startActivity(browserIntent)


        }
    }


}

package com.demo2.view.ui.fragments.common


import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

import com.demo2.R
import com.demo2.utilities.Constants
import com.demo2.utilities.FileUtils
import com.demo2.view.service.MyViewModelFactory
import com.demo2.view.ui.base.BaseFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.demo2.utilities.Pref
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.dialog_upload_file.*
import kotlinx.android.synthetic.main.dialog_upload_file.view.*
import kotlinx.android.synthetic.main.fragment_kyc.view.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**know before start
 * "kyc_status" - (Display setting 0 = Not Requested, 1 Pending request ,2 Approved Request & 3 Rejected Request)
 * */

class KycFragment : BaseFragment() {
    var rootView: View? = null

    var kycDetailsModel: TradeDashboardModel? = null
    var myMainViewModel: KycViewModel? = null

    /**declarations for image/pdf selection*/
    private var currentPhotoPath: String? = null
    private var mImageBitmap: Bitmap? = null
    private var mSelectionPath: String? = null
    private var photoFile: File? = null
    private var selectedType: Int = 0
    private lateinit var SelectedImage: TextView
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
            rootView = inflater.inflate(R.layout.fragment_kyc, container, false)
        }
        return rootView

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Pref.setLocale(activity!!, Pref.getLocalization(activity!!))
        homeController.tv_title.text = getString(R.string.kyc_verification)
        homeController.tv_title.visibility = View.VISIBLE
        homeController.iv_navigation.visibility = View.GONE
        homeController.ll_bottombar.visibility = View.GONE
        homeController.iv_back.visibility = View.VISIBLE
        init()
        //setup()
        onClickListeners()

    }


    private fun init() {
        myMainViewModel = ViewModelProviders.of(
            this@KycFragment,
            MyViewModelFactory(KycViewModel(activity!!))
        )[KycViewModel::class.java]
    }

//commenting due to demo2 no include KYC process
    /*private fun setup() {

        *//**kycDetailsModel not null then came from dashboard of oryx trade*//*
        if (kycDetailsModel == null) {
            kycDetailsModel = homeController.oldDashboardModel
        }
        rootView!!.tv_upload_text.setHtmlData(kycDetailsModel!!.payload!!.kycUploadMsg!!)

        *//* *rootView!!.tv_UpLoadedProof.setTextColor(ContextCompat.getColor(activity!!,R.color.proof_padding_review_text))
            rootView!!.cv_alreadyProofUploaded.setCardBackgroundColor(ContextCompat.getColor(activity!!,R.color.proof_padding_card_color))*//*

        when (kycDetailsModel!!.payload!!.kycStatus) {
            "0" -> {
                *//**kyc not uploaded yet*//*
                rootView!!.ll_upload_form.visibility = View.VISIBLE
            }
            "1" -> {
                *//**kyc uploaded and in aproval - pending*//*
                rootView!!.ll_upload_form.visibility = View.GONE
                rootView!!.cv_proof_status_msg.setCardBackgroundColor(
                    ContextCompat.getColor(
                        activity!!,
                        R.color.proof_padding_card_color
                    )
                )
                rootView!!.tv_proof_status_msg.setTextColor(
                    ContextCompat.getColor(
                        activity!!,
                        R.color.proof_padding_review_text
                    )
                )
                rootView!!.cv_proof_status_msg.visibility = View.VISIBLE
                rootView!!.tv_proof_status_msg.text = kycDetailsModel!!.payload!!.kycStatusMsg
                rootView!!.ll_upload_form.visibility = View.GONE
            }
            "2" -> {
                *//**kyc uploaded and approved*//*
            }
            "3" -> {
                *//**kyc uploaded and rejected*//*
                rootView!!.ll_upload_form.visibility = View.VISIBLE
            }
        }
        addObservers()
    }*/

    private fun onClickListeners() {

        rootView!!.tv_choose_id_proof.setOnClickListener {
            callPermissions(rootView!!.tv_selected_id_proof)
        }
        rootView!!.tv_choose_bank_account.setOnClickListener {
            callPermissions(rootView!!.tv_selected_bank_account)
        }
        rootView!!.cv_upload.setOnClickListener {

            if (rootView!!.tv_selected_id_proof.tag.toString().isEmpty()) {
                Toast.makeText(
                    activity!!,
                    getString(R.string.id_proof_required_tx),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (rootView!!.tv_selected_bank_account.tag.toString().isEmpty()) {
                Toast.makeText(
                    activity!!,
                    getString(R.string.bank_proof_required_tc),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                homeController.showProgressDialog()
                var id_proof: MultipartBody.Part? = null
                var bank_Proof: MultipartBody.Part? = null

                id_proof = createIdProof()
                bank_Proof = createBankProof()
                myMainViewModel!!.uploadKycProofs(id_proof = id_proof, bank_proof = bank_Proof)

                // uploadProofsViewModel!!.uploadProofFiles(id_proof, residence_Proof, bank_Proof)

            }
        }

    }

    private fun addObservers() {
        myMainViewModel!!.isLoading?.observe(this@KycFragment, Observer {
            if (it) {
                homeController.showProgressDialog()
            } else {
                homeController.dismissProgressDialog()
            }
        })

        myMainViewModel!!.kycUploadResponse?.observe(this@KycFragment, Observer {

            homeController.messageToast(responseBody = it)
            homeController.onBackPressed()
        })
    }


    private fun createBankProof(): MultipartBody.Part {
        return if (rootView!!.tv_selected_bank_account.tag.toString().endsWith("jpg") || rootView!!.tv_selected_bank_account.tag.toString().endsWith(
                "png"
            ) || rootView!!.tv_selected_bank_account.tag.toString().endsWith("jpeg")
        ) {
            prepareImageFilePart(
                "bank_proof",
                rootView!!.tv_selected_bank_account.tag.toString()
            )
        } else {
            preparePdfFilePart(
                "bank_proof",
                rootView!!.tv_selected_bank_account.tag.toString()
            )
        }
    }


    private fun createIdProof(): MultipartBody.Part {
        return if (rootView!!.tv_selected_id_proof.tag.toString().endsWith("jpg") || rootView!!.tv_selected_id_proof.tag.toString().endsWith(
                "png"
            ) || rootView!!.tv_selected_id_proof.tag.toString().endsWith("jpeg")
        ) {
            prepareImageFilePart(
                "id_proof",
                rootView!!.tv_selected_id_proof.tag.toString()
            )
        } else {
            preparePdfFilePart(
                "id_proof",
                rootView!!.tv_selected_id_proof.tag.toString()
            )
        }
    }


    private fun callPermissions(
        tvSelectedidproof: TextView
    ) {
        SelectedImage = tvSelectedidproof
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

    private fun showSelectionDailog() {
        mBottomSheetDialog = BottomSheetDialog(activity!!)
        val sheetView = activity!!.layoutInflater.inflate(R.layout.dialog_upload_file, null)
        mBottomSheetDialog!!.setContentView(sheetView)
        mBottomSheetDialog!!.choosePdf.visibility = View.GONE
        mBottomSheetDialog!!.view_select_docs.visibility = View.GONE
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

    @Throws(IOException::class)
    private fun myRotateImageIfRequired(img: Bitmap, selectedImage: String?): Bitmap {

        //  ExifInterface ei = new ExifInterface(selectedImage.getPath());
        val ei = ExifInterface(selectedImage.toString())
        val orientation =
            ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

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
            callPermissions(SelectedImage)
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
                try {
                    mImageBitmap =
                        myRotateImageIfRequired(mImageBitmap!!, Uri.parse(currentPhotoPath).path)
                } catch (e: Exception) {

                }

                SelectedImage.text = photoFile!!.name
                mSelectionPath = photoFile!!.absolutePath
                SelectedImage.tag = photoFile!!.absolutePath
                selectedType = Constants.codeCameraRequest
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

        if (data == null) {

            if (mSelectionPath == null) {

            }

            return
        }

        if (requestCode == Constants.codePickImageRequest) {
            val uri = data.data
            try {


                var bitmap=FileUtils.getBitmapFromUri(activity!!, uri)

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

                }

                SelectedImage.text =
                    mSelectionPath!!.substring(mSelectionPath!!.lastIndexOf("/")).replace("/", "")
                SelectedImage.tag = mSelectionPath
                selectedType = Constants.codePickImageRequest

            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        if (requestCode == Constants.codePickPdfRequest) {
            val uri = data.data

            mSelectionPath = FileUtils.getRealPath(activity!!, data.data!!)

            if (mSelectionPath != null) {
                SelectedImage.text =
                    mSelectionPath!!.substring(mSelectionPath!!.lastIndexOf("/")).replace("/", "")
                SelectedImage.tag = mSelectionPath
                selectedType = Constants.codePickPdfRequest
            } else {
                Toast.makeText(
                    activity,
                    getString(R.string.provide_valid_receipt),
                    Toast.LENGTH_SHORT
                ).show()
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

}

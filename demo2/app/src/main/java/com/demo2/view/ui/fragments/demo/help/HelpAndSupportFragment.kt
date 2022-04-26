package com.demo2.view.ui.fragments.demo.help


import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
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
import android.view.*
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.demo2.R
import com.demo2.utilities.Constants
import com.demo2.utilities.Constants.Companion.codeCameraRequest
import com.demo2.utilities.Constants.Companion.codePickImageRequest
import com.demo2.utilities.FileUtils
import com.demo2.view.service.MyViewModelFactory
import com.demo2.view.service.NetworkUtil
import com.demo2.view.ui.base.BaseFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.demo2.utilities.Pref
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_home.tv_title
import kotlinx.android.synthetic.main.dialog_create_ticket.*
import kotlinx.android.synthetic.main.dialog_upload_file.view.*
import kotlinx.android.synthetic.main.fragment_help_and_support.view.*
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
class HelpAndSupportFragment : BaseFragment() {

    var rootView: View? = null
    var mContext: Context? = null
    var helpAndSupportViewModel: HelpAndSupportViewModel? = null
    var closeTicketId: Int? = null
    lateinit var dialog: Dialog

    var helpAndSupportAllTicketModel: HelpAndSupportAllTicketModel? = null
    var helpAndSupportOpenTicketModel: HelpAndSupportAllTicketModel? = null
    var helpAndSupportCloseTicketModel: HelpAndSupportAllTicketModel? = null
    private val TAG = HelpAndSupportFragment::class.java.simpleName


    /**flags*/
    private var typeAllTicket = "typeAllTicket"
    private var typeOpenTicket = "typeOpenTicket"
    private var typeClosedTicket = "typeClosedTicket "
    private var currentSelectedTicketTab = typeAllTicket


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
    private var saveButtomClicked = false
    private var fileName = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        if (rootView == null) {
            rootView =
                inflater.inflate(R.layout.fragment_help_and_support, container, false)

        }
        mContext = activity

        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Pref.setLocale(activity!!, Pref.getLocalization(activity!!))

        homeController.tv_title.text = getString(R.string.help_amp_support)
        homeController.resideMenu!!.addIgnoredView(rootView!!.horizontal_scroll_ticket)
        homeController.unSelectBottomBar()
        homeController.ll_bottombar.visibility = View.GONE
        homeController.iv_navigation.visibility = View.GONE
        homeController.iv_back.visibility = View.VISIBLE
        if (activity!!.supportFragmentManager!!.findFragmentByTag("HelpAndSupportFragment_DBB") != null) {
            homeController.viewVisibleDrawerBottomBar(0)

            homeController.iv_help.setColorFilter(
                ContextCompat.getColor(
                    activity!!,
                    R.color.dashboard_selected
                )
            )
            homeController.tv_help.setTextColor(
                ContextCompat.getColor(
                    activity!!,
                    R.color.dashboard_selected
                )
            )
        } else if (activity!!.supportFragmentManager!!.findFragmentByTag("HelpAndSupportFragment_D") != null) {
            homeController.viewVisibleDrawerBottomBar(1)
        } else {
            homeController.viewVisibleDrawerBottomBar(2)
        }


        init()
        setUp()
        onClickListeners()
    }

    private fun init() {

        helpAndSupportViewModel = ViewModelProviders
            .of(
                this@HelpAndSupportFragment,
                MyViewModelFactory(HelpAndSupportViewModel(activity!!))
            )[HelpAndSupportViewModel::class.java]

    }

    private fun setUp() {
        rootView!!.rv_ticket_list.layoutManager =
            LinearLayoutManager(activity!!, LinearLayoutManager.VERTICAL, false)

        if (!helpAndSupportViewModel!!.helpAndSupportAllTicketModel?.hasObservers()!!) {
            addObservers()

            helpAndSupportViewModel?.getAllTicketList(0)
        }
    }

    private fun addObservers() {

        helpAndSupportViewModel!!.isLoading?.observe(this@HelpAndSupportFragment, Observer {
            if (it) {
                homeController.showProgressDialog()
            } else {
                homeController.dismissProgressDialog()

            }
        })
        helpAndSupportViewModel!!.responseError?.observe(this@HelpAndSupportFragment, Observer {
            homeController.errorBody(it)
        })

        helpAndSupportViewModel!!.helpAndSupportAllTicketModel?.observe(
            this@HelpAndSupportFragment,
            Observer {

                if (helpAndSupportAllTicketModel == null) {
                    helpAndSupportAllTicketModel = it

                    /**api called first time need to set new adapter*/
                    helpAndSupportAllTicketModel!!.payload!!.supportTicket?.let {
                        rootView!!.rv_ticket_list.adapter = HelpAndSupportAllTicketAdapter(
                            activity!!,
                            helpAndSupportAllTicketModel!!.payload!!.supportTicket,
                            this@HelpAndSupportFragment,
                            currentSelectedTicketTab

                        )
                    }
                } else {

                    helpAndSupportAllTicketModel = it

                    updateAllTicketUi()
                }

            })


        helpAndSupportViewModel!!.helpAndSupportOpenTicketModel?.observe(
            this@HelpAndSupportFragment,
            Observer {

                if (helpAndSupportOpenTicketModel == null) {
                    helpAndSupportOpenTicketModel = it

                    /**api called first time need to set new adapter*/
                    helpAndSupportOpenTicketModel!!.payload!!.supportTicket?.let {
                        rootView!!.rv_ticket_list.adapter = HelpAndSupportAllTicketAdapter(
                            activity!!,
                            helpAndSupportOpenTicketModel!!.payload!!.supportTicket,
                            this@HelpAndSupportFragment,
                            currentSelectedTicketTab
                        )
                    }
                } else {

                    helpAndSupportOpenTicketModel = it
                    updateOpenTicketUi()
                }

            })


        helpAndSupportViewModel!!.helpAndSupportCloseTicketModel?.observe(
            this@HelpAndSupportFragment,
            Observer {

                if (helpAndSupportCloseTicketModel == null) {
                    helpAndSupportCloseTicketModel = it


                    /**api called first time need to set new adapter*/
                    helpAndSupportCloseTicketModel!!.payload!!.supportTicket?.let {
                        rootView!!.rv_ticket_list.adapter = HelpAndSupportAllTicketAdapter(
                            activity!!,
                            helpAndSupportCloseTicketModel!!.payload!!.supportTicket,
                            this@HelpAndSupportFragment,
                            currentSelectedTicketTab
                        )
                    }
                } else {

                    helpAndSupportCloseTicketModel = it
                }

            })


        helpAndSupportViewModel!!.helpAndSupportCreateTicket?.observe(
            this@HelpAndSupportFragment,
            Observer {
                mSelectionPath = null
                if (dialog!!.isShowing) dialog!!.dismiss()
                val res = it.body()?.string()
                val jsonObject = JSONObject(res)

                // homeController.message("The ticket is created successfully.")
                homeController.message(jsonObject.getString("message"))

                val jsonPayload = jsonObject.optJSONObject("payload")
                val jsonSupportTicket = jsonPayload.optJSONObject("support_ticket")

                var tempObj: HelpAndSupportAllTicketModel.Payload.SupportTicket =
                    HelpAndSupportAllTicketModel.Payload.SupportTicket(
                        jsonSupportTicket?.getString("created_at"),
                        jsonSupportTicket?.getInt("id"),
                        jsonSupportTicket?.getString("is_deleted"),
                        jsonSupportTicket?.getString("is_read"),
                        jsonSupportTicket?.getString("slug"),
                        jsonSupportTicket?.getString("status"),
                        jsonSupportTicket?.getString("subject_id"),
                        jsonSupportTicket?.getString("updated_at"),
                        jsonSupportTicket?.getInt("user_id")
                    )

                if (helpAndSupportAllTicketModel != null) {
                    helpAndSupportAllTicketModel!!.payload!!.supportTicket?.add(0, tempObj)
                    updateAllTicketUi()
                }
                if (helpAndSupportOpenTicketModel != null) {
                    helpAndSupportOpenTicketModel!!.payload!!.supportTicket?.add(0, tempObj)
                    updateOpenTicketUi()
                }

            })


        helpAndSupportViewModel!!.helpAndSupportCloseTicket?.observe(
            this@HelpAndSupportFragment,
            Observer {

                val res = it.body()?.string()
                val jsonObject = JSONObject(res)

                homeController.message(jsonObject.getString("message"))

                val jsonPayload = jsonObject.optJSONObject("payload")
                val jsonSupportTicket = jsonPayload.optJSONObject("support_ticket")

                var tempObj: HelpAndSupportAllTicketModel.Payload.SupportTicket =
                    HelpAndSupportAllTicketModel.Payload.SupportTicket(
                        jsonSupportTicket?.getString("created_at"),
                        jsonSupportTicket?.getInt("id"),
                        jsonSupportTicket?.getString("is_deleted"),
                        jsonSupportTicket?.getString("is_read"),
                        jsonSupportTicket?.getString("slug"),
                        jsonSupportTicket?.getString("status"),
                        jsonSupportTicket?.getString("subject_id"),
                        jsonSupportTicket?.getString("updated_at"),
                        jsonSupportTicket?.getInt("user_id")
                    )

                if (helpAndSupportAllTicketModel != null) {
                    for (i in helpAndSupportAllTicketModel?.payload?.supportTicket!!.indices) {

                        if (helpAndSupportViewModel!!.passedSupportIdForCloseTicket == helpAndSupportAllTicketModel?.payload?.supportTicket!![i]!!.id) {
                            helpAndSupportAllTicketModel?.payload?.supportTicket?.set(i, tempObj)
                            break
                        }
                    }
//                    helpAndSupportAllTicketModel!!.payload!!.supportTicket?.add(0, tempObj)
                    updateAllTicketUi()
                }
                if (helpAndSupportOpenTicketModel != null) {
                    updateOpenTicketUi()
                }

                if (helpAndSupportCloseTicketModel != null) {
                    helpAndSupportCloseTicketModel!!.payload!!.supportTicket?.add(0, tempObj)
                    updateCloseTicketUi()
                }

            })

    }

    private fun updateCloseTicketUi() {
        helpAndSupportCloseTicketModel!!.payload!!.supportTicket?.let {

            (rootView!!.rv_ticket_list.adapter as HelpAndSupportAllTicketAdapter?)?.let { adapter ->
                /**already adapter set just need to notify*/
                adapter.notifyDataSetChanged()
            } ?: run {
                /**need to set new adapter*/
                rootView!!.rv_ticket_list.adapter = HelpAndSupportAllTicketAdapter(
                    activity!!,
                    helpAndSupportCloseTicketModel!!.payload!!.supportTicket,
                    this@HelpAndSupportFragment,
                    currentSelectedTicketTab
                )
            }

        }

    }

    private fun updateOpenTicketUi() {

        helpAndSupportOpenTicketModel!!.payload!!.supportTicket?.let {

            (rootView!!.rv_ticket_list.adapter as HelpAndSupportAllTicketAdapter?)?.let { adapter ->
                /**already adapter set just need to notify*/
                adapter.notifyDataSetChanged()
            } ?: run {
                /**need to set new adapter*/
                rootView!!.rv_ticket_list.adapter = HelpAndSupportAllTicketAdapter(
                    activity!!,
                    helpAndSupportOpenTicketModel!!.payload!!.supportTicket,
                    this@HelpAndSupportFragment,
                    currentSelectedTicketTab
                )
            }

        }

    }

    private fun updateAllTicketUi() {
        helpAndSupportAllTicketModel!!.payload!!.supportTicket?.let {

            (rootView!!.rv_ticket_list.adapter as HelpAndSupportAllTicketAdapter?)?.let { adapter ->
                /**already adapter set just need to notify*/
                adapter.notifyDataSetChanged()
            } ?: run {
                /**need to set new adapter*/
                rootView!!.rv_ticket_list.adapter = HelpAndSupportAllTicketAdapter(
                    activity!!,
                    helpAndSupportAllTicketModel!!.payload!!.supportTicket,
                    this@HelpAndSupportFragment,
                    currentSelectedTicketTab
                )
            }

        }
    }

    private fun onClickListeners() {

        /**managed ticket clicks below*/

        rootView!!.ln_all_ticket_type.setOnClickListener {
            if (currentSelectedTicketTab == typeAllTicket) {
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

            currentSelectedTicketTab = typeAllTicket

            selectTabView(rootView!!.tv_all_ticket_type)
//            unSelectFundTypes()
//            selectViewTab(rootView!!.view_all_ticket_type, rootView!!.tv_all_ticket_type)

            if (helpAndSupportAllTicketModel == null) {
                helpAndSupportViewModel!!.getAllTicketList(0)
            } else {
                rootView!!.rv_ticket_list.adapter = HelpAndSupportAllTicketAdapter(
                    activity!!,
                    helpAndSupportAllTicketModel!!.payload!!.supportTicket,
                    this@HelpAndSupportFragment,
                    currentSelectedTicketTab
                )
            }

        }
        rootView!!.ln_open_ticket_type.setOnClickListener {
            if (currentSelectedTicketTab == typeOpenTicket) {
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

            currentSelectedTicketTab = typeOpenTicket
            selectTabView(rootView!!.tv_open_ticket_type)
//            unSelectFundTypes()
//            selectViewTab(rootView!!.view_open_ticket_type, rootView!!.tv_open_ticket_type)

            if (helpAndSupportOpenTicketModel == null) {
                helpAndSupportViewModel!!.getOpenTicketList(0)
            } else {
                rootView!!.rv_ticket_list.adapter = HelpAndSupportAllTicketAdapter(
                    activity!!,
                    helpAndSupportOpenTicketModel!!.payload!!.supportTicket,
                    this@HelpAndSupportFragment,
                    currentSelectedTicketTab
                )
            }

        }
        rootView!!.ln_closed_ticket_type.setOnClickListener {
            if (currentSelectedTicketTab == typeClosedTicket) {
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

            currentSelectedTicketTab = typeClosedTicket
            selectTabView(rootView!!.tv_closed_ticket_type)
//            unSelectFundTypes()
//            selectViewTab(rootView!!.view_closed_ticket_type, rootView!!.tv_closed_ticket_type)

            if (helpAndSupportCloseTicketModel == null) {
                helpAndSupportViewModel?.getCloseTicketList(0)
            } else {
                rootView!!.rv_ticket_list.adapter = HelpAndSupportAllTicketAdapter(
                    activity!!,
                    helpAndSupportCloseTicketModel!!.payload!!.supportTicket,
                    this@HelpAndSupportFragment,
                    currentSelectedTicketTab
                )
            }

        }

        homeController.cv_create_ticket.setOnClickListener {

            if (!NetworkUtil.isInternetAvailable(activity!!)) {
                Toast.makeText(
                    activity!!,
                    getString(R.string.no_internet_connection),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            createTicketDialog()
        }

        /**new pagination handling with offset*/
        rootView!!.scroll.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (v.getChildAt(v.childCount - 1) != null) {
                if ((scrollY >= (v.getChildAt(v.childCount - 1).measuredHeight - v.measuredHeight))
                    && scrollY > oldScrollY
                ) {
                    if (currentSelectedTicketTab == typeAllTicket) {

                        if (!helpAndSupportAllTicketModel!!.paginationEndedAll) {
                            helpAndSupportViewModel?.getAllTicketList(helpAndSupportAllTicketModel!!.payload!!.supportTicket!!.size)

                        }

                    } else if (currentSelectedTicketTab == typeOpenTicket) {

                        if (!helpAndSupportOpenTicketModel!!.paginationEndedOpen) {
                            helpAndSupportViewModel?.getOpenTicketList(helpAndSupportOpenTicketModel!!.payload!!.supportTicket!!.size)

                        }

                    } else {

                        if (!helpAndSupportCloseTicketModel!!.paginationEndedClose) {
                            helpAndSupportViewModel?.getCloseTicketList(
                                helpAndSupportCloseTicketModel!!.payload!!.supportTicket!!.size
                            )

                        }

                    }
                }
            }
        })

    }

    private fun selectTabView(tvOtmWalletType: TextView) {
        rootView!!.tv_all_ticket_type.background = null
        rootView!!.tv_open_ticket_type.background = null
        rootView!!.tv_closed_ticket_type.background = null

        tvOtmWalletType.background =
            ContextCompat.getDrawable(context!!, R.drawable.bg_register_tab_selected)

    }

    private fun unSelectFundTypes() {
        rootView!!.view_all_ticket_type.visibility = View.GONE
        rootView!!.view_open_ticket_type.visibility = View.GONE
        rootView!!.view_closed_ticket_type.visibility = View.GONE
        rootView!!.ln_all_ticket_type.setBackgroundColor(
            ContextCompat.getColor(
                activity!!,
                android.R.color.transparent
            )
        )

        rootView!!.ln_open_ticket_type.setBackgroundColor(
            ContextCompat.getColor(
                activity!!,
                android.R.color.transparent
            )
        )

        rootView!!.ln_closed_ticket_type.setBackgroundColor(
            ContextCompat.getColor(
                activity!!,
                android.R.color.transparent
            )
        )

        rootView!!.tv_all_ticket_type.setTextColor(
            ContextCompat.getColor(
                activity!!,
                R.color.gray
            )
        )
        rootView!!.tv_open_ticket_type.setTextColor(
            ContextCompat.getColor(
                activity!!,
                R.color.gray
            )
        )
        rootView!!.tv_closed_ticket_type.setTextColor(
            ContextCompat.getColor(
                activity!!,
                R.color.gray
            )
        )
    }

    override fun onResume() {
        super.onResume()
        homeController.cv_create_ticket.visibility = View.VISIBLE

    }

    override fun onPause() {
        super.onPause()
        homeController.cv_create_ticket.visibility = View.GONE

    }

    private fun createTicketDialog() {
        photoFile = null

        dialog = Dialog(activity!!)
        dialog.setContentView(R.layout.dialog_create_ticket)
        dialog.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setGravity(Gravity.CENTER)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.iv_close.setOnClickListener { dialog.dismiss() }

        dialog.cv_cancel.setOnClickListener { dialog.dismiss() }

        dialog.btn_file_choose.setOnClickListener {
            callPermissions()
        }
        saveButtomClicked = false
        val viewTreeObserverBank = dialog.rl_spinner.viewTreeObserver

        if (viewTreeObserverBank.isAlive) {
            viewTreeObserverBank.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    try {
                        dialog!!.rl_spinner.viewTreeObserver.removeOnGlobalLayoutListener(
                            this
                        )

                        dialog!!.sp_title_type.dropDownWidth =
                            dialog!!.rl_spinner.measuredWidth
                    } catch (e: Exception) {

                    }

                }
            })
        }


        dialog.sp_title_type.adapter = HighLightArrayAdapterV2(
            context = activity!!,
            dropdownResource = R.layout.row_spinner_create_ticket_dropdown,
            viewResource = R.layout.row_spinner_login,
            objects = ArrayList<String>().apply {
                helpAndSupportAllTicketModel?.payload!!.supportSubject!!.forEach {
                    this.add(it!!.subjectEn.toString().trim())
                }
            })

        dialog.sp_title_type.setOnTouchListener(View.OnTouchListener { v, event ->
            hideSoftKeyboard()
            dialog.currentFocus?.clearFocus()
            false
        })
        dialog.sp_title_type.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                dialog.sp_title_type.adapter.let {
                    var x: HighLightArrayAdapterV2 = it as HighLightArrayAdapterV2
                    x.setSelection(p2)
                }
            }
        }

        dialog.cv_save.setOnClickListener {

            if (dialog.edt_message.text.toString().trim().isEmpty()) {
                Toast.makeText(
                    activity!!,
                    getString(R.string.message_validation),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                helpAndSupportViewModel!!.isLoading!!.value = true

                photoFile?.let {
                    mSelectionPath = photoFile!!.absolutePath
                    dialog.tv_file_name.text = photoFile!!.name
                }

                selectedType = Constants.codeCameraRequest

                if (!saveButtomClicked) {
                    saveButtomClicked = true
                    if (mSelectionPath == null || mSelectionPath!!.isEmpty()) {

                        helpAndSupportViewModel?.createNewTicket(
                            helpAndSupportAllTicketModel?.payload!!.supportSubject!!.get(
                                dialog.sp_title_type.selectedItemPosition
                            )!!.id!!, dialog!!.edt_message.text.toString().trim()
                        )

                    } else {

                        var attachment: MultipartBody.Part? = null

                        attachment =
                            if (selectedType == codeCameraRequest || selectedType == codePickImageRequest) {
                                prepareImageFilePart("attachment[]", mSelectionPath!!)
                            } else {
                                preparePdfFilePart("attachment[]", mSelectionPath!!)
                            }

                        helpAndSupportViewModel?.createNewTicket(
                            subject_id = helpAndSupportAllTicketModel?.payload!!.supportSubject!!.get(
                                dialog.sp_title_type.selectedItemPosition
                            )!!.id!!.toString().trim()
                                .toRequestBody("text/plain".toMediaTypeOrNull()),
                            message = dialog!!.edt_message.text.toString().trim()
                                .toRequestBody("text/plain".toMediaTypeOrNull()),
                            attachment = attachment
                        )

                    }
                } else {
                }
            }
        }

        dialog.show()

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
            fileName = photoFile!!.name
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
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
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
                mSelectionPath = photoFile!!.absolutePath

                selectedType = Constants.codeCameraRequest

                dialog.tv_file_name.text = fileName

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

                        Log.d(TAG, "onActivityResult: " + imageUri)

                    }
                } else if (data.data != null) {
                    var bitmap = FileUtils.getBitmapFromUri(activity!!, uri)
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

                    dialog.tv_file_name.text =
                        mSelectionPath!!.substring(mSelectionPath!!.lastIndexOf("/") + 1)
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
                        Log.e(
                            "zxczxc",
                            " real path - " + mSelectionPath + "    ----   " + pdfUri.path
                        )
                        dialog.tv_file_name.text =
                            mSelectionPath!!.substring(mSelectionPath!!.lastIndexOf("/") + 1)

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
                    Log.e("zxczxc", " real path - " + mSelectionPath + "    ----   " + uri.path)
                    dialog.tv_file_name.text =
                        mSelectionPath!!.substring(mSelectionPath!!.lastIndexOf("/") + 1)

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

        if (string.equals("dialog")) {
            closeTicket(position)

        } else {
            var bottomBarVisibility = homeController!!.ll_bottombar.visibility
            homeController.addLoadFragment(
                HelpAndSupportChatFragment().apply {

                    if (currentSelectedTicketTab == typeAllTicket) {
                        this.supportId =
                            helpAndSupportAllTicketModel!!.payload!!.supportTicket?.get(position)!!.id
                        this.supportTitle =
                            helpAndSupportAllTicketModel!!.payload!!.supportTicket?.get(position)!!.subjectId

                    } else if (currentSelectedTicketTab == typeOpenTicket) {
                        this.supportId =
                            helpAndSupportOpenTicketModel!!.payload!!.supportTicket?.get(position)!!.id
                        this.supportTitle =
                            helpAndSupportOpenTicketModel!!.payload!!.supportTicket?.get(position)!!.subjectId

                    } else if (currentSelectedTicketTab == typeClosedTicket) {
                        this.supportId =
                            helpAndSupportCloseTicketModel!!.payload!!.supportTicket?.get(position)!!.id
                        this.supportTitle =
                            helpAndSupportCloseTicketModel!!.payload!!.supportTicket?.get(position)!!.subjectId

                    }
                    this.bottomBarVisibility = bottomBarVisibility
                },
                "HelpAndSupportChatFragment", this.javaClass.simpleName
            )
        }

    }

    private fun closeTicket(position: Int) {
        androidx.appcompat.app.AlertDialog.Builder(mContext!!, R.style.MyDialogTheme).let {
            it.setMessage(getString(R.string.close_ticket_message))
            it.setPositiveButton(
                getString(R.string.yes)

            ) { dialog, _ ->

                if (currentSelectedTicketTab == typeAllTicket) {

                    closeTicketId =
                        helpAndSupportAllTicketModel!!.payload!!.supportTicket?.get(position)!!.id
                    helpAndSupportViewModel?.closeTicket(closeTicketId!!, typeAllTicket)
                } else if (currentSelectedTicketTab == typeOpenTicket) {
                    closeTicketId =
                        helpAndSupportOpenTicketModel!!.payload!!.supportTicket?.get(position)!!.id!!
                    helpAndSupportViewModel?.closeTicket(closeTicketId!!, typeOpenTicket)
                }
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

}

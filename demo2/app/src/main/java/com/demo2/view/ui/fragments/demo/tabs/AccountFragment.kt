package com.demo2.view.ui.fragments.demo.tabs


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.demo2.R
import com.demo2.utilities.Pref
import com.demo2.view.service.MyViewModelFactory
import com.demo2.view.ui.base.BaseFragment
import com.demo2.view.ui.fragments.demo.account.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_account.view.*


class AccountFragment : BaseFragment() {

    var rootview: View? = null
    private var myAccountDetailViewModel: MyAccountDetailViewModel? = null
    var myAccountPayloadModel: MyAccountDetailModel.Payload? = null
    var fromWithdrawal: Boolean? = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //if (rootview == null) {
        rootview = inflater.inflate(R.layout.fragment_account, container, false)
        init()
        setup()
        onClickListeners()
        //}
        return rootview
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Pref.setLocale(activity!!, Pref.getLocalization(activity!!))

        homeController.tv_title.visibility = View.VISIBLE
        homeController.toolbar.visibility = View.VISIBLE

        homeController.tv_title.text = getString(R.string.title_account)

        homeController.unSelectBottomBar()
        homeController.visibleBottomBar(0)

        homeController.iv_navigation.visibility = View.VISIBLE


        homeController.iv_back.visibility = View.GONE

        if (activity!!.supportFragmentManager!!.findFragmentByTag("AccountFragment_DBB") != null) {
            homeController.viewVisibleDrawerBottomBar(0)
        }

        homeController.iv_account.setColorFilter(
            ContextCompat.getColor(
                activity!!,
                R.color.dashboard_selected
            )
        )
        homeController.tv_account.setTextColor(
            ContextCompat.getColor(
                activity!!,
                R.color.dashboard_selected
            )
        )
    }

    private fun init() {
        myAccountDetailViewModel = ViewModelProviders.of(
            this@AccountFragment,
            MyViewModelFactory(MyAccountDetailViewModel(activity!!))
        )[MyAccountDetailViewModel::class.java]

        myAccountDetailViewModel!!.myaccountDetailCall(Pref.getLocalization(activity!!))
    }

    private fun setup() {
        if (userModel.payload!!.user!!.packageId == "0") {
            rootview!!.ll_customer_support.visibility = View.GONE
        } else {
            rootview!!.ll_customer_support.visibility = View.VISIBLE

        }
        addObservers()

    }


    private fun onClickListeners() {
        rootview!!.cvCopyLink.setOnClickListener{
            val clipboard: ClipboardManager =
                activity!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            if (clipboard != null) {
                val clip = ClipData.newPlainText(
                    "label",
                   "temp change"
                )
                clipboard.setPrimaryClip(clip)
                           Toast.makeText(activity, getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show();
            }
        }
        rootview!!.cv_my_account.setOnClickListener {
            var myAccountFragment = MyAccountFragment()
            var bundle = Bundle()
            bundle.putString(
                "myAccountPayloadModel",
                Gson().toJson(myAccountPayloadModel)
            )
            myAccountFragment.arguments = bundle
            homeController.loadFragment(
                myAccountFragment,
                "MyAccountFragment",
                this.javaClass.simpleName
            )

        }
        rootview!!.ll_customer_support.setOnClickListener {
            homeController.loadFragment(
                CustomerSupportFragment(),
                "RegisterUserFragment",
                this.javaClass.simpleName
            )
        }
        rootview!!.cv_register_downline.setOnClickListener {
            homeController.loadFragment(
                RegisterUserFragment(),
                "RegisterUserFragment",
                this.javaClass.simpleName
            )

        }
        rootview!!.cv_my_network.setOnClickListener {
            homeController.loadFragment(
                NetworkTreeFragment().apply {
                    this.rootDownlines = myAccountPayloadModel!!.myAccount!!.directDownlines
                },
                "NetworkTreeFragment",
                this.javaClass.simpleName
            )

        }

        /*rootview!!.ll_your_booking.setOnClickListener {
            homeController.loadFragment(
                BookingFragment().apply {
                    this.fromHotel = false
                },
                "BookingFragment",
                this.javaClass.simpleName
            )
        }*/

        rootview!!.cv_upload_proof.setOnClickListener {
            var uploadProofsFragment = UploadProofsFragment()
            var bundle = Bundle()
            bundle.putString("upload_status", myAccountPayloadModel!!.upload_status)
            bundle.putString("message_display", myAccountPayloadModel!!.message_display)
            bundle.putString("proof_pending_msg", myAccountPayloadModel!!.proof_pending_msg)
            uploadProofsFragment.arguments = bundle
            homeController.loadFragment(
                uploadProofsFragment,
                "UploadProofsFragment",
                this.javaClass.simpleName
            )
        }

    }

    private fun addObservers() {
        myAccountDetailViewModel!!.responseError!!.observe(this@AccountFragment, Observer {
            it?.let {
                homeController.errorBody(it)
            }
        })


        myAccountDetailViewModel!!.isLoading!!.observe(this@AccountFragment, Observer {
            it?.let {
                if (it) {
                    homeController.showProgressDialog()
                } else {
                    homeController.dismissProgressDialog()
                }
            }
        })

        myAccountDetailViewModel!!.myAccountDetailModel!!.observe(this@AccountFragment, Observer {
            it?.let {
                myAccountPayloadModel = it!!.payload
                //Log.e(TAG, "addObservers: ${myAccountPayloadModel!!.myAccount!!.username}")
                rootview!!.tv_username.text =
                    "Welcome, ${myAccountPayloadModel!!.myAccount!!.name}"
                rootview!!.tv_balance_amount.text =
                    "$${parseDouble(myAccountPayloadModel!!.currentBalance!!)}"
                myAccountPayloadModel!!.currentPackage?.let {
                    rootview!!.tv_package_amount.text =
                        "$${parseDouble(myAccountPayloadModel!!.currentPackage!!.amount!!)} ${getString(
                            R.string.package_tag
                        )}"

                } ?: run {
                    rootview!!.tv_package_amount.text =
                        "${getString(R.string.not_available)} ${getString(R.string.package_tag)}"
                }
                rootview!!.tv_risk_disclose.text = myAccountPayloadModel!!.riskDiscloser
                //myAccountDetailViewModel!!.myAccountDetailModel!!.removeObservers(this@AccountFragment)

                if (myAccountPayloadModel!!.myAccount!!.proofStatus.equals("1")) {
                    rootview!!.ll_upload_proof.visibility = View.GONE
                    rootview!!.cv_upload_proof.visibility = View.GONE
                } else {
                    rootview!!.ll_upload_proof.visibility = View.VISIBLE
                    rootview!!.cv_upload_proof.visibility = View.VISIBLE
                }

                if (fromWithdrawal!!) {
                    fromWithdrawal = false
                    rootview!!.ll_my_account.performClick()
                }
            }
            rootview!!.ll_main.visibility = View.VISIBLE
        })
    }
}

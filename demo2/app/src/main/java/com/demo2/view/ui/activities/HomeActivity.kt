package com.demo2.view.ui.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.special.ResideMenu.ResideMenu
import com.demo2.R
import com.demo2.utilities.Constants
import com.demo2.utilities.Pref
import com.demo2.utilities.makeGone
import com.demo2.utilities.makeVisible
import com.demo2.view.service.ApiClient
import com.demo2.view.ui.base.BaseActivity
import com.demo2.view.ui.fragments.common.*
import com.demo2.view.ui.fragments.demo.account.*
import com.demo2.view.ui.fragments.demo.help.HelpAndSupportFragment
import com.demo2.view.ui.fragments.demo.report.TradingProfitReportFragment
import com.demo2.view.ui.fragments.demo.selftrading.SelfTradingFragment
import com.demo2.view.ui.fragments.demo.stock.StockMarketFragment
import com.demo2.view.ui.fragments.demo.stock.StockHelpAndFaqFragment

import com.demo2.view.ui.fragments.demo.tabs.AccountFragment
import com.demo2.view.ui.fragments.demo.tabs.HelpFragment
import com.demo2.view.ui.fragments.demo.tabs.HomeFragment
import com.demo2.view.ui.fragments.demo.wallets.*
import kotlinx.android.synthetic.main.activity_home.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class HomeActivity : BaseActivity() {

    var resideMenu: ResideMenu? = null
    val TAG = this.javaClass.simpleName
    var oldDashboardModel: TradeDashboardModel? = null
    //  var shownMessage = false


    /**Navigation options*/
    var navFundWallet: TextView? = null
    var navCommission: TextView? = null
    var navWithdrawalWallet: TextView? = null
    var navCapitalWithdrawal: TextView? = null
    var navNetworkTree: TextView? = null
    var navYourProfile: TextView? = null

    var navOryx: TextView? = null
    var navOryxHotel: TextView? = null
    var navSelfTrading: TextView? = null
    var navKycVerification: TextView? = null
    var navPackages: TextView? = null
    var navTermsOfUse: TextView? = null
    var navPrivacyPolicy: TextView? = null
    var navLanguageSpinner: Spinner? = null

    //new options add in drawer
    var tv_menu_nav: TextView? = null
    var ll_dash_nav: LinearLayout? = null
    var img_dashboard_nav: ImageView? = null
    var tv_dashboard_nav: TextView? = null

    var ll_my_profile_nav_main: LinearLayout? = null
    var img_my_profile_nav_main: ImageView? = null
    var tv_my_profile_nav_main: TextView? = null

    var ll_my_network_nav_main: LinearLayout? = null
    var img_my_network_nav_main: ImageView? = null
    var tv_my_network_nav_main: TextView? = null
    var img_my_network_right: ImageView? = null

    var ll_my_network_nav_child: LinearLayout? = null
    var ll_network_tree_nav_child_one: LinearLayout? = null
    var ll_registered_account_nav_child_one: LinearLayout? = null
    var tv_network_tree_nav_child: TextView? = null
    var tv_registered_account_nav_child: TextView? = null

    var ll_funding_nav_main: LinearLayout? = null
    var img_funding_nav_main: ImageView? = null
    var tv_funding_nav_main: TextView? = null
    var img_funding_right: ImageView? = null

    var ll_funding_nav_child: LinearLayout? = null
    var ll_topup_fund_nav_child_one: LinearLayout? = null
    var ll_buy_package_nav_child_one: LinearLayout? = null
    var ll_capital_withdrawal_nav_child_one: LinearLayout? = null
    var tv_topup_fund_nav_child: TextView? = null
    var tv_buy_package_nav_child: TextView? = null
    var tv_capital_withdrawal_nav_child: TextView? = null

    var ll_earnings_nav_main: LinearLayout? = null
    var img_earning_nav_main: ImageView? = null
    var tv_earnings_nav_main: TextView? = null
    var img_earnings_right: ImageView? = null

    var ll_earnings_nav_child: LinearLayout? = null
    var ll_cash_wallet_nav_child_one: LinearLayout? = null
    var tv_cash_wallet_nav_child: TextView? = null


    var ll_stock_nav_main: LinearLayout? = null
    var ll_stock_nav_top: LinearLayout? = null

    var img_stock_nav_main: ImageView? = null
    var tv_stock_nav_main: TextView? = null
    var img_stock_right: ImageView? = null

    var ll_stock_nav_child: LinearLayout? = null
    var ll_stock_nav_child_one: LinearLayout? = null
    var tv_stock_nav_child: TextView? = null
    var tv_stock_nav_child_one: TextView? = null
    var ll_stock_wallet_nav_child_one: LinearLayout? = null
    var ll_stock_wallet_nav_child_third: LinearLayout? = null
    var tv_stock_nav_child_two: TextView? = null


    var ll_withdrawal_nav: LinearLayout? = null
    var img_withdrawal_nav: ImageView? = null
    var tv_withdrawal_nav: TextView? = null


    var ll_selftrading_nav: LinearLayout? = null
    var img_selftrading_nav: ImageView? = null
    var tv_selftrading_nav: TextView? = null

    var ll_report_nav: LinearLayout? = null
    var tv_total_report_nav: TextView? = null
    var tv_trading_profit_nav: TextView? = null
    var tv_lot_rebate_nav: TextView? = null
    var tv_lot_rebate_commission_nav: TextView? = null
    var tv_lot_leadership_bonus_nav: TextView? = null
    var tv_profit_sharing_nav: TextView? = null
    var tv_overriding_nav: TextView? = null

    var ll_setting_nav: LinearLayout? = null
    var img_setting_nav: ImageView? = null
    var tv_settings_nav: TextView? = null

    var ll_news_nav: LinearLayout? = null
    var img_news_nav: ImageView? = null
    var tv_news_nav: TextView? = null

    var ll_faq_nav: LinearLayout? = null
    var img_faq_nav: ImageView? = null
    var tv_faq_nav: TextView? = null

    var ll_support_nav: LinearLayout? = null
    var img_support_nav: ImageView? = null
    var tv_support_nav: TextView? = null
    var ll_logout_nav: LinearLayout? = null
    var img_logout_nav: ImageView? = null
    var tv_logout_nav: TextView? = null

    var ll_qr_code_nav: LinearLayout? = null
    var cvCopyLink: CardView? = null
    var fl_qr_code_nav: FrameLayout? = null
    var tv_tap_to_show_qr_code: TextView? = null
    var tv_click_to_copy: TextView? = null
    var my_profile_viewTagSelected: String? = null


    var ll_news_tools_nav_main: LinearLayout? = null
    var img_news_tools_nav_main: ImageView? = null
    var tv_news_tools_nav_main: TextView? = null
    var img_news_tools_right: ImageView? = null

    var ll_news_event_nav_child: LinearLayout? = null
    var ll_news_event_nav_child_one: LinearLayout? = null
    var ll_industry_tools_nav_child_one: LinearLayout? = null
    var tv_news_event_nav_child: TextView? = null
    var tv_industry_tools_nav_child: TextView? = null

    var ll_report_nav_child_one: LinearLayout? = null
    var ll_my_network_nav_top: LinearLayout? = null
    var ll_funding_nav_top: LinearLayout? = null
    var ll_earnings_nav_top: LinearLayout? = null
    var ll_news_tools_nav_top: LinearLayout? = null
    var viewMyNetwork: View? = null
    var viewFunding: View? = null
    var viewEarning: View? = null
    var viewStock: View? = null

    var viewWithdrawal: View? = null
    var viewNewsAndTools: View? = null
    var viewHelpAndFAQ: View? = null
    var viewSupport: View? = null
    var tvUserName: TextView? = null
    var tvRank: TextView? = null
    var tvStatus: TextView? = null
    var ivSettings: ImageView? = null
    var ivMessage: ImageView? = null
    var ivProfile: ImageView? = null
    var ivLogout: ImageView? = null
    var ivProfilePhoto: ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Pref.setLocale(this@HomeActivity, Pref.getLocalization(this@HomeActivity))
        setContentView(R.layout.activity_home)
        addListeners()
        setup()
        setupNavigationMenu()
        setupBottomTextSizes()
        setBottomTabClick()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onBackPressed() {
        if (resideMenu!!.isOpened) {
            resideMenu!!.closeMenu()
        } else {
            // super.onBackPressed()

            /**************************************setup whole navigation of app here**********************************************************************************8*/

            supportFragmentManager.findFragmentById(R.id.fragment_container).let {
                when (it) {

                    is AccountFragment -> {
                        // popFragmentsUntil(HomeFragment().javaClass.simpleName)
                        Log.e("zxczxc", "onBackPressed AccountFragment")
                        ll_home.performClick()
                    }
                    is HelpFragment -> {
                        Log.e("zxczxc", "onBackPressed HelpFragment")
                        ll_home.performClick()
                    }
                    is SettingsFragment -> {
                        Log.e("zxczxc", "onBackPressed SettingsFragment")
                        supportFragmentManager.popBackStack(
                            null,
                            FragmentManager.POP_BACK_STACK_INCLUSIVE
                        )
                        ll_home!!.performClick()
                    }

                    is UploadProofsFragment -> {

                        Log.e("zxczxc", "onBackPressed UploadProofsFragment")
                        if ((it as UploadProofsFragment).fromWithdrawalWallet) {
                            ll_home.performClick() //ll_account
                        } else {
                            super.onBackPressed()
                        }

                    }
                    is CapitalWithdrawalWallet -> {

                        if (supportFragmentManager.backStackEntryCount <= 1) {
                            ll_home.performClick()
                        } else {
                            super.onBackPressed()
                        }

                    }
                    is ProfitCapitalWalletCapitalFormStep4 -> {
                        //clearFragment(CapitalWithdrawalWallet().javaClass.simpleName)
                        supportFragmentManager.popBackStack(
                            null,
                            FragmentManager.POP_BACK_STACK_INCLUSIVE
                        )
                        ll_capital_withdrawal_nav_child_one!!.performClick()
                    }
                    is KycFragment -> {
                        ll_home.performClick()
                    }
                    else -> {
                        Log.e(
                            "zxczxc",
                            "onBackPressed fragment backstacks - ${supportFragmentManager.backStackEntryCount}"
                        )
                        if (supportFragmentManager.backStackEntryCount <= 1) {
                            finishAffinity()
                        } else {
                            super.onBackPressed()
                        }
                    }
                }
            }
            /**************************************setup whole navigation of app here**********************************************************************************8*/
        }
    }

    private fun setup() {
        Pref.setLocale(this@HomeActivity, Pref.getLocalization(this@HomeActivity))
        Log.d(TAG, "token - ${userModel?.payload?.token}")
        loadFragment(HomeFragment(), "HomeFragment")
        //     userModel = Pref.getUserModel(this)
        //   loadFragment(WalletsFragment(), "WalletFragment")
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return resideMenu!!.dispatchTouchEvent(ev)
    }

    override fun onResume() {
        super.onResume()
        /*     userModel = Pref.getUserModel(this)
             Glide.with(this).load(userModel!!.payload!!.user!!.profile_image)
                 .apply(RequestOptions().placeholder(R.drawable.user_empty))
                 .into(resideMenu!!.layoutLeftMenu.findViewById(R.id.ivProfilePhoto))
             resideMenu!!.layoutLeftMenu!!.findViewById<TextView>(R.id.tvUserName)
                 .setText(userModel!!.payload!!.user!!.username)
             resideMenu!!.layoutLeftMenu!!.findViewById<TextView>(R.id.tvRank)
                 .setText(userModel!!.payload!!.user!!.rank_detail!!.name)*/

    }

    private fun setupNavigationMenu() {
        resideMenu = ResideMenu(this)

        //resideMenu!!.setBackground(R.drawable.navigation_back)
        resideMenu!!.attachToActivity(this)
        resideMenu!!.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT)
        resideMenu!!.setSwipeDirectionDisable(ResideMenu.DIRECTION_LEFT)
        resideMenu!!.setScaleValue(0.0f)
        resideMenu!!.menuListener = object : ResideMenu.OnMenuListener {
            override fun openMenu() {
                Log.e("zxczxc", "openMenu()")
//                resideMenu!!.menuHolder.background = ContextCompat.getDrawable(this@HomeActivity, R.mipmap.sidemenu_bg)
                resideMenu!!.menuHolder.background = ContextCompat.getDrawable(
                    this@HomeActivity,
                    R.mipmap.sidemenu_bg_new
                )

                // userModel = Pref.getUserModel(this@HomeActivity)

                Glide.with(this@HomeActivity)
                    .load(Pref.getValue(this@HomeActivity, Constants.prefProfile, ""))
                    .apply(RequestOptions().placeholder(R.drawable.user_empty))
                    .into(resideMenu!!.layoutLeftMenu.findViewById(R.id.ivProfilePhoto))
                resideMenu!!.layoutLeftMenu!!.findViewById<TextView>(R.id.tvUserName)
                    .setText(userModel!!.payload!!.user!!.username)
                resideMenu!!.layoutLeftMenu!!.findViewById<TextView>(R.id.tvRank)
                    .setText(Pref.getValue(this@HomeActivity, Constants.prefRank, ""))

                setViewForDrawerItemSelectionHighlight()
                hideSoftKeyboard()

            }

            override fun closeMenu() {
                Log.e("zxczxc", "closeMenu()")
                resideMenu!!.menuHolder.background = null

            }

        }
        /**------------Initializing Navigation options' views------------------------------------*/

        navFundWallet = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_fund_wallet)
        navCommission = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_commission)
        navWithdrawalWallet = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_withdrawal_wallet)
        navCapitalWithdrawal =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_capital_withdrawal)
        navNetworkTree = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_network_tree)

        navYourProfile = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_your_profile)

        navOryx = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_oryx)
        navOryxHotel = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_oryx_hotel)
        navSelfTrading = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_self_trading)
        navKycVerification = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_kyc_verification)
        navPackages = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_packages)
        navTermsOfUse = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_terms_of_use)
        navPrivacyPolicy = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_privacy_policy)
        navLanguageSpinner = resideMenu!!.scrollViewLeftMenu!!.findViewById(R.id.sp_SelectLanguage1)

        ll_dash_nav = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ll_dash_nav)
        tv_menu_nav = resideMenu!!.scrollViewLeftMenu!!.findViewById(R.id.tv_menu_nav)
        img_dashboard_nav = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.img_dashboard_nav)
        tv_dashboard_nav = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_dashboard_nav)


        ll_my_profile_nav_main =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ll_my_profile_nav_main)
        img_my_profile_nav_main =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.img_my_profile_nav_main)
        tv_my_profile_nav_main =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_my_profile_nav_main)

        ll_my_network_nav_main =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ll_my_network_nav_main)
        img_my_network_nav_main =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.img_my_network_nav_main)
        tv_my_network_nav_main =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_my_network_nav_main)
        img_my_network_right = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.img_my_network_right)
        viewMyNetwork = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.viewMyNetwork)


        ll_my_network_nav_child =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ll_my_network_nav_child)
        ll_network_tree_nav_child_one =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ll_network_tree_nav_child_one)
        ll_registered_account_nav_child_one =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ll_registered_account_nav_child_one)
        tv_network_tree_nav_child =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_network_tree_nav_child)
        tv_registered_account_nav_child =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_registered_account_nav_child)

        ll_funding_nav_main = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ll_funding_nav_main)
        img_funding_nav_main = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.img_funding_nav_main)
        tv_funding_nav_main = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_funding_nav_main)
        img_funding_right = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.img_funding_right)
        viewFunding = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.viewFunding)

        ll_funding_nav_child = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ll_funding_nav_child)
        ll_topup_fund_nav_child_one =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ll_topup_fund_nav_child_one)
        ll_buy_package_nav_child_one =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ll_buy_package_nav_child_one)
        ll_capital_withdrawal_nav_child_one =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ll_capital_withdrawal_nav_child_one)
        tv_topup_fund_nav_child =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_topup_fund_nav_child)
        tv_buy_package_nav_child =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_buy_package_nav_child)
        tv_capital_withdrawal_nav_child =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_capital_withdrawal_nav_child)

        ll_earnings_nav_main = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ll_earnings_nav_main)
        img_earning_nav_main = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.img_earning_nav_main)
        tv_earnings_nav_main = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_earnings_nav_main)
        img_earnings_right = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.img_earnings_right)
        viewEarning = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.viewEarning)

        ll_stock_nav_main = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ll_stock_nav_main)
        ll_stock_nav_top = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ll_stock_nav_top)

        img_stock_nav_main = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.img_stock_nav_main)
        tv_stock_nav_main = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_stock_nav_main)
        img_stock_right = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.img_stock_right)
        viewStock = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.view_stock)

        ll_stock_nav_child = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ll_stock_nav_child)
        ll_stock_nav_child_one =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ll_stock_nav_child_one)
        ll_stock_wallet_nav_child_one =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ll_stock_wallet_nav_child_one)

        tv_stock_nav_child = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_stock_nav_child)
        tv_stock_nav_child_one =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_stock_wallet_nav)

        ll_stock_wallet_nav_child_third =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ll_stock_wallet_nav_child_three)
        tv_stock_nav_child_two = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_stock_help_faq_nav)


        ll_earnings_nav_child =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ll_earnings_nav_child)
        ll_cash_wallet_nav_child_one =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ll_cash_wallet_nav_child_one)
        tv_cash_wallet_nav_child =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_cash_wallet_nav_child)

        ll_withdrawal_nav = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ll_withdrawal_nav)
        img_withdrawal_nav = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.img_withdrawal_nav)
        tv_withdrawal_nav = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_withdrawal_nav)
        viewWithdrawal = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.viewWithdrawal)

        ll_selftrading_nav = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ll_selftrading_nav)
        img_selftrading_nav = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.img_selftrading_nav)
        tv_selftrading_nav = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_selftrading_nav)

        ll_report_nav = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ll_report_nav)
        ll_report_nav_child_one =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ll_report_nav_child_one)
        tv_total_report_nav = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_report_nav)
        tv_trading_profit_nav =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_trading_profit_nav)
        tv_lot_rebate_nav = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_lot_rebate_nav)
        tv_lot_rebate_commission_nav =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_lot_rebate_commission_nav)
        tv_lot_leadership_bonus_nav =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_lot_leadership_bonus_nav)
        tv_profit_sharing_nav =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_profit_sharing_nav)
        tv_overriding_nav = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_overriding_nav)

        ll_setting_nav = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ll_setting_nav)
        img_setting_nav = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.img_setting_nav)
        tv_settings_nav = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_settings_nav)

        ll_news_nav = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ll_news_nav)
        img_news_nav = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.img_news_nav)
        tv_news_nav = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_news_nav)
        viewNewsAndTools = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.viewNewsAndTools)

        ll_faq_nav = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ll_faq_nav)
        img_faq_nav = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.img_faq_nav)
        tv_faq_nav = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_faq_nav)
        viewHelpAndFAQ = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.viewHelpAndFAQ)

        ll_support_nav = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ll_support_nav)
        img_support_nav = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.img_support_nav)
        tv_support_nav = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_support_nav)
        viewSupport = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.viewSupport)

        ll_logout_nav = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ll_logout_nav)
        img_logout_nav = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.img_logout_nav)
        tv_logout_nav = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_logout_nav)
        ll_qr_code_nav = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ll_qr_code_nav)
        cvCopyLink = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.cvCopyLink)
        fl_qr_code_nav = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.fl_qr_code_nav)
        tv_tap_to_show_qr_code =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_tap_to_show_qr_code)
        tv_click_to_copy = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_click_to_copy)

        ll_news_tools_nav_main =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ll_news_tools_nav_main)
        img_news_tools_nav_main =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.img_news_tools_nav_main)
        tv_news_tools_nav_main =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_news_tools_nav_main)
        img_news_tools_right = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.img_news_tools_right)

        ll_news_event_nav_child =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ll_news_event_nav_child)
        ll_news_event_nav_child_one =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ll_news_event_nav_child_one)
        ll_industry_tools_nav_child_one =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ll_industry_tools_nav_child_one)
        tv_news_event_nav_child =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_news_event_nav_child)
        tv_industry_tools_nav_child =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tv_industry_tools_nav_child)

        ll_my_network_nav_top =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ll_my_network_nav_top)
        ll_funding_nav_top = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ll_funding_nav_top)
        ll_earnings_nav_top = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ll_earnings_nav_top)
        ll_news_tools_nav_top =
            resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ll_news_tools_nav_top)
        tvUserName = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tvUserName)
        tvRank = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tvRank)
        tvStatus = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.tvStatus)
        ivSettings = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ivSettings)
        ivMessage = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ivMessage)
        ivProfile = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ivProfile)
        ivLogout = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ivLogout)
        ivProfilePhoto = resideMenu!!.layoutLeftMenu!!.findViewById(R.id.ivProfilePhoto)


        /**-------------Setting name in order to be localized------------------------------------*/

        tv_menu_nav!!.text = getString(R.string.menu_nav)
        tv_dashboard_nav!!.text = getString(R.string.dashboard_nav)
        navFundWallet!!.text = getString(R.string.fund_wallet_nav)
        navCommission!!.text = getString(R.string.commission_nav)
        navWithdrawalWallet!!.text = getString(R.string.withdrawal_wallet_nav)
        navCapitalWithdrawal!!.text = getString(R.string.capital_withdrawal_nav)
        navNetworkTree!!.text = getString(R.string.network_tree_nav)
        tv_total_report_nav!!.text = getString(R.string.reports_nav)
        tv_trading_profit_nav!!.text = getString(R.string.trading_profit_nav)
        tv_lot_rebate_nav!!.text = getString(R.string.lot_rebate_nav)
        tv_lot_rebate_commission_nav!!.text = getString(R.string.lot_rebate_commission_nav)
        tv_lot_leadership_bonus_nav!!.text = getString(R.string.leadership_bonus_nav)
        tv_profit_sharing_nav!!.text = getString(R.string.profit_sharing_nav)
        navYourProfile!!.text = getString(R.string.your_profile_nav)
        tv_support_nav!!.text = getString(R.string.support_nav)
        tv_faq_nav!!.text = getString(R.string.faq_nav)
        tv_news_nav!!.text = getString(R.string.news_nav)

        tv_my_profile_nav_main!!.text = getString(R.string.my_profile_nav)
        tv_stock_nav_main!!.text = getString(R.string.stock_nav)
        tv_stock_nav_child!!.text = getString(R.string.stock_markets_nav_child)
        tv_stock_nav_child_one!!.text = getString(R.string.stock_wallet_nav)
        tv_stock_nav_child_two!!.text = getString(R.string.vexstock_faq)

        tv_my_network_nav_main!!.text = getString(R.string.my_network_nav)
        tv_network_tree_nav_child!!.text = getString(R.string.network_tree_nav)
        tv_registered_account_nav_child!!.text = getString(R.string.register_accounts_nav)
        tv_funding_nav_main!!.text = getString(R.string.funding_nav)
        tv_topup_fund_nav_child!!.text = getString(R.string.topup_fund_nav)
        tv_buy_package_nav_child!!.text = getString(R.string.buy_package_nav)
        tv_capital_withdrawal_nav_child!!.text = getString(R.string.capital_withdrawal_nav)
        tv_earnings_nav_main!!.text = getString(R.string.earnings_nav)
        tv_cash_wallet_nav_child!!.text = getString(R.string.cash_wallet_nav)
        tv_withdrawal_nav!!.text = getString(R.string.withdrawal_nav)
        tv_selftrading_nav!!.text = getString(R.string.self_trading)

        navOryx!!.text = getString(R.string.trade_navigation)
        navOryxHotel!!.text = getString(R.string.hotel_navigation)
        navSelfTrading!!.text = getString(R.string.self_trading)
        navPackages!!.text = getString(R.string.packages_tag)
        navKycVerification!!.text = getString(R.string.kyc_verification)
        navTermsOfUse!!.text = getString(R.string.terms_navigation)
        navPrivacyPolicy!!.text = getString(R.string.privacy_policy_navigation)
        tv_settings_nav!!.text = getString(R.string.settings)
        tv_logout_nav!!.text = getString(R.string.logout)
        tv_tap_to_show_qr_code!!.text = getString(R.string.tap_to_show_qr)
        tv_click_to_copy!!.text = getString(R.string.click_to_copy)

        tv_news_tools_nav_main!!.text = getString(R.string.news_and_tools_nav)
        tv_news_event_nav_child!!.text = getString(R.string.news_and_events_nav)
        tv_industry_tools_nav_child!!.text = getString(R.string.industry_tools_nav)

        if (oldDashboardModel != null) {
            oldDashboardModel!!.payload!!.profileImage?.let {
                if (it.isNotEmpty()) {
                    val options: RequestOptions = RequestOptions()
                        .placeholder(R.drawable.user_empty)
                        .error(R.drawable.user_empty)

                    Glide.with(this@HomeActivity)
                        .load(it)
                        .apply(options)
                        .into(ivProfilePhoto!!)
                }
            }
        }


        /**------------Navigation clicks------------------------------------*/


        ivSettings!!.let {
            it.setOnClickListener {
                ll_setting_nav!!.performClick() // open settings
            }
        }

        ivMessage!!.let {
            it.setOnClickListener {
                ll_support_nav!!.performClick()
            }
        }

        ivProfile!!.let {
            it.setOnClickListener {
                ll_my_profile_nav_main!!.performClick()
            }
        }

        ivLogout!!.let {
            it.setOnClickListener {
                ll_logout_nav!!.performClick()
            }
        }

        tv_dashboard_nav!!.let {
            it.setOnClickListener {
                resideMenu!!.closeMenu()
                ll_home.performClick()
            }
        }

        ll_my_profile_nav_main!!.let {
            it.setOnClickListener {
                resideMenu!!.closeMenu()
                //if(getCurrentFragment() is MyAccountFragment) return@setOnClickListener

                Thread(Runnable {
                    loadFragment(
                        MyAccountFragment().apply {
                            this.viewTagSelected = "personal"
                            my_profile_viewTagSelected = this.viewTagSelected
                        },
                        "MyAccountFragment_D",
                        supportFragmentManager.findFragmentById(R.id.fragment_container)!!.javaClass.simpleName
                    )
                }).run()
            }
        }


        ll_my_network_nav_main!!.let {
            it.setOnClickListener {
//                removeAllViewBG()
                if (ll_my_network_nav_child!!.visibility == View.VISIBLE) {
                    img_my_network_right!!.rotation = 90f
                    ll_my_network_nav_child!!.visibility = View.GONE
                    /*setSelectedItemBG(
                        img_my_network_nav_main!!,
                        ll_my_network_nav_top!!,
                        -1,
                        R.drawable.my_network_nav_new
                    )
                    showSeparationView(viewMyNetwork, false)*/
                } else {
                    img_my_network_right!!.rotation = -90f
                    ll_my_network_nav_child!!.visibility = View.VISIBLE
                    /*setSelectedItemBG(
                        img_my_network_nav_main!!,
                        ll_my_network_nav_top!!,
                        R.drawable.bg_menu_selected,
                        R.drawable.my_network_selected_nav_new
                    )
                    showSeparationView(viewMyNetwork, true)*/
                }
            }
        }
        ll_stock_nav_main!!.let {
            it.setOnClickListener {
//                removeAllViewBG()
                if (ll_stock_nav_child!!.visibility == View.VISIBLE) {
                    img_stock_right!!.rotation = 90f
                    ll_stock_nav_child!!.visibility = View.GONE
                    /*setSelectedItemBG(
                        img_my_network_nav_main!!,
                        ll_my_network_nav_top!!,
                        -1,
                        R.drawable.my_network_nav_new
                    )
                    showSeparationView(viewMyNetwork, false)*/
                } else {
                    img_stock_right!!.rotation = -90f
                    ll_stock_nav_child!!.visibility = View.VISIBLE
                    /*setSelectedItemBG(
                        img_my_network_nav_main!!,
                        ll_my_network_nav_top!!,
                        R.drawable.bg_menu_selected,
                        R.drawable.my_network_selected_nav_new
                    )
                    showSeparationView(viewMyNetwork, true)*/
                }
            }
        }

        ll_network_tree_nav_child_one!!.let {
            it.setOnClickListener {
                resideMenu!!.closeMenu()
                if (getCurrentFragment() is NetworkNewFragment) return@setOnClickListener
                Thread(Runnable {
                    loadFragment(
                        NetworkNewFragment().apply {
                            //this.rootDownlines = "0" //temp dummy  number
                        },
                        "NetworkTreeFragment_D",
                        supportFragmentManager.findFragmentById(R.id.fragment_container)!!.javaClass.simpleName
                    )
                }).run()
            }
        }

        ll_registered_account_nav_child_one!!.let {
            it.setOnClickListener {
                resideMenu!!.closeMenu()
                if (getCurrentFragment() is RegisterUserFragment) return@setOnClickListener
                Thread(Runnable {
                    loadFragment(
                        RegisterUserFragment(),
                        "RegisterUserFragment_D",
                        supportFragmentManager.findFragmentById(R.id.fragment_container)!!.javaClass.simpleName
                    )
                }).run()
            }
        }


        ll_funding_nav_main!!.let {
            it.setOnClickListener {
//                removeAllViewBG()
                if (ll_funding_nav_child!!.visibility == View.VISIBLE) {
                    img_funding_right!!.rotation = 90f
                    ll_funding_nav_child!!.visibility = View.GONE
                    /*setSelectedItemBG(
                        img_funding_nav_main!!,
                        ll_funding_nav_top!!,
                        -1,
                        R.drawable.funding_nav_new
                    )
                    showSeparationView(viewFunding, false)*/
                } else {
                    img_funding_right!!.rotation = -90f
                    ll_funding_nav_child!!.visibility = View.VISIBLE
                    /*setSelectedItemBG(
                        img_funding_nav_main!!,
                        ll_funding_nav_top!!,
                        R.drawable.bg_menu_selected,
                        R.drawable.funding_selected_nav_new
                    )
                    showSeparationView(viewFunding, true)*/
                }
            }
        }

        ll_topup_fund_nav_child_one!!.let {
            it.setOnClickListener {
                resideMenu!!.closeMenu()
                if (getCurrentFragment() is FundWallets) return@setOnClickListener
                Thread(Runnable {
                    loadFragment(
                        FundWallets(),
                        "FundWallets_D",
                        supportFragmentManager.findFragmentById(R.id.fragment_container)!!.javaClass.simpleName
                    )
                }).run()
            }
        }

        ll_buy_package_nav_child_one!!.let {
            it.setOnClickListener {
                resideMenu!!.closeMenu()
                Thread(Runnable {
                    loadFragment(
                        MyAccountFragment().apply {
                            this.viewTagSelected = "buy_package"
                            my_profile_viewTagSelected = this.viewTagSelected
                        },
                        "MyAccountFragment_D",
                        supportFragmentManager.findFragmentById(R.id.fragment_container)!!.javaClass.simpleName
                    )
                }).run()
            }
        }

        ll_capital_withdrawal_nav_child_one!!.let {
            it.setOnClickListener {
                resideMenu!!.closeMenu()
                if (getCurrentFragment() is CapitalWithdrawalWallet) return@setOnClickListener
                Thread(Runnable {
                    Constants.isNeedToUpdateUserInvestments = true
                    loadFragment(
                        CapitalWithdrawalWallet(),
                        "CapitalWithdrawalWallet_D",
                        supportFragmentManager.findFragmentById(R.id.fragment_container)!!.javaClass.simpleName
                    )
                }).run()
            }
        }

        ll_earnings_nav_main!!.let {
            it.setOnClickListener {
//                removeAllViewBG()
                if (ll_earnings_nav_child!!.visibility == View.VISIBLE) {
                    img_earnings_right!!.rotation = 90f
                    ll_earnings_nav_child!!.visibility = View.GONE
                    /*setSelectedItemBG(
                        img_earning_nav_main!!,
                        ll_earnings_nav_top!!,
                        -1,
                        R.drawable.earnings_nav_new
                    )
                    showSeparationView(viewEarning, false)*/
                } else {
                    img_earnings_right!!.rotation = -90f
                    ll_earnings_nav_child!!.visibility = View.VISIBLE
                    /*setSelectedItemBG(
                        img_earning_nav_main!!,
                        ll_earnings_nav_top!!,
                        R.drawable.bg_menu_selected,
                        R.drawable.earnings_selected_nav_new
                    )
                    showSeparationView(viewEarning, true)*/
                }
            }
        }

        ll_cash_wallet_nav_child_one!!.let {
            it.setOnClickListener {
                resideMenu!!.closeMenu()
                if (getCurrentFragment() is CommissionBalanceWallet) return@setOnClickListener
                Thread(Runnable {
                    loadFragment(
                        CommissionBalanceWallet(),
                        "CommissionBalanceWallet_D",
                        supportFragmentManager.findFragmentById(R.id.fragment_container)!!.javaClass.simpleName
                    )
                }).run()
            }
        }

        ll_stock_nav_child_one!!.let {
            it.setOnClickListener {
                resideMenu!!.closeMenu()
                if (getCurrentFragment() is StockMarketFragment) return@setOnClickListener
                Thread(Runnable {
                    loadFragment(
                        StockMarketFragment(),
                        "StockMarketFragment",
                        supportFragmentManager.findFragmentById(R.id.fragment_container)!!.javaClass.simpleName
                    )
                }).run()
            }
        }

        ll_stock_wallet_nav_child_one!!.let {
            it.setOnClickListener {
                resideMenu!!.closeMenu()
                if (getCurrentFragment() is StockWalletFragment) return@setOnClickListener
                Thread(Runnable {
                    loadFragment(
                        StockWalletFragment(),
                        "StockWalletFragment",
                        supportFragmentManager.findFragmentById(R.id.fragment_container)!!.javaClass.simpleName
                    )
                }).run()
            }
        }
        ll_stock_wallet_nav_child_third!!.let {
            it.setOnClickListener {
                resideMenu!!.closeMenu()
                var url =  Pref.getValue(this@HomeActivity,"stock_help","")
                if (getCurrentFragment() is StockHelpAndFaqFragment) return@setOnClickListener
                Thread(Runnable {
                    loadFragment(
                        StockHelpAndFaqFragment.newInstance(
                            url.toString(),
                            this.resources.getString(R.string.vexstock_faq)
                        )
                        ,
                        "StockHelpAndFaqFragment",
                        supportFragmentManager.findFragmentById(R.id.fragment_container)!!.javaClass.simpleName
                    )
                }).run()
            }
        }

        //same as ll_report_nav click event
        ll_report_nav_child_one!!.let {
            it.setOnClickListener {
                resideMenu!!.closeMenu()
                if (getCurrentFragment() is TradingProfitReportFragment) return@setOnClickListener
                Thread(Runnable {
                    loadFragment(
                        TradingProfitReportFragment(),
                        "TradingProfitReportFragment",
                        supportFragmentManager.findFragmentById(R.id.fragment_container)!!.javaClass.simpleName
                    )
                }).run()
            }
        }

        tv_withdrawal_nav!!.let {
            it.setOnClickListener {
                resideMenu!!.closeMenu()
                if (getCurrentFragment() is WithdrawalWallets) return@setOnClickListener
                Thread(Runnable {
                    loadFragment(
                        WithdrawalWallets(),
                        "WithdrawalWallets_D",
                        supportFragmentManager.findFragmentById(R.id.fragment_container)!!.javaClass.simpleName
                    )
                }).run()
            }
        }

        tv_selftrading_nav!!.let {
            it.setOnClickListener {
                resideMenu!!.closeMenu()
                if (getCurrentFragment() is SelfTradingFragment) return@setOnClickListener
                Thread(Runnable {
                    loadFragment(
                        SelfTradingFragment(),
                        "SelfTradingFragment_D",
                        supportFragmentManager.findFragmentById(R.id.fragment_container)!!.javaClass.simpleName
                    )
                }).run()
            }
        }


        ll_news_tools_nav_main!!.let {
            it.setOnClickListener {
//                removeAllViewBG()
                if (ll_news_event_nav_child!!.visibility == View.VISIBLE) {
                    img_news_tools_right!!.rotation = 90f
                    ll_news_event_nav_child!!.visibility = View.GONE
                    /*setSelectedItemBG(
                        img_news_tools_nav_main!!,
                        ll_news_tools_nav_top!!,
                        -1,
                        R.drawable.news_tools_new
                    )
                    showSeparationView(viewNewsAndTools, false)*/
                } else {
                    img_news_tools_right!!.rotation = -90f
                    ll_news_event_nav_child!!.visibility = View.VISIBLE
                    /*setSelectedItemBG(
                        img_news_tools_nav_main!!,
                        ll_news_tools_nav_top!!,
                        R.drawable.bg_menu_selected,
                        R.drawable.news_tools_selected_new
                    )
                    showSeparationView(viewNewsAndTools, true)*/
                }
            }
        }

        ll_news_event_nav_child_one!!.let {
            it.setOnClickListener {
                resideMenu!!.closeMenu()
                if (getCurrentFragment() is NewsAndUpdateNavFragment) return@setOnClickListener
                Thread(Runnable {
                    loadFragment(
                        NewsAndUpdateNavFragment(),
                        "NewsAndUpdateNavFragment_D",
                        supportFragmentManager.findFragmentById(R.id.fragment_container)!!.javaClass.simpleName
                    )
                }).run()
            }
        }

        ll_industry_tools_nav_child_one!!.let {
            it.setOnClickListener {
                Log.e(
                    TAG,
                    "^^^^^^  " + Pref!!.getCommonDataModel(this@HomeActivity)!!.payload!!.industryToolsLink
                )
                resideMenu!!.closeMenu()
                if (getCurrentFragment() is WebViewNavFragment) return@setOnClickListener
                Thread(Runnable {
                    loadFragment(
                        WebViewNavFragment.newInstance(
                            Pref.getCommonDataModel(this@HomeActivity)!!.payload!!.industryToolsLink!!,
                            this.resources.getString(R.string.industry_tools_nav)
                        )
                        ,
                        "WebViewNavFragment",
                        supportFragmentManager.findFragmentById(R.id.fragment_container)!!.javaClass.simpleName
                    )
                }).run()
            }
        }

        ll_news_nav!!.let {
            it.setOnClickListener {
                resideMenu!!.closeMenu()
                if (getCurrentFragment() is NewsAndUpdateNavFragment) return@setOnClickListener
                Thread(Runnable {
                    loadFragment(
                        NewsAndUpdateNavFragment(),
                        "NewsAndUpdateNavFragment_D",
                        supportFragmentManager.findFragmentById(R.id.fragment_container)!!.javaClass.simpleName
                    )
                }).run()
            }
        }

        ll_report_nav!!.let {
            it.setOnClickListener {
                resideMenu!!.closeMenu()
                if (getCurrentFragment() is TradingProfitReportFragment) return@setOnClickListener
                Thread(Runnable {
                    loadFragment(
                        TradingProfitReportFragment(),
                        "TradingProfitReportFragment",
                        supportFragmentManager.findFragmentById(R.id.fragment_container)!!.javaClass.simpleName
                    )
                }).run()
            }
        }

        tv_trading_profit_nav!!.let {
            it.setOnClickListener {
                reportFragmentOpen("trading")
            }
        }

        tv_lot_rebate_nav!!.let {
            it.setOnClickListener {
                reportFragmentOpen("lotrebate")
            }
        }

        tv_lot_rebate_commission_nav!!.let {
            it.setOnClickListener {
                reportFragmentOpen("lotrebatecommission")
            }
        }

        tv_lot_leadership_bonus_nav!!.let {
            it.setOnClickListener {
                reportFragmentOpen("leadershipbonus")
            }
        }

        tv_profit_sharing_nav!!.let {
            it.setOnClickListener {
                reportFragmentOpen("profitsharing")
            }
        }



        ll_support_nav!!.let {
            it.setOnClickListener {
                resideMenu!!.closeMenu()
                if (getCurrentFragment() is HelpAndSupportFragment) return@setOnClickListener
                Thread(Runnable {
                    loadFragment(
                        HelpAndSupportFragment(),
                        "HelpAndSupportFragment_D",
                        supportFragmentManager.findFragmentById(R.id.fragment_container)!!.javaClass.simpleName
                    )
                }).run()
            }
        }

        ll_faq_nav!!.let {
            it.setOnClickListener {
                resideMenu!!.closeMenu()
                if (getCurrentFragment() is HelpFragment) return@setOnClickListener
                Thread(Runnable {
                    loadFragment(
                        HelpFragment(),
                        "HelpFragment_D",
                        supportFragmentManager.findFragmentById(R.id.fragment_container)!!.javaClass.simpleName
                    )
                }).run()
            }
        }

        navKycVerification!!.let {
            it.setOnClickListener {
                resideMenu!!.closeMenu()
                Thread(Runnable {
                    loadFragment(
                        KycFragment(),
                        "KycFragment",
                        supportFragmentManager.findFragmentById(R.id.fragment_container)!!.javaClass.simpleName
                    )
                }).run()
            }
        }

        navLanguageSpinner!!.let {
            var isUserAction = false
            // it?.background =
            //    ContextCompat.getDrawable(this@HomeActivity, R.drawable.bg_spinner)

            //  val listItemsTxt = arrayOf("English", "Chinese")
            //  val listflag = arrayOf(R.drawable.england_flag, R.drawable.chinese_flag)
            val listItemsTxt =
                arrayOf("English", "Chinese", "Chinese Traditional", "Korean", "Thai", "Vietnam")
            Log.e("TestEntryValid", "***  " + listItemsTxt.size)
            val listflag = arrayOf(
                R.drawable.england_flag,
                R.drawable.chinese_flag,
                R.drawable.chinese_flag,
                R.drawable.england_flag,
                R.drawable.england_flag,
                R.drawable.england_flag
            )

            /* var spinnerAdapter =
                 LanguageSelectionSpinnerAdapter(this@HomeActivity, listItemsTxt, listflag)
             it?.adapter = spinnerAdapter*/

            it?.adapter = HighLightArrayAdapterV2(
                context = this@HomeActivity,
                dropdownResource = R.layout.row_spinner_login_dropdown,
                viewResource = R.layout.row_spinner_login,
                objects = ArrayList<String>().apply {
                    this.add("English")
                    this.add("中文(Chinese)")
                    this.add("中文(Chinese Traditional)")
                    this.add("한국어(Korean)")
                    this.add("ไทย(Thai)")
                    this.add("Việt Nam(Vietnam)")
                })


            if (Pref.getValue(
                    this@HomeActivity,
                    Constants.Localization,
                    ""
                ).equals("en")
            ) {
                it?.setSelection(0)
            } else if (Pref.getValue(
                    this@HomeActivity,
                    Constants.Localization,
                    ""
                ).equals("cn")
            ) {
                it?.setSelection(1)
            } else if (Pref.getValue(
                    this@HomeActivity,
                    Constants.Localization,
                    ""
                ).equals("cn_tr")
            ) {
                it?.setSelection(2)
            } else if (Pref.getValue(
                    this@HomeActivity,
                    Constants.Localization,
                    ""
                ).equals("ko")
            ) {
                it?.setSelection(3)
            } else if (Pref.getValue(
                    this@HomeActivity,
                    Constants.Localization,
                    ""
                ).equals("th")
            ) {
                it?.setSelection(4)
            } else if (Pref.getValue(
                    this@HomeActivity,
                    Constants.Localization,
                    ""
                ).equals("vi")
            ) {
                it?.setSelection(5)
            }

            it?.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemSelected(
                    parentView: AdapterView<*>,
                    selectedItemView: View,
                    position: Int,
                    id: Long
                ) {
                    if (position == 0) {
                        Pref.setLocale(this@HomeActivity, "en")
                        //   Pref.setValue(this@HomeActivity, Constants.Localization, "en")
                    } else if (position == 1) {
                        Pref.setLocale(this@HomeActivity, "cn")
                        //     Pref.setValue(this@HomeActivity, Constants.Localization, "chi")
                    } else if (position == 2) {
                        Pref.setLocale(this@HomeActivity, "cn_tr")
                        //     Pref.setValue(this@HomeActivity, Constants.Localization, "chi")
                    } else if (position == 3) {
                        Pref.setLocale(this@HomeActivity, "ko")
                        //     Pref.setValue(this@HomeActivity, Constants.Localization, "chi")
                    } else if (position == 4) {
                        Pref.setLocale(this@HomeActivity, "th")
                        //     Pref.setValue(this@HomeActivity, Constants.Localization, "chi")
                    } else if (position == 5) {
                        Pref.setLocale(this@HomeActivity, "vi")
                        //     Pref.setValue(this@HomeActivity, Constants.Localization, "chi")
                    }

                    if (isUserAction) {
                        ApiClient.getClient(this@HomeActivity).getCommonData(
                            Pref.getLocalization(
                                this@HomeActivity
                            )
                        ).enqueue(
                            object : Callback<CommonDataModel> {
                                override fun onFailure(call: Call<CommonDataModel>, t: Throwable) {

                                }

                                override fun onResponse(
                                    call: Call<CommonDataModel>,
                                    response: Response<CommonDataModel>
                                ) {
                                    Pref.setValue(
                                        this@HomeActivity,
                                        Constants.prefCommonData!!,
                                        Gson().toJson(
                                            response.body()
                                        )
                                    )
                                }
                            }
                        )
                        showProgressDialog()

                        Handler().postDelayed(Runnable {
                            dismissProgressDialog()
                        }, 5)
                        var intent = Intent(this@HomeActivity, HomeActivity::class.java)
                        overridePendingTransition(0, 0)
                        startActivity(intent)
                        resideMenu!!.closeMenu()
                    } else {
                        isUserAction = true
                    }

                }
            }
        }

        navTermsOfUse!!.setOnClickListener {
            resideMenu!!.closeMenu()
            Thread(Runnable {
                startActivity(
                    Intent(this@HomeActivity, PdfViewActivity::class.java).putExtra(
                        "url", commonDataModel!!.payload!!.termsConditions!!
                    ).putExtra("isPdf", true)
                )
            }).run()
        }
        navPrivacyPolicy!!.setOnClickListener {
            resideMenu!!.closeMenu()
            Thread(Runnable {
                startActivity(
                    Intent(this@HomeActivity, PdfViewActivity::class.java).putExtra(
                        "url", commonDataModel!!.payload!!.privacyPolicy!!
                    ).putExtra("isPdf", true)
                )
            }).run()
        }
        ll_setting_nav!!.setOnClickListener {
            resideMenu!!.closeMenu()
            Thread(Runnable {
                loadFragment(
                    SettingsFragment(),
                    "SettingsFragment",
                    this.javaClass.simpleName
                )
            }).run()
        }
        navPackages!!.setOnClickListener {
            resideMenu!!.closeMenu()
            Thread(Runnable {
                loadFragment(
                    PackagesFragment(),
                    "PackagesFragment",
                    this.javaClass.simpleName
                )
            }).run()
        }


        cvCopyLink!!.setOnClickListener {
            val clipboard: ClipboardManager =
                this!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            if (clipboard != null) {
                val clip = ClipData.newPlainText(
                    "qrCode",
                    userModel!!.payload!!.user!!.shareLink
                )
                clipboard.setPrimaryClip(clip)
                Toast.makeText(
                    this@HomeActivity,
                    getString(R.string.copied_to_clipboard),
                    Toast.LENGTH_SHORT
                ).show();
            }
        }

        fl_qr_code_nav!!.setOnClickListener {
            resideMenu!!.closeMenu()
            showQRCodeDialog(userModel!!.payload!!.user!!.shareLink)
        }
        ll_logout_nav!!.setOnClickListener {
            resideMenu!!.closeMenu()
            Thread(Runnable {
                logout()
            }).run()
        }

        setBottomTabClick()
        /**---------------------------------------------------------------------------------------------------------------------------------------*/
    }

    fun reportFragmentOpen(fragName: String) {
        resideMenu!!.closeMenu()
        Log.e(
            "TestLIstClick",
            "####  $fragName " + " ^^^ " + this.supportFragmentManager.findFragmentByTag(fragName)
        )
        if (getCurrentFragment() is TradingProfitReportFragment) {
            if (this.supportFragmentManager.findFragmentByTag(fragName) != null && this.supportFragmentManager.findFragmentByTag(
                    fragName
                )!!.isVisible
            )
                return
        }

        Thread(Runnable {
            loadFragment(
                TradingProfitReportFragment().apply {
                    this.fragNameReport = fragName
                },
                fragName,
                this.javaClass.simpleName
            )
        }).run()

    }

    private fun addListeners() {
        rl_message.setOnClickListener {
            loadFragment(
                when (iv_message.tag) {
                    null -> {
                        HelpAndSupportFragment()
                    }
                    getString(R.string.otmtrade_key) -> {
                        HelpAndSupportFragment()
                    }
                    else -> HelpAndSupportFragment()
                }
                ,
                "HelpAndSupportFragment",
                this.javaClass.simpleName
            )
        }
        iv_navigation.setOnClickListener {
            resideMenu!!.openMenu(ResideMenu.DIRECTION_LEFT)
        }
        iv_back.setOnClickListener {
            //  supportFragmentManager.popBackStack()
            onBackPressed()
        }

        /**----------------------llBottomNew tab click----------------------*/
//        setBottomTabClick()

        /**----------------------otm trade tab clicks----------------------*/
        ll_home.setOnClickListener {

            if (supportFragmentManager.findFragmentById(R.id.fragment_container)!!.javaClass.simpleName != HomeFragment().javaClass.simpleName) {
                loadFragmentWithClearedStack(
                    HomeFragment(),
                    "HomeFragment",
                    this.javaClass.simpleName
                )
            }
            setUpTabVisibility()
        }

        ll_topup.setOnClickListener {

            loadFragment(FundWallets(), "FundWallets", this.javaClass.simpleName)
            unSelectBottomBar()
            iv_topup.setColorFilter(
                ContextCompat.getColor(
                    this@HomeActivity,
                    R.color.dashboard_selected
                )
            )
            tv_topup.setTextColor(
                ContextCompat.getColor(
                    this@HomeActivity,
                    R.color.dashboard_selected
                )
            )
        }
        ll_help.setOnClickListener {
            if (supportFragmentManager.findFragmentById(R.id.fragment_container)!!.javaClass.simpleName != HelpAndSupportFragment().javaClass.simpleName) {

                //loadFragment(HelpFragment(), "HelpFragment", this.javaClass.simpleName)
                loadFragment(
                    HelpAndSupportFragment(),
                    "HelpAndSupportFragment_DBB",
                    this.javaClass.simpleName
                )
            }
        }
        ll_account.setOnClickListener {
            if (supportFragmentManager.findFragmentById(R.id.fragment_container)!!.javaClass.simpleName != AccountFragment().javaClass.simpleName) {

                loadFragment(AccountFragment(), "AccountFragment_DBB", this.javaClass.simpleName)
            }
        }
    }

    private fun setBottomTabClick() {
        setUpTabVisibility()
        llFundingTab.setOnClickListener {
            ll_topup_fund_nav_child_one!!.performClick()
        }
        llEarningTab.setOnClickListener {
            ll_cash_wallet_nav_child_one!!.performClick()
        }
        llDashboardTab.setOnClickListener {
            ll_home.performClick()
        }
        llWithdrawalTab.setOnClickListener {
            tv_withdrawal_nav!!.performClick()
        }
    }

    private fun setUpTabVisibility() {
        var userModel: UserModel? = Pref.getUserModel(this@HomeActivity!!)
        Log.e("BottomTab", "888  " + userModel!!.payload!!.user!!.packageId)
        if (userModel!!.payload!!.user!!.packageId == "0") {
            llEarningTab.visibility = View.GONE
            llWithdrawalTab.visibility = View.GONE
        } else {
            llEarningTab.visibility = View.VISIBLE
            llWithdrawalTab.visibility = View.VISIBLE
        }
    }

    private fun setupBottomTextSizes() {
        Handler().postDelayed({
            run {

                tv_account.setTextSize(TypedValue.COMPLEX_UNIT_PX, tv_wallet.textSize)
                tv_help.setTextSize(TypedValue.COMPLEX_UNIT_PX, tv_wallet.textSize)
                tv_topup.setTextSize(TypedValue.COMPLEX_UNIT_PX, tv_wallet.textSize)
                tv_home.setTextSize(TypedValue.COMPLEX_UNIT_PX, tv_wallet.textSize)

                tv_home_hotel.setTextSize(TypedValue.COMPLEX_UNIT_PX, tv_wallet.textSize)
                tv_booking_hotel.setTextSize(TypedValue.COMPLEX_UNIT_PX, tv_wallet.textSize)
                tv_search_hotel.setTextSize(TypedValue.COMPLEX_UNIT_PX, tv_wallet.textSize)
                tv_help_hotel.setTextSize(TypedValue.COMPLEX_UNIT_PX, tv_wallet.textSize)
                tv_account_hotel.setTextSize(TypedValue.COMPLEX_UNIT_PX, tv_wallet.textSize)

                tv_faq_selfTrad.setTextSize(TypedValue.COMPLEX_UNIT_PX, tv_wallet.textSize)
                tv_help_selfTrad.setTextSize(TypedValue.COMPLEX_UNIT_PX, tv_wallet.textSize)
                tv_topup_selfTrad.setTextSize(TypedValue.COMPLEX_UNIT_PX, tv_wallet.textSize)
                tv_home_selfTrad.setTextSize(TypedValue.COMPLEX_UNIT_PX, tv_wallet.textSize)

            }
        }, 500)
    }

    internal fun unSelectBottomBar() {

        /*iv_home.setColorFilter(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.dashboard_unselected
            )
        )*/
        tv_home.setTextColor(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.dashboard_unselected
            )
        )

        iv_wallet.setColorFilter(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.dashboard_unselected
            )
        )
        tv_wallet.setTextColor(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.dashboard_unselected
            )
        )

        iv_topup.setColorFilter(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.dashboard_unselected
            )
        )
        tv_topup.setTextColor(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.dashboard_unselected
            )
        )

        iv_help.setColorFilter(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.dashboard_unselected
            )
        )
        tv_help.setTextColor(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.dashboard_unselected
            )
        )

        iv_account.setColorFilter(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.dashboard_unselected
            )
        )
        tv_account.setTextColor(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.dashboard_unselected
            )
        )

        iv_home_setfTrad.setColorFilter(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.dashboard_unselected
            )
        )
        tv_home_selfTrad.setTextColor(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.dashboard_unselected
            )
        )

        iv_wallet_selfTrad.setColorFilter(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.dashboard_unselected
            )
        )
        tv_wallet_selfTrad.setTextColor(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.dashboard_unselected
            )
        )

        iv_topup_selfTrad.setColorFilter(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.dashboard_unselected
            )
        )
        tv_topup_selfTrad.setTextColor(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.dashboard_unselected
            )
        )

        iv_faq_selfTrad.setColorFilter(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.dashboard_unselected
            )
        )
        tv_faq_selfTrad.setTextColor(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.dashboard_unselected
            )
        )

        iv_help_selfTrad.setColorFilter(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.dashboard_unselected
            )
        )
        tv_help_selfTrad.setTextColor(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.dashboard_unselected
            )
        )

        iv_home_hotel.setColorFilter(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.bottom_unselect
            )
        )
        tv_home_hotel.setTextColor(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.bottom_unselect
            )
        )

        iv_booking_hotel.setColorFilter(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.bottom_unselect
            )
        )
        tv_booking_hotel.setTextColor(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.bottom_unselect
            )
        )

        iv_search_hotel.setColorFilter(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.bottom_unselect
            )
        )

        tv_search_hotel.setTextColor(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.bottom_unselect
            )
        )

        iv_help_hotel.setColorFilter(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.bottom_unselect
            )
        )
        tv_help_hotel.setTextColor(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.bottom_unselect
            )
        )

        iv_account_hotel.setColorFilter(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.bottom_unselect
            )
        )
        tv_account_hotel.setTextColor(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.bottom_unselect
            )
        )

        llHeaderHotelHome.visibility = View.GONE
    }

    /**i -> 0 = Trade, 1 = Hotel, 2 = Self Trade , 3 = goneBoth */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    internal fun visibleBottomBar(i: Int) {
        llBottomNew.visibility = View.VISIBLE //added new 
        ll_bottom.visibility = View.GONE
        ll_bottombar.visibility = View.GONE
        ll_bottombar_hotel.visibility = View.GONE
        ll_bottomBar_selfTrad.visibility = View.GONE
        toolbar.setBackgroundColor(ContextCompat.getColor(this@HomeActivity, R.color.white))
        when (i) {
            0 -> {
                ll_bottombar.visibility = View.VISIBLE
                //rlMainHomeActivity.background =
                //   ContextCompat.getDrawable(this@HomeActivity, R.drawable.navigation_back)
                rlMainHomeActivity.setBackgroundColor(
                    ContextCompat.getColor(
                        this@HomeActivity,
                        R.color.white
                    )
                )
                iv_navigation.setColorFilter(
                    ContextCompat.getColor(
                        this@HomeActivity,
                        R.color.white
                    )
                )
                tv_title.setTextColor(ContextCompat.getColor(this@HomeActivity, R.color.white))

                toolbar.setBackgroundColor(
                    ContextCompat.getColor(
                        this@HomeActivity,
                        R.color.colorPrimaryDark
                    )
                )
            }
            1 -> {
                ll_bottombar_hotel.visibility = View.VISIBLE
                rlMainHomeActivity.setBackgroundColor(
                    ContextCompat.getColor(
                        this@HomeActivity,
                        R.color.white
                    )
                )
                iv_navigation.setColorFilter(
                    ContextCompat.getColor(
                        this@HomeActivity,
                        R.color.bottom_unselect
                    )
                )
                tv_title.setTextColor(
                    ContextCompat.getColor(
                        this@HomeActivity,
                        R.color.hotel_title_color
                    )
                )
            }

            2 -> {

                ll_bottomBar_selfTrad.visibility = View.VISIBLE
                rlMainHomeActivity.setBackgroundColor(
                    ContextCompat.getColor(
                        this@HomeActivity,
                        R.color.white
                    )
                )
                iv_navigation.setColorFilter(
                    ContextCompat.getColor(
                        this@HomeActivity,
                        R.color.bottom_unselect
                    )
                )
                tv_title.setTextColor(
                    ContextCompat.getColor(
                        this@HomeActivity,
                        R.color.hotel_title_color
                    )
                )

                toolbar.setBackgroundColor(
                    ContextCompat.getColor(
                        this@HomeActivity,
                        R.color.off_white
                    )
                )

            }
            else -> {

            }
        }

    }

    private fun logout() {
        AlertDialog.Builder(this@HomeActivity, R.style.MyDialogTheme).apply {
            this.setMessage(getString(R.string.logout_message))
            this.setPositiveButton(
                getString(R.string.yes)

            ) { dialog, _ ->

                if (!Pref.getUserModel(this@HomeActivity)!!.payload!!.user!!.fingerPrintSetInThisDevice) {
                    Pref.deleteAll(this@HomeActivity)
                    dialog.dismiss()
                    startActivity(
                        Intent(
                            this@HomeActivity,
                            AuthOptionsActivity::class.java
                        )
                    )
                    finish()
                } else {
                    showProgressDialog()
                    ApiClient.getClient(this@HomeActivity).logout(
                        localization = Pref.getLocalization(this@HomeActivity),
                        authorization = Pref.getprefAuthorizationToken(this@HomeActivity)
                    )
                        .enqueue(object : Callback<ResponseBody> {
                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                dismissProgressDialog()
                            }

                            override fun onResponse(
                                call: Call<ResponseBody>,
                                response: Response<ResponseBody>
                            ) {
                                dismissProgressDialog()
                                if (response.isSuccessful) {

                                    Pref.deleteAll(this@HomeActivity)
                                    //Pref.setLocale(this@HomeActivity, "en")
                                    dialog.dismiss()
                                    startActivity(
                                        Intent(
                                            this@HomeActivity,
                                            AuthOptionsActivity::class.java
                                        )
                                    )
                                    finish()

                                } else {
                                    errorBody(response.errorBody()!!)
                                }
                            }

                        })
                }
            }
            this.setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }

        }.create().apply {
            this.setOnShowListener {
                this.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(ContextCompat.getColor(this@HomeActivity, R.color.black))
                this.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(ContextCompat.getColor(this@HomeActivity, R.color.black))
            }
        }.show()

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        supportFragmentManager.findFragmentById(R.id.fragment_container)
            ?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        supportFragmentManager.findFragmentById(R.id.fragment_container)
            ?.onActivityResult(requestCode, resultCode, data)
    }

    fun getCurrentFragment(): Fragment? {
        return supportFragmentManager.findFragmentById(R.id.fragment_container)
    }

    fun viewVisibleDrawerBottomBar(type: Int) { // 0 == drawer and bottom bar, 1 == drawer, 2 == back
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        if (type == 0) {
            iv_navigation.visibility = View.VISIBLE
            ll_bottombar.visibility = View.GONE //View.VISIBLE
            iv_back.visibility = View.GONE
            params.setMargins(0, 0, 0, 0) //UTILS.dpToPixel(applicationContext, 35)
        } else if (type == 1) {
            iv_navigation.visibility = View.VISIBLE
            ll_bottombar.visibility = View.GONE
            iv_back.visibility = View.GONE
            params.setMargins(0, 0, 0, 0)
        } else {
            iv_navigation.visibility = View.GONE
            ll_bottombar.visibility = View.GONE
            iv_back.visibility = View.VISIBLE
            params.setMargins(0, 0, 0, 0)
        }
        ln_all_page_bg.background = ContextCompat.getDrawable(
            applicationContext!!,
            if (getCurrentFragment() is RegisterUserFragment) R.mipmap.login_full_bg else R.mipmap.dashboard_app_bg
        )
        ln_all_page_bg.layoutParams = params
        ll_bottomBar_selfTrad.visibility = View.GONE
    }

    private fun setViewForDrawerItemSelectionHighlight() {
        var userModel: UserModel? = Pref.getUserModel(this@HomeActivity!!)
        Log.e("TestEntry", "888  " + userModel!!.payload!!.user!!.packageId)
        if (userModel!!.payload!!.user!!.packageId == "0") {
            ll_earnings_nav_main!!.visibility = View.GONE
            ll_withdrawal_nav!!.visibility = View.GONE
            ll_selftrading_nav!!.visibility = View.GONE
            ll_report_nav!!.visibility = View.GONE
            ll_faq_nav!!.visibility = View.GONE
            ll_qr_code_nav!!.visibility = View.GONE
            ll_capital_withdrawal_nav_child_one!!.visibility = View.GONE

            if (userModel!!.payload!!.user?.isConsultant == "1") {
                ll_registered_account_nav_child_one!!.visibility = View.GONE
            } else {
                ll_registered_account_nav_child_one!!.visibility = View.VISIBLE
            }
        } else {
            ll_earnings_nav_main!!.visibility = View.VISIBLE
            ll_withdrawal_nav!!.visibility = View.VISIBLE
            ll_selftrading_nav!!.visibility = View.GONE
            ll_report_nav!!.visibility = View.GONE // 
            ll_faq_nav!!.visibility = View.VISIBLE
            ll_qr_code_nav!!.visibility = View.VISIBLE

            if (userModel!!.payload!!.user?.promoAccount == "1"){
                ll_capital_withdrawal_nav_child_one!!.makeGone()
            }else{
                oldDashboardModel!!.payload?.let {

                    if(it.capital_withdraw==1){
                        ll_capital_withdrawal_nav_child_one!!.makeVisible()

                    }else{
                        ll_capital_withdrawal_nav_child_one!!.makeGone()
                    }
                }
            }

//            ll_capital_withdrawal_nav_child_one!!.visibility = if (!Pref.getValue(
//                    this@HomeActivity!!,
//                    Constants.prefPromoAccount,
//                    ""
//                ).isNullOrEmpty() &&
//                Pref.getValue(this@HomeActivity!!, Constants.prefPromoAccount, "").equals("1")
//            ) View.GONE else View.VISIBLE
            ll_registered_account_nav_child_one!!.visibility = View.VISIBLE
        }

        if (userModel!!.payload!!.user?.isConsultant == "1") {
            //ll_earnings_nav_main!!.visibility = View.GONE
            //ll_withdrawal_nav!!.visibility = View.GONE
            //ll_report_nav!!.visibility = View.GONE
            //ll_faq_nav!!.visibility = View.GONE
            ll_qr_code_nav!!.visibility = View.GONE
            //ll_capital_withdrawal_nav_child_one!!.visibility = View.GONE
            ll_my_network_nav_main!!.visibility = View.GONE
        }

        /*img_dashboard_nav!!.setColorFilter(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.register_back_btn_color
            )
        )
        tv_dashboard_nav!!.setTextColor(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.off_white
            )
        )*/
        setSelectedItemBG(img_dashboard_nav!!, ll_dash_nav!!, -1, R.drawable.dashboard_nav_new)


        /*img_my_profile_nav_main!!.setColorFilter(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.register_back_btn_color
            )
        )
        tv_my_profile_nav_main!!.setTextColor(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.off_white
            )
        )*/
        setSelectedItemBG(
            img_my_profile_nav_main!!,
            ll_my_profile_nav_main!!,
            -1,
            R.drawable.my_profile_new
        )

        /*img_my_network_nav_main!!.setColorFilter(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.register_back_btn_color
            )
        )
        tv_my_network_nav_main!!.setTextColor(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.off_white
            )
        )
        img_my_network_right!!.setColorFilter(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.register_back_btn_color
            )
        )*/
        setSelectedItemBG(
            img_my_network_nav_main!!,
            ll_my_network_nav_top!!,
            -1,
            R.drawable.my_network_nav_new
        )

        /*tv_network_tree_nav_child!!.setTextColor(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.off_white
            )
        )*/
        setSelectedItemBG(null, ll_network_tree_nav_child_one!!, -1, -1)


        /*tv_registered_account_nav_child!!.setTextColor(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.off_white
            )
        )*/
        setSelectedItemBG(null, ll_registered_account_nav_child_one!!, -1, -1)


        setSelectedItemBG(img_stock_nav_main!!, ll_stock_nav_top!!, -1, R.drawable.dashboard_stocks)
        setSelectedItemBG(null, ll_stock_nav_child!!, -1, -1)
        setSelectedItemBG(null, ll_stock_nav_child_one!!, -1, -1)
        setSelectedItemBG(null, ll_stock_wallet_nav_child_one!!, -1, -1)
        setSelectedItemBG(null, ll_stock_wallet_nav_child_third!!, -1, -1)

        /*img_news_tools_nav_main!!.setColorFilter(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.register_back_btn_color
            )
        )
        tv_news_tools_nav_main!!.setTextColor(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.off_white
            )
        )
        img_news_tools_right!!.setColorFilter(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.register_back_btn_color
            )
        )*/
        setSelectedItemBG(
            img_news_tools_nav_main!!,
            ll_news_tools_nav_top!!,
            -1,
            R.drawable.news_tools_new
        )

        /*tv_news_event_nav_child!!.setTextColor(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.off_white
            )
        )*/
        setSelectedItemBG(null, ll_news_event_nav_child_one!!, -1, -1)

        /*tv_industry_tools_nav_child!!.setTextColor(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.off_white
            )
        )*/
        setSelectedItemBG(null, ll_industry_tools_nav_child_one!!, -1, -1)

        /*img_funding_nav_main!!.setColorFilter(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.register_back_btn_color
            )
        )
        tv_funding_nav_main!!.setTextColor(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.off_white
            )
        )
        img_funding_right!!.setColorFilter(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.register_back_btn_color
            )
        )*/
        setSelectedItemBG(
            img_funding_nav_main!!,
            ll_funding_nav_top!!,
            -1,
            R.drawable.funding_nav_new
        )

        /*tv_topup_fund_nav_child!!.setTextColor(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.off_white
            )
        )*/
        setSelectedItemBG(null, ll_topup_fund_nav_child_one!!, -1, -1)

        /*tv_buy_package_nav_child!!.setTextColor(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.off_white
            )
        )*/
        setSelectedItemBG(null, ll_buy_package_nav_child_one!!, -1, -1)

        /*tv_capital_withdrawal_nav_child!!.setTextColor(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.off_white
            )
        )*/
        setSelectedItemBG(null, ll_capital_withdrawal_nav_child_one!!, -1, -1)

        /*img_earning_nav_main!!.setColorFilter(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.register_back_btn_color
            )
        )
        tv_earnings_nav_main!!.setTextColor(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.off_white
            )
        )
        img_earnings_right!!.setColorFilter(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.register_back_btn_color
            )
        )*/
        setSelectedItemBG(
            img_earning_nav_main!!,
            ll_earnings_nav_top!!,
            -1,
            R.drawable.earnings_nav_new
        )

        /*tv_cash_wallet_nav_child!!.setTextColor(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.off_white
            )
        )*/
        setSelectedItemBG(null, ll_cash_wallet_nav_child_one!!, -1, -1)

        /*img_withdrawal_nav!!.setColorFilter(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.register_back_btn_color
            )
        )
        tv_withdrawal_nav!!.setTextColor(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.off_white
            )
        )*/
        setSelectedItemBG(
            img_withdrawal_nav!!,
            ll_withdrawal_nav!!,
            -1,
            R.drawable.withdrawal_nav_new
        )

        img_selftrading_nav!!.setColorFilter(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.register_back_btn_color
            )
        )
        tv_selftrading_nav!!.setTextColor(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.off_white
            )
        )

        /*tv_total_report_nav!!.setTextColor(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.off_white
            )
        )*/
        setSelectedItemBG(null, ll_report_nav_child_one!!, -1, -1)

        tv_trading_profit_nav!!.setTextColor(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.off_white
            )
        )
        tv_lot_rebate_nav!!.setTextColor(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.off_white
            )
        )
        tv_lot_rebate_commission_nav!!.setTextColor(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.off_white
            )
        )
        tv_lot_leadership_bonus_nav!!.setTextColor(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.off_white
            )
        )
        tv_profit_sharing_nav!!.setTextColor(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.off_white
            )
        )
        tv_overriding_nav!!.setTextColor(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.off_white
            )
        )

        img_setting_nav!!.setColorFilter(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.register_back_btn_color
            )
        )
        tv_settings_nav!!.setTextColor(ContextCompat.getColor(this@HomeActivity, R.color.off_white))

        img_news_nav!!.setColorFilter(ContextCompat.getColor(this@HomeActivity, R.color.off_white))
        tv_news_nav!!.setTextColor(ContextCompat.getColor(this@HomeActivity, R.color.off_white))

//        img_faq_nav!!.setColorFilter(ContextCompat.getColor(this@HomeActivity, R.color.off_white))
        setSelectedItemBG(img_faq_nav!!, ll_faq_nav!!, -1, R.drawable.help_faq_new)
        tv_faq_nav!!.setTextColor(ContextCompat.getColor(this@HomeActivity, R.color.off_white))

        /*img_support_nav!!.setColorFilter(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.off_white
            )
        )*/
        setSelectedItemBG(img_support_nav!!, ll_support_nav!!, -1, R.drawable.support_new)
        tv_support_nav!!.setTextColor(ContextCompat.getColor(this@HomeActivity, R.color.off_white))

        img_logout_nav!!.setColorFilter(
            ContextCompat.getColor(
                this@HomeActivity,
                R.color.off_white
            )
        )
        tv_logout_nav!!.setTextColor(ContextCompat.getColor(this@HomeActivity, R.color.off_white))

        hideAllSeparationViews()*/

        Log.e("TestCurrentFragment", "****  " + getCurrentFragment())
        if (getCurrentFragment() is HomeFragment) {
//            setSelectedItemView(img_dashboard_nav!!, tv_dashboard_nav!!, R.color.red_dark_btn)
            setSelectedItemBG(
                img_dashboard_nav!!,
                ll_dash_nav!!,
                R.drawable.bg_menu_selected,
                R.drawable.dashboard_selected_nav_new
            )
        }

        if (getCurrentFragment() is MyAccountFragment) {
            Log.e("TestSelectedTab", "***   " + my_profile_viewTagSelected)
            if (!my_profile_viewTagSelected.isNullOrEmpty()) {
                if (my_profile_viewTagSelected == "personal") {
//                    setSelectedItemView(img_my_profile_nav_main, tv_my_profile_nav_main!!, R.color.red_dark_btn)
                    setSelectedItemBG(
                        img_my_profile_nav_main,
                        ll_my_profile_nav_main!!,
                        R.drawable.bg_menu_selected,
                        R.drawable.my_profile_selected_new
                    )
                } else {
//                    setSelectedItemView(img_funding_nav_main!!, tv_funding_nav_main!!, R.color.red_dark_btn)
                    setSelectedItemBG(
                        img_funding_nav_main!!,
                        ll_funding_nav_top,
                        R.drawable.bg_menu_selected,
                        R.drawable.funding_selected_nav_new
                    )
                    setSelectedItemBG(
                        null,
                        ll_buy_package_nav_child_one,
                        R.drawable.bg_menu_selected,
                        -1
                    )

//                    setSelectedItemView(img_funding_right!!, tv_buy_package_nav_child!!, R.color.red_dark_btn)
//                    setSelectedItemBG(img_funding_right!!, null, R.drawable.bg_menu_selected, R.color.off_white)
                }
            }

        }

        if (getCurrentFragment() is NetworkNewFragment || getCurrentFragment() is RegisterUserFragment) {
//            setSelectedItemView(img_my_network_nav_main!!, tv_my_network_nav_main!!, R.color.red_dark_btn)
            setSelectedItemBG(
                img_my_network_nav_main!!,
                ll_my_network_nav_top,
                R.drawable.bg_menu_selected,
                R.drawable.my_network_selected_nav_new
            )
//            setSelectedItemView(img_my_network_right!!, null, R.color.red_dark_btn)
            showSeparationView(viewMyNetwork, true)


            if (getCurrentFragment() is NetworkNewFragment) {
//                setSelectedItemView(null, tv_network_tree_nav_child, R.color.red_dark_btn)
                setSelectedItemBG(
                    null,
                    ll_network_tree_nav_child_one,
                    R.drawable.bg_menu_selected,
                    -1
                )
            } else {
//                setSelectedItemView(null, tv_registered_account_nav_child, R.color.red_dark_btn)
                setSelectedItemBG(
                    null,
                    ll_registered_account_nav_child_one,
                    R.drawable.bg_menu_selected,
                    -1
                )
            }
        }
        if (getCurrentFragment() is StockMarketFragment) {
//            setSelectedItemView(img_my_network_nav_main!!, tv_my_network_nav_main!!, R.color.red_dark_btn)
            setSelectedItemBG(
                img_stock_nav_main!!,
                ll_stock_nav_top,
                R.drawable.bg_menu_selected,
                R.drawable.ic_stocks_selected
            )
//            setSelectedItemView(img_my_network_right!!, null, R.color.red_dark_btn)
            showSeparationView(viewStock, true)


            if (getCurrentFragment() is StockMarketFragment) {
//                setSelectedItemView(null, tv_network_tree_nav_child, R.color.red_dark_btn)
                setSelectedItemBG(
                    null,
                    ll_stock_nav_child_one,
                    R.drawable.bg_menu_selected,
                    -1
                )
            }

        }
        if (getCurrentFragment() is StockWalletFragment) {
//            setSelectedItemView(img_my_network_nav_main!!, tv_my_network_nav_main!!, R.color.red_dark_btn)
            setSelectedItemBG(
                img_stock_nav_main!!,
                ll_stock_nav_top,
                R.drawable.bg_menu_selected,
                R.drawable.ic_stocks_selected
            )
//            setSelectedItemView(img_my_network_right!!, null, R.color.red_dark_btn)
            showSeparationView(viewStock, true)


            if (getCurrentFragment() is StockWalletFragment) {
//            setSelectedItemView(img_my_network_nav_main!!, tv_my_network_nav_main!!, R.color.red_dark_btn)

//                setSelectedItemView(null, tv_network_tree_nav_child, R.color.red_dark_btn)
                setSelectedItemBG(
                    null,
                    ll_stock_wallet_nav_child_one,
                    R.drawable.bg_menu_selected,
                    -1
                )

//            setSelectedItemView(img_my_network_right!!, null, R.color.red_dark_btn)
                showSeparationView(viewStock, true)


            }

        }
        if (getCurrentFragment() is StockHelpAndFaqFragment) {
//            setSelectedItemView(img_my_network_nav_main!!, tv_my_network_nav_main!!, R.color.red_dark_btn)
            setSelectedItemBG(
                img_stock_nav_main!!,
                ll_stock_nav_top,
                R.drawable.bg_menu_selected,
                R.drawable.ic_stocks_selected
            )
//            setSelectedItemView(img_my_network_right!!, null, R.color.red_dark_btn)
            showSeparationView(viewStock, true)


            if (getCurrentFragment() is StockHelpAndFaqFragment) {
//            setSelectedItemView(img_my_network_nav_main!!, tv_my_network_nav_main!!, R.color.red_dark_btn)

//                setSelectedItemView(null, tv_network_tree_nav_child, R.color.red_dark_btn)
                setSelectedItemBG(
                    null,
                    ll_stock_wallet_nav_child_third,
                    R.drawable.bg_menu_selected,
                    -1
                )

//            setSelectedItemView(img_my_network_right!!, null, R.color.red_dark_btn)
                showSeparationView(viewStock, true)


            }

        }

        if (getCurrentFragment() is FundWallets) {

//            setSelectedItemView(img_funding_nav_main!!, tv_funding_nav_main!!, R.color.red_dark_btn)
            setSelectedItemBG(
                img_funding_nav_main!!,
                ll_funding_nav_top,
                R.drawable.bg_menu_selected,
                R.drawable.funding_selected_nav_new
            )
            showSeparationView(viewFunding, true)
//            setSelectedItemView(img_funding_right!!, tv_topup_fund_nav_child!!, R.color.red_dark_btn)
            setSelectedItemBG(
                null,
                ll_topup_fund_nav_child_one,
                R.drawable.bg_menu_selected,
                -1
            )

        }

        if (getCurrentFragment() is CapitalWithdrawalWallet) {
//            setSelectedItemView(img_funding_nav_main!!, tv_funding_nav_main!!, R.color.red_dark_btn)
            setSelectedItemBG(
                img_funding_nav_main!!,
                ll_funding_nav_top,
                R.drawable.bg_menu_selected,
                R.drawable.funding_selected_nav_new
            )
            showSeparationView(viewFunding, true)
//            setSelectedItemView(img_funding_right!!, tv_capital_withdrawal_nav_child!!, R.color.red_dark_btn)
            setSelectedItemBG(
                null,
                ll_capital_withdrawal_nav_child_one,
                R.drawable.bg_menu_selected,
                -1
            )
        }

        if (getCurrentFragment() is CommissionBalanceWallet) {
//            setSelectedItemView(img_earning_nav_main!!, tv_earnings_nav_main!!, R.color.red_dark_btn)
            setSelectedItemBG(
                img_earning_nav_main!!,
                ll_earnings_nav_top,
                R.drawable.bg_menu_selected,
                R.drawable.earnings_selected_nav_new
            )
            showSeparationView(viewEarning, true)
//            setSelectedItemView(img_earnings_right!!, tv_cash_wallet_nav_child!!, R.color.red_dark_btn)
            setSelectedItemBG(null, ll_cash_wallet_nav_child_one, R.drawable.bg_menu_selected, -1)
        }

        if (getCurrentFragment() is WithdrawalWallets) {
//            setSelectedItemView(img_withdrawal_nav!!, tv_withdrawal_nav!!, R.color.red_dark_btn)
            setSelectedItemBG(
                img_withdrawal_nav!!,
                ll_withdrawal_nav,
                R.drawable.bg_menu_selected,
                R.drawable.withdrawal_selected_nav_new
            )
            showSeparationView(viewWithdrawal, true)
        }

        if (getCurrentFragment() is SelfTradingFragment) {
            //Not in use
            setSelectedItemView(img_selftrading_nav!!, tv_selftrading_nav!!, R.color.red_dark_btn)
        }

        if (getCurrentFragment() is TradingProfitReportFragment) {
//            setSelectedItemView(null, tv_total_report_nav!!, R.color.red_dark_btn)
            setSelectedItemBG(
                img_earning_nav_main!!,
                ll_earnings_nav_top,
                R.drawable.bg_menu_selected,
                R.drawable.earnings_selected_nav_new
            )
            showSeparationView(viewEarning, true)
            setSelectedItemBG(null, ll_report_nav_child_one, R.drawable.bg_menu_selected, -1)
        }

        /*     // not in use
             if (getCurrentFragment() is TradingProfitReportFragment) {

                 if (this.supportFragmentManager.findFragmentByTag("trading") != null && this.supportFragmentManager.findFragmentByTag(
                         "trading"
                     )!!.isVisible
                 ) setSelectedItemView(
                     null,
                     tv_trading_profit_nav!!,
                     R.color.red_dark_btn
                 )
                 else if (this.supportFragmentManager.findFragmentByTag("lotrebate") != null && this.supportFragmentManager.findFragmentByTag(
                         "lotrebate"
                     )!!.isVisible
                 ) setSelectedItemView(
                     null,
                     tv_lot_rebate_nav!!,
                     R.color.red_dark_btn
                 )
                 else if (this.supportFragmentManager.findFragmentByTag("lotrebatecommission") != null && this.supportFragmentManager.findFragmentByTag(
                         "lotrebatecommission"
                     )!!.isVisible
                 ) setSelectedItemView(
                     null,
                     tv_lot_rebate_commission_nav!!,
                     R.color.red_dark_btn
                 )
                 else if (this.supportFragmentManager.findFragmentByTag("leadershipbonus") != null && this.supportFragmentManager.findFragmentByTag(
                         "leadershipbonus"
                     )!!.isVisible
                 ) setSelectedItemView(
                     null,
                     tv_lot_leadership_bonus_nav!!,
                     R.color.red_dark_btn
                 )
                 else if (this.supportFragmentManager.findFragmentByTag("profitsharing") != null && this.supportFragmentManager.findFragmentByTag(
                         "profitsharing"
                     )!!.isVisible
                 ) setSelectedItemView(
                     null,
                     tv_profit_sharing_nav!!,
                     R.color.red_dark_btn
                 )
                 else if (this.supportFragmentManager.findFragmentByTag("overriding") != null && this.supportFragmentManager.findFragmentByTag(
                         "overriding"
                     )!!.isVisible
                 ) setSelectedItemView(
                     null,
                     tv_overriding_nav!!,
                     R.color.red_dark_btn
                 )

             }
     */

        //not in use
        if (getCurrentFragment() is SettingsFragment) setSelectedItemView(
            img_setting_nav!!,
            tv_settings_nav!!,
            R.color.red_dark_btn
        )

        //not in use
        if (getCurrentFragment() is NewsAndUpdateNavFragment) setSelectedItemView(
            img_news_nav!!,
            tv_news_nav!!,
            R.color.red_dark_btn
        )

        if (getCurrentFragment() is NewsAndUpdateNavFragment || getCurrentFragment() is WebViewNavFragment) {
//            setSelectedItemView(img_news_tools_nav_main!!, tv_news_tools_nav_main!!, R.color.red_dark_btn)
            setSelectedItemBG(
                img_news_tools_nav_main!!,
                ll_news_tools_nav_top!!,
                R.drawable.bg_menu_selected,
                R.drawable.news_tools_selected_new
            )
            showSeparationView(viewNewsAndTools, true)
//            setSelectedItemView(img_news_tools_right!!, null, R.color.red_dark_btn)


            if (getCurrentFragment() is NewsAndUpdateNavFragment) {
//                setSelectedItemView(null, tv_news_event_nav_child, R.color.red_dark_btn)
                setSelectedItemBG(
                    null,
                    ll_news_event_nav_child_one!!,
                    R.drawable.bg_menu_selected,
                    -1
                )
            } else {
//                setSelectedItemView(null, tv_industry_tools_nav_child, R.color.red_dark_btn)
                setSelectedItemBG(
                    null,
                    ll_industry_tools_nav_child_one!!,
                    R.drawable.bg_menu_selected,
                    -1
                )
            }
        }

//        if (getCurrentFragment() is HelpFragment) setSelectedItemView(img_faq_nav!!, tv_faq_nav!!, R.color.red_dark_btn)
        if (getCurrentFragment() is HelpFragment) {
            setSelectedItemBG(
                img_faq_nav!!,
                ll_faq_nav!!,
                R.drawable.bg_menu_selected,
                R.drawable.help_faq_selected_new
            )
            showSeparationView(viewHelpAndFAQ, true)
        }


//        if (getCurrentFragment() is HelpAndSupportFragment) setSelectedItemView(img_support_nav!!, tv_support_nav!!, R.color.red_dark_btn)
        if (getCurrentFragment() is HelpAndSupportFragment) {
            setSelectedItemBG(
                img_support_nav!!,
                ll_support_nav!!,
                R.drawable.bg_menu_selected,
                R.drawable.support_selected_new
            )
            showSeparationView(viewSupport, true)
        }


    }

    // not in use
    private fun setSelectedItemView(imgNav: ImageView?, tvNav: TextView?, color: Int) {
        if (imgNav != null) {
            imgNav!!.setColorFilter(
                ContextCompat.getColor(
                    this@HomeActivity,
                    color
                )
            )
        }

        if (tvNav != null) {
            tvNav!!.setTextColor(
                ContextCompat.getColor(
                    this@HomeActivity,
                    color
                )
            )
        }
    }

    private fun setSelectedItemBG(imgNav: ImageView?, mView: View?, viewBgID: Int, resId: Int) {
        if (imgNav != null) {
            /*imgNav!!.setColorFilter(
                ContextCompat.getColor(
                    this@HomeActivity,
                    color
                )
            )*/
            imgNav!!.setImageResource(resId)
        }

        var drawableBG: Drawable? = null
        if (viewBgID != -1) {
            drawableBG = ContextCompat.getDrawable(this@HomeActivity, viewBgID)
        }
        if (mView != null) {
            mView!!.background = drawableBG
        } else {
            mView!!.background == null
        }
    }

    //deselect all views
    private fun removeAllViewBG() {
        setSelectedItemBG(img_dashboard_nav!!, ll_dash_nav!!, -1, R.drawable.dashboard_nav_new)
        setSelectedItemBG(
            img_my_profile_nav_main!!,
            ll_my_profile_nav_main!!,
            -1,
            R.drawable.my_profile_new
        )
        setSelectedItemBG(
            img_my_network_nav_main!!,
            ll_my_network_nav_top!!,
            -1,
            R.drawable.my_network_nav_new
        )
        setSelectedItemBG(null, ll_network_tree_nav_child_one!!, -1, -1)
        setSelectedItemBG(null, ll_registered_account_nav_child_one!!, -1, -1)
        setSelectedItemBG(
            img_funding_nav_main!!,
            ll_funding_nav_top!!,
            -1,
            R.drawable.funding_nav_new
        )
        setSelectedItemBG(null, ll_topup_fund_nav_child_one!!, -1, -1)
        setSelectedItemBG(null, ll_buy_package_nav_child_one!!, -1, -1)
        setSelectedItemBG(null, ll_capital_withdrawal_nav_child_one!!, -1, -1) //not in use
        setSelectedItemBG(
            img_earning_nav_main!!,
            ll_earnings_nav_top!!,
            -1,
            R.drawable.earnings_nav_new
        )
        setSelectedItemBG(null, ll_cash_wallet_nav_child_one!!, -1, -1)
        setSelectedItemBG(null, ll_report_nav_child_one!!, -1, -1)
        setSelectedItemBG(
            img_withdrawal_nav!!,
            ll_withdrawal_nav!!,
            -1,
            R.drawable.withdrawal_nav_new
        )
        setSelectedItemBG(
            img_news_tools_nav_main!!,
            ll_news_tools_nav_top!!,
            -1,
            R.drawable.news_tools_new
        )
        setSelectedItemBG(null, ll_news_event_nav_child_one!!, -1, -1)
        setSelectedItemBG(null, ll_industry_tools_nav_child_one!!, -1, -1)
        setSelectedItemBG(img_faq_nav!!, ll_faq_nav!!, -1, R.drawable.help_faq_new)
        setSelectedItemBG(img_support_nav!!, ll_support_nav!!, -1, R.drawable.support_new)
    }

    private fun hideAllSeparationViews() {
        viewMyNetwork!!.visibility = View.GONE
        viewFunding!!.visibility = View.GONE
        viewEarning!!.visibility = View.GONE
        viewWithdrawal!!.visibility = View.GONE
        viewNewsAndTools!!.visibility = View.GONE
        viewHelpAndFAQ!!.visibility = View.GONE
        viewSupport!!.visibility = View.GONE
    }

    private fun showSeparationView(mView: View?, isShow: Boolean) {
        //temp comented seperation view for all selection
        /*if (mView != null){
            if (isShow){
                mView!!.visibility = View.VISIBLE
            }else{
                mView!!.visibility = View.GONE
            }

        }*/
    }

    /*private fun setBottomTabIcon(imgNav: ImageView?, resId: Int){
        ivFundingTab!!.setImageResource(R.drawable.funding_nav_new)
        ivEarningTab!!.setImageResource(R.drawable.earnings_nav_new)
        ivDashboardTab!!.setImageResource(R.drawable.dashboard_nav_new)
        ivWithdrawalTab!!.setImageResource(R.drawable.withdrawal_nav_new)

        if (imgNav != null) {
            imgNav!!.setImageResource(resId)
        }
     }*/

}

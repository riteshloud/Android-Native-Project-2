package com.demo2.utilities

open class Constants {
    companion object {

        var userInvestmentsCapital = ""
        var isNeedToUpdateUserInvestments = true

        const val passwordKeyAES ="android"
        const val amountBreakLimit: Int = 6
    //    const val paginationLimit: Int = 20
        const val paginationLimit: Int = 20 //1
        const val networkPaginationLimit: Int = 700
        const val hotelPaginationLimit : Int = 20
        const val connectionTimeOut: Long = 80000
        const val readTimeOut: Long = 80000
        const val maxAmount: Double = 999999999.99
        const val maxIntCount: Int = 9
        const val amountDecimal: Int = 2

        const val maxAmountCapital: Double = 9999999999999999999.99
        const val maxIntCountCapital: Int = 19
        const val amountDecimalCapital: Int = 2

        const val Localization: String = "localization"
        const val prefAuthorizationToken: String = "authorizationtoken"
        const val pref: String = "mypreference"
        const val prefLoginPassword: String = "password"
        const val prefLoginUsername: String = "email"
        const val prefFingerPrintSetInThisDevice : String = "prefFingerPrintSetInThisDevice"
        //only for fingerprint enable to save to next time loogin with this user
        const val prefFingerUsername : String = "prefFingerUsername"
        const val prefFingerPassword : String = "prefFingerPassword"
        const val prefFingerUUID : String = "prefFingerUUID"
        const val prefIsRemember: String = "isrememeber"
        const val prefCommonData: String = "commondata"
        const val prefUserData: String = "userdata"
        const val prefProfile: String = "profile"
        const val prefRank: String = "rank"

        const val deviceType: String = "android"
        const val typeProfitWithdrawalForm: String = "0"
        const val typeCapitalWithdrawalForm: String = "1"
        const val typeBank: String = "Bank"
        const val typeUSDT: String = "USDT"
        const val prefPromoAccount : String = "prefPromoAccount"


       /** "sourceWallet" - Values are any one from " pips_rebate_wallet , pips_commission_wallet , overriding_wallet , mt_wallet , profit_sharing_wallet ,leader_bonus_wallet"
         * "transfer_wallet" - Value are select as 0 for transfer to withdrawal wallet,1 for transfer to mt_topup_wallet,2 for transfer to otm_wallet* */
        const val transferSourcePipsRebate = "pips_rebate_wallet"
        const val transferSourcePipsCommission = "pips_commission_wallet"
        const val transferSourceOverriding = "overriding_wallet"
        const val transferSourceMt = "mt_wallet"
        const val transferSourceProfitSharing = "profit_sharing_wallet"
        const val transferSourceLeaderBonus = "leader_bonus_wallet"
        const val transferToWithdrawal = 0
        const val transferToPoint = 1
        const val transferToOTM = 2
        const val transferToFund = 3
        const val TransactionNormal= "normal"
        const val TransactionUUID= "finger"


        /**intent codes */
        const val codeSettings = 101
        const val codeCameraRequest = 201
        const val codePickImageRequest = 301
        const val codePickPdfRequest = 401
        const val codePermissions = 1111

      //  const val WhichVideo="WhichVideo"

        /** Self Trading "sourceWallet" - Values are any one from "pips_commission and self_trading_withdrawal" */
        const val transferSourceSelfTradPipsRebate = "pips_commission"
        const val transferSourceSelfTradPipsCommission = "self_trading_withdrawal"
        const val transferToSelfTradWithdrawalWallet = 0//withdrawal wallet
        const val transferToSelfTradStWithdrawalWallet = 1//self trade withdrawal wallet


      /** For Hotel Values */

        const val HotelId = "HotelId"
        const val NumberOfRooms = "NumberOfRooms"
        const val CheckInDate = "CheckInDate"
        const val CheckOutDate = "CheckOutDate"
        const val ConfirmHotelData = "ConfirmHotelData"
        const val ShowAnnouncement = "ShowAnnouncement"

        /** Payment types */
        const val TYPE_USDT = "usdt"
        const val TYPE_ONLINE = "online"
        const val TYPE_DIRECT_TRANSFER = "direct_transfer"
        const val TYPE_BANK_3RD_PARTY = "bank3rdParty"
        const val TYPE_MT5 = "mt5request"
        const val TYPE_TRANSFER = "transfer"

    }
}
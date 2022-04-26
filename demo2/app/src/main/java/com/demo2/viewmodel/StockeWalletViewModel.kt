package com.demo2.viewmodel

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.demo2.model.*
import com.demo2.utilities.Constants
import com.demo2.utilities.Pref
import com.demo2.view.service.ApiClient
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StockeWalletViewModel(activity: Activity) : ViewModel() {

    var topupFundResponseModel: MutableLiveData<StockWalletTopUpModel>? = MutableLiveData()
    var stockMoneyTransferModel: MutableLiveData<StockMoneyTransferModel>? = MutableLiveData()

    var stockWalletDetail: MutableLiveData<StockWalletDetail>? = MutableLiveData()
    var stockWalletFundTopUpHistoryModel: MutableLiveData<StockWalletFundTopUpHistoryModel>? = MutableLiveData()
    var cancelStockFundRequestModel: MutableLiveData<BaseResponse>? = MutableLiveData()

    var isLoading: MutableLiveData<Boolean>? = MutableLiveData()
    var responseError: MutableLiveData<ResponseBody>? = MutableLiveData()

    // var responseError1: MutableLiveData<ResponseBody>? = MutableLiveData()
    var context = activity


    fun getStockWalletDetail(
        offset: Int
    ) {
        isLoading?.value = true
        ApiClient.getClient(context).stockWalletDetails(
            localization = Pref.getLocalization(context),
            authorization = Pref.getprefAuthorizationToken(context),
            offset = offset,
            limit = Constants.paginationLimit

        ).enqueue(object : Callback<StockWalletDetail> {
            override fun onFailure(call: Call<StockWalletDetail>, t: Throwable) {
                isLoading?.value = false
                Log.v("=====EXCE", "-" + t.message)
            }

            override fun onResponse(
                call: Call<StockWalletDetail>,
                response: Response<StockWalletDetail>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    if (offset == 0) {
                        stockWalletDetail?.value = response.body().apply {

                          this!!.paginationEnded = response.body()!!.payload!!.history!!.size < Constants.paginationLimit

                        }
                    } else {
                        stockWalletDetail?.value = stockWalletDetail!!.value.apply {

//                                this!!.payload?.history = response!!.body()!!.payload!!.history!!
                                this!!.payload!!.history!!.addAll(response.body()!!.payload!!.history!!)
                                this!!.paginationEnded = response.body()!!.payload!!.history!!.size < Constants.paginationLimit


                        }
                    }
                } else {
                    responseError?.value = response.errorBody()
                }
            }
        })
    }

    fun getStockWalletFundTopUpHistory(
        offset: Int
    ) {
        isLoading?.value = true
        ApiClient.getClient(context).stockWalletFundTopUpHistory(
            localization = Pref.getLocalization(context),
            authorization = Pref.getprefAuthorizationToken(context),
            offset = offset,
            limit = Constants.paginationLimit

        ).enqueue(object : Callback<StockWalletFundTopUpHistoryModel> {
            override fun onFailure(call: Call<StockWalletFundTopUpHistoryModel>, t: Throwable) {
                isLoading?.value = false
                Log.v("=====EXCE", "-" + t.message)
            }

            override fun onResponse(
                call: Call<StockWalletFundTopUpHistoryModel>,
                response: Response<StockWalletFundTopUpHistoryModel>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    if (offset == 0) {
                        stockWalletFundTopUpHistoryModel?.value = response.body().apply {

                          this!!.paginationEnded = response.body()!!.payload!!.history!!.size < Constants.paginationLimit

                        }
                    } else {
                        stockWalletFundTopUpHistoryModel?.value = stockWalletFundTopUpHistoryModel!!.value.apply {

//                                this!!.payload?.history = response!!.body()!!.payload!!.history!!
                                this!!.payload!!.history!!.addAll(response.body()!!.payload!!.history!!)
                                this!!.paginationEnded = response.body()!!.payload!!.history!!.size < Constants.paginationLimit

                        }
                    }
                } else {
                    responseError?.value = response.errorBody()
                }
            }
        })
    }

    fun cancelStockFundRequest(transaction_id: String) {
        isLoading?.value = true
        ApiClient.getClient(context).cancelStockFundRequest(
            localization = Pref.getLocalization(context),
            authorization = Pref.getprefAuthorizationToken(context),
            transaction_id = transaction_id
        ).enqueue(object : Callback<BaseResponse> {
            override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                isLoading?.value = false
                Log.v("=====EXCE", "-" + t.message)
            }

            override fun onResponse(
                call: Call<BaseResponse>,
                response: Response<BaseResponse>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    cancelStockFundRequestModel?.value = response.body()
                } else {
                    responseError?.value = response.errorBody()
                }
            }
        })
    }


    fun topupFundUSDTWithUUID(
        amount: RequestBody,
        securityPassword: RequestBody,
        type: RequestBody,
        receipt: MultipartBody.Part,
        authType: RequestBody,
        usdtAddress: RequestBody

    ) {

        isLoading!!.value = true
        ApiClient.getClient(context).stockWalletTopUp(
            localization = Pref.getLocalization(context),
            authorization = Pref.getprefAuthorizationToken(context),
            amount = amount,
            securityPassword = securityPassword,
            type = type,
            authType = authType,
            usdtAddress = usdtAddress,
            bank_proof = receipt
        ).enqueue(object : Callback<StockWalletTopUpModel> {
            override fun onFailure(call: Call<StockWalletTopUpModel>, t: Throwable) {
           Log.v("=====exce","-"+t.message)
                isLoading?.value = false
                //Log.e("zxczxc", "onFailure - ${t.message}")
            }

            override fun onResponse(
                call: Call<StockWalletTopUpModel>,
                response: Response<StockWalletTopUpModel>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    Toast.makeText(context, response.body()!!.message, Toast.LENGTH_SHORT).show()
                    topupFundResponseModel!!.value = response.body()

                } else {
                    responseError?.value = response.errorBody()
                }
            }

        })
    }

    fun topupFundOnline(
        amount: String,
        securityPassword: String,
        type: String,
        bank_id : String,
        bank_amount : String,
//        account_name : String,
//        account_no : String,
        bankDetail: String
    ) {
        if (bankDetail=="1") {
            isLoading!!.value = true
            ApiClient.getClient(context).stockWalletTopupWalletOnlineChina(
                localization = Pref.getLocalization(context),
                authorization = Pref.getprefAuthorizationToken(context),
                amount = amount,
                securityPassword = securityPassword,
                type = type,
                bank_amount = bank_amount
//                account_name = account_name
            ).enqueue(object : Callback<StockWalletTopUpModel> {
                override fun onFailure(call: Call<StockWalletTopUpModel>, t: Throwable) {
                    isLoading?.value = false
                    Log.e("zxczxc", "onFailure - ${t.message}")
                }

                override fun onResponse(
                    call: Call<StockWalletTopUpModel>,
                    response: Response<StockWalletTopUpModel>
                ) {
                    isLoading?.value = false
                    if (response.isSuccessful) {
                        Toast.makeText(context, response.body()!!.message, Toast.LENGTH_SHORT)
                            .show()
                        topupFundResponseModel!!.value = response.body()

                    } else {
                        topupFundResponseModel!!.value = response.body()

                   //     responseError?.value = response.errorBody()
                    }
                }

            })
        }else{
            isLoading!!.value = true
            ApiClient.getClient(context).stockWalletTopupWalletOnline(
                localization = Pref.getLocalization(context),
                authorization = Pref.getprefAuthorizationToken(context),
                amount = amount,
                securityPassword = securityPassword,
                type = type,
                bank_id = bank_id,
                bank_amount = bank_amount
            ).enqueue(object : Callback<StockWalletTopUpModel> {
                override fun onFailure(call: Call<StockWalletTopUpModel>, t: Throwable) {
                    isLoading?.value = false
                    Log.e("zxczxc", "onFailure - ${t.message}")
                }

                override fun onResponse(
                    call: Call<StockWalletTopUpModel>,
                    response: Response<StockWalletTopUpModel>
                ) {
                    isLoading?.value = false
                    if (response.isSuccessful) {
                        Toast.makeText(context, response.body()!!.message, Toast.LENGTH_SHORT)
                            .show()
                        topupFundResponseModel!!.value = response.body()

                    } else {

                       responseError?.value = response.errorBody()
                    }
                }

            })
        }
    }


    fun topupFundUSDT(
        amount: RequestBody,
        securityPassword: RequestBody,
        type: RequestBody,
        receipt: MultipartBody.Part,
        usdtAddress: RequestBody

        ) {

        isLoading!!.value = true
        ApiClient.getClient(context).stockWalletTopUp(
            localization = Pref.getLocalization(context),
            authorization = Pref.getprefAuthorizationToken(context),
            amount = amount,
            securityPassword = securityPassword,
            type = type,
            usdtAddress = usdtAddress,

            bank_proof = receipt
        ).enqueue(object : Callback<StockWalletTopUpModel> {
            override fun onFailure(call: Call<StockWalletTopUpModel>, t: Throwable) {
                isLoading?.value = false
                //Log.e("zxczxc", "onFailure - ${t.message}")
            }

            override fun onResponse(
                call: Call<StockWalletTopUpModel>,
                response: Response<StockWalletTopUpModel>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    Toast.makeText(context, response.body()!!.message, Toast.LENGTH_SHORT).show()
                    topupFundResponseModel!!.value = response.body()

                } else {
                    responseError?.value = response.errorBody()
                }
            }

        })
    }




    fun topupFundOnlineUUID(
        amount: String,
        securityPassword: String,
        type: String,
        bank_id : String,
        bank_amount : String,
//        account_name : String,
//        account_no : String,
        authType : String,
        bankDetail:String
    ) {
        if (bankDetail=="1") {
            isLoading!!.value = true
            ApiClient.getClient(context).stockWalletTopupWalletOnlineUUID(
                localization = Pref.getLocalization(context),
                authorization = Pref.getprefAuthorizationToken(context),
                amount = amount,
                securityPassword = securityPassword,
                type = type,
                bank_amount = bank_amount,
//                account_name = account_name,
                authType = authType
            ).enqueue(object : Callback<StockWalletTopUpModel> {
                override fun onFailure(call: Call<StockWalletTopUpModel>, t: Throwable) {
                    isLoading?.value = false
                    Log.e("zxczxc", "onFailure - ${t.message}")
                }

                override fun onResponse(
                    call: Call<StockWalletTopUpModel>,
                    response: Response<StockWalletTopUpModel>
                ) {
                    isLoading?.value = false
                    if (response.isSuccessful) {
                        Toast.makeText(context, response.body()!!.message, Toast.LENGTH_SHORT)
                            .show()
                        topupFundResponseModel!!.value = response.body()

                    } else {

                  responseError?.value = response.errorBody()
                    }
                }

            })
        }
        else{
            isLoading!!.value = true
            ApiClient.getClient(context).stockWalletTopupWalletOnlineWithChina(
                localization = Pref.getLocalization(context),
                authorization = Pref.getprefAuthorizationToken(context),
                amount = amount,
                securityPassword = securityPassword,
                type = type,
                bank_id = bank_id,
                bank_amount = bank_amount,
                authType = authType
            ).enqueue(object : Callback<StockWalletTopUpModel> {
                override fun onFailure(call: Call<StockWalletTopUpModel>, t: Throwable) {
                    isLoading?.value = false
                    Log.e("zxczxc", "onFailure - ${t.message}")
                }

                override fun onResponse(
                    call: Call<StockWalletTopUpModel>,
                    response: Response<StockWalletTopUpModel>
                ) {
                    isLoading?.value = false
                    if (response.isSuccessful) {
                        Toast.makeText(context, response.body()!!.message, Toast.LENGTH_SHORT)
                            .show()
                        topupFundResponseModel!!.value = response.body()

                    } else {
                        responseError?.value = response.errorBody()
                    }
                }

            })
        }
    }


    // for UUID or normal both
    fun topupFundStockDirectTransfer(
            amount: String,
            securityPassword: String,
            type: String,
            bank_amount : String,
            authType : String
    ) {
            isLoading!!.value = true
            ApiClient.getClient(context).topupFundStockDirectTransfer(
                    localization = Pref.getLocalization(context),
                    authorization = Pref.getprefAuthorizationToken(context),
                    amount = amount,
                    securityPassword = securityPassword,
                    type = type,
                    bank_amount = bank_amount,
                    authType = authType
            ).enqueue(object : Callback<StockWalletTopUpModel> {
                override fun onFailure(call: Call<StockWalletTopUpModel>, t: Throwable) {
                    isLoading?.value = false
                    Log.e("zxczxc", "onFailure - ${t.message}")
                }

                override fun onResponse(
                        call: Call<StockWalletTopUpModel>,
                        response: Response<StockWalletTopUpModel>
                ) {
                    isLoading?.value = false
                    if (response.isSuccessful) {
                        Toast.makeText(context, response.body()!!.message, Toast.LENGTH_SHORT)
                                .show()
                        topupFundResponseModel!!.value = response.body()

                    } else {
                        responseError?.value = response.errorBody()
                    }
                }
            })
    }


    fun stockWalletTransferMoney(
        amount: String,
        securityPassword: String,
        fundType: String
    ){
        isLoading!!.value = true
        ApiClient.getClient(context).stockWalletTransferMoney(
            localization = Pref.getLocalization(context),
            authorization = Pref.getprefAuthorizationToken(context),
            amount = amount,
            securityPassword = securityPassword,
            fundType = fundType
        ).enqueue(object : Callback<StockMoneyTransferModel> {
            override fun onFailure(call: Call<StockMoneyTransferModel>, t: Throwable) {
                isLoading?.value = false
                Log.e("zxczxc", "onFailure - ${t.message}")
            }

            override fun onResponse(
                call: Call<StockMoneyTransferModel>,
                response: Response<StockMoneyTransferModel>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    Toast.makeText(context, response.body()!!.message, Toast.LENGTH_SHORT)
                        .show()
                    stockMoneyTransferModel!!.value = response.body()

                } else {
                    responseError?.value = response.errorBody()
                }
            }

        })
    }
    fun stockWalletTransferMoneywithFinger(
        amount: String,
        securityPassword: String,
        fundType: String,
        authType : String
    ){
        isLoading!!.value = true
        ApiClient.getClient(context).stockWalletTransferMoneywithFinger(
            localization = Pref.getLocalization(context),
            authorization = Pref.getprefAuthorizationToken(context),
            amount = amount,
            securityPassword = securityPassword,
            fundType = fundType,
            authType = authType
        ).enqueue(object : Callback<StockMoneyTransferModel> {
            override fun onFailure(call: Call<StockMoneyTransferModel>, t: Throwable) {
                isLoading?.value = false
                Log.e("zxczxc", "onFailure - ${t.message}")
            }

            override fun onResponse(
                call: Call<StockMoneyTransferModel>,
                response: Response<StockMoneyTransferModel>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    Toast.makeText(context, response.body()!!.message, Toast.LENGTH_SHORT)
                        .show()
                    stockMoneyTransferModel!!.value = response.body()

                } else {
                    responseError?.value = response.errorBody()
                }
            }

        })
    }
}
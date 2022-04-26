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

class WalletFundsViewModel(activity: Activity) : ViewModel() {

    var fundTopupHistoryModel: MutableLiveData<HistoryFundTopupModel>? = MutableLiveData()
    var isLoading: MutableLiveData<Boolean>? = MutableLiveData()
    var responseError: MutableLiveData<ResponseBody>? = MutableLiveData()
    var activity = activity
    var topupFundResponseModel: MutableLiveData<TopupFundResponseModel>? = MutableLiveData()
    var transferFundResponseModel: MutableLiveData<TransferFundResponseModel>? = MutableLiveData()
    var usdtTopupFundResponseModel: MutableLiveData<TopupFundResponseModel>? = MutableLiveData()
    var walletHistoryModel: MutableLiveData<HistoryFundWallet>? = MutableLiveData()
    var mt4TopupHistoryModel: MutableLiveData<HistoryMt4TopupModel>? = MutableLiveData()
    var varifyResponseModel: MutableLiveData<Response<ResponseBody>>? = MutableLiveData()
    var cancelFundRequestModel: MutableLiveData<BaseResponse>? = MutableLiveData()

    fun getFundTopupHistory(offset: Int) {
        isLoading!!.value = true
        ApiClient.getClient(activity).getFundTopupHistory(
            authorization = Pref.getprefAuthorizationToken(activity),
            localization = Pref.getLocalization(activity),
            offset = offset,
            limit = Constants.paginationLimit
        ).enqueue(object : Callback<HistoryFundTopupModel> {
            override fun onFailure(call: Call<HistoryFundTopupModel>, t: Throwable) {
                isLoading?.value = false
            }

            override fun onResponse(
                call: Call<HistoryFundTopupModel>,
                response: Response<HistoryFundTopupModel>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    if (fundTopupHistoryModel?.value == null) {
                        fundTopupHistoryModel?.value = response.body().apply {
                            this!!.paginationEnded =
                                response.body()!!.payload!!.history!!.size < Constants.paginationLimit
                        }
                    } else {
                        fundTopupHistoryModel?.value = fundTopupHistoryModel!!.value.apply {
                            this!!.payload!!.history!!.addAll(response.body()!!.payload!!.history!!)
                            this.paginationEnded =
                                response.body()!!.payload!!.history!!.size < Constants.paginationLimit
                        }
                    }
                } else {
                    responseError?.value = response.errorBody()
                }
            }

        })
    }

    fun cancelFundRequest(transaction_id: String) {
        isLoading?.value = true
        ApiClient.getClient(activity).cancelFundRequest(
                localization = Pref.getLocalization(activity),
                authorization = Pref.getprefAuthorizationToken(activity),
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
                    cancelFundRequestModel?.value = response.body()
                } else {
                    responseError?.value = response.errorBody()
                }
            }
        })
    }


    fun getWalletHistory(offset: Int) {
        isLoading!!.value = true
        ApiClient.getClient(activity).getWalletHistory(
            authorization = Pref.getprefAuthorizationToken(activity),
            localization = Pref.getLocalization(activity),
            offset = offset,
            limit = Constants.paginationLimit
        ).enqueue(object : Callback<HistoryFundWallet> {
            override fun onFailure(call: Call<HistoryFundWallet>, t: Throwable) {
                isLoading?.value = false
            }

            override fun onResponse(
                call: Call<HistoryFundWallet>,
                response: Response<HistoryFundWallet>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    if (walletHistoryModel?.value == null) {
                        walletHistoryModel?.value = response.body().apply {
                            this!!.paginationEnded =
                                response.body()!!.payload!!.history!!.size < Constants.paginationLimit

                        }
                    } else {
                        walletHistoryModel?.value = walletHistoryModel!!.value.apply {
                            this!!.payload!!.history!!.addAll(response.body()!!.payload!!.history!!)
                            this!!.paginationEnded =
                                response.body()!!.payload!!.history!!.size < Constants.paginationLimit

                        }
                    }
                } else {
                    responseError?.value = response.errorBody()
                }
            }

        })
    }

    fun getMt4TopupHistory(offset: Int) {
        isLoading!!.value = true
        ApiClient.getClient(activity).getMt4TopupHistory(
            authorization = Pref.getprefAuthorizationToken(activity),
            localization = Pref.getLocalization(activity),
            offset = offset,
            limit = Constants.paginationLimit
        ).enqueue(object : Callback<HistoryMt4TopupModel> {
            override fun onFailure(call: Call<HistoryMt4TopupModel>, t: Throwable) {
                isLoading?.value = false
            }

            override fun onResponse(
                call: Call<HistoryMt4TopupModel>,
                response: Response<HistoryMt4TopupModel>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    if (mt4TopupHistoryModel?.value == null) {
                        mt4TopupHistoryModel?.value = response.body().apply {
                            this!!.paginationEnded =
                                response.body()!!.payload!!.history!!.size < Constants.paginationLimit

                        }
                    } else {
                        mt4TopupHistoryModel?.value = mt4TopupHistoryModel!!.value.apply {
                            this!!.payload!!.history!!.addAll(response.body()!!.payload!!.history!!)
                            this!!.paginationEnded =
                                response.body()!!.payload!!.history!!.size < Constants.paginationLimit
                        }
                    }
                } else {
                    responseError?.value = response.errorBody()
                }
            }

        })
    }

    fun topupFundUSDT(
        amount: RequestBody,
        securityPassword: RequestBody,
        type: RequestBody,
        usdtAddress: RequestBody,
        receipt: MultipartBody.Part
    ) {

        isLoading!!.value = true
        ApiClient.getClient(activity).topupWalletUSDT(
            localization = Pref.getLocalization(activity),
            authorization = Pref.getprefAuthorizationToken(activity),
            amount = amount,
            securityPassword = securityPassword,
            type = type,
            usdtAddress = usdtAddress,
            bank_proof = receipt
        ).enqueue(object : Callback<TopupFundResponseModel> {
            override fun onFailure(call: Call<TopupFundResponseModel>, t: Throwable) {
                isLoading?.value = false
                //Log.e("zxczxc", "onFailure - ${t.message}")
            }

            override fun onResponse(
                call: Call<TopupFundResponseModel>,
                response: Response<TopupFundResponseModel>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    Toast.makeText(activity, response.body()!!.message, Toast.LENGTH_SHORT).show()
                    topupFundResponseModel!!.value = response.body()

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
        ApiClient.getClient(activity).topupWalletUSDT(
            localization = Pref.getLocalization(activity),
            authorization = Pref.getprefAuthorizationToken(activity),
            amount = amount,
            securityPassword = securityPassword,
            type = type,
            authType = authType,
            usdtAddress = usdtAddress,
            bank_proof = receipt
        ).enqueue(object : Callback<TopupFundResponseModel> {
            override fun onFailure(call: Call<TopupFundResponseModel>, t: Throwable) {
                isLoading?.value = false
                //Log.e("zxczxc", "onFailure - ${t.message}")
            }

            override fun onResponse(
                call: Call<TopupFundResponseModel>,
                response: Response<TopupFundResponseModel>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    Toast.makeText(activity, response.body()!!.message, Toast.LENGTH_SHORT).show()
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
        bankDetail: String
    ) {
        if (bankDetail=="1") {
            isLoading!!.value = true
            ApiClient.getClient(activity).topupWalletOnlineChina(
                localization = Pref.getLocalization(activity),
                authorization = Pref.getprefAuthorizationToken(activity),
                amount = amount,
                securityPassword = securityPassword,
                type = type,
                bank_amount = bank_amount
            ).enqueue(object : Callback<TopupFundResponseModel> {
                override fun onFailure(call: Call<TopupFundResponseModel>, t: Throwable) {
                    isLoading?.value = false
                    Log.e("zxczxc", "onFailure - ${t.message}")
                }

                override fun onResponse(
                    call: Call<TopupFundResponseModel>,
                    response: Response<TopupFundResponseModel>
                ) {
                    isLoading?.value = false
                    if (response.isSuccessful) {
                        Toast.makeText(activity, response.body()!!.message, Toast.LENGTH_SHORT)
                            .show()
                        topupFundResponseModel!!.value = response.body()

                    } else {
                        responseError?.value = response.errorBody()
                    }
                }

            })
        }else{
            isLoading!!.value = true
            ApiClient.getClient(activity).topupWalletOnline(
                localization = Pref.getLocalization(activity),
                authorization = Pref.getprefAuthorizationToken(activity),
                amount = amount,
                securityPassword = securityPassword,
                type = type,
                bank_id = bank_id,
                bank_amount = bank_amount
            ).enqueue(object : Callback<TopupFundResponseModel> {
                override fun onFailure(call: Call<TopupFundResponseModel>, t: Throwable) {
                    isLoading?.value = false
                    Log.e("zxczxc", "onFailure - ${t.message}")
                }

                override fun onResponse(
                    call: Call<TopupFundResponseModel>,
                    response: Response<TopupFundResponseModel>
                ) {
                    isLoading?.value = false
                    if (response.isSuccessful) {
                        Toast.makeText(activity, response.body()!!.message, Toast.LENGTH_SHORT)
                            .show()
                        topupFundResponseModel!!.value = response.body()

                    } else {
                        responseError?.value = response.errorBody()
                    }
                }

            })
        }
    }

    fun topupFundOnlineUUID(
        amount: String,
        securityPassword: String,
        type: String,
        bank_id : String,
        bank_amount : String,
        authType : String,
        bankDetail:String
    ) {
        if (bankDetail=="1") {
            isLoading!!.value = true
            ApiClient.getClient(activity).topupWalletOnlineUUID(
                localization = Pref.getLocalization(activity),
                authorization = Pref.getprefAuthorizationToken(activity),
                amount = amount,
                securityPassword = securityPassword,
                type = type,
                bank_amount = bank_amount,
                authType = authType
            ).enqueue(object : Callback<TopupFundResponseModel> {
                override fun onFailure(call: Call<TopupFundResponseModel>, t: Throwable) {
                    isLoading?.value = false
                    Log.e("zxczxc", "onFailure - ${t.message}")
                }

                override fun onResponse(
                    call: Call<TopupFundResponseModel>,
                    response: Response<TopupFundResponseModel>
                ) {
                    isLoading?.value = false
                    if (response.isSuccessful) {
                        Toast.makeText(activity, response.body()!!.message, Toast.LENGTH_SHORT)
                            .show()
                        topupFundResponseModel!!.value = response.body()

                    } else {
                        responseError?.value = response.errorBody()
                    }
                }

            })
        }else{
            isLoading!!.value = true
            ApiClient.getClient(activity).topupWalletOnlineWithChina(
                localization = Pref.getLocalization(activity),
                authorization = Pref.getprefAuthorizationToken(activity),
                amount = amount,
                securityPassword = securityPassword,
                type = type,
                bank_id = bank_id,
                bank_amount = bank_amount,
                authType = authType
            ).enqueue(object : Callback<TopupFundResponseModel> {
                override fun onFailure(call: Call<TopupFundResponseModel>, t: Throwable) {
                    isLoading?.value = false
                    Log.e("zxczxc", "onFailure - ${t.message}")
                }

                override fun onResponse(
                    call: Call<TopupFundResponseModel>,
                    response: Response<TopupFundResponseModel>
                ) {
                    isLoading?.value = false
                    if (response.isSuccessful) {
                        Toast.makeText(activity, response.body()!!.message, Toast.LENGTH_SHORT)
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
    fun topupFundDirectTransfer(
            amount: String,
            securityPassword: String,
            type: String,
            bank_amount : String,
            authType : String
    ) {
        isLoading!!.value = true
        ApiClient.getClient(activity).topupWalletDirectTransfer(
                localization = Pref.getLocalization(activity),
                authorization = Pref.getprefAuthorizationToken(activity),
                amount = amount,
                securityPassword = securityPassword,
                type = type,
                bank_amount = bank_amount,
                authType = authType
        ).enqueue(object : Callback<TopupFundResponseModel> {
            override fun onFailure(call: Call<TopupFundResponseModel>, t: Throwable) {
                isLoading?.value = false
                Log.e("zxczxc", "onFailure - ${t.message}")
            }

            override fun onResponse(
                    call: Call<TopupFundResponseModel>,
                    response: Response<TopupFundResponseModel>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    Toast.makeText(activity, response.body()!!.message, Toast.LENGTH_SHORT)
                            .show()
                    topupFundResponseModel!!.value = response.body()

                } else {
                    responseError?.value = response.errorBody()
                }
            }

        })

    }

    fun varifyDownlineSponserCall(name: String) {
        isLoading?.value = true
        ApiClient.getClient(activity).verifyDownlineSponser(
            localization = Pref.getLocalization(activity),
            authorization = Pref.getprefAuthorizationToken(activity)
            , sponsor_name = name
        )
            .enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    isLoading?.value = false
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    isLoading?.value = false
                    if (response.isSuccessful) {
                        varifyResponseModel?.value = response
                    } else {
                        responseError?.value = response.errorBody()
                    }
                }
            })
    }

    /**following 2 are for downline upline transfer--------------------------------*/

    fun varifyDownlineSponserCall(
        amount: String,
        securityPassword: String,
        type: String,
        sponserName: String,
        isVerified: Boolean
    ) {
        isLoading?.value = true
        ApiClient.getClient(activity).verifyDownlineSponser(
            localization = Pref.getLocalization(activity),
            authorization = Pref.getprefAuthorizationToken(activity)
            , sponsor_name = sponserName
        )
            .enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    isLoading?.value = false
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {

                        // varifyResponseModel?.value = response
                        transferToDownlineUpline(
                            amount = amount,
                            securityPassword = securityPassword,
                            type = type,
                            sponserName = sponserName,
                            isVerified = true
                        )
                    } else {
                        responseError?.value = response.errorBody()
                        isLoading!!.value = false
                    }
                }
            })
    }

    fun varifyDownlineSponserCallWithUUID(
        amount: String,
        uuid: String,
        type: String,
        sponserName: String,
        isVerified: Boolean
    ) {
        isLoading?.value = true
        ApiClient.getClient(activity).verifyDownlineSponser(
            localization = Pref.getLocalization(activity),
            authorization = Pref.getprefAuthorizationToken(activity)
            , sponsor_name = sponserName
        )
            .enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    isLoading?.value = false
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {

                        // varifyResponseModel?.value = response
                        transferToDownlineUplineWithUUID(
                            amount = amount,
                            uuid = uuid,
                            type = type,
                            sponserName = sponserName,
                            isVerified = true
                        )
                    } else {
                        responseError?.value = response.errorBody()
                        isLoading!!.value = false
                    }
                }
            })
    }

    fun transferToDownlineUpline(
        amount: String,
        securityPassword: String,
        type: String,
        sponserName: String,
        isVerified: Boolean
    ) {
        isLoading!!.value = true

        if (!isVerified) {
            varifyDownlineSponserCall(
                amount = amount,
                securityPassword = securityPassword,
                type = type,
                sponserName = sponserName,
                isVerified = isVerified
            )
            return
        }

        ApiClient.getClient(activity).transferToDownlineUpline(
            authorization = Pref.getprefAuthorizationToken(activity),
            localization = Pref.getLocalization(activity),
            amount = amount,
            securityPassword = securityPassword,
            type = type,
            sponsorName = sponserName
        ).enqueue(object : Callback<TransferFundResponseModel> {
            override fun onFailure(call: Call<TransferFundResponseModel>, t: Throwable) {
                isLoading?.value = false
                Log.e("zxczxc", "onFailure - ${t.message}")
            }

            override fun onResponse(
                call: Call<TransferFundResponseModel>,
                response: Response<TransferFundResponseModel>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    Toast.makeText(activity, response.body()!!.message, Toast.LENGTH_SHORT).show()
                    transferFundResponseModel!!.value = response.body()

                } else {
                    responseError?.value = response.errorBody()
                }
            }

        })
    }

    fun transferToDownlineUplineWithUUID(
        amount: String,
        uuid: String,
        type: String,
        sponserName: String,
        isVerified: Boolean
    ) {
        isLoading!!.value = true

        if (!isVerified) {
            varifyDownlineSponserCallWithUUID(
                amount = amount,
                uuid= uuid,
                type = type,
                sponserName = sponserName,
                isVerified = isVerified
            )
            return
        }

        ApiClient.getClient(activity).transferToDownlineUplineWithUUID(
            authorization = Pref.getprefAuthorizationToken(activity),
            localization = Pref.getLocalization(activity),
            amount = amount,
            securityPassword = uuid,
            type = type,
            sponsorName = sponserName,
            authType = Constants.TransactionUUID
        ).enqueue(object : Callback<TransferFundResponseModel> {
            override fun onFailure(call: Call<TransferFundResponseModel>, t: Throwable) {
                isLoading?.value = false
                Log.e("zxczxc", "onFailure - ${t.message}")
            }

            override fun onResponse(
                call: Call<TransferFundResponseModel>,
                response: Response<TransferFundResponseModel>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    Toast.makeText(activity, response.body()!!.message, Toast.LENGTH_SHORT).show()
                    transferFundResponseModel!!.value = response.body()

                } else {
                    responseError?.value = response.errorBody()
                }
            }

        })
    }

    /**following 2 are for downline otm transfer--------------------------------*/

    fun varifyDownlineSponserCall(
        amount: String,
        securityPassword: String,
        sponserName: String,
        isVerified: Boolean
    ) {
        isLoading?.value = true
        ApiClient.getClient(activity).verifyDownlineSponser(
            localization = Pref.getLocalization(activity),
            authorization = Pref.getprefAuthorizationToken(activity)
            , sponsor_name = sponserName
        )
            .enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    isLoading?.value = false
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {

                        // varifyResponseModel?.value = response
                        transferToDownlineOTM(
                            amount = amount,
                            securityPassword = securityPassword,
                            sponserName = sponserName,
                            isVerified = true
                        )
                    } else {
                        responseError?.value = response.errorBody()
                        isLoading!!.value = false
                    }
                }
            })
    }

    fun transferToDownlineOTM(
        amount: String,
        securityPassword: String,
        sponserName: String,
        isVerified: Boolean
    ) {
        isLoading!!.value = true

        if (!isVerified) {
            varifyDownlineSponserCall(
                amount = amount,
                securityPassword = securityPassword,
                sponserName = sponserName,
                isVerified = isVerified
            )
            return
        }

        ApiClient.getClient(activity).transferToDownlineOTM(
            authorization = Pref.getprefAuthorizationToken(activity),
            localization = Pref.getLocalization(activity),
            amount = amount,
            securityPassword = securityPassword,
            sponsorName = sponserName
        ).enqueue(object : Callback<TransferFundResponseModel> {
            override fun onFailure(call: Call<TransferFundResponseModel>, t: Throwable) {
                isLoading?.value = false
                Log.e("zxczxc", "onFailure - ${t.message}")
            }

            override fun onResponse(
                call: Call<TransferFundResponseModel>,
                response: Response<TransferFundResponseModel>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    Toast.makeText(activity, response.body()!!.message, Toast.LENGTH_SHORT).show()
                    transferFundResponseModel!!.value = response.body()

                } else {
                    responseError?.value = response.errorBody()
                }
            }

        })
    }

    fun varifyDownlineSponserCallWithUUID(
        amount: String,
        uuid: String,
        sponserName: String,
        isVerified: Boolean
    ) {
        isLoading?.value = true
        ApiClient.getClient(activity).verifyDownlineSponser(
            localization = Pref.getLocalization(activity),
            authorization = Pref.getprefAuthorizationToken(activity)
            , sponsor_name = sponserName
        )
            .enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    isLoading?.value = false
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {

                        // varifyResponseModel?.value = response
                        transferToDownlineOTMWithUUID(
                            amount = amount,
                            uuid = uuid,
                            sponserName = sponserName,
                            isVerified = true
                        )
                    } else {
                        responseError?.value = response.errorBody()
                        isLoading!!.value = false
                    }
                }
            })
    }

    fun transferToDownlineOTMWithUUID(
        amount: String,
        uuid: String,
        sponserName: String,
        isVerified: Boolean
    ) {
        isLoading!!.value = true

        if (!isVerified) {
            varifyDownlineSponserCallWithUUID(
                amount = amount,
                uuid = uuid,
                sponserName = sponserName,
                isVerified = isVerified
            )
            return
        }

        ApiClient.getClient(activity).transferToDownlineOTMWithUUID(
            authorization = Pref.getprefAuthorizationToken(activity),
            localization = Pref.getLocalization(activity),
            amount = amount,
            authType = Constants.TransactionUUID,
            securityPassword = uuid,
            sponsorName = sponserName
        ).enqueue(object : Callback<TransferFundResponseModel> {
            override fun onFailure(call: Call<TransferFundResponseModel>, t: Throwable) {
                isLoading?.value = false
                Log.e("zxczxc", "onFailure - ${t.message}")
            }

            override fun onResponse(
                call: Call<TransferFundResponseModel>,
                response: Response<TransferFundResponseModel>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    Toast.makeText(activity, response.body()!!.message, Toast.LENGTH_SHORT).show()
                    transferFundResponseModel!!.value = response.body()

                } else {
                    responseError?.value = response.errorBody()
                }
            }

        })
    }
}
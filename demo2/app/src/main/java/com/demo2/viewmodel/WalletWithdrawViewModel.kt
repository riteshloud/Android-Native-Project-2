package com.demo2.viewmodel

import android.app.Activity
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.demo2.model.*
import com.demo2.utilities.Constants
import com.demo2.utilities.Pref
import com.demo2.view.service.ApiClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class WalletWithdrawViewModel(activity: Activity) : ViewModel() {
    var activity = activity
    var historyWithdrawalWalletModel: MutableLiveData<HistoryWithdrawalModel>? = MutableLiveData()
    var withdrawalRequestResponseModel: MutableLiveData<WithdrawalRequestResponseModel>? = MutableLiveData()

    var isLoading: MutableLiveData<Boolean>? = MutableLiveData()
    var responseError: MutableLiveData<ResponseBody>? = MutableLiveData()

    fun withdrawalRequest(
        amount: String,
        securityPassword: String, request_type: String,
        authType : String
    ) {
        isLoading!!.value = true
        ApiClient.getClient(activity).withdrawalRequest(
            authorization = Pref.getprefAuthorizationToken(activity),
            localization = Pref.getLocalization(activity),
            amount = amount,
            securityPassword = securityPassword,
            request_type = request_type,
            authType = authType
        ).enqueue(object : Callback<WithdrawalRequestResponseModel> {
            override fun onFailure(call: Call<WithdrawalRequestResponseModel>, t: Throwable) {
                isLoading?.value = false
            }

            override fun onResponse(
                call: Call<WithdrawalRequestResponseModel>,
                response: Response<WithdrawalRequestResponseModel>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    Toast.makeText(activity, response.body()!!.message, Toast.LENGTH_SHORT).show()
                    withdrawalRequestResponseModel!!.value = response.body()
                } else {
                    responseError?.value = response.errorBody()
                }
            }
        })
    }

    fun withdrawalRequestWithUSDT(
        amount: RequestBody,
        //bank_country: RequestBody,//commented as per new requirements as per bank removal
        securityPassword: RequestBody,
        request_type: RequestBody,
        usdt_address: RequestBody,
        usdt_trc_address: RequestBody,
        usdt_proof: MultipartBody.Part?,
        usdt_trc_proof: MultipartBody.Part?,
        usdc_erc_address: RequestBody,
        usdc_erc_proof: MultipartBody.Part?

        ) {
        isLoading!!.value = true
        ApiClient.getClient(activity).withdrawalRequestwithUsdt(
            authorization = Pref.getprefAuthorizationToken(activity),
            localization = Pref.getLocalization(activity),
            amount = amount,
            //bank_country = bank_country,//commented as per new requirements as per bank removal
            securityPassword = securityPassword,
            request_type = request_type,
            usdt_address = usdt_address,
            usdt_trc_address = usdt_trc_address,
            usdt_proof = usdt_proof,
            usdt_trc_proof = usdt_trc_proof,
            usdc_erc_address=usdc_erc_address,
            usdc_erc_proof=usdc_erc_proof
        ).enqueue(object : Callback<WithdrawalRequestResponseModel> {
            override fun onFailure(call: Call<WithdrawalRequestResponseModel>, t: Throwable) {
                isLoading?.value = false
            }

            override fun onResponse(
                call: Call<WithdrawalRequestResponseModel>,
                response: Response<WithdrawalRequestResponseModel>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    Toast.makeText(activity, response.body()!!.message, Toast.LENGTH_SHORT).show()
                    withdrawalRequestResponseModel!!.value = response.body()
                } else {
                    responseError?.value = response.errorBody()
                }
            }
        })
    }

    fun withdrawalRequestWithUSDTWithProof(
        amount: RequestBody,
        //bank_country: RequestBody,//commented as per new requirements as per bank removal
        securityPassword: RequestBody,
        request_type: RequestBody,
        usdt_proof: MultipartBody.Part?,
        usdt_trc_proof: MultipartBody.Part?,
        usdc_erc_proof: MultipartBody.Part?
    ) {
        isLoading!!.value = true
        ApiClient.getClient(activity).withdrawalRequestwithUsdtWithProof(
            authorization = Pref.getprefAuthorizationToken(activity),
            localization = Pref.getLocalization(activity),
            amount = amount,
            //bank_country = bank_country,//commented as per new requirements as per bank removal
            securityPassword = securityPassword,
            request_type = request_type,
            usdt_proof = usdt_proof,
            usdt_trc_proof = usdt_trc_proof,
            usdc_erc_proof=usdc_erc_proof
        ).enqueue(object : Callback<WithdrawalRequestResponseModel> {
            override fun onFailure(call: Call<WithdrawalRequestResponseModel>, t: Throwable) {
                isLoading?.value = false
            }

            override fun onResponse(
                call: Call<WithdrawalRequestResponseModel>,
                response: Response<WithdrawalRequestResponseModel>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    Toast.makeText(activity, response.body()!!.message, Toast.LENGTH_SHORT).show()
                    withdrawalRequestResponseModel!!.value = response.body()
                } else {
                    responseError?.value = response.errorBody()
                }
            }
        })
    }

    fun withdrawalRequestWithUSDTWithAddress(
        amount: String,
        securityPassword: String,
        request_type: String,
        usdt_address: String,
        usdt_trc_address: String,
        usdc_erc_address: String

    ) {
        isLoading!!.value = true
        ApiClient.getClient(activity).withdrawalRequestWithUsdtWithAddress(
            authorization = Pref.getprefAuthorizationToken(activity),
            localization = Pref.getLocalization(activity),
            amount = amount,
            securityPassword = securityPassword,
            request_type = request_type,
            usdt_address = usdt_address,
            usdt_trc_address = usdt_trc_address,
            usdc_erc_address=usdc_erc_address
        ).enqueue(object : Callback<WithdrawalRequestResponseModel> {
            override fun onFailure(call: Call<WithdrawalRequestResponseModel>, t: Throwable) {
                isLoading?.value = false
            }

            override fun onResponse(
                call: Call<WithdrawalRequestResponseModel>,
                response: Response<WithdrawalRequestResponseModel>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    Toast.makeText(activity, response.body()!!.message, Toast.LENGTH_SHORT).show()
                    withdrawalRequestResponseModel!!.value = response.body()
                } else {
                    responseError?.value = response.errorBody()
                }
            }
        })
    }

    fun withdrawalRequestWithUUID(
        amount: String,
        uuid: String, request_type: String
    ) {
        isLoading!!.value = true
        ApiClient.getClient(activity).withdrawalRequestWithUUID(
            authorization = Pref.getprefAuthorizationToken(activity),
            localization = Pref.getLocalization(activity),
            amount = amount,
            securityPassword = uuid,
            authType = Constants.TransactionUUID,
            request_type = request_type
        ).enqueue(object : Callback<WithdrawalRequestResponseModel> {
            override fun onFailure(call: Call<WithdrawalRequestResponseModel>, t: Throwable) {
                isLoading?.value = false
            }

            override fun onResponse(
                call: Call<WithdrawalRequestResponseModel>,
                response: Response<WithdrawalRequestResponseModel>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    Toast.makeText(activity, response.body()!!.message, Toast.LENGTH_SHORT).show()
                    withdrawalRequestResponseModel!!.value = response.body()
                } else {
                    responseError?.value = response.errorBody()
                }
            }
        })
    }

    fun withdrawalRequestWithUSDTWithUUID(
        amount: RequestBody,
        //bank_country: RequestBody,//commented as per new requirements as per bank removal
        uuid: RequestBody,
        request_type: RequestBody,
        usdt_address: RequestBody,
        usdt_trc_address: RequestBody,
        usdt_proof: MultipartBody.Part?,
        usdt_trc_proof: MultipartBody.Part?,
        usdc_erc_proof: MultipartBody.Part?,
        usdc_erc_address: RequestBody

    ) {
        isLoading!!.value = true
        ApiClient.getClient(activity).withdrawalRequestwithUsdtWithUUID(
            authorization = Pref.getprefAuthorizationToken(activity),
            localization = Pref.getLocalization(activity),
            amount = amount,

            authType = Constants.TransactionUUID.toRequestBody(
                "text/plain".toMediaTypeOrNull()
            ),
            //bank_country = bank_country,//commented as per new requirements as per bank removal
            securityPassword = uuid,
            request_type = request_type,
            usdt_address = usdt_address,
            usdt_trc_address = usdt_trc_address,
            usdt_proof = usdt_proof,
            usdt_trc_proof = usdt_trc_proof,
            usdc_erc_proof=usdc_erc_proof,
            usdc_erc_address=usdc_erc_address
        ).enqueue(object : Callback<WithdrawalRequestResponseModel> {
            override fun onFailure(call: Call<WithdrawalRequestResponseModel>, t: Throwable) {
                isLoading?.value = false
            }

            override fun onResponse(
                call: Call<WithdrawalRequestResponseModel>,
                response: Response<WithdrawalRequestResponseModel>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    Toast.makeText(activity, response.body()!!.message, Toast.LENGTH_SHORT).show()
                    withdrawalRequestResponseModel!!.value = response.body()
                } else {
                    responseError?.value = response.errorBody()
                }
            }
        })
    }

    fun withdrawalRequestWithUSDTWithProofWithUUID(
        amount: RequestBody,
        //bank_country: RequestBody,//commented as per new requirements as per bank removal
        uuid: RequestBody,
        request_type: RequestBody,
        usdt_proof: MultipartBody.Part?,
        usdt_trc_proof: MultipartBody.Part?,
        usdc_erc_proof: MultipartBody.Part?

    ) {
        isLoading!!.value = true
        ApiClient.getClient(activity).withdrawalRequestwithUsdtWithProofWithUUID(
            authorization = Pref.getprefAuthorizationToken(activity),
            localization = Pref.getLocalization(activity),
            amount = amount,
            //bank_country = bank_country,//commented as per new requirements as per bank removal
            authType = Constants.TransactionUUID.toRequestBody(
                "text/plain".toMediaTypeOrNull()
            ), securityPassword = uuid,
            request_type = request_type,
            usdt_proof = usdt_proof,
            usdt_trc_proof = usdt_trc_proof,
            usdc_erc_proof=usdc_erc_proof
        ).enqueue(object : Callback<WithdrawalRequestResponseModel> {
            override fun onFailure(call: Call<WithdrawalRequestResponseModel>, t: Throwable) {
                isLoading?.value = false
            }

            override fun onResponse(
                call: Call<WithdrawalRequestResponseModel>,
                response: Response<WithdrawalRequestResponseModel>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    Toast.makeText(activity, response.body()!!.message, Toast.LENGTH_SHORT).show()
                    withdrawalRequestResponseModel!!.value = response.body()
                } else {
                    responseError?.value = response.errorBody()
                }
            }
        })
    }

    fun withdrawalRequestWithUSDTWithAddressWithUUID(
        amount: String,
        uuid: String,
        request_type: String,
        usdt_address: String,
        usdt_trc_address: String,
        usdc_erc_address: String

    ) {
        isLoading!!.value = true
        ApiClient.getClient(activity).withdrawalRequestWithUsdtWithAddressWithUUID(
            authorization = Pref.getprefAuthorizationToken(activity),

            authType = Constants.TransactionUUID, localization = Pref.getLocalization(activity),
            amount = amount,
            securityPassword = uuid,
            request_type = request_type,
            usdt_address = usdt_address,
            usdt_trc_address = usdt_trc_address,
            usdc_erc_address=usdc_erc_address
        ).enqueue(object : Callback<WithdrawalRequestResponseModel> {
            override fun onFailure(call: Call<WithdrawalRequestResponseModel>, t: Throwable) {
                isLoading?.value = false
            }

            override fun onResponse(
                call: Call<WithdrawalRequestResponseModel>,
                response: Response<WithdrawalRequestResponseModel>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    Toast.makeText(activity, response.body()!!.message, Toast.LENGTH_SHORT).show()
                    withdrawalRequestResponseModel!!.value = response.body()
                } else {
                    responseError?.value = response.errorBody()
                }
            }
        })
    }

    fun getWithdrawalWalletHistory(offset: Int) {
        isLoading!!.value = true
        ApiClient.getClient(activity).getWithdrawWalletHistory(
            authorization = Pref.getprefAuthorizationToken(activity),
            localization = Pref.getLocalization(activity),
            offset = offset,
            limit = Constants.paginationLimit
        ).enqueue(object : Callback<HistoryWithdrawalModel> {
            override fun onFailure(
                call: Call<HistoryWithdrawalModel>,
                t: Throwable
            ) {
                isLoading?.value = false
            }

            override fun onResponse(
                call: Call<HistoryWithdrawalModel>,
                response: Response<HistoryWithdrawalModel>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    if (historyWithdrawalWalletModel?.value == null) {
                        historyWithdrawalWalletModel?.value = response.body().apply {
                            this!!.paginationEnded =
                                response.body()!!.payload!!.history!!.size < Constants.paginationLimit
                        }
                    } else {
                        historyWithdrawalWalletModel?.value =
                            historyWithdrawalWalletModel!!.value.apply {
                                this!!.paginationEnded =
                                    response.body()!!.payload!!.history!!.size < Constants.paginationLimit
                                this.payload!!.history!!.addAll(response.body()!!.payload!!.history!!)
                            }
                    }
                } else {
                    responseError?.value = response.errorBody()
                }
            }
        })
    }

    fun reSentVerification(ref_id: Int) {
        isLoading!!.value = true
        ApiClient.getClient(activity).reSendVerification(
            localization = Pref.getLocalization(activity),
            authorization = Pref.getprefAuthorizationToken(activity),
            ref_id = ref_id
        ).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(
                call: Call<ResponseBody>,
                t: Throwable
            ) {
                isLoading?.value = false
            }

            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {

                    var s = response.body()!!.string()

                    val jsonObject = JSONObject(s)
                    val subjson = jsonObject.getString("message")

                    Toast.makeText(activity, subjson, Toast.LENGTH_SHORT).show()
                } else {
                    responseError?.value = response.errorBody()
                }
            }
        })
    }
}
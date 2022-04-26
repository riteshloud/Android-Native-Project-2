package com.demo2.viewmodel

import android.app.Activity
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.demo2.model.CommonDataModel
import com.demo2.model.UserModel
import com.demo2.utilities.Constants
import com.demo2.utilities.Pref

import com.demo2.view.service.ApiClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel(activity: Activity): ViewModel() {

    var userModel: MutableLiveData<UserModel>? = MutableLiveData()
    var isLoading: MutableLiveData<Boolean>? = MutableLiveData()
    var responseError: MutableLiveData<ResponseBody>? = MutableLiveData()
    var context = activity

    fun loginCall(
        currentLocalization: String,
        username: String,
        password: String,
        device_type: String,
        device_token: String
    ) {
        isLoading?.value = true
        ApiClient.getClient(context).login(
            localization = currentLocalization,
            username = username,
            password = password,
            device_type = device_type,
            device_token = device_token
        ).enqueue(object : Callback<UserModel> {
            override fun onFailure(call: Call<UserModel>, t: Throwable) {
                isLoading?.value = false

            }

            override fun onResponse(
                call: Call<UserModel>,
                response: Response<UserModel>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    this@LoginViewModel.userModel?.value = response.body()
                } else {
                    responseError?.value = response.errorBody()
                }
            }
        })
    }

    fun loginCallWithUUID(
        currentLocalization: String,
        username: String,
        password: String,
        device_type: String,
        device_token: String,
        finger_uuid : String
    ) {
        isLoading?.value = true
        ApiClient.getClient(context).login(
            localization = currentLocalization,
            username = username,
            password = password,
            device_type = device_type,
            device_token = device_token,
            finger_uuid = finger_uuid
        ).enqueue(object : Callback<UserModel> {
            override fun onFailure(call: Call<UserModel>, t: Throwable) {
                isLoading?.value = false

            }

            override fun onResponse(
                call: Call<UserModel>,
                response: Response<UserModel>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    this@LoginViewModel.userModel?.value = response.body()
                } else {
                    responseError?.value = response.errorBody()
                }
            }
        })
    }

    fun loadCommonData() {
        ApiClient.getClient(context!!).getCommonData(Pref.getLocalization(context))
            .enqueue(object : Callback<CommonDataModel> {
                override fun onFailure(call: Call<CommonDataModel>, t: Throwable) {
                    isLoading?.value = false
                    Log.e("zxczxc", "${t.message}")
                }

                override fun onResponse(
                    call: Call<CommonDataModel>,
                    response: Response<CommonDataModel>
                ) {
                    isLoading?.value = false
                    if (response.isSuccessful) {
                        response.body()!!.payload!!.agreementContent?.let { Log.e("Response", it) }
                        Pref.setValue(context, Constants.prefCommonData!!, Gson().toJson(response.body()))
                    } else {
                        responseError?.value = response.errorBody()
                    }
                }
            })
    }

}

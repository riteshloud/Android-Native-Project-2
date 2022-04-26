package com.demo2.viewmodel

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.demo2.model.CommonDataModel
import com.demo2.utilities.Constants
import com.demo2.utilities.Pref
import com.demo2.view.service.ApiClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CommonDataViewModel(activity: Activity) : ViewModel() {

    var commonDataModel: MutableLiveData<CommonDataModel>? = null
    var isLoading: MutableLiveData<Boolean>? = MutableLiveData()
    var responseError: MutableLiveData<ResponseBody>? = MutableLiveData()
    var context = activity
    val getCommonDataModel: LiveData<CommonDataModel>
        get() {
            if (commonDataModel == null) {
                commonDataModel = MutableLiveData()
                loadCommonData()
            }
            return commonDataModel!!
        }

    fun loadCommonData() {
        isLoading?.value = true
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
                        commonDataModel?.value = response.body()
                       // Pref.setValue(context, Constants.prefCommonData!!, Gson().toJson(commonDataModel))
                    } else {
                        responseError?.value = response.errorBody()
                    }
                }
            })
    }


}

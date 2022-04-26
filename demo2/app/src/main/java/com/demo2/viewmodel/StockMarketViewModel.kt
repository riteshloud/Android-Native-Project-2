package com.demo2.viewmodel

import android.app.Activity
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.demo2.model.*
import com.demo2.utilities.Constants
import com.demo2.utilities.Pref
import com.demo2.view.service.ApiClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StockMarketViewModel(activity: Activity) : ViewModel() {

//    var varifySponserResponse: MutableLiveData<Response<ResponseBody>>? = MutableLiveData()
    var stockMarketList: MutableLiveData<StockMarketListModel>? = MutableLiveData()
//    var stockDetail: MutableLiveData<StockDetaiModel>? = MutableLiveData()
    var stockStates: MutableLiveData<StockStatesModel>? = MutableLiveData()
    var stockPortfolio: MutableLiveData<StockPortfolioModel>? = MutableLiveData()
    var stockChart: MutableLiveData<StockChartModel>? = MutableLiveData()
    var investHistory: MutableLiveData<StockInvestHistoryModel>? = MutableLiveData()

    var investAmount: MutableLiveData<InvestAmountModel>? = MutableLiveData()
    var closeInvestment: MutableLiveData<CloseInvestmentModel>? = MutableLiveData()

    var isLoading: MutableLiveData<Boolean>? = MutableLiveData()
    var responseError: MutableLiveData<ResponseBody>? = MutableLiveData()
    var isStateCallActive  = false // bcoz state and history API calling together

    // var responseError1: MutableLiveData<ResponseBody>? = MutableLiveData()
    var context = activity

    fun getStockMarketList(
        offset: Int,
        keyword: String
    ) {
        isLoading?.value = true
        ApiClient.getClient(context).getStockMarketList(
            localization = Pref.getLocalization(context),
            authorization = Pref.getprefAuthorizationToken(context),
            offset = offset,
            limit = Constants.paginationLimit,
            keyword = keyword
        ).enqueue(object : Callback<StockMarketListModel> {
            override fun onFailure(call: Call<StockMarketListModel>, t: Throwable) {
                isLoading?.value = false

            }

            override fun onResponse(
                call: Call<StockMarketListModel>,
                response: Response<StockMarketListModel>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    if (offset == 0) {
                        stockMarketList?.value = response.body().apply {
                            this!!.paginationEnded =
                                response.body()!!.payload!!.stockCategories!!.size < Constants.paginationLimit
                        }
                    } else {
                        stockMarketList?.value = stockMarketList!!.value.apply {
                            this!!.payload!!.stockCategories =
                                response!!.body()!!.payload!!.stockCategories
                            this!!.payload!!.stockCategories!!.addAll(response.body()!!.payload!!.stockCategories!!)
                            this!!.paginationEnded =
                                response.body()!!.payload!!.stockCategories!!.size < Constants.paginationLimit
                        }
                    }
                } else {
                    responseError?.value = response.errorBody()
                }
            }
        })
    }

    /*fun getStockDetail(
        offset: Int,
        stock_cat_id: String,
        fromPortfolio: Boolean
    ) {
        isLoading?.value = true
        ApiClient.getClient(context).getStockDetail(
            localization = Pref.getLocalization(context),
            authorization = Pref.getprefAuthorizationToken(context),
            offset = offset,
            limit = Constants.paginationLimit,
            stock_category_id = stock_cat_id
        ).enqueue(object : Callback<StockDetaiModel> {
            override fun onFailure(call: Call<StockDetaiModel>, t: Throwable) {
                isLoading?.value = false
                Log.v("=====EXCE", "-" + t.message)
            }

            override fun onResponse(
                call: Call<StockDetaiModel>,
                response: Response<StockDetaiModel>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    if (offset == 0) {
                        stockDetail?.value = response.body().apply {

                            if (fromPortfolio) {
                                this!!.paginationEnded =
                                    response.body()!!.payload!!.portfolio!!.size < Constants.paginationLimit
                            } else {
                                this!!.paginationEnded =
                                    response.body()!!.payload!!.stats!!.investmentHistory!!.size < Constants.paginationLimit
                            }
                        }
                    } else {
                        stockDetail?.value = stockDetail!!.value.apply {

                            if (fromPortfolio) {
                                this!!.payload!!.portfolio =
                                    response!!.body()!!.payload!!.portfolio
                                this!!.payload!!.portfolio!!.addAll(response.body()!!.payload!!.portfolio!!)
                                this!!.paginationEnded =
                                    response.body()!!.payload!!.portfolio!!.size < Constants.paginationLimit
                            } else {
                                this!!.payload!!.stats!!.investmentHistory =
                                    response!!.body()!!.payload!!.stats!!.investmentHistory
                                this!!.payload!!.stats!!.investmentHistory!!.addAll(response.body()!!.payload!!.stats!!.investmentHistory!!)
                                this!!.paginationEnded =
                                    response.body()!!.payload!!.stats!!.investmentHistory!!.size < Constants.paginationLimit
                            }

                        }
                    }
                } else {
                    responseError?.value = response.errorBody()
                }
            }
        })
    }*/

    fun investAmount(
        stock_cat_id: String,
        amount: String,
        plan: String
    ) {
        isLoading?.value = true
        ApiClient.getClient(context).investAmount(
            localization = Pref.getLocalization(context),
            authorization = Pref.getprefAuthorizationToken(context),
            amount = amount,
            stock_category_id = stock_cat_id,
            plan = plan
        ).enqueue(object : Callback<InvestAmountModel> {
            override fun onFailure(call: Call<InvestAmountModel>, t: Throwable) {
                isLoading?.value = false
                Log.v("=====EXCE", "-" + t.message)
            }

            override fun onResponse(
                call: Call<InvestAmountModel>,
                response: Response<InvestAmountModel>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    investAmount!!.value=response.body()
                }else{
                    responseError?.value = response.errorBody()

                }
            }
        })
    }
    fun closeInvestment(

        investment_id: String

    ) {
        isLoading?.value = true
        ApiClient.getClient(context).closeInvestment(
            localization = Pref.getLocalization(context),
            authorization = Pref.getprefAuthorizationToken(context),

            investment_id = investment_id
        ).enqueue(object : Callback<CloseInvestmentModel> {
            override fun onFailure(call: Call<CloseInvestmentModel>, t: Throwable) {
                isLoading?.value = false
                Log.v("=====EXCE", "-" + t.message)
            }

            override fun onResponse(
                call: Call<CloseInvestmentModel>,
                response: Response<CloseInvestmentModel>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    closeInvestment!!.value=response.body()
                }else{
                    responseError?.value = response.errorBody()

                }
            }
        })
    }



    fun getStockStates(

        stock_cat_id: String
    ) {
        isStateCallActive = true
        isLoading?.value = true
        ApiClient.getClient(context).getStockStates(
            localization = Pref.getLocalization(context),
            authorization = Pref.getprefAuthorizationToken(context),
            stock_category_id = stock_cat_id
        ).enqueue(object : Callback<StockStatesModel> {
            override fun onFailure(call: Call<StockStatesModel>, t: Throwable) {
                isLoading?.value = false
                isStateCallActive = false
                Log.v("=====EXCE", "-" + t.message)
            }

            override fun onResponse(
                call: Call<StockStatesModel>,
                response: Response<StockStatesModel>
            ) {
                isLoading?.value = false
                isStateCallActive = false
                if (response.isSuccessful) {
                    stockStates?.value = response.body()
                } else {
                    responseError?.value = response.errorBody()
                }
            }
        })
    }


    fun getStockChart(

        stock_cat_id: String
    ) {
        isLoading?.value = true
        ApiClient.getClient(context).getStockChart(
            localization = Pref.getLocalization(context),
            authorization = Pref.getprefAuthorizationToken(context),
            stock_category_id = stock_cat_id
        ).enqueue(object : Callback<StockChartModel> {
            override fun onFailure(call: Call<StockChartModel>, t: Throwable) {
                isLoading?.value = false
                Log.v("=====EXCE", "-" + t.message)
            }

            override fun onResponse(
                call: Call<StockChartModel>,
                response: Response<StockChartModel>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    stockChart?.value = response.body()
                } else {
                    responseError?.value = response.errorBody()
                }
            }
        })
    }


    fun getStockPortfolio(
        offset: Int,
        stock_cat_id: String
    ) {
        isLoading?.value = true
        ApiClient.getClient(context).getStockPortfolio(
            localization = Pref.getLocalization(context),
            authorization = Pref.getprefAuthorizationToken(context),
            offset = offset,
            limit = Constants.paginationLimit,
            stock_category_id = stock_cat_id
        ).enqueue(object : Callback<StockPortfolioModel> {
            override fun onFailure(call: Call<StockPortfolioModel>, t: Throwable) {
                isLoading?.value = false

            }

            override fun onResponse(
                call: Call<StockPortfolioModel>,
                response: Response<StockPortfolioModel>
            ) {
                isLoading?.value = false
                if (response.isSuccessful) {
                    if (offset == 0) {
                        stockPortfolio?.value = response.body().apply {
                            this!!.paginationEnded =
                                response.body()!!.payload!!.portfolio!!.size < Constants.paginationLimit
                        }
                    } else {
                        stockPortfolio?.value = stockPortfolio!!.value.apply {
                            this!!.payload!!.portfolio =
                                response!!.body()!!.payload!!.portfolio
                            this!!.payload!!.portfolio!!.addAll(response.body()!!.payload!!.portfolio!!)
                            this!!.paginationEnded =
                                response.body()!!.payload!!.portfolio!!.size < Constants.paginationLimit
                        }
                    }
                } else {
                    responseError?.value = response.errorBody()
                }
            }
        })
    }
    fun getInvestHistory(
        offset: Int,
        stock_cat_id: String
    ) {
        isLoading?.value = true
        ApiClient.getClient(context).getInvestHistory(
            localization = Pref.getLocalization(context),
            authorization = Pref.getprefAuthorizationToken(context),
            offset = offset,
            limit = Constants.paginationLimit,
            stock_category_id = stock_cat_id
        ).enqueue(object : Callback<StockInvestHistoryModel> {
            override fun onFailure(call: Call<StockInvestHistoryModel>, t: Throwable) {
                if (isStateCallActive){
//                    isLoading?.value = false
                }else{
                    isLoading?.value = false
                }


            }

            override fun onResponse(
                call: Call<StockInvestHistoryModel>,
                response: Response<StockInvestHistoryModel>
            ) {
                if (isStateCallActive){
//                    isLoading?.value = false
                }else{
                    isLoading?.value = false
                }

                if (response.isSuccessful) {
                    if (offset == 0) {
                        investHistory?.value = response.body().apply {
                            this!!.paginationEnded =
                                response.body()!!.payload!!.investmentHistory!!.size < Constants.paginationLimit
                        }
                    } else {
                        investHistory?.value = investHistory!!.value.apply {
                            this!!.payload!!.investmentHistory =
                                response!!.body()!!.payload!!.investmentHistory
                            this!!.payload!!.investmentHistory!!.addAll(response.body()!!.payload!!.investmentHistory!!)
                            this!!.paginationEnded =
                                response.body()!!.payload!!.investmentHistory!!.size < Constants.paginationLimit
                        }
                    }
                } else {
                    responseError?.value = response.errorBody()
                }
            }
        })
    }
}
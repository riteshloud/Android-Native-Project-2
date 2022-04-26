package com.demo2.utilities

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.util.Log
import com.google.gson.Gson
import java.util.*


open class Pref {

    companion object {

        private val TAG: String? = "Pref"
        private var sharedPreferences: SharedPreferences? = null

        fun openPref(context: Context) {
            sharedPreferences = context.getSharedPreferences(Constants.pref, Context.MODE_PRIVATE)

        }

        fun getValue(
            context: Context, key: String,
            defaultValue: String?
        ): String? {
            try {
                openPref(context)
                val result = sharedPreferences!!.getString(key, defaultValue)
                return result
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return defaultValue
        }

        fun setValue(context: Context, key: String, value: Int) {
            openPref(context)
            var prefsPrivateEditor: Editor? = sharedPreferences!!.edit()
            prefsPrivateEditor!!.putInt(key, value)
            prefsPrivateEditor.commit()


        }

        fun setValue(context: Context, key: String, value: Long) {

            openPref(context)
            var prefsPrivateEditor: Editor? = sharedPreferences!!.edit()
            prefsPrivateEditor!!.putLong(key, value)
            prefsPrivateEditor.commit()
            sharedPreferences = null


        }

        fun getValue(
            context: Context, key: String,
            defaultValue: Int
        ): Int {

            openPref(context)
            val result = sharedPreferences!!.getInt(key, defaultValue)
            sharedPreferences = null
            return result


            return defaultValue
        }

        fun getValue(
            context: Context, key: String,
            defaultValue: Long
        ): Long {

            openPref(context)
            val result = sharedPreferences!!.getLong(key, defaultValue)
            sharedPreferences = null
            return result


            return defaultValue
        }

        fun setValue(context: Context, key: String, value: String) {

            openPref(context)
            var prefsPrivateEditor: Editor? = sharedPreferences!!.edit()
            prefsPrivateEditor!!.putString(key, value)
            prefsPrivateEditor.commit()
            sharedPreferences = null

        }

        fun getValue(
            context: Context, key: String,
            defaultValue: Boolean
        ): Boolean {

            openPref(context)
            val result = sharedPreferences!!.getBoolean(key, defaultValue)
            sharedPreferences = null
            return result

            return defaultValue
        }

        fun setValue(context: Context, key: String, value: Boolean) {

            openPref(context)
            val prefsPrivateEditor = sharedPreferences!!.edit()
            prefsPrivateEditor.putBoolean(key, value)
            prefsPrivateEditor.commit()
            sharedPreferences = null


        }

        fun deleteAll(context: Context) {
            openPref(context)
            val gson = Gson()
            var CommonDataModel =
                gson.fromJson(
                    getValue(
                        context,
                        Constants.prefCommonData,
                        ""
                    ), CommonDataModel::class.java
                )
            var isRemember =
                getValue(context, Constants.prefIsRemember, false)
            var email = getValue(context, Constants.prefLoginUsername, "")
            var password =
                getValue(context, Constants.prefLoginPassword, "")
            var isFingerPrintSetInThisDevice =
                getUserModel(context)!!.payload!!.user!!.fingerPrintSetInThisDevice
            var emailFinger = getValue(context, Constants.prefFingerUsername, "")
            var passwordFinger =
                getValue(context, Constants.prefFingerPassword, "")
            var uuidFinger =
                if (getUserModel(context)!!.payload!!.user!!.fingerPrintSetInThisDevice) getUserModel(
                    context
                )!!.payload!!.user!!.fingerUUID.toString().trim() else ""
            Log.e("TestClerea", "***   " + uuidFinger)
            var lang = Pref.getLocalization(context)
            sharedPreferences!!.edit().clear().commit()
            if (isRemember) {
                setValue(context, Constants.prefIsRemember, isRemember)
                setValue(context, Constants.prefLoginUsername, email!!)
                setValue(context, Constants.prefLoginPassword, password!!)
            }
            setValue(context, Constants.prefFingerUsername, emailFinger!!)
            setValue(context, Constants.prefFingerPassword, passwordFinger!!)
            setValue(context, Constants.prefFingerUUID, uuidFinger!!)
            setValue(
                context,
                Constants.prefFingerPrintSetInThisDevice,
                isFingerPrintSetInThisDevice
            )

            setValue(
                context,
                Constants.prefCommonData,
                gson.toJson(CommonDataModel)
            )
            setValue(context, Constants.Localization, lang)
        }


        fun getCommonDataModel(context: Context): CommonDataModel? {
            val gson = Gson()
            getValue(context, Constants.prefCommonData, null).let {
                return gson.fromJson(
                    it,
                    CommonDataModel::class.java
                )
            }
            return null


        }

        fun getUserModel(context: Context): UserModel? {
            val gson = Gson()
            getValue(context, Constants.prefUserData, null).let {
                return gson.fromJson(
                    it,
                    UserModel::class.java
                )
            }
            return null

        }

        fun getprefAuthorizationToken(context: Context): String {
            return getValue(context, Constants.prefAuthorizationToken, "").toString().apply {
                Log.d(TAG, "token - $this")
            }
        }

        fun getLocalization(context: Context): String {
            Log.e("zxczxc", " getting locale "+getValue(context, Constants.Localization, "en").toString())

            return getValue(context, Constants.Localization, "en").toString()
        }

        fun setLocale(mContext: Context, lang: String) {
            var langwage=""
            Log.e("zxczxc", " setting locale $lang")
            //header : X-localization:en // for english language, chi // for chinese language, my// for malay language if not pass set the default to english
            val config = Configuration()
            setValue(mContext, Constants.Localization, lang)


            if (lang.equals("en", ignoreCase = true)) {
                langwage = "en"
                val locale = Locale(langwage)
                Locale.setDefault(locale)
                config.setLocale(locale)

            } else if (lang.equals("cn", ignoreCase = true)) {
                langwage = "zh"
                val locale = Locale(langwage)
                Locale.setDefault(locale)
                config.setLocale(locale)

            } else if (lang.equals("cn_tr")) {
                langwage = Locale.TRADITIONAL_CHINESE.toString()
               // val locale = Locale(Locale.TRADITIONAL_CHINESE)
                Locale.setDefault(Locale.TRADITIONAL_CHINESE)
                config.setLocale(Locale.TRADITIONAL_CHINESE)

            } else if (lang.equals("ko", ignoreCase = true)) {
                langwage = "ko"
                val locale = Locale(langwage)
                Locale.setDefault(locale)
                config.setLocale(locale)

            } else if (lang.equals("vi", ignoreCase = true)) {
                langwage = "vi"
                val locale = Locale(langwage)
                Locale.setDefault(locale)
                config.setLocale(locale)

            } else if (lang.equals("th", ignoreCase = true)) {
                langwage = "th"
                val locale = Locale(langwage)
                Locale.setDefault(locale)
                config.setLocale(locale)

            }
            Log.e("zxczxc", " setting locale $langwage")

      /*      val locale = Locale(langwage)
            Locale.setDefault(locale)
            config.setLocale(locale)


 if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N){
                mContext.createConfigurationContext(config);
            } else {
                mContext.resources.updateConfiguration(config,  mContext.resources.displayMetrics);
            }
          */

            mContext.resources.updateConfiguration(
                config,
                mContext.resources.displayMetrics
            )


        }

        fun updateResources(
            context: Context,
            language: String
        ): Context? {

            var lang = language
            Log.e("zxczxc", " setting locale $lang")
            //header : X-localization:en // for english language, chi // for chinese language, my// for malay language if not pass set the default to english
            setValue(context, Constants.Localization, lang)

            if (lang.equals("my", ignoreCase = true)) {
                lang = "ms"
            }
            if (lang.equals("cn", ignoreCase = true)) {
                lang = "zh"
            }

            var context = context
            val locale = Locale(lang)
            Locale.setDefault(locale)
            val res: Resources = context.resources
            val config =
                Configuration(res.configuration)
            config.setLocale(locale)
            context = context.createConfigurationContext(config)
            context.resources.updateConfiguration(
                config,
                context.resources.displayMetrics
            )
            return context
        }

    }


}

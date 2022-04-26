package com.demo2.utilities

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.biometric.BiometricManager

class BiometricUtils {
    companion object {
        fun isHardwareAvailable(context: Context): Boolean {
            Log.e("BiometricUtils", "Checking BiometricUtils.isHardwareAvailable")

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val bm = BiometricManager.from(context)
                val canAuthenticate = bm.canAuthenticate()
                !(canAuthenticate == BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE || canAuthenticate == BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE)
            } else {
                false
            }
        }

        fun hasBiometricEnrolled(context: Context): Boolean {
            Log.e("BiometricUtils", "Checking BiometricUtils.hasBiometricEnrolled")

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val bm = BiometricManager.from(context)
                val canAuthenticate = bm.canAuthenticate()
                (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS)
            } else {
                false
            }
        }

        fun checkBiometricPossible(context: Context): Boolean {
            return (isHardwareAvailable(context) && hasBiometricEnrolled(context))
        }
    }
}
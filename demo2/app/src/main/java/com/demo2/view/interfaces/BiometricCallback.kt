package com.demo2.view.interfaces

import androidx.biometric.BiometricPrompt

interface BiometricCallback {

    fun onSuccess(result: BiometricPrompt.AuthenticationResult)
    fun onFail()
    fun onError(
        errorCode: Int,
        errString: CharSequence
    )
}
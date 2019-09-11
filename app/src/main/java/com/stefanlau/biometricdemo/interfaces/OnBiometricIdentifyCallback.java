package com.stefanlau.biometricdemo.interfaces;

import android.hardware.biometrics.BiometricPrompt;
import android.hardware.fingerprint.FingerprintManager;

/**
 * 指纹验证回调接口
 */
public interface OnBiometricIdentifyCallback {

    /**
     * 5次输错，使用密码
     */
    void onUsePassword();

    /**
     * 验证成功 高于api23
     *
     * @param result
     */
    void onSucceeded(FingerprintManager.AuthenticationResult result);

    /**
     * 验证成功 高于api28
     *
     * @param result
     */
    void onSucceeded(BiometricPrompt.AuthenticationResult result);

    /**
     * 失败
     */
    void onFailed();

    /**
     * 验证错误
     *
     * @param code
     * @param reason
     */
    void onError(int code, String reason);

    /**
     * 取消验证
     */
    void onCancel();

}
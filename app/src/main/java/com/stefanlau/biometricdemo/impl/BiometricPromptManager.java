package com.stefanlau.biometricdemo.impl;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.hardware.biometrics.BiometricPrompt;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;

import com.stefanlau.biometricdemo.interfaces.IBiometricPromptImpl;
import com.stefanlau.biometricdemo.interfaces.OnBiometricIdentifyCallback;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

/**
 * 生物识别管理
 *
 * Created by Stefan Lau on 2018/12/11.
 */
public class BiometricPromptManager {

    private IBiometricPromptImpl mImpl;
    private Activity mActivity;

    public static BiometricPromptManager from(Activity activity) {
        return new BiometricPromptManager(activity);
    }

    public BiometricPromptManager(Activity activity) {
        mActivity = activity;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) { //api高于28
            mImpl = new BiometricPromptApi28(activity);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //api高于23
            mImpl = new BiometricPromptApi23(activity);
        }
    }

    public void authenticate(boolean loginFlg, @NonNull OnBiometricIdentifyCallback callback) {
        mImpl.authenticate(loginFlg, new CancellationSignal(), callback);
    }

    /**
     * 判断是否录入了指纹
     *
     * @return true if at least one fingerprint is enrolled, false otherwise
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean hasEnrolledFingerprints() {
        FingerprintManager manager = mActivity.getSystemService(FingerprintManager.class);
        return manager != null && manager.hasEnrolledFingerprints();
    }

    /**
     * 判断手机硬件是否支持指纹
     *
     * @return true if hardware is present and functional, false otherwise.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean isHardwareDetected() {
        FingerprintManager fm = mActivity.getSystemService(FingerprintManager.class);
        return fm != null && fm.isHardwareDetected();
    }

    /**
     * 判断有没有锁屏密码(九宫格图案 / PIN码 / 数字+字母密码)
     *
     * @return
     */
    public boolean isKeyguardSecure() {
        KeyguardManager keyguardManager = (KeyguardManager) mActivity.getSystemService(Context.KEYGUARD_SERVICE);
        return keyguardManager != null && keyguardManager.isKeyguardSecure();
    }

    /**
     * 判断设置是否支持生物识别
     *
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean isBiometricPromptEnable() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && isHardwareDetected()
                && hasEnrolledFingerprints()
                && isKeyguardSecure();
    }

}

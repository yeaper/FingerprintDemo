package com.stefanlau.biometricdemo.interfaces;

import android.os.CancellationSignal;

import com.stefanlau.biometricdemo.impl.BiometricPromptManager;

import androidx.annotation.NonNull;

/**
 * 指纹验证接口
 * Created by Stefan Lau on 2018/12/11.
 */
public interface IBiometricPromptImpl {

    void authenticate(boolean loginFlg, @NonNull CancellationSignal cancel, @NonNull OnBiometricIdentifyCallback callback);
}

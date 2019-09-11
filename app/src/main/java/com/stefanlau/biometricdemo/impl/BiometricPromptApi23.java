package com.stefanlau.biometricdemo.impl;

import android.app.Activity;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.util.Base64;
import android.util.Log;

import com.stefanlau.biometricdemo.interfaces.OnBiometricIdentifyCallback;
import com.stefanlau.biometricdemo.utils.ACache;
import com.stefanlau.biometricdemo.base.App;
import com.stefanlau.biometricdemo.utils.KeyGenTool;
import com.stefanlau.biometricdemo.interfaces.IBiometricPromptImpl;
import com.stefanlau.biometricdemo.widget.BiometricPromptDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * Created by Stefan Lau on 2018/12/11.
 */
@RequiresApi(Build.VERSION_CODES.M)
public class BiometricPromptApi23 implements IBiometricPromptImpl {

    private static final String TAG = "BiometricPromptApi23";
    private Activity mActivity;
    private BiometricPromptDialog mDialog;
    private FingerprintManager mFingerprintManager;
    private CancellationSignal mCancellationSignal;
    private OnBiometricIdentifyCallback mManagerIdentifyCallback;
    private FingerprintManager.AuthenticationCallback mFmAuthCallback = new FingerprintManageCallbackImpl();

    private ACache aCache;

    public BiometricPromptApi23(Activity activity) {
        mActivity = activity;
        aCache = ACache.get(App.getContext());
        mFingerprintManager = getFingerprintManager(activity);
    }

    @Override
    public void authenticate(boolean loginFlg, @Nullable CancellationSignal cancel, @NonNull OnBiometricIdentifyCallback callback) {
        //指纹识别的回调
        mManagerIdentifyCallback = callback;

        mDialog = BiometricPromptDialog.newInstance();
        mDialog.setOnBiometricPromptDialogActionCallback(new BiometricPromptDialog.OnBiometricPromptDialogActionCallback() {
            @Override
            public void onDialogDismiss() {
                //当dialog消失的时候，包括点击userPassword、点击cancel、和识别成功之后
                if (mCancellationSignal != null && !mCancellationSignal.isCanceled()) {
                    mCancellationSignal.cancel();
                }
            }

            @Override
            public void onCancel() {
                //点击cancel键
                if (mManagerIdentifyCallback != null) {
                    mManagerIdentifyCallback.onCancel();
                }
            }
        });
        mDialog.show(mActivity.getFragmentManager(), "BiometricPromptApi23");

        mCancellationSignal = cancel;
        if (mCancellationSignal == null) {
            mCancellationSignal = new CancellationSignal();
        }
        mCancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() { //取消指纹验证的监听
                mDialog.dismiss();
            }
        });

        KeyGenTool mKeyGenTool = new KeyGenTool(mActivity);
        FingerprintManager.CryptoObject object;
        if (loginFlg){
            //解密
            try {
                /**
                 * 可通过服务器保存iv,然后在使用之前从服务器获取
                 */
                String ivStr = aCache.getAsString("iv");
                byte[] iv = Base64.decode(ivStr, Base64.URL_SAFE);

                object = new FingerprintManager.CryptoObject(mKeyGenTool.getDecryptCipher(iv));
                mFingerprintManager.authenticate(object, mCancellationSignal,
                        0, mFmAuthCallback, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            //加密
            try {
                object = new FingerprintManager.CryptoObject(mKeyGenTool.getEncryptCipher());
                mFingerprintManager.authenticate(object, mCancellationSignal,
                        0, mFmAuthCallback, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 指纹验证回调
     */
    private class FingerprintManageCallbackImpl extends FingerprintManager.AuthenticationCallback {

        @Override
        public void onAuthenticationFailed() { //验证失败
            super.onAuthenticationFailed();
            mDialog.setState(BiometricPromptDialog.STATE_FAILED);
            mManagerIdentifyCallback.onFailed();
        }

        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString) { //多次验证失败，出现错误
            super.onAuthenticationError(errorCode, errString);
            mDialog.setState(BiometricPromptDialog.STATE_ERROR);
            mManagerIdentifyCallback.onError(errorCode, errString.toString());
        }


        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) { //验证成功
            super.onAuthenticationSucceeded(result);
            mDialog.setState(BiometricPromptDialog.STATE_SUCCEED);
            mManagerIdentifyCallback.onSucceeded(result);

        }
    }

    /**
     * 获取指纹管理器
     *
     * @param context
     * @return
     */
    private FingerprintManager getFingerprintManager(Context context) {
        if (mFingerprintManager == null) {
            return context.getSystemService(FingerprintManager.class);
        }
        return mFingerprintManager;
    }

    /**
     * 判断手机硬件是否支持指纹
     * @return
     */
    public boolean isHardwareDetected() {
        if (mFingerprintManager != null) {
            return mFingerprintManager.isHardwareDetected();
        }
        return false;
    }

    /**
     * 判断是否录入了指纹
     * @return
     */
    public boolean hasEnrolledFingerprints() {
        if (mFingerprintManager != null) {
            return mFingerprintManager.hasEnrolledFingerprints();
        }
        return false;
    }
}

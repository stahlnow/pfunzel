package com.stahlnow.android.pfunzel;

// Custom "Camera 2 object" due to error importing Camera2 with support on older SDK versions (< 23)
// see stackoverflow question: http://stackoverflow.com/questions/36372666/how-to-handle-implementing-different-apis-for-older-and-newer-android-versions/36373002

import android.annotation.TargetApi;
import android.app.Activity;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.util.Log;

public class Camera2Object {

    private static final String TAG = "Camera2Object";

    private final CameraManager mCameraManager;

    public Camera2Object(Activity activity) {
        mCameraManager = (CameraManager) activity.getSystemService(activity.CAMERA_SERVICE);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void ledon() {

        try {
            String[] cams = mCameraManager.getCameraIdList();

            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cams[0]);
            if (characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {
                mCameraManager.setTorchMode(cams[0], true);
            }

        } catch (CameraAccessException e) {
            Log.e(TAG, "Can't access camera.");
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void ledoff() {
        try {
            String[] cams = mCameraManager.getCameraIdList();

            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cams[0]);
            if (characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {
                mCameraManager.setTorchMode(cams[0], false);
            }

        } catch (CameraAccessException e) {
            Log.e(TAG, "Can't access camera.");
        }
    }
}

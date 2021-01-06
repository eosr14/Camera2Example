package com.example.finiview.camera.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.finiview.camera.common.base.BaseViewModel
import com.example.finiview.camera.common.event.*


enum class CameraFocusMode {
    MANUAL, FIXED, INFINITY, MACRO, AUTO, CONTINUOUS_FOCUS_PICTURE, CONTINUOUS_FOCUS_VIDEO, EDOF
}

enum class CameraImageRatioMode {
    RATIO_1_1, RATIO_4_3, RATIO_16_9
}

enum class CameraFlashMode {
    OFF, ON, AUTO, AUTO_RED_EYE, TORCH
}

enum class CameraAntiBandingMode {
    AUTO, HZ50, HZ60, NONE
}

class CameraOptionViewModel : BaseViewModel() {

    private val _cameraFocusMode = MutableLiveData<CameraFocusMode>()
    val cameraFocusMode: LiveData<CameraFocusMode> = _cameraFocusMode

    fun onClickCameraIso(iso: Int) = RxEventBus.sendEvent(OnClickCameraIsoEvent(iso))

    fun onClickCameraExposure(exposure: Long) = RxEventBus.sendEvent(OnClickCameraExposureEvent(exposure))

    fun onClickCameraFocus(cameraFocusMode: CameraFocusMode) {
        _cameraFocusMode.value = cameraFocusMode
        RxEventBus.sendEvent(OnClickCameraFocusEvent(cameraFocusMode))
    }

    fun onClickCameraImageRatio(cameraImageRatio: CameraImageRatioMode) = RxEventBus.sendEvent(
            OnClickCameraImageRatioEvent(cameraImageRatio)
    )

    fun onClickCameraFlash(cameraFlashMode: CameraFlashMode) = RxEventBus.sendEvent(
            OnClickCameraFlashEvent(cameraFlashMode)
    )

    fun onClickCameraAntiBanding(cameraAntiBandingMode: CameraAntiBandingMode) = RxEventBus.sendEvent(OnClickCameraAntiBandingEvent(cameraAntiBandingMode))

}
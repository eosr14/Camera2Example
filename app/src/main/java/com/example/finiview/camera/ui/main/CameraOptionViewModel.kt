package com.example.finiview.camera.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.finiview.camera.common.base.BaseViewModel
import com.example.finiview.camera.common.event.*
import com.example.finiview.camera.common.notifyObserver


enum class CameraFocusMode {
    MANUAL, AUTO, INFINITY
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

    private val _isCameraFocusManual = MutableLiveData(false)
    val isCameraFocusManual: LiveData<Boolean> = _isCameraFocusManual

    fun onClickCameraIso(iso: Int) = RxEventBus.sendEvent(OnClickCameraIsoEvent(iso))

    fun onClickCameraExposure(exposure: Long) =
        RxEventBus.sendEvent(OnClickCameraExposureEvent(exposure))

    fun onClickCameraFocus(cameraFocusMode: CameraFocusMode) {
        _isCameraFocusManual.value = when (cameraFocusMode) {
            CameraFocusMode.MANUAL -> true
            else -> false
        }

        RxEventBus.sendEvent(OnClickCameraFocusEvent(cameraFocusMode))
    }

    fun onClickCameraFocusManual(focusDistance : Float) {
        RxEventBus.sendEvent(OnClickCameraFocusManualEvent(focusDistance))
    }

    fun onClickCameraImageRatio(cameraImageRatio: CameraImageRatioMode) = RxEventBus.sendEvent(
        OnClickCameraImageRatioEvent(cameraImageRatio)
    )

    fun onClickCameraFlash(cameraFlashMode: CameraFlashMode) = RxEventBus.sendEvent(
        OnClickCameraFlashEvent(cameraFlashMode)
    )

    fun onClickCameraAntiBanding(cameraAntiBandingMode: CameraAntiBandingMode) =
        RxEventBus.sendEvent(OnClickCameraAntiBandingEvent(cameraAntiBandingMode))

}
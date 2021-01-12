package com.example.finiview.camera.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.finiview.camera.common.base.BaseViewModel
import com.example.finiview.camera.common.event.*

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

    private val _isCameraFocusManual = MutableLiveData(true)
    val isCameraFocusManual: LiveData<Boolean> = _isCameraFocusManual

    private val _cameraImageRatio = MutableLiveData(CameraImageRatioMode.RATIO_1_1)
    val cameraImageRatio: LiveData<CameraImageRatioMode> = _cameraImageRatio

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

    fun onClickCameraFocusManual(focusDistance: Float) {
        RxEventBus.sendEvent(OnClickCameraFocusManualEvent(focusDistance))
    }

    fun onClickCameraImageRatio(cameraImageRatio: CameraImageRatioMode) {
        _cameraImageRatio.value = cameraImageRatio
    }

    fun onClickCameraFlash(cameraFlashMode: CameraFlashMode) = RxEventBus.sendEvent(
            OnClickCameraFlashEvent(cameraFlashMode)
    )

    fun onClickCameraAntiBanding(cameraAntiBandingMode: CameraAntiBandingMode) =
            RxEventBus.sendEvent(OnClickCameraAntiBandingEvent(cameraAntiBandingMode))

}
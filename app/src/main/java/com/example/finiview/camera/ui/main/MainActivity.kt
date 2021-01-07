package com.example.finiview.camera.ui.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.CameraCaptureSession.CaptureCallback
import android.media.ExifInterface
import android.media.Image
import android.media.ImageReader
import android.media.ImageReader.OnImageAvailableListener
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView.SurfaceTextureListener
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.finiview.camera.OnProgressChanged
import com.example.finiview.camera.R
import com.example.finiview.camera.Zoom
import com.example.finiview.camera.common.base.BaseActivity
import com.example.finiview.camera.common.event.*
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
import java.nio.ByteBuffer
import java.util.*


class MainActivity : BaseActivity() {

    private lateinit var cameraDevice: CameraDevice
    private lateinit var previewBuilder: CaptureRequest.Builder
    private lateinit var cameraCaptureSessions: CameraCaptureSession

    private var imageReader: ImageReader? = null
    private var imageDimension: Size? = null

    private var cameraId = CAMERA_BACK

    private var backgroundHandler: Handler? = null
    private var backgroundThread: HandlerThread? = null

    private var isPermissionsGranted: Boolean = false

    private val textureListener: SurfaceTextureListener = object : SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            openCamera()
        }

        override fun onSurfaceTextureSizeChanged(
            surface: SurfaceTexture,
            width: Int,
            height: Int
        ) {
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            return false
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 상태바 숨기기
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // 화면 켜짐 유지
        window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        setContentView(R.layout.activity_main)
        checkCameraPermission()
        bindView()
    }

    private fun checkCameraPermission() {
        val cameraPermissionCheck = ContextCompat.checkSelfPermission(
            this@MainActivity,
            Manifest.permission.CAMERA
        )

        isPermissionsGranted = cameraPermissionCheck == PackageManager.PERMISSION_GRANTED

        if (!isPermissionsGranted) {
            // 권한 없음
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_CAMERA_REQUEST_CODE
            )
        }
    }

    override fun onResume() {
        super.onResume()
        eventObserve()
        startBackgroundThread()

        if (textureView.isAvailable) {
            openCamera()
        } else {
            textureView.surfaceTextureListener = textureListener;
        }
    }

    override fun onPause() {
        super.onPause()
        stopBackgroundThread();
        clearDisposable()
    }

    override fun onDestroy() {
        super.onDestroy()
        closeCamera()
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("Camera Background").apply {
            start()
            backgroundHandler = Handler(looper)
        }
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()

        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun createCameraPreview() {
        try {
            textureView.surfaceTexture?.let { texture ->
                imageDimension?.let {
                    texture.setDefaultBufferSize(it.width, it.height)
                }

                val surface = Surface(texture)
                previewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                previewBuilder.addTarget(surface)
                cameraDevice.createCaptureSession(
                    listOf(surface),
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                            cameraCaptureSessions = cameraCaptureSession
                            updatePreview()
                        }

                        override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                            Toast.makeText(
                                this@MainActivity,
                                "Configuration change",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    null
                )
            }

        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }


    private fun bindView() {
        textureView.surfaceTextureListener = textureListener;

        iv_main_capture.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // request permission
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST_CODE
                )
            } else {
                takePicture()
            }
        }

        iv_main_switch_camera.setOnClickListener {
            switchCamera()
        }

        sb_main_zoom.apply {
            max = 5

            setOnSeekBarChangeListener(object : OnProgressChanged() {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    updateZoom(progress + 1)
                }
            })
        }

        sc_main_torch_switch.setOnCheckedChangeListener { _, isChecked ->
            setFlashOnOff(isChecked)
        }

        iv_main_setting.setOnClickListener {
            val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            try {
                val characteristics = manager.getCameraCharacteristics(cameraDevice.id)
                val isoRange =
                    characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
                val exposureRange =
                    characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)
                val manualFocusMinValue =
                    characteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE)
                supportFragmentManager.beginTransaction().run {
                    CameraOptionDialog(isoRange, exposureRange, manualFocusMinValue).show(
                        this,
                        null
                    )
                }
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }
    }

    private fun updateZoom(progress: Int) {
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val characteristics = manager.getCameraCharacteristics(cameraId)

            Zoom(characteristics).apply {
                if (::previewBuilder.isInitialized) {
                    setZoom(previewBuilder, progress.toFloat())
                    cameraCaptureSessions.setRepeatingRequest(
                        previewBuilder.build(),
                        null,
                        backgroundHandler
                    );

                    runOnUiThread {
                        tv_main_zoom_level.text = progress.toFloat().toString()
                    }
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun switchCamera() {
        when (cameraId) {
            CAMERA_BACK -> {
                cameraId = CAMERA_FRONT
                cameraDevice.close()
                openCamera()
            }
            else -> {
                cameraId = CAMERA_BACK
                cameraDevice.close()
                openCamera()
            }
        }
        Log.i(TAG, "switchCamera = $cameraId")
    }

    private fun setFlashOnOff(isFlashOn: Boolean) {
        try {
            cameraCaptureSessions.stopRepeating()
            previewBuilder.set(
                CaptureRequest.FLASH_MODE,
                if (isFlashOn) CaptureRequest.FLASH_MODE_TORCH else null
            )
            cameraCaptureSessions.setRepeatingRequest(previewBuilder.build(), null, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace();
        }
    }

    private fun setIsoValue(iso: Int) {
        try {
            cameraCaptureSessions.stopRepeating()
            previewBuilder.set(
                CaptureRequest.CONTROL_AE_MODE,
                CameraCharacteristics.CONTROL_AE_MODE_OFF
            )
            previewBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, iso);
            cameraCaptureSessions.setRepeatingRequest(previewBuilder.build(), null, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun setExposureValue(exposure: Long) {
        try {
            cameraCaptureSessions.stopRepeating()
            previewBuilder.set(
                CaptureRequest.CONTROL_AE_MODE,
                CameraCharacteristics.CONTROL_AE_MODE_OFF
            )
            previewBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, exposure)
            cameraCaptureSessions.setRepeatingRequest(previewBuilder.build(), null, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun setFocusManualValue(focusDistance: Float) {
        try {
            cameraCaptureSessions.stopRepeating()
            previewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
            previewBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, focusDistance)
            cameraCaptureSessions.setRepeatingRequest(previewBuilder.build(), null, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun setAutoFocusMode() {
        try {
            cameraCaptureSessions.stopRepeating()
            previewBuilder.set(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
            )
            cameraCaptureSessions.setRepeatingRequest(previewBuilder.build(), null, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun takePicture() {
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val characteristics = manager.getCameraCharacteristics(cameraDevice.id)
            val jpegSizes: Array<Size>? =
                characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    ?.getOutputSizes(ImageFormat.JPEG)
            var width = 640
            var height = 480

            if (jpegSizes != null && jpegSizes.isNotEmpty()) {
                width = jpegSizes[0].width
                height = jpegSizes[0].height
            }

            imageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1)

            val outputSurfaces: MutableList<Surface> = ArrayList(2)
            imageReader?.let { outputSurfaces.add(it.surface) }
            outputSurfaces.add(Surface(textureView.surfaceTexture))

            imageReader?.let { previewBuilder.addTarget(it.surface) }
            previewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)

            val rotation = windowManager.defaultDisplay.rotation
            previewBuilder[CaptureRequest.JPEG_ORIENTATION] = ORIENTATIONS[rotation]


            val fileName = "${UUID.randomUUID()}.jpg"
            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                "/Camera2Example"
            )

            if (!dir.exists()) {
                dir.mkdir()
            }

            val file = File(dir, fileName)

            val readerListener: OnImageAvailableListener = object : OnImageAvailableListener {
                override fun onImageAvailable(reader: ImageReader) {
                    var image: Image? = null
                    try {
                        image = reader.acquireLatestImage()
                        val buffer: ByteBuffer = image.planes[0].buffer
                        val bytes = ByteArray(buffer.capacity())
                        buffer.get(bytes)
                        save(bytes)
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {
                        image?.close()
                    }
                }

                @Throws(IOException::class)
                private fun save(bytes: ByteArray) {
                    var output: OutputStream? = null
                    try {
                        output = FileOutputStream(file)
                        output.write(bytes)
                    } finally {
                        output?.close()
                    }
                }
            }

            imageReader?.setOnImageAvailableListener(readerListener, backgroundHandler)

            val captureListener: CaptureCallback = object : CaptureCallback() {
                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    super.onCaptureCompleted(session, request, result)
                    Toast.makeText(this@MainActivity, "Saved:$file", Toast.LENGTH_SHORT).show()
                    createCameraPreview()
                }
            }

            cameraDevice.createCaptureSession(
                outputSurfaces,
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        try {
                            session.capture(
                                previewBuilder.build(),
                                captureListener,
                                backgroundHandler
                            )
                        } catch (e: CameraAccessException) {
                            e.printStackTrace()
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {}
                },
                backgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun openCamera() {
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        Log.i(TAG, "is camera open")
        try {
            val characteristics = manager.getCameraCharacteristics(cameraId)
            characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)?.let { map ->
                imageDimension = map.getOutputSizes(SurfaceTexture::class.java)[0]
            }

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            val deviceStateCallback = object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    try {
                        createCameraPreview()
                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                    }
                }

                override fun onDisconnected(camera: CameraDevice) {
                    cameraDevice.close()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    Toast.makeText(this@MainActivity, "카메라를 열지 못했습니다.", Toast.LENGTH_SHORT).show()
                    cameraDevice.close()
                }
            }

            manager.openCamera(cameraId, deviceStateCallback, null)
        } catch (e: CameraAccessException) {
            Toast.makeText(this@MainActivity, "카메라를 열지 못했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updatePreview() {
        previewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        try {
            cameraCaptureSessions.setRepeatingRequest(
                previewBuilder.build(),
                null,
                backgroundHandler
            )
            updateZoom(1)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun closeCamera() {
        cameraDevice.close()
        imageReader?.close()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_CAMERA_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(
                        this@MainActivity,
                        R.string.permission_camera_granted,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.permission_camera_define),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(
                        this@MainActivity,
                        R.string.permission_write_external_storage_granted,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        R.string.permission_write_external_storage_define,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            else -> {
            }
        }

        recreate()
    }

    private fun eventObserve() {
        addDisposable(
            RxEventBus.getSingleEventType(EventBusInterface::class.java)
                .compose(bindToLifecycle())
                .subscribe {
                    when (it) {
                        is OnClickCameraIsoEvent -> setIsoValue(it.iso)

                        is OnClickCameraExposureEvent -> {
                            setExposureValue(it.exposure)
                        }

                        is OnClickCameraFocusEvent -> {
                            when (it.cameraFocusMode) {
                                CameraFocusMode.AUTO -> {
                                    setAutoFocusMode()
                                }
                                CameraFocusMode.INFINITY -> {
                                    setFocusManualValue(0f)
                                }
                                else -> {

                                }
                            }
                        }

                        is OnClickCameraFocusManualEvent -> {
                            setFocusManualValue(it.focusDistance)
                        }

                        is OnClickCameraImageRatioEvent -> {

                        }

                        is OnClickCameraFlashEvent -> {

                        }

                        is OnClickCameraAntiBandingEvent -> {

                        }

                    }
                })
    }

    companion object {
        private const val TAG = "Camera2Example"

        private const val CAMERA_BACK = "0"
        private const val CAMERA_FRONT = "1"

        private const val PERMISSION_CAMERA_REQUEST_CODE = 10000
        private const val PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 10001

        private val ORIENTATIONS = SparseIntArray()

        init {
            ORIENTATIONS.append(ExifInterface.ORIENTATION_NORMAL, 0)
            ORIENTATIONS.append(ExifInterface.ORIENTATION_ROTATE_90, 90)
            ORIENTATIONS.append(ExifInterface.ORIENTATION_ROTATE_180, 180)
            ORIENTATIONS.append(ExifInterface.ORIENTATION_ROTATE_270, 270)
        }
    }

}
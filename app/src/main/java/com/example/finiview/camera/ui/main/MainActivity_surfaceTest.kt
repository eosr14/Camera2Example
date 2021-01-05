//package com.example.finiview.camera
//
//import android.Manifest
//import android.content.Context
//import android.content.pm.PackageManager
//import android.graphics.ImageFormat
//import android.hardware.Sensor
//import android.hardware.SensorManager
//import android.hardware.camera2.*
//import android.media.ExifInterface
//import android.media.ImageReader
//import android.os.Bundle
//import android.os.Handler
//import android.os.HandlerThread
//import android.util.DisplayMetrics
//import android.util.Log
//import android.util.Size
//import android.util.SparseIntArray
//import android.view.SurfaceHolder
//import android.view.WindowManager
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.constraintlayout.widget.ConstraintLayout
//import androidx.core.app.ActivityCompat
//import kotlinx.android.synthetic.main.activity_main.*
//
//class MainActivity_surfaceTest : AppCompatActivity() {
//
////    private val surfaceReadyCallback = object : SurfaceHolder.Callback {
////        override fun surfaceCreated(holder: SurfaceHolder) {
////            startCameraSession()
////        }
////
////        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
////        }
////
////        override fun surfaceDestroyed(holder: SurfaceHolder) {
////        }
////    }
//
//    private lateinit var surfaceViewHolder: SurfaceHolder
//    private lateinit var cameraDevice: CameraDevice
//    private lateinit var imageReader: ImageReader
//    private lateinit var previewBuilder: CaptureRequest.Builder
//    private lateinit var session: CameraCaptureSession
//
//    private lateinit var accelerometer: Sensor
//    private lateinit var magnetometer: Sensor
//    private lateinit var sensorManager: SensorManager
//
//    private val deviceOrientation: DeviceOrientation by lazy { DeviceOrientation() }
//    private var height = 0
//    private var width = 0
//
//    private var cameraId = CAMERA_BACK
//
//    private var handler: Handler? = null
//
//    private val deviceStateCallback = object : CameraDevice.StateCallback() {
//        override fun onOpened(camera: CameraDevice) {
//            cameraDevice = camera
//            try {
//                takePreview()
//            } catch (e: CameraAccessException) {
//                e.printStackTrace()
//            }
//        }
//
//        override fun onDisconnected(camera: CameraDevice) {
//            cameraDevice.close()
//        }
//
//        override fun onError(camera: CameraDevice, error: Int) {
//            Toast.makeText(this@MainActivity_surfaceTest, "카메라를 열지 못했습니다.", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private val mSessionPreviewStateCallback = object : CameraCaptureSession.StateCallback() {
//        override fun onConfigured(session: CameraCaptureSession) {
//            this@MainActivity_surfaceTest.session = session
//            try {
//                // Key-Value 구조로 설정
//                // 오토포커싱이 계속 동작
//                previewBuilder.set(
//                    CaptureRequest.CONTROL_AF_MODE,
//                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
//                )
//                //필요할 경우 플래시가 자동으로 켜짐
//                previewBuilder.set(
//                    CaptureRequest.CONTROL_AE_MODE,
//                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
//                )
//                this@MainActivity_surfaceTest.session.setRepeatingRequest(previewBuilder.build(), null, handler)
//            } catch (e: CameraAccessException) {
//                e.printStackTrace()
//            }
//
//        }
//
//        override fun onConfigureFailed(session: CameraCaptureSession) {
//            Toast.makeText(this@MainActivity_surfaceTest, "카메라 구성 실패", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // 상태바 숨기기
//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_FULLSCREEN,
//            WindowManager.LayoutParams.FLAG_FULLSCREEN
//        )
//
//        // 화면 켜짐 유지
//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
//            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//        )
//
////        if (!CameraPermissionHelper.hasCameraPermission(this)) {
////            CameraPermissionHelper.requestCameraPermission(this)
////            return
////        }
//
//        setContentView(R.layout.activity_main)
//        initSensor()
//        initView()
//    }
//
//    override fun onResume() {
//        super.onResume()
//
//        sensorManager.registerListener(
//            deviceOrientation.eventListener, accelerometer, SensorManager.SENSOR_DELAY_UI
//        )
//        sensorManager.registerListener(
//            deviceOrientation.eventListener, magnetometer, SensorManager.SENSOR_DELAY_UI
//        )
//    }
//
//    override fun onPause() {
//        super.onPause()
//        sensorManager.unregisterListener(deviceOrientation.eventListener)
//    }
//
//    private fun initSensor() {
//        sensorManager = (getSystemService(Context.SENSOR_SERVICE) as SensorManager).apply {
//            accelerometer = this.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
//            magnetometer = this.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
//        }
//    }
//
//    private fun initView() {
//        with(DisplayMetrics()) {
//            windowManager.defaultDisplay.getMetrics(this)
//            height = heightPixels
//            width = widthPixels
//        }
//
//        surfaceViewHolder = surfaceView.holder
//        surfaceViewHolder.addCallback(object : SurfaceHolder.Callback {
//            override fun surfaceCreated(holder: SurfaceHolder) {
//                initCameraAndPreview()
//            }
//
//            override fun surfaceDestroyed(holder: SurfaceHolder) {
//                cameraDevice.close()
//            }
//
//            override fun surfaceChanged(
//                holder: SurfaceHolder, format: Int,
//                width: Int, height: Int
//            ) {
//
//            }
//        })
//
//        iv_main_switch_camera.setOnClickListener {
//            switchCamera()
//        }
//    }
//
//    fun initCameraAndPreview() {
//        val handlerThread = HandlerThread("CAMERA2")
//        handlerThread.start()
//        handler = Handler(handlerThread.looper)
//
//        openCamera()
//    }
//
//    private fun switchCamera() {
//        when (cameraId) {
//            CAMERA_BACK -> {
//                cameraId = CAMERA_FRONT
//                cameraDevice.close()
//                openCamera()
//            }
//            else -> {
//                cameraId = CAMERA_BACK
//                cameraDevice.close()
//                openCamera()
//            }
//        }
//    }
//
////    private fun takePicture() {
////        try {
////            val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
////            val characteristics = manager.getCameraCharacteristics(cameraDevice!!.id)
////            var jpegSizes: Array<Size>? = null
////            jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!.getOutputSizes(ImageFormat.JPEG)
////
////            var width = jpegSizes[0].width
////            var height = jpegSizes[0].height
////
////            imageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1)
////
////            val outputSurface = ArrayList<Surface>(2)
////            outputSurface.add(imageReader!!.surface)
////            outputSurface.add(Surface(textureView!!.surfaceTexture))
////
////            val captureBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
////            captureBuilder.addTarget(imageReader!!.surface)
////
////            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
////
////            // 사진의 rotation 을 설정해준다
////            val rotation = windowManager.defaultDisplay.rotation
////            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION,ORIENTATIONS.get(rotation))
////
////            var file = File(Environment.getExternalStorageDirectory().toString() + "/pic${fileCount}.jpg")
////            val readerListener = object : ImageReader.OnImageAvailableListener {
////                override fun onImageAvailable(reader: ImageReader?) {
////                    var image : Image? = null
////
////                    try {
////                        image = imageReader!!.acquireLatestImage()
////
////                        val buffer = image!!.planes[0].buffer
////                        val bytes = ByteArray(buffer.capacity())
////                        buffer.get(bytes)
////
////                        var output: OutputStream? = null
////                        try {
////                            output = FileOutputStream(file)
////                            output.write(bytes)
////                        } finally {
////                            output?.close()
////
////                            var uri = Uri.fromFile(file)
////                            Log.d(TAG, "uri 제대로 잘 바뀌었는지 확인 ${uri}")
////
////                            // 프리뷰 이미지에 set 해줄 비트맵을 만들어준다
////                            var bitmap: Bitmap = BitmapFactory.decodeFile(file.path)
////
////                            // 비트맵 사진이 90도 돌아가있는 문제를 해결하기 위해 rotate 해준다
////                            var rotateMatrix = Matrix()
////                            rotateMatrix.postRotate(90F)
////                            var rotatedBitmap: Bitmap = Bitmap.createBitmap(bitmap, 0,0, bitmap.width, bitmap.height, rotateMatrix, false)
////
////                            // 90도 돌아간 비트맵을 이미지뷰에 set 해준다
////                            img_previewImage.setImageBitmap(rotatedBitmap)
////
////                            // 리사이클러뷰 갤러리로 보내줄 uriList 에 찍은 사진의 uri 를 넣어준다
////                            uriList.add(0, uri.toString())
////
////                            fileCount++
////                        }
////
////                    } catch (e: FileNotFoundException) {
////                        e.printStackTrace()
////                    } catch (e: IOException) {
////                        e.printStackTrace()
////                    } finally {
////                        image?.close()
////                    }
////                }
////
////            }
////
////            // imageReader 객체에 위에서 만든 readerListener 를 달아서, 이미지가 사용가능하면 사진을 저장한다
////            imageReader!!.setOnImageAvailableListener(readerListener, null)
////
////            val captureListener = object : CameraCaptureSession.CaptureCallback() {
////                override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
////                    super.onCaptureCompleted(session, request, result)
////                    /*Toast.makeText(this@MainActivity, "Saved:$file", Toast.LENGTH_SHORT).show()*/
////                    Toast.makeText(this@MainActivity, "사진이 촬영되었습니다", Toast.LENGTH_SHORT).show()
////                    createCameraPreviewSession()
////                }
////            }
////
////            // outputSurface 에 위에서 만든 captureListener 를 달아, 캡쳐(사진 찍기) 해주고 나서 카메라 미리보기 세션을 재시작한다
////            cameraDevice!!.createCaptureSession(outputSurface, object : CameraCaptureSession.StateCallback() {
////                override fun onConfigureFailed(session: CameraCaptureSession) {}
////
////                override fun onConfigured(session: CameraCaptureSession) {
////                    try {
////                        session.capture(captureBuilder.build(), captureListener, null)
////                    } catch (e: CameraAccessException) {
////                        e.printStackTrace()
////                    }
////                }
////
////            }, null)
////
////
////        } catch (e: CameraAccessException) {
////            e.printStackTrace()
////        }
////    }
//
//
//    private fun openCamera() {
//        try {
//            val mCameraManager = this.getSystemService(Context.CAMERA_SERVICE) as CameraManager
//            val characteristics = mCameraManager.getCameraCharacteristics(cameraId)
//            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
//
//            val largestPreviewSize = map!!.getOutputSizes(ImageFormat.JPEG)[0]
////            setAspectRatioTextureView(largestPreviewSize.height, largestPreviewSize.width)
//
//            imageReader = ImageReader.newInstance(
//                largestPreviewSize.width,
//                largestPreviewSize.height,
//                ImageFormat.JPEG,
//                7
//            )
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
//                != PackageManager.PERMISSION_GRANTED
//            ) return
//
//            mCameraManager.openCamera(cameraId, deviceStateCallback, handler)
//        } catch (e: CameraAccessException) {
//            Toast.makeText(this@MainActivity_surfaceTest, "카메라를 열지 못했습니다.", Toast.LENGTH_SHORT).show()
//        }
//    }
//
////    private fun setAspectRatioTextureView(ResolutionWidth: Int, ResolutionHeight: Int) {
////        if (ResolutionWidth > ResolutionHeight) {
////            val newWidth = width
////            val newHeight = width * ResolutionWidth / ResolutionHeight
////            updateTextureViewSize(newWidth, newHeight)
////
////        } else {
////            val newWidth = width
////            val newHeight = width * ResolutionHeight / ResolutionWidth
////            updateTextureViewSize(newWidth, newHeight)
////        }
////    }
//
//    private fun updateTextureViewSize(viewWidth: Int, viewHeight: Int) {
//        Log.d("ViewSize", "TextureView Width : $viewWidth TextureView Height : $viewHeight")
//        surfaceView.layoutParams = ConstraintLayout.LayoutParams(viewWidth, viewHeight)
//    }
//
//    @Throws(CameraAccessException::class)
//    fun takePreview() {
//        previewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
//        previewBuilder.addTarget(surfaceViewHolder.surface)
//        cameraDevice.createCaptureSession(
//            listOf(surfaceViewHolder.surface, imageReader.surface),
//            mSessionPreviewStateCallback,
//            handler
//        )
//    }
//
//
////    @SuppressLint("MissingPermission")
////    private fun startCameraSession() {
////        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
////        if (cameraManager.cameraIdList.isEmpty()) {
////            // no cameras
////            return
////        }
////        val firstCamera = cameraManager.cameraIdList[0]
////        cameraManager.openCamera(firstCamera, object : CameraDevice.StateCallback() {
////            override fun onDisconnected(p0: CameraDevice) {}
////            override fun onError(p0: CameraDevice, p1: Int) {}
////
////            override fun onOpened(cameraDevice: CameraDevice) {
////                // use the camera
////                val cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraDevice.id)
////
////                cameraCharacteristics[CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP]?.let { streamConfigurationMap ->
////                    streamConfigurationMap.getOutputSizes(ImageFormat.YUV_420_888)
////                        ?.let { yuvSizes ->
////                            val previewSize = yuvSizes.last()
////
////
////                            val displayRotation = windowManager.defaultDisplay.rotation
////                            val swappedDimensions =
////                                areDimensionsSwapped(displayRotation, cameraCharacteristics)
////
////                            // swap width and height if needed
////                            val rotatedPreviewWidth =
////                                if (swappedDimensions) previewSize.height else previewSize.width
////                            val rotatedPreviewHeight =
////                                if (swappedDimensions) previewSize.width else previewSize.height
////                            surfaceView.holder.setFixedSize(
////                                rotatedPreviewWidth,
////                                rotatedPreviewHeight
////                            )
////
////
////                            val previewSurface = surfaceView.holder.surface
////                            val captureCallback = object : CameraCaptureSession.StateCallback() {
////                                override fun onConfigureFailed(session: CameraCaptureSession) {}
////
////                                override fun onConfigured(session: CameraCaptureSession) {
////                                    // session configured
////                                    val previewRequestBuilder =
////                                        cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
////                                            .apply {
////                                                addTarget(previewSurface)
////                                            }
////                                    session.setRepeatingRequest(
////                                        previewRequestBuilder.build(),
////                                        object : CameraCaptureSession.CaptureCallback() {},
////                                        Handler { true }
////                                    )
////                                }
////                            }
////
////                            cameraDevice.createCaptureSession(
////                                mutableListOf(previewSurface),
////                                captureCallback,
////                                Handler { true })
////
////                        }
////
////                }
////            }
////        }, Handler { true })
////    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        if (!CameraPermissionHelper.hasCameraPermission(this)) {
//            Toast.makeText(
//                this,
//                "Camera permission is needed to run this application",
//                Toast.LENGTH_LONG
//            )
//                .show()
//            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
//                // Permission denied with checking "Do not ask again".
//                CameraPermissionHelper.launchPermissionSettings(this)
//            }
//            finish()
//        }
//
//        recreate()
//    }
//
////    private fun areDimensionsSwapped(
////        displayRotation: Int,
////        cameraCharacteristics: CameraCharacteristics
////    ): Boolean {
////        var swappedDimensions = false
////        when (displayRotation) {
////            Surface.ROTATION_0, Surface.ROTATION_180 -> {
////                if (cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) == 90 || cameraCharacteristics.get(
////                        CameraCharacteristics.SENSOR_ORIENTATION
////                    ) == 270
////                ) {
////                    swappedDimensions = true
////                }
////            }
////            Surface.ROTATION_90, Surface.ROTATION_270 -> {
////                if (cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) == 0 || cameraCharacteristics.get(
////                        CameraCharacteristics.SENSOR_ORIENTATION
////                    ) == 180
////                ) {
////                    swappedDimensions = true
////                }
////            }
////            else -> {
////                // invalid display rotation
////            }
////        }
////        return swappedDimensions
////    }
//
//    companion object {
//        private const val TAG = "Camera2Example"
//
//        private const val CAMERA_BACK = "0"
//        private const val CAMERA_FRONT = "1"
//
//        private val ORIENTATIONS = SparseIntArray()
//
//        init {
//            ORIENTATIONS.append(ExifInterface.ORIENTATION_NORMAL, 0)
//            ORIENTATIONS.append(ExifInterface.ORIENTATION_ROTATE_90, 90)
//            ORIENTATIONS.append(ExifInterface.ORIENTATION_ROTATE_180, 180)
//            ORIENTATIONS.append(ExifInterface.ORIENTATION_ROTATE_270, 270)
//        }
//    }
//
//}
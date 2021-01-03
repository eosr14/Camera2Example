package com.example.finiview.camera

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.ExifInterface
import kotlin.math.abs

class DeviceOrientation {
    private val smoothness = 1
    private var averagePitch = 0f
    private var averageRoll = 0f

    var orientation = ORIENTATION_PORTRAIT
        private set

    private var pitches: FloatArray = FloatArray(smoothness)
    private var rolls: FloatArray = FloatArray(smoothness)

    var eventListener: SensorEventListener = object : SensorEventListener {

        var mGravity: FloatArray? = null
        var mGeomagnetic: FloatArray? = null

        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) mGravity = event.values
            if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) mGeomagnetic = event.values
            if (mGravity != null && mGeomagnetic != null) {
                val R = FloatArray(9)
                val I = FloatArray(9)
                val success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic)
                if (success) {
                    val orientationData = FloatArray(3)
                    SensorManager.getOrientation(R, orientationData)
                    averagePitch = addValue(orientationData[1], pitches)
                    averageRoll = addValue(orientationData[2], rolls)
                    orientation = calculateOrientation()
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    private fun addValue(value: Float, values: FloatArray): Float {
        var value = value
        value = Math.round(Math.toDegrees(value.toDouble())).toFloat()
        var average = 0f
        for (i in 1 until smoothness) {
            values[i - 1] = values[i]
            average += values[i]
        }
        values[smoothness - 1] = value
        average = (average + value) / smoothness
        return average
    }

    private fun calculateOrientation(): Int {
        // finding local orientation dip
        return if ((orientation == ORIENTATION_PORTRAIT || orientation == ORIENTATION_PORTRAIT_REVERSE)
            && averageRoll > -30 && averageRoll < 30
        ) {
            if (averagePitch > 0) ORIENTATION_PORTRAIT_REVERSE else ORIENTATION_PORTRAIT
        } else {
            // divides between all orientations
            if (abs(averagePitch) >= 30) {
                if (averagePitch > 0) ORIENTATION_PORTRAIT_REVERSE else ORIENTATION_PORTRAIT
            } else {
                if (averageRoll > 0) {
                    ORIENTATION_LANDSCAPE_REVERSE
                } else {
                    ORIENTATION_LANDSCAPE
                }
            }
        }
    }

    companion object {
        private const val ORIENTATION_PORTRAIT = ExifInterface.ORIENTATION_ROTATE_90 // 6
        private const val ORIENTATION_LANDSCAPE_REVERSE = ExifInterface.ORIENTATION_ROTATE_180 // 3
        private const val ORIENTATION_LANDSCAPE = ExifInterface.ORIENTATION_NORMAL // 1
        private const val ORIENTATION_PORTRAIT_REVERSE = ExifInterface.ORIENTATION_ROTATE_270 // 8
    }
}
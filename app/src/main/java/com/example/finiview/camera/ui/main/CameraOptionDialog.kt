package com.example.finiview.camera.ui.main

import android.os.Bundle
import android.util.Range
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.finiview.camera.OnProgressChanged
import com.example.finiview.camera.R
import com.example.finiview.camera.common.inject
import com.example.finiview.camera.databinding.DialogCameraOptionBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CameraOptionDialog(
    private val isoRange: Range<Int>?,
    private val exposureRange: Range<Long>?,
    private val manualFocusMinValue: Float?
) : BottomSheetDialogFragment() {

    private val viewModel: CameraOptionViewModel by lazy {
        CameraOptionViewModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<DialogCameraOptionBinding>(
            inflater,
            R.layout.dialog_camera_option,
            container,
            false
        )

        context?.let { context ->
            (context as? AppCompatActivity)?.let {
                binding.viewModel = viewModel.inject(it)
            }

            isoRange?.let { isoRange ->
                binding.tvOptionIsoTitle.text = String.format(
                    context.getString(R.string.dialog_camera_option_iso_title),
                    isoRange.lower,
                    isoRange.upper
                )

                binding.sbOptionIso.apply {
                    max = ((isoRange.upper - isoRange.lower) / OPTION_ISO_STEP)

                    setOnSeekBarChangeListener(object : OnProgressChanged() {
                        override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean
                        ) {
                            val isoValue = isoRange.lower + (progress * OPTION_ISO_STEP)
                            binding.tvOptionIso.text = String.format(
                                context.getString(R.string.dialog_camera_option_iso),
                                isoValue
                            )
                            viewModel.onClickCameraIso(isoValue)
                        }
                    })
                }
            }

            exposureRange?.let { exposureRange ->
                binding.tvOptionExposureTitle.text = String.format(
                    context.getString(R.string.dialog_camera_option_exposure_title),
                    exposureRange.lower,
                    exposureRange.upper
                )

                binding.sbOptionExposure.apply {
                    setOnSeekBarChangeListener(object : OnProgressChanged() {
                        override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean
                        ) {
                            val exposureValue =
                                (progress * (exposureRange.upper - exposureRange.lower) / 100 + exposureRange.lower)
                            binding.tvOptionExposure.text = String.format(
                                context.getString(R.string.dialog_camera_option_exposure),
                                exposureValue
                            )
                            viewModel.onClickCameraExposure(exposureValue)
                        }
                    })
                }
            }

            manualFocusMinValue?.let { manualFocusMinValue ->
                binding.sbOptionFocusManual.apply {
                    setOnSeekBarChangeListener(object : OnProgressChanged() {
                        override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean
                        ) {
                            val manualFocusValue = progress.toFloat() * manualFocusMinValue / 100
                            binding.tvOptionFocusManual.text = String.format(
                                context.getString(R.string.dialog_camera_option_focus_manual_value),
                                manualFocusValue
                            )
                            viewModel.onClickCameraFocusManual(manualFocusValue)
                        }
                    })
                }
            }


        }

        return binding.root
    }

    companion object {
        private const val OPTION_ISO_STEP = 50
    }

}
package com.example.finiview.camera

import android.widget.SeekBar

internal infix fun SeekBar.onProgressChanged(zoomUpdated: () -> Unit) {
    setOnSeekBarChangeListener(object : OnProgressChanged() {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            zoomUpdated()
        }
    })
}

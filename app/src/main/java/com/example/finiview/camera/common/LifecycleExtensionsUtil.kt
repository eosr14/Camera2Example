package com.example.finiview.camera.common

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

fun <T : ViewModel> T.inject(activity: AppCompatActivity): T =
    ViewModelProvider(activity.viewModelStore, createViewModel(this)).get(this.javaClass)

fun <T : ViewModel> T.inject(fragment: Fragment): T =
    ViewModelProvider(fragment.viewModelStore, createViewModel(this)).get(this.javaClass)

@Suppress("UNCHECKED_CAST")
private fun <T : ViewModel> createViewModel(model: T) = object : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return model as T
    }
}
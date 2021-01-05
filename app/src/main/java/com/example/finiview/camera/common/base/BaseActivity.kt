package com.example.finiview.camera.common.base

import android.annotation.SuppressLint
import android.os.Bundle
import com.example.finiview.camera.common.view.ProgressDialog
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

@SuppressLint("Registered")
open class BaseActivity : RxBaseActivity() {

    private val compositeDisposable = CompositeDisposable()
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressDialog = ProgressDialog(this)
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        compositeDisposable.dispose()
        super.onDestroy()
    }

    fun addDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    fun clearDisposable() = compositeDisposable.clear()

}
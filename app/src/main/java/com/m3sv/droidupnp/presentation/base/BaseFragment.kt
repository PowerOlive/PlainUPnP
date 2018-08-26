package com.m3sv.droidupnp.presentation.base

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import com.m3sv.droidupnp.di.ViewModelFactory
import dagger.android.support.DaggerFragment
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject


abstract class BaseFragment : DaggerFragment() {

    protected val disposables = CompositeDisposable()

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun onDestroyView() {
        disposables.clear()
        super.onDestroyView()
    }

    protected inline fun <reified T : ViewModel> BaseFragment.getViewModel(): T {
        return ViewModelProviders.of(requireActivity(), viewModelFactory).get(T::class.java)
    }

    protected inline fun <T> LiveData<T>.observe(observer: Observer<T>) {
        observe(this@BaseFragment, observer)
    }
}
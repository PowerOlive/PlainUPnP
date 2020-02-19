package com.m3sv.plainupnp.di.main

import com.m3sv.plainupnp.presentation.controls.ControlsFragment
import com.m3sv.plainupnp.presentation.main.MainActivity
import dagger.Subcomponent

@Subcomponent(modules = [MainActivityModule::class])
interface MainActivitySubComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): MainActivitySubComponent
    }

    fun inject(activity: MainActivity)

    fun inject(fragment: ControlsFragment)
}
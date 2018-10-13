package com.m3sv.droidupnp.upnp

import android.arch.lifecycle.LiveData
import com.m3sv.droidupnp.data.Directory
import com.m3sv.droidupnp.data.RendererState
import com.m3sv.droidupnp.upnp.observers.ContentDirectoryDiscoveryObservable
import com.m3sv.droidupnp.upnp.observers.RendererDiscoveryObservable
import io.reactivex.Observable
import com.m3sv.droidupnp.data.IUpnpDevice
import org.droidupnp.model.upnp.didl.IDIDLItem

interface UpnpManager {
    val rendererDiscoveryObservable: RendererDiscoveryObservable

    val contentDirectoryDiscoveryObservable: ContentDirectoryDiscoveryObservable

    val selectedDirectoryObservable: Observable<Directory>

    val rendererState: LiveData<RendererState>

    val renderedItem: LiveData<RenderedItem>

    val contentData: LiveData<List<DIDLObjectDisplay>>

    fun addObservers()

    fun removeObservers()

    fun selectContentDirectory(contentDirectory: IUpnpDevice?)

    fun selectRenderer(renderer: IUpnpDevice?)

    fun renderItem(item: IDIDLItem, position: Int)

    fun resumeUpnpController()

    fun pauseUpnpController()

    fun resumeRendererUpdate()

    fun pauseRendererUpdate()

    fun pausePlayback(): Unit?

    fun stopPlayback()

    fun resumePlayback()

    fun playNext()

    fun playPrevious()

    fun browseHome()

    fun browseTo(id: String, parentId: String?, addToStructure: Boolean = true)

    fun browsePrevious()

    fun moveTo(progress: Int, max: Int)
}
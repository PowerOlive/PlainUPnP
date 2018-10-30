package com.m3sv.plainupnp.upnp

import org.droidupnp.legacy.cling.CDevice
import org.droidupnp.legacy.cling.didl.*
import org.droidupnp.legacy.upnp.ContentCallback
import org.droidupnp.legacy.upnp.IContentDirectoryCommand
import org.fourthline.cling.controlpoint.ControlPoint
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.model.types.UDAServiceType
import org.fourthline.cling.support.contentdirectory.callback.Browse
import org.fourthline.cling.support.contentdirectory.callback.Search
import org.fourthline.cling.support.model.BrowseFlag
import org.fourthline.cling.support.model.DIDLContent
import org.fourthline.cling.support.model.SortCriterion
import org.fourthline.cling.support.model.item.AudioItem
import org.fourthline.cling.support.model.item.ImageItem
import org.fourthline.cling.support.model.item.VideoItem
import timber.log.Timber
import java.util.*

class ContentDirectoryCommand(
    private val controlPoint: ControlPoint,
    private val controller: UpnpServiceController
) : IContentDirectoryCommand {

    private val mediaReceiverRegistarService: Service<*, *>?
        get() = if (controller.selectedContentDirectory == null)
                null
            else (controller.selectedContentDirectory as CDevice)
                .device
                .findService(UDAServiceType("X_MS_MediaReceiverRegistar"))

    private val contentDirectoryService: Service<*, *>?
        get() = if (controller.selectedContentDirectory == null)
                null
            else (controller.selectedContentDirectory as CDevice)
                .device
                .findService(UDAServiceType("ContentDirectory"))

    private fun buildContentList(
        parent: String?,
        didl: DIDLContent?
    ): List<DIDLObjectDisplay> {
        val result = ArrayList<DIDLObjectDisplay>()

        if (parent != null)
            result.add(DIDLObjectDisplay(ClingDIDLParentContainer(parent)))

        didl?.let {
            for (item in it.containers) {
                result.add(DIDLObjectDisplay(ClingDIDLContainer(item)))
                Timber.v("Add container: " + item.title)
            }

            for (item in it.items) {
                val clingItem: ClingDIDLItem = when (item) {
                    is VideoItem -> ClingVideoItem(item)
                    is AudioItem -> ClingAudioItem(item)
                    is ImageItem -> ClingImageItem(item)
                    else -> ClingDIDLItem(item)
                }

                result.add(DIDLObjectDisplay(clingItem))

                Timber.v("Add item: " + item.title)

                for (p in item.properties)
                    Timber.v(p.descriptorName + " " + p.toString())
            }

        }


        return result
    }

    override fun browse(
        directoryID: String,
        parent: String?,
        callback: ContentCallback
    ) {
        contentDirectoryService?.let {
            controlPoint.execute(object : Browse(
                it,
                directoryID,
                BrowseFlag.DIRECT_CHILDREN,
                "*",
                0,
                null,
                SortCriterion(true, "dc:title")
            ) {
                override fun received(actionInvocation: ActionInvocation<*>, didl: DIDLContent) {
                    callback(buildContentList(parent, didl))
                }

                override fun updateStatus(status: Browse.Status) {
                    Timber.v("updateStatus ! ")
                }

                override fun failure(
                    invocation: ActionInvocation<*>,
                    operation: UpnpResponse,
                    defaultMsg: String
                ) {
                    Timber.w("Fail to browse! $defaultMsg")
                    callback(null)
                }
            })
        }
    }

    override fun search(search: String, parent: String?, callback: ContentCallback) {
        contentDirectoryService?.let {
            controlPoint.execute(object : Search(it, parent, search) {
                override fun received(actionInvocation: ActionInvocation<*>, didl: DIDLContent) {
                    try {
                        callback(buildContentList(parent, didl))
                    } catch (e: Exception) {
                        Timber.e(e)
                    }
                }

                override fun updateStatus(status: Search.Status) {
                    Timber.v("updateStatus ! ")
                }

                override fun failure(
                    invocation: ActionInvocation<*>,
                    operation: UpnpResponse,
                    defaultMsg: String
                ) {
                    Timber.w("Fail to browse ! $defaultMsg")
                }
            })
        }
    }
}
package org.droidupnp.legacy.cling.didl

import com.m3sv.plainupnp.data.upnp.DIDLParentContainer
import org.fourthline.cling.support.model.container.Container

class ClingDIDLParentContainer(id: String) : ClingDIDLObject(Container()),
    DIDLParentContainer {

    override val childCount: Int = 0

    init {
        didlObject.id = id
    }

    override fun getTitle(): String = ".."
}

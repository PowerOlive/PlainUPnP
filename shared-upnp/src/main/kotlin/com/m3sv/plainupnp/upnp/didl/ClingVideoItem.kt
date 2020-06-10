package com.m3sv.plainupnp.upnp.didl

import com.m3sv.plainupnp.upnp.R
import org.fourthline.cling.support.model.item.VideoItem

class ClingVideoItem(item: VideoItem) : ClingDIDLItem(item) {

    override val dataType: String = "video/*"

    override val icon: Int = R.drawable.ic_action_video

}
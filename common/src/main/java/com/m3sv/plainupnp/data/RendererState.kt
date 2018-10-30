package com.m3sv.plainupnp.data


data class RendererState(
    val durationRemaining: String?,
    val durationElapse: String?,
    val progress: Int,
    val title: String?,
    val artist: String?,
    val state: UpnpRendererState.State
)
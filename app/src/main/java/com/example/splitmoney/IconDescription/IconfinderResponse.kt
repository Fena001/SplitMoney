package com.example.splitmoney.IconDescription

data class IconfinderResponse(
    val icons: List<Icon>
)

data class Icon(
    val raster_sizes: List<RasterSize>
)

data class RasterSize(
    val formats: List<Format>
)

data class Format(
    val preview_url: String
)

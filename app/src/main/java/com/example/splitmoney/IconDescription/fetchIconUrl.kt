package com.example.splitmoney.IconDescription

suspend fun fetchIconUrl(query: String): String? {
    return try {
        val response = IconfinderClient.api.searchIcons(query)
        response.icons.firstOrNull()
            ?.raster_sizes?.lastOrNull()  // Highest quality
            ?.formats?.firstOrNull()
            ?.preview_url
    } catch (e: Exception) {
        null
    }
}

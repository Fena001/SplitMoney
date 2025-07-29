data class Group(
    val groupId: String = "",
    val name: String = "",
    val type: String = "",
    val imageUrl: String = "",
    val members: Map<String, Boolean> = emptyMap()  // expects a Map, not a List
)
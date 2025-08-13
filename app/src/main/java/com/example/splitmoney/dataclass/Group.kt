data class Group(
    val groupId: String = "",
    val name: String = "",
    val type: String = "",
    val imageUrl: String = "",
    val members: Map<String, Boolean> = emptyMap(),
    val netBalance: Float = 0f, // Added for balance display
    val otherParty: String = "" // Added for "You owe X" logic
)

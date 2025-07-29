import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val friends: Map<String, Boolean> = emptyMap(),
    val groups: Map<String, Boolean> = emptyMap()
) : Parcelable

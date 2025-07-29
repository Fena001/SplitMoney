package com.example.splitmoney.AddGroup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CreateGroupViewModel(
        private val repository: CreateGroupRepository = CreateGroupRepository()
) : ViewModel() {

    private val _groupName = MutableStateFlow("")
    val groupName: StateFlow<String> = _groupName

    private val _groupType = MutableStateFlow("")
    val groupType: StateFlow<String> = _groupType

    private val _imageUrl = MutableStateFlow("")
    val imageUrl: StateFlow<String> = _imageUrl

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun setGroupName(name: String) {
        _groupName.value = name
    }

    fun setGroupType(type: String) {
        _groupType.value = type
    }

    fun setImageUrl(url: String) {
        _imageUrl.value = url
    }

    fun createGroup(
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (groupName.value.isBlank() || groupType.value.isBlank()) {
            onFailure(Exception("Group name or type is empty"))
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            repository.createGroup(
                name = groupName.value,
                type = groupType.value,
                imageUrl = imageUrl.value,
                onSuccess = { groupId ->
                    _isLoading.value = false
                    onSuccess(groupId)
                },
                onFailure = {
                    _isLoading.value = false
                    onFailure(it)
                }
            )
        }
    }

}

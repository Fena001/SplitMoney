package com.example.splitmoney.GroupContacts

import android.app.Application
import android.provider.ContactsContract
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel

class ContactViewModel(application: Application) : AndroidViewModel(application) {

    private val _contacts = mutableStateListOf<Contact>()
    val contacts: List<Contact> = _contacts

    fun loadContacts() {
        val contentResolver = getApplication<Application>().contentResolver
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null, null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val name = it.getString(
                    it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                )
                val phone = it.getString(
                    it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                )
                _contacts.add(Contact(name, phone))
            }
        }
    }

    fun toggleSelection(contact: Contact) {
        val index = _contacts.indexOf(contact)
        if (index != -1) {
            _contacts[index] = contact.copy(isSelected = !contact.isSelected)
        }
    }

    fun getSelectedContacts(): List<Contact> {
        return _contacts.filter { it.isSelected }
    }
}

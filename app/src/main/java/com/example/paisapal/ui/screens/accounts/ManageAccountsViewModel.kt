package com.example.paisapal.ui.screens.accounts


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.repository.UserAccount
import com.example.domain.repository.UserAccountsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageAccountsViewModel @Inject constructor(
    private val userAccountsRepository: UserAccountsRepository
) : ViewModel() {

    val accounts: StateFlow<List<UserAccount>> = userAccountsRepository
        .observeAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addAccount(last4Digits: String, accountName: String?) {
        viewModelScope.launch {
            userAccountsRepository.addAccount(last4Digits, accountName)
        }
    }

    fun removeAccount(last4Digits: String) {
        viewModelScope.launch {
            userAccountsRepository.removeAccount(last4Digits)
        }
    }
}

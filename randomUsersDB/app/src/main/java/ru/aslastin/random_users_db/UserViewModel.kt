package ru.aslastin.random_users_db

import android.content.Context
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel(private val repository: UserRepository) : ViewModel() {

    @Inject
    constructor(@ApplicationContext appContext: Context, repository: UserRepository) : this(
        repository
    ) {
        viewModelScope.launch {
            val sharedPreferences =
                appContext.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE)
            val isVirgin = sharedPreferences?.getBoolean(VIRGIN, true) ?: true
            if (isVirgin) {
                val operationInfo = repository.initUsers().toOperationInfo()
                if (operationInfo.type == OperationInfoType.SUCCESS) {
                    sharedPreferences?.edit()?.apply {
                        putBoolean(VIRGIN, false)
                        apply()
                    }
                }
                sharedPreferences?.edit()?.apply {
                    putInt(STATUS, operationInfo.type.ordinal)
                    apply()
                }
                _status.value = operationInfo.type
            } else {
                _status.value =
                    OperationInfoType.values()[sharedPreferences?.getInt(STATUS, 0) ?: 0]
            }
        }
    }

    val users: LiveData<List<User>> = repository.users.asLiveData()

    private val _operationResponseInfo =
        MutableLiveData<Pair<OperationInfo, OperationType>?>(null)

    val operationResponseInfo: LiveData<Pair<OperationInfo, OperationType>?>
        get() = _operationResponseInfo

    private val _status = MutableLiveData<OperationInfoType?>(null)

    val status: LiveData<OperationInfoType?>
        get() = _status

    fun showOperationInfoDone() {
        _operationResponseInfo.value = null
    }

    private fun <T> operation(type: OperationType, operation: suspend () -> ResponseWrapper<T>) {
        viewModelScope.launch {
            _operationResponseInfo.value = Pair(operation().toOperationInfo(), type)
        }
    }

    fun addRandomUser() = operation(OperationType.ADD) {
        repository.addRandomUser()
    }

    fun deleteUser(user: User) = operation(OperationType.DELETE) {
        repository.deleteUser(user)
    }

    fun resetUsers() = operation(OperationType.RESET) {
        repository.resetUsers()
    }

    companion object {
        private const val SHARED_PREFERENCES = "SHARED_PREFERENCES_"
        private const val VIRGIN = "VIRGIN"
        private const val STATUS = "STATUS"
    }
}

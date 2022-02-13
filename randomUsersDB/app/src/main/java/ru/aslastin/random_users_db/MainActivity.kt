package ru.aslastin.random_users_db

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import ru.aslastin.random_users_db.databinding.ActivityMainBinding

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val usersAdapter = UserListAdapter { user -> userViewModel.deleteUser(user) }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = usersAdapter
            setHasFixedSize(true)
        }

        userViewModel.users.observe(this) { users -> usersAdapter.submitList(users) }

        binding.floatingActionButtonAdd.setOnClickListener { userViewModel.addRandomUser() }

        binding.floatingActionButtonReset.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            userViewModel.resetUsers()
        }

        userViewModel.operationResponseInfo.observe(this) {
            if (it == null) return@observe
            val (operationInfo, type) = it
            if (type == OperationType.RESET) {
                binding.progressBar.visibility = View.GONE
            }
            showOperationInfo(operationInfo)
            userViewModel.showOperationInfoDone()
        }

        userViewModel.status.observe(this) {
            if (it == null) return@observe
            initUI(it)
        }
    }

    private fun initUI(type: OperationInfoType) = when (type) {
        OperationInfoType.SUCCESS -> initSuccess()
        OperationInfoType.GENERIC_ERROR ->
            initFailure(resources.getString(R.string.failed_to_load_users))
        OperationInfoType.NETWORK_ERROR ->
            initFailure(resources.getString(R.string.no_connection))
    }

    private fun initSuccess() {
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
        binding.floatingActionButtonAdd.visibility = View.VISIBLE
        binding.floatingActionButtonReset.visibility = View.VISIBLE
    }

    private fun initFailure(failureMsg: String) {
        binding.progressBar.visibility = View.GONE
        binding.failLoadUsers.visibility = View.VISIBLE
        binding.textViewErrorDesc.text = failureMsg
    }

    private fun showOperationInfo(info: OperationInfo) {
        val msg = when (info.type) {
            OperationInfoType.SUCCESS -> "Success\nCode: ${info.code}\nMsg: ${info.msg}"
            OperationInfoType.GENERIC_ERROR -> "Error\nCode: ${info.code}\nMsg: ${info.msg}"
            OperationInfoType.NETWORK_ERROR -> "Network error"
        }
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

}

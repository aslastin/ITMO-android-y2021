package ru.aslastin.randomUsers

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch
import ru.aslastin.randomUsers.MyApp.Companion.INSTANCE
import ru.aslastin.randomUsers.databinding.ActivityMainBinding

private const val BUNDLE_USERS = "BUNDLE_USERS"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var users: MutableList<User>? = null
    private var usersAdapter: UserAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        users = savedInstanceState?.getParcelableArrayList(BUNDLE_USERS)

        if (users == null) {
            initUsers()
        } else {
            initUsersSuccess(users!!)
        }

        binding.floatingActionButton.setOnClickListener { addRandomUser() }
    }

    private fun initUsers() = lifecycle.coroutineScope.launch {
        val response = INSTANCE.serviceGetUsers()
        when (response) {
            is ResponseWrapper.Success -> initUsersSuccess(response.value)
            is ResponseWrapper.GenericError ->
                initUsersFailure(resources.getString(R.string.failed_to_load_users))
            is ResponseWrapper.NetworkError ->
                initUsersFailure(resources.getString(R.string.no_connection))
        }
        showResponse(response)
    }

    private fun initUsersSuccess(users: MutableList<User>) {
        this.users = users
        usersAdapter = UserAdapter(users, this::deleteUser)
        usersAdapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                (binding.recyclerView.layoutManager as LinearLayoutManager)
                    .scrollToPositionWithOffset(positionStart, 0)
            }
        })

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = usersAdapter
            setHasFixedSize(true)
        }

        binding.progressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
        binding.floatingActionButton.visibility = View.VISIBLE
    }

    private fun initUsersFailure(failureMsg: String) {
        binding.progressBar.visibility = View.GONE
        binding.failLoadUsers.visibility = View.VISIBLE
        binding.textViewErrorDesc.text = failureMsg
    }

    private fun addRandomUser() = lifecycle.coroutineScope.launch {
        val response = INSTANCE.serviceAddRandomUser()
        if (response is ResponseWrapper.Success) {
            users = users?.toMutableList()?.apply { add(0, response.value) }
            usersAdapter?.setList(users!!)
        }
        showResponse(response, Toast.LENGTH_SHORT)
    }

    private fun deleteUser(user: User) = lifecycle.coroutineScope.launch {
        val response = INSTANCE.serviceDeleteUser(user)
        if (response is ResponseWrapper.Success) {
            users = users?.toMutableList()?.apply { remove(user) }
            usersAdapter?.setList(users!!)
        }
        showResponse(response, Toast.LENGTH_SHORT)
    }

    private fun <T> showResponse(
        response: ResponseWrapper<T>,
        length: Int = Toast.LENGTH_LONG
    ) {
        val msg = when (response) {
            is ResponseWrapper.Success -> "Success\nCode: ${response.code}\nMsg: ${response.msg}"
            is ResponseWrapper.GenericError -> "Error\nCode: ${response.code}\nMsg: ${response.msg}"
            is ResponseWrapper.NetworkError -> "Network error"
        }
        showToast(msg, length)
    }

    private fun showToast(toastMsg: String, length: Int = Toast.LENGTH_LONG) {
        Toast.makeText(this, toastMsg, length).show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (users != null) {
            outState.putParcelableArrayList(BUNDLE_USERS, users!!.toCollection(ArrayList()))
        }
        super.onSaveInstanceState(outState)
    }

}

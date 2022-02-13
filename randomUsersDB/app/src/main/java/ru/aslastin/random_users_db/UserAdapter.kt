package ru.aslastin.random_users_db

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView


class UserListAdapter(
    private val onDeleteButtonClick: (User) -> Unit
) : ListAdapter<User, UserListAdapter.UserViewHolder>(USERS_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) =
        holder.bind(getItem(position), onDeleteButtonClick)

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title = itemView.findViewById<TextView>(R.id.textView_title)
        val body = itemView.findViewById<TextView>(R.id.textView_body)
        val buttonDelete = itemView.findViewById<Button>(R.id.button_delete)

        fun bind(user: User, onDeleteButtonClick: (User) -> Unit) {
            title.text = user.title
            body.text = user.body
            buttonDelete.setOnClickListener {
                onDeleteButtonClick(user)
            }
        }

        companion object {
            fun create(parent: ViewGroup): UserViewHolder =
                UserViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.recyclerview_item, parent, false)
                )
        }
    }

    companion object {
        private val USERS_COMPARATOR = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}

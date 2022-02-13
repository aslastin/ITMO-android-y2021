package ru.aslastin.randomUsers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class DiffUtilCallback(
    private val oldList: List<User>,
    private val newList: List<User>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition].id == newList[newItemPosition].id

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition] == newList[newItemPosition]
}

class UserAdapter(
    private val users: MutableList<User>,
    private val onDeleteButtonClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val holder = UserViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.list_item, parent, false)
        )
        holder.buttonDelete.setOnClickListener {
            onDeleteButtonClick(users[holder.adapterPosition])
        }
        return holder
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) =
        holder.bind(users[position])

    override fun getItemCount() = users.size

    fun setList(newUsers: MutableList<User>) {
        val diffResult = DiffUtil.calculateDiff(
            DiffUtilCallback(users, newUsers)
        )
        users.clear()
        users.addAll(newUsers)
        diffResult.dispatchUpdatesTo(this)
    }

    class UserViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        val title = root.findViewById<TextView>(R.id.textView_title)
        val body = root.findViewById<TextView>(R.id.textView_body)
        val buttonDelete = root.findViewById<Button>(R.id.button_delete)

        fun bind(user: User) {
            title.text = user.title
            body.text = user.body
        }
    }
}

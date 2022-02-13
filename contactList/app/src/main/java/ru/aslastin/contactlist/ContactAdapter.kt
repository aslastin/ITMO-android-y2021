package ru.aslastin.contactlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlin.random.Random

class ContactAdapter(
    private val contactList: List<Contact>,
    private val iconIdList: List<Int>,
    private val onPhoneClick: (Contact) -> Unit,
    private val onMsgButtonClick: (Contact) -> Unit
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val holder = ContactViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.list_item, parent, false)
        )
        holder.phoneView.setOnClickListener {
            onPhoneClick(contactList[holder.absoluteAdapterPosition])
        }
        holder.msgImageView.setOnClickListener {
            onMsgButtonClick(contactList[holder.absoluteAdapterPosition])
        }
        return holder
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) =
        holder.bind(contactList[position], iconIdList[position])

    override fun getItemCount() = contactList.size

    class ContactViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        val nameView = root.findViewById<TextView>(R.id.name)
        val phoneView = root.findViewById<TextView>(R.id.phone)
        val msgImageView = root.findViewById<ImageView>(R.id.msg_imageView)
        var iconImageView = root.findViewById<ImageView>(R.id.icecream_imageView)

        fun bind(contact: Contact, iconId: Int) {
            nameView.text = contact.name
            phoneView.text = contact.phoneNumber
            iconImageView.setImageResource(iconId)
        }
    }
}

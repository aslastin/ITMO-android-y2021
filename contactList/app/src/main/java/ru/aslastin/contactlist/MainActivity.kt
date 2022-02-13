package ru.aslastin.contactlist

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import ru.aslastin.contactlist.databinding.ActivityMainBinding
import java.util.ArrayList
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    companion object {
        const val READ_CONTACTS_REQ_CODE = 1
        const val SEND_MSG_REQ_CODE = 2

        const val BUNDLE_CONTACTS = "BUNDLE_CONTACTS"
        const val BUNDLE_RANDOM_ICON_IDS = "BUNDLE_RANDOM_ICON_IDS"
    }

    lateinit var binding: ActivityMainBinding

    var contacts: List<Contact>? = null
    var randomIconIds: List<Int>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState != null) {
            contacts = savedInstanceState.getParcelableArrayList(BUNDLE_CONTACTS)
            randomIconIds = savedInstanceState.getIntegerArrayList(BUNDLE_RANDOM_ICON_IDS)
            if (contacts == null || randomIconIds == null) {
                turnOnSadGirl()
            } else {
                putContacts(false)
            }
        } else if (isPermissionGranted(Manifest.permission.READ_CONTACTS)) {
            putContacts(true)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS),
                READ_CONTACTS_REQ_CODE
            )
        }
    }

    private fun isPermissionGranted(permission: String) =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            READ_CONTACTS_REQ_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    turnOnSadGirl()
                } else {
                    putContacts(true)
                }
            }
            SEND_MSG_REQ_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    showToast(resources.getString(R.string.msg_permission_denied))
                }
            }
        }
    }

    private fun showToast(text: String) =
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()

    private fun putContacts(withInit: Boolean) {
        if (withInit) {
            initData()
        }
        if (contacts == null || randomIconIds == null) {
            somethingWentWrongSadGirl()
        } else {
            turnOffSadGirl()
        }
        val viewManager = LinearLayoutManager(this)
        binding.recyclerView.apply {
            layoutManager = viewManager
            adapter = ContactAdapter(
                contacts!!,
                randomIconIds!!,
                onPhoneClick = {
                    val intent = Intent(
                        Intent.ACTION_DIAL,
                        Uri.parse("tel:${it.phoneNumber}")
                    )
                    try {
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        showToast(resources.getString(R.string.no_messenger))
                    }
                },
                onMsgButtonClick = {
                    if (!isPermissionGranted(Manifest.permission.SEND_SMS)) {
                        ActivityCompat.requestPermissions(
                            this@MainActivity,
                            arrayOf(Manifest.permission.SEND_SMS),
                            SEND_MSG_REQ_CODE
                        )
                        return@ContactAdapter
                    }
                    val intent = Intent(
                        Intent.ACTION_SENDTO,
                        Uri.parse("smsto:${it.phoneNumber}")
                    )
                    intent.putExtra("sms_body", "[=^ ◡ ^=]\n")
                    try {
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        showToast(resources.getString(R.string.cannot_send_msg))
                    }
                }
            )
        }
        if (withInit) {
            showToast(
                resources.getQuantityString(
                    R.plurals.numberOfContacts,
                    contacts!!.size,
                    contacts!!.size
                )
            )
        }
    }

    private fun initData() {
        contacts = fetchAllContacts()
        randomIconIds = contacts!!.indices.map {
            when (Random.nextInt(4)) {
                0 -> R.drawable.ic_green_icon
                1 -> R.drawable.ic_rainbow_icon
                2 -> R.drawable.ic_yellow_icon
                else -> R.drawable.ic_red_icon
            }
        }
    }

    private fun turnOffSadGirl() {
        binding.recyclerView.visibility = View.VISIBLE
        binding.imageView.visibility = View.GONE
        binding.textView.visibility = View.GONE
    }

    private fun turnOnSadGirl() {
        binding.recyclerView.visibility = View.GONE
        binding.imageView.visibility = View.VISIBLE
        binding.textView.visibility = View.VISIBLE
    }

    private fun somethingWentWrongSadGirl() {
        binding.recyclerView.visibility = View.GONE
        binding.imageView.visibility = View.VISIBLE
        binding.textView.visibility = View.GONE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (contacts != null && randomIconIds != null) {
            outState.putParcelableArrayList(BUNDLE_CONTACTS, contacts!!.toCollection(ArrayList()))
            outState.putIntegerArrayList(BUNDLE_RANDOM_ICON_IDS, randomIconIds as ArrayList<Int>?)
        }
        super.onSaveInstanceState(outState)
    }
}

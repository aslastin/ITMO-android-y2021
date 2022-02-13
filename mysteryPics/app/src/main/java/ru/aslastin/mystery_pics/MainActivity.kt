package ru.aslastin.mystery_pics

import android.app.Dialog
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.*
import ru.aslastin.mystery_pics.databinding.ActivityMainBinding
import java.io.IOException
import java.net.URL
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val ALL_IMAGE_DATA_BUNDLE = "ALL_IMAGE_DATA_BUNDLE"

        private var downloadJobStatic: Job? = null
        private var allImageDataStatic: List<ImageData>? = null

        private fun initAllImageData() = CoroutineScope(Dispatchers.Main).launch {
            allImageDataStatic = withContext(Dispatchers.Default) {
                var resJson = withContext(Dispatchers.IO) {
                    try {
                        URL(Utils.Unsplash.getRandomPhotosURL()).openConnection().run {
                            connect()
                            getInputStream().bufferedReader().readLines().joinToString("")
                        }
                    } catch (e: IOException) {
                        null
                    }
                } ?: return@withContext null
                // мем (без этого как-то работало...)
                resJson = resJson.replace("\\u0026", "&")
                Utils.Unsplash.extractAllImageData(resJson)
            }
        }
    }

    private lateinit var binding: ActivityMainBinding

    private var allImageData: List<ImageData>? = null

    private lateinit var dialog: Dialog
    private lateinit var dialogImageView: ImageView
    private lateinit var dialogTextView: TextView
    private lateinit var dialogButton: Button
    private var dialogJob: Job? = null

    private var localBinder: ImageService.LocalBinder? = null
    private var isBound = false

    private val boundServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            localBinder = service as ImageService.LocalBinder
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            isBound = false
            localBinder = null
        }
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, ImageService::class.java)
        startService(intent)
        bindService(intent, boundServiceConnection, BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(boundServiceConnection)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initDialogData()

        // Инициализируем "сырой" список всех картинок
        if (savedInstanceState == null) {
            firstTimeCreated()
            return
        }

        allImageData = savedInstanceState.getParcelableArrayList(ALL_IMAGE_DATA_BUNDLE)
        if (allImageData == null && downloadJobStatic == null) {
            firstTimeCreated()
            return
        }

        binding.preparationLayout.root.visibility = View.VISIBLE
        waitDownloadJobAndInitRecyclerView()
    }

    private fun initDialogData() {
        dialog = Dialog(this)

        dialog.setContentView(R.layout.dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.setOnDismissListener {
            dialogJob?.cancel()
        }

        dialogImageView = dialog.findViewById(R.id.dialogImageView)

        dialogTextView = dialog.findViewById(R.id.dialogTextView)

        dialogButton = dialog.findViewById(R.id.dialogButton)
        dialogButton.setOnClickListener { dialog.dismiss() }
    }

    private fun firstTimeCreated() {
        // перестаем ссылаться на старые данные
        downloadJobStatic = null
        allImageDataStatic = null

        if (!Utils.isNetworkAvailable(this)) {
            binding.noConnectionLayout.root.visibility = View.VISIBLE
            return
        }
        binding.preparationLayout.root.visibility = View.VISIBLE
        downloadJobStatic = initAllImageData()

        waitDownloadJobAndInitRecyclerView()
    }

    private fun waitDownloadJobAndInitRecyclerView() = lifecycle.coroutineScope.launch {
        var firstTime = false
        if (allImageData == null) {
            firstTime = true
            // дожидаемся инициализации данных
            downloadJobStatic?.join()
            downloadJobStatic = null
            // Во время загрузки какой-то нехороший человек отключил интернет
            if (allImageDataStatic == null) {
                binding.preparationLayout.root.visibility = View.GONE
                binding.noConnectionLayout.root.visibility = View.VISIBLE
                return@launch
            }
            allImageData = allImageDataStatic
            allImageDataStatic = null
        }

        if (firstTime) {
            Toast.makeText(this@MainActivity, "Preparation done!", Toast.LENGTH_SHORT)
                .show()
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = ImageDataAdapter(allImageData!!, this@MainActivity::onItemClick)
            setHasFixedSize(true)
        }
        // Выключаем вспомогательные layout и показываем основной список
        binding.recyclerView.visibility = View.VISIBLE
        binding.noConnectionLayout.root.visibility = View.GONE
        binding.preparationLayout.root.visibility = View.GONE
    }

    private fun onItemClick(imageData: ImageData, imageViewItem: ImageView) {
        dialogJob = lifecycle.coroutineScope.launch {
            // Работа с картинками осуществляется через сервис, поэтому ждем его
            while (localBinder == null) {
                delay(500L)
            }

            // Если картинка не загружена - выставляем инфу о начале загрузки
            if (!localBinder!!.hasBitmaps(imageData.url)) {
                dialogImageView.setImageResource(R.drawable.downloading)
                dialogTextView.text = resources.getString(R.string.preparing_data)
                dialog.show()
            }
            // Даем сервису задание - загрузи картинки по урлу
            // Если загрузка прошла неуспешно - вернет null
            val bitmaps = localBinder!!.getBitmaps(imageData.url)

            if (bitmaps != null) {
                val (fullBitmap, reducedBitmap) = bitmaps

                dialogImageView.setImageBitmap(fullBitmap)
                dialogTextView.text = imageData.description

                imageData.bitmap = reducedBitmap
                imageViewItem.setImageBitmap(reducedBitmap)
            } else {
                dialogImageView.setImageResource(R.drawable.no_connection)
                dialogTextView.text = resources.getString(R.string.some_error_occurred_msg)
            }

            dialog.show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (allImageData != null) {
            outState.putParcelableArrayList(
                ALL_IMAGE_DATA_BUNDLE,
                allImageData!!.toCollection(ArrayList())
            )
        }
        super.onSaveInstanceState(outState)
    }
}

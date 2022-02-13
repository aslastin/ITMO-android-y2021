package ru.aslastin.mystery_pics

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class ImageDataAdapter(
    private val allImageData: List<ImageData>,
    private val onClick: (ImageData, ImageView) -> Unit
) : RecyclerView.Adapter<ImageDataAdapter.ImageDataViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageDataViewHolder {
        val holder = ImageDataViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.list_item, parent, false)
        )
        holder.cardView.setOnClickListener {
            onClick(
                allImageData[holder.adapterPosition],
                holder.cardView.findViewById(R.id.imageView)
            )
        }
        return holder
    }

    override fun onBindViewHolder(holder: ImageDataViewHolder, position: Int) =
        holder.bind(allImageData[position])

    override fun getItemCount() = allImageData.size

    class ImageDataViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        val textViewDesc = root.findViewById<TextView>(R.id.textViewDesc)
        var imageView = root.findViewById<ImageView>(R.id.imageView)
        val cardView = root.findViewById<CardView>(R.id.cardView)

        fun bind(imageData: ImageData) {
            textViewDesc.text = imageData.description
            if (imageData.bitmap == null) {
                imageView.setImageResource(R.drawable.question_mark)
            } else {
                imageView.setImageBitmap(imageData.bitmap)
            }
        }
    }
}


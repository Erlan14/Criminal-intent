package com.example.criminal_intent

import android.app.Dialog
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class ImageDialogFragment(private val image: Bitmap) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.fragment_image_dialog, null)
        val imageView = view.findViewById<ImageView>(R.id.iv_photo)
        imageView.setImageBitmap(image)
        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(view)
        return alertDialog.create()
    }
}
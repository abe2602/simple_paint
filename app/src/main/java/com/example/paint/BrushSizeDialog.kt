package com.example.paint

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.DialogFragment
import com.example.paint.databinding.DialogBrushSizeBinding


class BrushSizeDialog(
    context: Context,
    private val okButton: (brushSize: Float) -> Unit,
) : Dialog(context) {

    private lateinit var binding: DialogBrushSizeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_brush_size)
        binding = DialogBrushSizeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {

            cancelButton.setOnClickListener {
                dismiss()
            }

            saveButton.setOnClickListener {
                val brushSizeText = brushSizeEditText.text.toString()

                if(brushSizeText.isNotEmpty()) {
                    okButton(brushSizeText.toFloat())
                }

                dismiss()
            }
        }
    }

}
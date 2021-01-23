package com.example.paint

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.example.paint.databinding.DialogBrushSizeBinding

class BrushSizeDialog(
    context: Context,
    private val okButton: (brushSize: Float) -> Unit,
) : AlertDialog(context) {

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
                dismiss()
                val brushSizeText = brushSizeEditText.text.toString()

                if(brushSizeText.isNotEmpty()) {
                    okButton(brushSizeText.toFloat())
                }

                dismiss()
            }
        }
    }
}
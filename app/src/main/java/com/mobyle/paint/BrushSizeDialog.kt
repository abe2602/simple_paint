package com.mobyle.paint

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.SeekBar
import com.mobyle.paint.databinding.DialogBrushSizeBinding


class BrushSizeDialog(
    context: Context,
    private val brushSize: Int,
    private val okButton: (brushSize: Float) -> Unit,
) : Dialog(context) {

    private lateinit var binding: DialogBrushSizeBinding
    private var newBrushSize: Int = brushSize

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_brush_size)
        binding = DialogBrushSizeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {

            brushSizeSeekBar.progress = brushSize

            brushSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                    newBrushSize = i
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}

                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })

            cancelButton.setOnClickListener {
                dismiss()
            }

            saveButton.setOnClickListener {
                if(newBrushSize != 0) {
                    okButton(newBrushSize.toFloat())
                }

                dismiss()
            }
        }
    }

}
package com.example.paint

import android.Manifest
import android.animation.Animator
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.paint.databinding.ActivityMainBinding
import com.flask.colorpicker.builder.ColorPickerDialogBuilder

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private var isFabOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = MainViewModel(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        listenButtonsAction()
        observeLiveData()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.action_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.share -> {
                if (isReadStorageAllowed()) {
                    viewModel.shareDraw(getBitmapFromView(binding.drawingView))
                } else {
                    requestStoragePermission()
                }
                true
            }
            R.id.save -> {
                if (isReadStorageAllowed()) {
                    viewModel.saveDrawInGallery(getBitmapFromView(binding.drawingView))
                } else {
                    requestStoragePermission()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        //Checking the request code of our request
        if (requestCode == 1) {
            //If permission is granted
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                        this@MainActivity,
                        getString(R.string.permission_granted_text),
                        Toast.LENGTH_LONG
                ).show()
            } else {
                //Displaying another toast if permission is not granted
                AlertDialog.Builder(this)
                        .setTitle(getString(R.string.permission_dialog_title))
                        .setMessage(getString(R.string.permission_dialog_text))
                        .setPositiveButton(getString(R.string.permission_dialog_positive_button_text)) { dialog: DialogInterface, _: Int ->
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.data = Uri.fromParts("package", packageName, null)
                            startActivity(intent)
                            dialog.dismiss()
                        }
                        .setNegativeButton(getString(R.string.permission_dialog_negative_button_text)){ dialog: DialogInterface, _: Int ->
                            dialog.dismiss()
                        }
                        .show()
            }
        }
    }

    private fun observeLiveData() {
        with(viewModel) {
            getStateEventLiveData().observe(this@MainActivity, {
                if(it == EventState.LOADING) {
                    displayLoading()
                }else if(it == EventState.SUCCESS) {
                    dismissLoading()
                }
            })

            getSaveImageLiveData().observe(this@MainActivity, {
                Toast.makeText(
                        this@MainActivity,
                        getString(R.string.save_image_success_text),
                        Toast.LENGTH_LONG
                ).show()
            })

            getShareImageLiveData().observe(this@MainActivity, { uri ->
                MediaScannerConnection.scanFile(
                        this@MainActivity, arrayOf(getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString()), null
                ) { _, _ ->
                    val shareIntent = Intent()
                    shareIntent.action = Intent.ACTION_SEND
                    shareIntent.putExtra(
                            Intent.EXTRA_STREAM,
                            uri
                    )
                    shareIntent.type = "image/jpeg"
                    ContextCompat.startActivity(
                            this@MainActivity,
                            Intent.createChooser(
                                    shareIntent,
                                    "Share"
                            ),
                            null
                    )
                }
            })
        }
    }

    private fun dismissLoading() {
        with(binding) {
            Handler(Looper.getMainLooper()).postDelayed({
                loading.visibility = View.GONE
                drawingView.visibility = View.VISIBLE
                colorButtons.visibility = View.VISIBLE
                actionButtonsBar.actionBarConstraintLayout.visibility = View.VISIBLE
            }, 500)
        }
    }

    private fun displayLoading() {
        with(binding) {
            loading.visibility = View.VISIBLE
            drawingView.visibility = View.GONE
            colorButtons.visibility = View.GONE
            actionButtonsBar.actionBarConstraintLayout.visibility = View.GONE
        }
    }

    private fun listenButtonsAction() {
        with(binding) {
            with(actionButtonsBar) {
                mainFab.setOnClickListener {
                    if(isFabOpen) {
                        dismissFabButtons()
                    } else {
                        displayFabButtons()
                    }
                }

                brushSizeFab.setOnClickListener {
                    BrushSizeDialog(this@MainActivity) {
                        drawingView.setBrushSize(it)
                    }.show()
                }

                eraserFab.setOnClickListener {
                    with(binding.drawingView) {
                        setBrushColor(Color.WHITE)
                    }
                }

                colorFab.setOnClickListener {
                    ColorPickerDialogBuilder
                            .with(this@MainActivity)
                            .density(12)
                            .setPositiveButton(getText(R.string.color_fab_positive_button_text)) { _: DialogInterface, selectedColor: Int, _: Array<Int> ->
                                colorFab.backgroundTintList = ColorStateList.valueOf(selectedColor)
                                drawingView.setBrushColor(selectedColor)
                                drawingView.addUsedColorToColorsBar(selectedColor)

                                val usedColors = drawingView.getPreviousColors()

                                firstUsedColorButton.setBackgroundColor(usedColors[1])
                                secondUsedColorButton.setBackgroundColor(usedColors[2])
                                thirdUsedColorButton.setBackgroundColor(usedColors[3])
                            }
                            .setNegativeButton(getText(R.string.color_fab_negative_button_text)) { _: DialogInterface, _: Int -> }
                            .build()
                            .show()
                }

                blackColorButton.setOnClickListener {
                    setUsedColorToBrush(0)
                }

                firstUsedColorButton.setOnClickListener {
                    setUsedColorToBrush(1)
                }

                secondUsedColorButton.setOnClickListener {
                    setUsedColorToBrush(2)
                }

                thirdUsedColorButton.setOnClickListener {
                    setUsedColorToBrush(3)
                }

                undoFab.setOnClickListener {
                    drawingView.undoLastDraw()
                }
            }
        }
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background

        if (bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }

        view.draw(canvas)

        return returnedBitmap
    }

    private fun setUsedColorToBrush(index: Int) {
        with(binding.drawingView) {
            setBrushColor(getUsedColor(index))
        }
    }

    private fun displayFabButtons() {
        with(binding) {
            with(actionButtonsBar) {
                isFabOpen = true

                mainFab.animate().rotation(180F)

                undoFab.visibility = View.VISIBLE
                brushSizeFab.visibility = View.VISIBLE
                eraserFab.visibility = View.VISIBLE

                undoFab.animate().translationY(-resources.getDimension(R.dimen.standard_75))
                brushSizeFab.animate().translationY(-resources.getDimension(R.dimen.standard_120))
                eraserFab.animate().translationY(-resources.getDimension(R.dimen.standard_165))
                    .setListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator?) {}

                        override fun onAnimationEnd(animation: Animator?) {
                            if (!isFabOpen) {
                                undoFab.visibility = View.GONE
                                brushSizeFab.visibility = View.GONE
                                eraserFab.visibility = View.GONE
                            }
                        }

                        override fun onAnimationCancel(animation: Animator?) {}

                        override fun onAnimationRepeat(animation: Animator?) {}

                    })
            }
        }
    }

    private fun dismissFabButtons() {
        with(binding) {
            with(actionButtonsBar) {
                isFabOpen = false

                brushSizeFab.animate().translationY(0F)
                mainFab.animate().rotation(0F)
                undoFab.animate().translationY(0F)
                eraserFab.animate().translationY(0F)
            }
        }
    }

    private fun isReadStorageAllowed(): Boolean =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        arrayOf(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ).toString()
                )
        ) { }

        ActivityCompat.requestPermissions(
                this, arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ),
                1
        )
    }
}
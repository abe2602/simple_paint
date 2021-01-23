package com.example.paint

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*


class MainViewModel(private val context: Context) : ViewModel() {
    private val stateEventLiveData = MutableLiveData<EventState>()
    fun getStateEventLiveData(): LiveData<EventState> = stateEventLiveData

    private val shareImageLiveData = MutableLiveData<Uri?>()
    fun getShareImageLiveData(): LiveData<Uri?> = shareImageLiveData

    private val saveImageLiveData = MutableLiveData<Unit>()
    fun getSaveImageLiveData(): LiveData<Unit> = saveImageLiveData


    //Initialization of job
    private var job = Job()

    // Initialization of scope for the coroutine to run in
    private var scopeForSaving = CoroutineScope(job + Dispatchers.Main)

    fun saveDrawInGallery(drawBitMap: Bitmap?) {
        stateEventLiveData.postValue(EventState.LOADING)

        // Using coroutine to put this task in another thread
        scopeForSaving.launch {
            withContext(Dispatchers.IO){
                drawBitMap?.saveImage(context)
                stateEventLiveData.postValue(EventState.SUCCESS)
                saveImageLiveData.postValue(Unit)
            }
        }
    }

    fun shareDraw(drawBitMap: Bitmap?) {
        stateEventLiveData.postValue(EventState.LOADING)

        // Using coroutine to put this task in another thread
        scopeForSaving.launch {
            withContext(Dispatchers.IO){
                shareImageLiveData.postValue(drawBitMap?.saveImage(context))
                stateEventLiveData.postValue(EventState.SUCCESS)
            }
        }
    }

}


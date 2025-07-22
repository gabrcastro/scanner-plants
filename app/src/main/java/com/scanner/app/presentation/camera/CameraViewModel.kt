import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
import androidx.camera.core.CameraSelector.DEFAULT_FRONT_CAMERA
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraPreviewViewModel(application: Application) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val applicationContext = getApplication<Application>()
        .applicationContext

    // Used to set up a link between the Camera and your UI.
    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest: StateFlow<SurfaceRequest?> = _surfaceRequest.asStateFlow()

    // Adiciona um StateFlow para controlar a lente da câmera
    private val _cameraSelector = MutableStateFlow(DEFAULT_BACK_CAMERA) // Inicia com a câmera traseira
    val cameraSelector: StateFlow<CameraSelector> = _cameraSelector.asStateFlow()

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    // Storing currentLifecycleOwner can be problematic if it becomes stale.
    // It's often better to pass it to functions that need it directly,
    // or re-obtain it if the camera needs to be bound outside of the initial Composable call.
    // For now, we'll keep it as per your structure but be mindful of its lifecycle.
    private var currentLifecycleOwner: LifecycleOwner? = null

    private val cameraExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }

    private val cameraPreviewUseCase = Preview.Builder().build().apply {
        // Use setSurfaceProvider from androidx.camera.core.Preview.SurfaceProvider
        setSurfaceProvider { newSurfaceRequest ->
            // Correctly update your StateFlow that CameraXViewfinder observes
            _surfaceRequest.value = newSurfaceRequest
        }
    }

    // Initialize ImageCapture when the ViewModel is created or before first use
    init {
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY) // Or MIN_LATENCY
            // Consider .setTargetRotation(Surface.ROTATION_0) or dynamically from display
            .build()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                cameraProvider = ProcessCameraProvider.getInstance(applicationContext).get()
                _cameraSelector.collect { selectedCamera ->
                    currentLifecycleOwner?.let { lifecycle -> // Only rebind if lifecycle owner is set
                        rebindCameraInternal(selectedCamera, lifecycle)
                    } ?: Log.w("CameraVM", "LifecycleOwner not set, skipping rebind on selector change.")
                }
            } catch (exc: Exception) {
                Log.e("CameraVM", "Failed to get CameraProvider: ${exc.message}")
            }
        }
    }

    // UI calls this to set the lifecycle owner and trigger initial bind
    fun setLifecycleOwnerAndBind(lifecycleOwner: LifecycleOwner) {
        currentLifecycleOwner = lifecycleOwner
        // Trigger initial bind if provider is ready, or it will happen when selector changes
        if (cameraProvider != null) {
            rebindCameraInternal(_cameraSelector.value, lifecycleOwner)
        }
    }

    // Renamed rebindCamera to avoid confusion and make it clear it's an internal detail
    private fun rebindCameraInternal(selectedCamera: CameraSelector, lifecycle: LifecycleOwner) {
        val provider = cameraProvider ?: return
        val localImageCapture = imageCapture ?: return

        try {
            provider.unbindAll()
            Log.d("CameraVM", "Rebinding camera with selector: $selectedCamera")
            provider.bindToLifecycle(
                lifecycle,
                selectedCamera,
                cameraPreviewUseCase,
                localImageCapture
            )
        } catch (exc: Exception) {
            Log.e("CameraVM", "Failed to rebind camera: ${exc.message}")
        }
    }

    fun bindToCamera(lifecycleOwner1: Context, lifecycleOwner: LifecycleOwner) { // Removed appContext, use applicationContext
        currentLifecycleOwner = lifecycleOwner // Guarda o lifecycleOwner
        viewModelScope.launch(Dispatchers.IO) { // Perform camera init on a background thread
            try {
                cameraProvider = ProcessCameraProvider.getInstance(applicationContext).get()

                // Launch a coroutine to observe _cameraSelector and re-bind the camera
                // This collect needs to be managed carefully with the lifecycle of bindToCamera
                viewModelScope.launch { // This nested launch for collect might be better outside or managed differently
                    _cameraSelector.collect { selectedCamera ->
                        rebindCamera(selectedCamera)
                    }
                }
                // Initial bind with the current selector
                // rebindCamera(_cameraSelector.value) // Call rebind directly after provider is ready


            } catch (exc: Exception) {
                Log.e("CameraVM", "Failed to get CameraProvider: ${exc.message}")
                // Handle error, e.g., update a state to show an error message in UI
            }
            // The awaitCancellation logic here was trying to keep the initial launch active.
            // The collection of _cameraSelector will keep its coroutine active.
            // The unbindAll in the finally block is good if this outer scope
            // represents the entire camera session.
        }
    }

    private fun rebindCamera(selectedCamera: CameraSelector) {
        val provider = cameraProvider ?: run {
            Log.e("CameraVM", "CameraProvider not available for rebind")
            return
        }
        val lifecycle = currentLifecycleOwner ?: run {
            Log.e("CameraVM", "LifecycleOwner not available for rebind")
            return
        }
        val localImageCapture = imageCapture ?: run {
            Log.e("CameraVM", "ImageCapture not initialized for rebind")
            return
        }

        try {
            provider.unbindAll() // Unbinds all use cases
            Log.d("CameraVM", "Rebinding camera with selector: $selectedCamera")
            provider.bindToLifecycle(
                lifecycle,
                selectedCamera,
                cameraPreviewUseCase,
                localImageCapture // Make sure to bind ImageCapture as well
            )
        } catch (exc: Exception) {
            Log.e("CameraVM", "Failed to rebind camera: ${exc.message}")
        }
    }

    fun takePhoto(
        onImageCaptured: (success: Boolean, uriString: String?, error: String?) -> Unit
    ) {
        val localImageCapture = this.imageCapture ?: run {
            Log.e("CameraVM", "ImageCapture not initialized for takePhoto.")
            onImageCaptured(false, null, "ImageCapture not initialized.")
            return
        }

        val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "$name.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ScannerApp") // Your app's folder
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                applicationContext.contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        localImageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(applicationContext), // Delivers result on the main thread
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("CameraVM", "Photo capture failed: ${exc.message}", exc)
                    onImageCaptured(false, null, "Photo capture failed: ${exc.message}")
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Log.d("CameraVM", msg)
                    onImageCaptured(true, output.savedUri?.toString(), null)
                }
            }
        )
    }

    fun switchCamera() {
        _cameraSelector.update { currentSelector ->
            if (currentSelector == DEFAULT_BACK_CAMERA) {
                DEFAULT_FRONT_CAMERA
            } else {
                DEFAULT_BACK_CAMERA
            }
        }
        // The collect block in bindToCamera should automatically trigger rebindCamera
    }

    override fun onCleared() {
        super.onCleared()
        try {
            cameraProvider?.unbindAll()
        } catch (e: Exception) {
            Log.e("CameraVM", "Error unbinding camera provider in onCleared: ${e.message}")
        }
        cameraExecutor.shutdown()
        Log.d("CameraVM", "ViewModel cleared and resources released.")
    }
}